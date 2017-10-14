package semantic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import lexicon.Symbol;
import lexicon.TokenType;
import syntactic.SyntacticListener;

public class SemanticAnalyser implements SyntacticListener
{
	private static final String SCOPE = "$";
	private static final String BLOCK = "#";
	
	private LinkedList<IndexedValue<String>> tokenStack = new LinkedList<>();
	private LinkedList<IndexedValue<TokenType>> expressionStack = new LinkedList<>();
	private Map<String, TokenType> variableTypes = new HashMap<>();
	private int scopeCount = 0;
	private int blockCount = 0;
	private int untypedVariables = 0;
	
	public int getScopeCount() { return this.scopeCount; }
	public int getBlockCount() { return this.blockCount; }
	public int getUntypedVariables() { return this.untypedVariables; }

	private String getVariableKey(String name, int scope) {
		return String.format("%s+%s", scope, name);
	}
	
	private String getVariableKey(String name) {
		return getVariableKey(name, this.scopeCount);
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
			String variableKey = getVariableKey(token);
			
			if (this.variableTypes.containsKey(variableKey))
				this.variableTypes.remove(variableKey);
		}
		
		return token;
	}
	
	private void pushExpression(int i, TokenType type) {
		this.expressionStack.push(new IndexedValue<TokenType>(i, type));
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
		while (!this.tokenStack.isEmpty() && this.tokenStack.peek().index > i) popToken();
		while (!this.expressionStack.isEmpty() && this.expressionStack.peek().index > i)
			this.expressionStack.pop();
	}
	
	@Override
	public void matchIndex(int i)
	{
		while (!this.tokenStack.isEmpty() && this.tokenStack.peek().index >= i) popToken();
		while (!this.expressionStack.isEmpty() && this.expressionStack.peek().index >= i)
			this.expressionStack.pop();
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
		TokenType type = parseType(symbol.getToken());
		Iterator<IndexedValue<String>> it = this.tokenStack.iterator();
		
		while (this.untypedVariables-- > 0)
			this.variableTypes.put(getVariableKey(it.next().value), type);
		
		this.untypedVariables = 0;
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
				TokenType type = this.variableTypes.get(getVariableKey(symbol.getToken(), scope));
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
		pushExpression(i, symbol.getType());
	}
	
	@Override
	public void onOperator(int i, Symbol symbol) {
		onValue(i, symbol);
	}
	
	@Override
	public void onExpressionBegin(int i, Symbol symbol)
	{
		matchIndex(i);
		pushExpression(i, null);
	}
	
	@Override
	public void onExpressionEnd(int i, Symbol symbol)
	{
		matchIndex(i);
		
		if (this.expressionStack.isEmpty())
			throw new SemanticException("Ending expression that didn't start", symbol);
		
		TokenType current = null;
		TokenType operator = null;
		
		Iterator<IndexedValue<TokenType>> it = this.expressionStack.iterator();
		while (it.hasNext())
		{
			TokenType type = it.next().value; it.remove();
			
			if (current == null) {
				current = type;
			}
			else if (operator == null)
			{
				operator = type;
				
				// End of expression
				if (type == null) break;
			}
			// Expression is formed
			else
			{
				if (operator == TokenType.AssignmentCommand) {
					if (current != type)
					{
						// If types are different:
						// 	 - If type is Boolean, throw because it can't match with any other type;
						//   - If type is Integer, throw because it can only be assigned with Integer variables;
						//	 - If current is Boolean, type is Real, which is incompatible.
						if (type == TokenType.Boolean || type == TokenType.Integer || current == TokenType.Boolean)
							throw new SemanticException("Incompatible types in the same operation", symbol);
						
						current = type;
					}
				}
				
				// No other operator supports the Boolean type
				else if (current == TokenType.Boolean || type == TokenType.Boolean)
					throw new SemanticException("Incompatible types in the same operation", symbol);
					
				// The result of a relational operation is a boolean value
				else if (operator == TokenType.RelationalOperator)
					current = TokenType.Boolean;
					
				// If type is Real, current should be Real, but if type is Integer,
				// current should be kept as is, Real or Integer.
				else if (type == TokenType.Real)
					current = TokenType.Real;
				
				operator = null;
			}
		}
		
		if (operator != null) throw new SemanticException("Operation is not complete", symbol);
		
		if (!this.expressionStack.isEmpty())
			this.expressionStack.push(new IndexedValue<TokenType>(i, current));
	}
}
