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
	private static final boolean DEBUG = true;
	private static final String SCOPE = "$";
	private static final String BLOCK = "#";
	
	private LinkedList<IndexedValue<String>> tokenStack = new LinkedList<>();
	private LinkedList<IndexedValue<TokenType>> expressionStack = new LinkedList<>();
	private Map<String, TokenType> identifiersTypes = new HashMap<>();
	
	private int scopeCount = 0;
	private int blockCount = 0;
	private int untypedVariables = 0;

	private String getIdentifierKey(String name, int scope) {
		return String.format("%s+%s", scope, name);
	}
	
	private String getIdentifierKey(String name) {
		return getIdentifierKey(name, this.scopeCount);
	}
	
	private void pushToken(int i, String token) {
		this.tokenStack.push(new IndexedValue<String>(i, token));
	}
	
	private String popToken()
	{
		String token = this.tokenStack.pop().value;
		
		if (token.equals(SCOPE)) {
			this.scopeCount--;
		}
		else if (token.equals(BLOCK)) {
			this.blockCount--;
		}
		else
		{
			String variableKey = getIdentifierKey(token);
			
			if (this.identifiersTypes.containsKey(variableKey))
				this.identifiersTypes.remove(variableKey);
		}
		
		return token;
	}
	
	private void pushExpression(int i, TokenType type) {
		this.expressionStack.push(new IndexedValue<TokenType>(i, type));
		printExpressions(3);
	}
	
	private TokenType parseType(String token)
	{
		switch (token)
		{
		case "integer": return TokenType.Integer;
		case "real": 	return TokenType.Real;
		case "boolean": return TokenType.Boolean;
		default: 		return null;
		}
	}
	
	private void matchIndexAfter(int i)
	{
		boolean changed = false;
		
		while (!this.tokenStack.isEmpty() && this.tokenStack.peek().index > i) popToken();
		while (!this.expressionStack.isEmpty() && this.expressionStack.peek().index > i)
		{
			IndexedValue<TokenType> type = this.expressionStack.pop();
			Log.d(3, "Pop " + type.value + ", its index " + type.index + " > " + i);
			changed = true;
		}
		
		if (changed) printExpressions(4);
	}
	
	@Override
	public void matchIndex(int i)
	{
		boolean changed = false;
		
		while (!this.tokenStack.isEmpty() && this.tokenStack.peek().index >= i) popToken();
		while (!this.expressionStack.isEmpty() && this.expressionStack.peek().index >= i)
		{
			IndexedValue<TokenType> type = this.expressionStack.pop();
			Log.d(3, "Pop " + type.value + ", its index " + type.index + " >= " + i);
			changed = true;
		}
		
		if (changed) printExpressions(4);
	}
	
	@Override
	public void onScopeBegin(int i, int line)
	{
		matchIndexAfter(i);
		pushToken(i, SCOPE);
		this.scopeCount++;
	}

	@Override
	public void onScopeEnd(int i, int line)
	{
		matchIndexAfter(i);
		
		if (this.blockCount > 0)
			throw new SemanticException("Closing a scope with openned blocks", line);
		
		while (!this.tokenStack.isEmpty())
			if (popToken().equals(SCOPE))
				return;
		
		throw new SemanticException("Closing a scope that wasn't openned", line);
	}
	
	@Override
	public void onBlockBegin(int i, Symbol symbol)
	{
		matchIndex(i);
		pushToken(i, BLOCK);
		this.blockCount++;
	}

	@Override
	public void onBlockEnd(int i, Symbol symbol)
	{
		matchIndex(i);
		
		if (this.blockCount == 0)
			throw new SemanticException("Closing a block but any was openned", symbol);
		
		while (!this.tokenStack.isEmpty())
			if (popToken().equals(BLOCK))
				return;
		
		throw new SemanticException("Closing a block that wasn't openned", symbol);
	}
	
	@Override
	public void onProcedureDeclaration(int i, Symbol symbol)
	{
		matchIndex(i);
		
		if (this.blockCount > 0)
			throw new SemanticException("Declaring the procedure inside a block", symbol); 
		
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		while (it.hasNext())
		{
			String token = it.next().value;
			
			if (token.equals(symbol.getToken()))
				throw new SemanticException("The procedure is already declared in this scope", symbol);
			
			else if (token.equals(SCOPE))
				break;
		}
				
		pushToken(i, symbol.getToken());
		this.identifiersTypes.put(getIdentifierKey(symbol.getToken()), TokenType.Procedure);
	}
	
	@Override
	public void onVariableDeclaration(int i, Symbol symbol)
	{
		matchIndex(i);
		
		if (this.blockCount > 0)
			throw new SemanticException("Declaring variable inside a block", symbol); 
		
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
	}
	
	@Override
	public void onTypeDefinition(int i, Symbol symbol)
	{
		matchIndex(i);
		
		TokenType type = parseType(symbol.getToken());
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		
		while (this.untypedVariables-- > 0)
			this.identifiersTypes.put(getIdentifierKey(it.next().value), type);
		
		this.untypedVariables = 0;
	}

	@Override
	public void onProcedure(int i, Symbol symbol)
	{
		matchIndex(i);
		
		if (this.blockCount == 0)
			throw new SemanticException("Using procedure outside a block", symbol); 
		
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
				
				if (type != TokenType.Procedure)
					throw new SemanticException("Using variable as procedure", symbol);
				
				return;
			}
		}
		
		throw new SemanticException("The procedure was not declared", symbol);
	}
	
	@Override
	public void onVariable(int i, Symbol symbol)
	{
		matchIndex(i);
		
		if (this.blockCount == 0)
			throw new SemanticException("Using variable outside a block", symbol); 
		
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

				if (type == TokenType.Procedure)
					throw new SemanticException("Using procedure as variable", symbol);

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
				else {
					// No other operator supports the Boolean type
					if (base == TokenType.Boolean || current == TokenType.Boolean)
						throw new SemanticException("Incompatible types in the same operation", symbol);
					
					// The result of a relational operation is a boolean value
					else if (operator == TokenType.RelationalOperator)
						current = TokenType.Boolean;
					
					// Additive or multiplicative operation
					// If base is Real, current should be Real, but if base is Integer,
					// current should be kept as is (Real or Integer)
					else if (base == TokenType.Real)
						current = TokenType.Real;
				}
				
				operator = null;
			}
			
			printExpressions(4);
		}
		
		Log.d(3, "Expression result: " + current);
		printExpressions(3);
		
		if (!this.expressionStack.isEmpty())
			pushExpression(this.expressionStack.peek().index, current);
	}
	
	private void printExpressions(int tabs)
	{
		if (!DEBUG) return;
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
