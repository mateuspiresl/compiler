package semantic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import lexical.Symbol;
import lexical.TokenType;
import syntactic.SyntacticListener;
import utils.Log;

public class SemanticAnalyser implements SyntacticListener
{
	/**
	 * Marks a scope beginning in the token stack.
	 */
	private static final String SCOPE = "$";
	
	/**
	 * The token stack.
	 * Stores the program identifier, procedures identifiers,
	 * variables identifiers and marks scopes. 
	 */
	private LinkedList<IndexedValue<String>> tokenStack = new LinkedList<>();
	/**
	 * The expression stack.
	 * Stores types and operators.
	 */
	private LinkedList<IndexedValue<TokenType>> expressionStack = new LinkedList<>();
	/**
	 * The identifiers types.
	 * Stores the type of an identifier as identifier key to type.
	 */
	private Map<String, TokenType> identifiersTypes = new HashMap<>();
	/**
	 * The procedures parameters.
	 * Stores the list of types of parameters for each procedure
	 * as procedure identifier key to list of types.
	 */
	private Map<String, TokenType[]> proceduresParameters = new HashMap<>();
	
	/**
	 * Indicates the depth of the scopes.
	 * Used to generate the identifier key.
	 */
	private int scopeCount = 0;
	/**
	 * Counts the number of variables declared but with no type defined.
	 * Used by {@link SemanticAnalyser#onTypeDefinition(int, Symbol)} as
	 * the number of variables from the top of the stack to define its
	 * types.
	 */
	private int untypedVariables = 0;

	/**
	 * Indicates the last procedure declared or used.
	 * Used to create the list of parameters types and to validate the
	 * types of the arguments.
	 */
	private String lastProcedureKey;
	/**
	 * Indicates the resultant type of the last expression.
	 * Used to validate conditions of control statements and parameters
	 * arguments.
	 */
	private TokenType lastExpressionType;
	/**
	 * Counts the number of procedure parameters been declared.
	 * As any procedure parameter and variable are declared in the same
	 * way, this counter is needed to ensure the number of declared
	 * variables that are actually parameters of a procedure.
	 */
	private int procedureParametersCount;
	/**
	 * Counts the number of procedure arguments.
	 * Used on the validation of procedure arguments.
	 */
	private int procedureArgumentsCount;
	
	
	/**
	 * Generates an identifier key using the scope depth.
	 * An identifier key is the concatenation of the scope depth, the
	 * character '+' and the identifier, as depth+identifier.
	 * It's needed to make the map of identifiers support variables of
	 * equal names but different scopes depth.
	 * @param name The identifier.
	 * @param scope The scope depth.
	 * @return The generated key.
	 */
	private String getIdentifierKey(String name, int scope) {
		return String.format("%s+%s", scope, name);
	}
	
	/**
	 * Generates an identifier key for an identifier declared in the
	 * current scope.
	 * @param name The identifier.
	 * @return The generated key.
	 */
	private String getIdentifierKey(String name) {
		return getIdentifierKey(name, this.scopeCount);
	}
	
	/**
	 * Pushes a token to the stack.
	 * The index 'i' of the token is used to ensure the location of
	 * validation on the lexical table.
	 * An invalid token can be pushed to the stack when the validation
	 * is directed to a wrong path. Later, when it changes its path,
	 * it has to go back to an previous index, what makes this useful
	 * to remove the invalid tokens, which are the ones stored with
	 * indexes ahead. For this work, the methods
	 * {@link #matchIndex(int)} and {@link #matchIndexAfter(int)}
	 * are used.
	 * @param token The token to store.
	 * @param i The token index.
	 */
	private void pushToken(int i, String token) {
		this.tokenStack.push(new IndexedValue<String>(i, token));
	}
	
	/**
	 * Pops a token from the stack.
	 * It controls the scope count, updating when any are removed,
	 * and removes identifiers types, in case of identifiers, and also
	 * procedure parameters types, when it'a procedure.
	 * @return The removed token.
	 */
	private String popToken()
	{
		String token = this.tokenStack.pop().value;
		
		if (token.equals(SCOPE)) {
			this.scopeCount--;
		}
		else
		{
			String key = getIdentifierKey(token);
			
			if (this.identifiersTypes.containsKey(key))
			{
				// In case of a procedure, removes its parameters types
				if (this.identifiersTypes.get(key) == TokenType.Procedure)
					this.proceduresParameters.remove(key);
				
				// In case of an identifier, removes its type
				this.identifiersTypes.remove(key);
			}
		}
		
		return token;
	}
	
	/**
	 * Pushes a type to the expression stack.
	 * The index i has the same utility as the
	 * {@link SemanticAnalyser#pushToken(int, String)}.
	 * @param i The type index.
	 * @param type The type.
	 */
	private void pushExpression(int i, TokenType type) {
		this.expressionStack.push(new IndexedValue<TokenType>(i, type));
		printExpressions(3);
	}
	
	/**
	 * Parses a token to it's real type. 
	 * @param token The token to parse.
	 * @return The type.
	 */
	private TokenType parseType(String token)
	{
		switch (token)
		{
		case "integer": 	return TokenType.Integer;
		case "real": 		return TokenType.Real;
		case "boolean": 	return TokenType.Boolean;
		case "program": 	return TokenType.Program;
		case "procedure": 	return TokenType.Procedure;
		default: 			return null;
		}
	}
	
	/**
	 * Removes every token that is registered with an index greater than
	 * the given one.
	 * @param i The index to keep in the stack.
	 */
	private void matchIndexAfter(int i)
	{
		int expressionStackSize = this.expressionStack.size();
		
		while (!this.tokenStack.isEmpty() && this.tokenStack.peek().index > i) popToken();
		while (!this.expressionStack.isEmpty() && this.expressionStack.peek().index > i)
		{
			IndexedValue<TokenType> type = this.expressionStack.pop();
			Log.d(3, "Pop " + type.value + ", its index " + type.index + " > " + i);
		}
		
		if (expressionStackSize != this.expressionStack.size()) printExpressions(4);
	}
	
	/**
	 * Removes every token that is registered with an index greater or equal
	 * to the given one.
	 */
	@Override
	public void matchIndex(int i) {
		matchIndexAfter(i - 1);
	}
	
	/**
	 * Register a scope beginning.
	 */
	@Override
	public void onScopeBegin(int i, int line)
	{
		pushToken(i, SCOPE);
		this.scopeCount++;
	}

	/**
	 * Registers a scope end.
	 * @throws SemanticException if any scope was opened.
	 */
	@Override
	public void onScopeEnd(int i, int line)
	{
		while (!this.tokenStack.isEmpty())
			if (popToken().equals(SCOPE))
				return;
		
		throw new SemanticException("Closing a scope that wasn't openned", line);
	}
	
	/**
	 * Registers a procedure identifier.
	 * @throws SemanticException if an identifier with the same name is already
	 * 		registered in the current scope.
	 */
	@Override
	public void onProcedureDeclaration(int i, Symbol symbol)
	{
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		while (it.hasNext())
		{
			String token = it.next().value;
			
			if (token.equals(symbol.getToken()))
				throw new SemanticException("The procedure is already declared in this scope", symbol);
			
			else if (token.equals(SCOPE))
				break;
		}
		
		Log.d(2, "Procedure " + symbol.getToken());
		pushToken(i, symbol.getToken());
		
		this.lastProcedureKey = getIdentifierKey(symbol.getToken());
		this.identifiersTypes.put(this.lastProcedureKey, TokenType.Procedure);
	}
	
	@Override
	public void onVariableDeclaration(int i, Symbol symbol)
	{
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		while (it.hasNext())
		{
			String token = it.next().value;
			
			if (token.equals(symbol.getToken()))
				throw new SemanticException("The variable is already declared in this scope", symbol);
			
			else if (token.equals(SCOPE))
				break;
		}
		
		pushToken(i, symbol.getToken());
		this.untypedVariables++; 
		this.procedureParametersCount++;
	}
	
	@Override
	public void onTypeDefinition(int i, Symbol symbol)
	{
		TokenType type = parseType(symbol.getToken());
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		
		while (this.untypedVariables-- > 0)
		{
			String key = getIdentifierKey(it.next().value);
			
			this.identifiersTypes.put(key, type);
			Log.d(2, "Variable " + key + " type " + type);
		}
		
		this.untypedVariables = 0;
	}

	@Override
	public void onProcedure(int i, Symbol symbol)
	{
		matchIndex(i);
		
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		int scope = this.scopeCount; 
		
		while (it.hasNext())
		{
			String token = it.next().value;
			
			if (token.equals(SCOPE)) {
				scope--;
			}
			else if (token.equals(symbol.getToken()))
			{
				String key = getIdentifierKey(symbol.getToken(), scope);
				Log.d(2, "For procedure " + symbol.getToken() + " found identifier key " + key);
				
				if (this.identifiersTypes.get(key) != TokenType.Procedure)
					throw new SemanticException("Using variable as procedure", symbol);
				
				this.lastProcedureKey = key;
				return;
			}
		}
		
		throw new SemanticException("The procedure was not declared", symbol);
	}
	
	@Override
	public void onVariable(int i, Symbol symbol)
	{
		matchIndex(i);
		
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		int scope = this.scopeCount; 
		
		while (it.hasNext())
		{
			String token = it.next().value;
			
			if (token.equals(SCOPE)) {
				scope--;
			}
			else if (token.equals(symbol.getToken()))
			{
				TokenType type = this.identifiersTypes.get(getIdentifierKey(symbol.getToken(), scope));

				if (type == TokenType.Program)
					throw new SemanticException("Using the program identifier as variable", symbol);
				
				if (type == TokenType.Procedure)
					throw new SemanticException("Using procedure as variable", symbol);
				
				if (type == null)
					throw new SemanticException("Unknown type variable", symbol);

				Log.d(2, "Include " + type);
				pushExpression(i, type);
				return;
			}
		}
		
		throw new SemanticException("The variable was not declared", symbol);
	}
	
	@Override
	public void onValue(int i, Symbol symbol)
	{
		matchIndex(i);
		Log.d(2, "Include " + symbol.getToken());
		pushExpression(i, symbol.getType());
	}
	
	@Override
	public void onOperator(int i, Symbol symbol) {
		onValue(i, symbol);
	}
	
	@Override
	public void onExpressionBegin(int i, Symbol symbol)
	{
		matchIndexAfter(i);
		pushExpression(i, null);
	}
	
	@Override
	public void onExpressionEnd(int i, Symbol symbol)
	{
		matchIndexAfter(i);
		
		if (this.expressionStack.isEmpty())
			throw new SemanticException("Ending expression that didn't start", symbol);
		
		TokenType current = null;
		TokenType operator = null;
		
		printExpressions(2);
		
		Iterator<IndexedValue<TokenType>> it = this.expressionStack.iterator();
		while (it.hasNext())
		{
			TokenType base = it.next().value; it.remove();
			
			if (current == null) {
				current = base;
				Log.d(3, "Current: " + current);
			}
			else if (operator == null)
			{
				operator = base;
				Log.d(3, "Operator: " + operator);
				
				// End of expression
				if (base == null) break;
			}
			// Expression is formed
			else
			{
				Log.d(4, "Formed expression: " + base + " " + operator + " " + current);
				
				if (operator == TokenType.AssignmentCommand)
				{
					// If types are equal, the result is the current type,
					// otherwise it's Real
					if (base != current)
					{
						Log.d(5, "Different types: " + base + " and " + current);
						
						// As the types are different:
						// 	 - If base or current are Boolean, throw because boolean values
						//			can only be in the same operation with another boolean
						//			value;
						//   - If base is Integer, throw because current is Real and it can
						//			only be assigned to Real variables.
						if (base == TokenType.Boolean || current == TokenType.Boolean
								|| base == TokenType.Integer)
							throw new SemanticException("Incompatible types are been assigned", symbol);
						
						current = TokenType.Real;
					}
				}
				// Logical operations can only be applied to boolean values
				// Its result is a boolean value, so the current value doesn't
				// need to be changed
				else if (operator == TokenType.LogicalOperator)
				{
					if (base != TokenType.Boolean || current != TokenType.Boolean)
						throw new SemanticException("Incompatible types for logical operation", symbol);
				}
				
				// No other operator supports the Boolean type
				else if (base == TokenType.Boolean || current == TokenType.Boolean)
					throw new SemanticException("Incompatible types in the same operation", symbol);
					
				// The result of a relational operation is a boolean value
				else if (operator == TokenType.RelationalOperator)
					current = TokenType.Boolean;
				
				// Additive or multiplicative operation
				// If base is Real, current should be Real, but if base is Integer,
				// current should be kept as is (Real or Integer)
				else if (base == TokenType.Real)
					current = TokenType.Real;
				
				operator = null;
			}
			
			printExpressions(4);
		}
		
		Log.d(3, "Expression result: " + current);
		printExpressions(3);
		
		if (!this.expressionStack.isEmpty())
			pushExpression(this.expressionStack.peek().index, current);
		
		this.lastExpressionType = current;
	}

	@Override
	public void onProcedureParametersDeclarationBegin(int i, Symbol symbol)
	{
		matchIndex(i);
		this.procedureParametersCount = 0;
	}

	@Override
	public void onProcedureParametersDeclarationEnd(int i, Symbol symbol)
	{
		matchIndex(i);
		
		TokenType[] parameters = new TokenType[this.procedureParametersCount];
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		
		while (this.procedureParametersCount-- > 0)
		{
			String key = getIdentifierKey(it.next().value);
			parameters[this.procedureParametersCount] = this.identifiersTypes.get(key);
		}
		
		this.proceduresParameters.put(this.lastProcedureKey, parameters);
		
		if (Log.DEBUG)
		{
			StringBuilder message = new StringBuilder()
				.append("Procedure ")
				.append(this.lastProcedureKey)
				.append(" parameters: ");
			
			for (TokenType type : parameters)
				message.append(type + " ");
			
			Log.d(2, message.toString());
		}
	}
	
	@Override
	public void onProcedureArgumentsBegin(int i, Symbol symbol)
	{
		matchIndex(i);
		this.procedureArgumentsCount = 0;
	}

	@Override
	public void onProcedureArgumentsEnd(int i, Symbol symbol)
	{
		matchIndex(i);
		
		int expectedCount = this.proceduresParameters.get(this.lastProcedureKey).length;
		
		if (expectedCount != this.procedureArgumentsCount)
			throw new SemanticException("Invalid number of arguments", symbol);
	}

	@Override
	public void onProcedureArgument(int i, Symbol symbol)
	{
		matchIndex(i);
		
		Log.d(2, "Procedure " + this.lastProcedureKey + " count " + this.procedureArgumentsCount
				+ " expression " + this.lastExpressionType);
		
		TokenType[] parameters = this.proceduresParameters.get(this.lastProcedureKey);
		
		if (this.procedureArgumentsCount == parameters.length)
			throw new SemanticException("Invalid number of arguments", symbol);
		
		TokenType expectedType = parameters[this.procedureArgumentsCount++]; 
		
		Log.d(2, "Comparing types: expected " + expectedType + ", found " + this.lastExpressionType);
		
		if ((expectedType != TokenType.Real || this.lastExpressionType != TokenType.Integer)
				&& expectedType != this.lastExpressionType)
			throw new SemanticException("Invalid argument type", symbol);
	}

	@Override
	public void onControlCondition(int i, Symbol symbol)
	{
		if (this.lastExpressionType != TokenType.Boolean)
			throw new SemanticException("Expression result for control statement isn't boolean", symbol);
	}
	
	private void printExpressions(int tabs)
	{
		if (!Log.DEBUG) return;
		
		if (this.expressionStack.isEmpty()) {
			Log.d(tabs, "Expression stack: --");
			return;
		}
		
		StringBuilder message = new StringBuilder();
		message.append("Expression stack: ");
		
		Iterator<IndexedValue<TokenType>> it = this.expressionStack.descendingIterator();
		while (it.hasNext())
		{
			IndexedValue<TokenType> val = it.next();
			message.append(val.value + " ");
		}
		
		message.append("(").append(this.expressionStack.peek().index).append(")");
		
		Log.d(tabs, message.toString());
	}
}
