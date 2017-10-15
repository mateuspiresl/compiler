package syntactic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lexical.Rules;
import lexical.Symbol;
import lexical.TokenType;
import utils.Log;

public class SyntacticAnalyser
{
	public static final Set<String> TYPES = new HashSet<String>();
	
	// Initialization
	static {
		TYPES.add("integer");
		TYPES.add("real");
		TYPES.add("boolean");
	}
	
	private List<Symbol> symbols;
	private SyntacticListener listener;
	
	public SyntacticAnalyser(List<Symbol> symbols, SyntacticListener listener)
	{
		this.symbols = symbols;
		this.listener = listener;
	}
	
	public SyntacticAnalyser(List<Symbol> symbols) {
		this(symbols, new EmptySyntaticListener());
	}
	
	// Helpers
	private boolean 	has(int i) 						{ return this.symbols.size() > i; }
	private Symbol 		get(int i) 						{ return this.symbols.get(i); }
	private String 		getToken(int i) 				{ return get(i).getToken(); }
	private TokenType 	getType(int i) 					{ return get(i).getType(); }
	private boolean 	isToken(int i, String token) 	{ return getToken(i).equals(token); }
	private boolean 	isType(int i, TokenType type) 	{ return getType(i) == type; }
	private int 		at(int i) 						{ return get(i).getAt(); }
	private Symbol 		last(int i) 					{ while (!has(i)) i--; return get(i); }
	
	public void analyse()
	{
		int i = 0;
		
		try {
			if (!isToken(i++, "program"))
				throw new SyntacticException("Missing key word 'program'", get(i - 1));
			
			if (!isType(i++, TokenType.Identifier))
				throw new SyntacticException("Missing program identifier", get(i - 1));
			
			this.listener.onScopeBegin(i - 1, at(i - 1));
			
			if (!getToken(i++).equals(";"))
				throw new SyntacticException("Missing ';'", get(i - 1));
			
			i = matchVariableDeclarations(i);
			i = matchProcedureDeclarations(i);
			i = matchCompoundCommand(i);
			
			this.listener.onScopeEnd(i - 1, at(i - 1));

			if (!getToken(i).equals("."))
				throw new SyntacticException("Missing '.' at end of file", get(i));
			
			if (has(i + 1))
				throw new SyntacticException("Remaining code after program end");
		}
		catch (IndexOutOfBoundsException e) {
			throw new SyntacticException("Unexpected end of file", last(i));
		}
	}
	
	private int matchVariableDeclarations(int i)
	{
		if (isToken(i, "var"))
			return matchVariableDeclarationList(i + 1, true);
		
		return i;
	}
	
	private int matchVariableDeclarationList(int i, boolean primary)
	{
		int state = i;
		
		if ((i = matchIdentifiersList(i)) > state)
		{
			if (!getToken(i).equals(":"))
				throw new SyntacticException("Missing ':'", get(i));
			
			if (!TYPES.contains(getToken(++i)))
				throw new SyntacticException("Invalid or missing type", get(i));
			
			this.listener.onTypeDefinition(i, get(i));
			
			if (!getToken(++i).equals(";"))
				throw new SyntacticException("Missing ';'", get(i));
			
			return matchVariableDeclarationList(i + 1, false);
		}
		else if (primary) {
			throw new SyntacticException("Missing identifier", get(i));
		}
		
		return i;
	}
	
	private int matchIdentifiersList(int i)
	{
		if (!isType(i, TokenType.Identifier)) return i;

		this.listener.onVariableDeclaration(i, get(i));
		
		if (isToken(++i, ","))
		{
			int state = i + 1;
			
			if ((i = matchIdentifiersList(state)) == state)
				throw new SyntacticException("Missing identifier", get(i));
		}
		
		return i;
	}
	
	private int matchProcedureDeclarations(int i)
	{
		int state = i;
		i = matchProcedureDeclaration(i);
		
		if (i > state)
		{
			if (!isToken(i, ";"))
				throw new SyntacticException("Missing ';'", get(i));
			
			i = matchProcedureDeclarations(i + 1);
		}
		
		return i;
	}
	
	private int matchProcedureDeclaration(int i)
	{
		if (!isToken(i, "procedure"))
			return i;
		
		if (!isType(++i, TokenType.Identifier))
			throw new SyntacticException("Missing procedure identifier", get(i));
		
		this.listener.onProcedureDeclaration(i, get(i));
		this.listener.onScopeBegin(i, at(i));
		this.listener.onProcedureParametersDeclarationBegin(i + 1, get(i + 1));

		i = matchParameters(i + 1);
		
		this.listener.onProcedureParametersDeclarationEnd(i, get(i));
		
		if (!isToken(i, ";"))
			throw new SyntacticException("Missing ';'", get(i));
		
		i = matchVariableDeclarations(i + 1);
		i = matchProcedureDeclarations(i);
		i = matchCompoundCommand(i);
		
		this.listener.onScopeEnd(i - 1, at(i - 1));
		
		return i;
	}
	
	private int matchParameters(int i)
	{
		if (isToken(i, "("))
		{
			i = matchParametersList(i + 1);
			
			if (!isToken(i, ")"))
				throw new SyntacticException("Missing ')'", get(i));
			
			return i + 1;
		}
		else return i;
	}
	
	private int matchParametersList(int i)
	{
		int state = i;
		
		if ((i = matchIdentifiersList(i)) == state)
			return i;
		
		if (!isToken(i, ":"))
			throw new SyntacticException("Missing ':'", get(i));
		
		if (!TYPES.contains(getToken(++i)))
			throw new SyntacticException("Invalid or missing type", get(i));
		
		this.listener.onTypeDefinition(i, get(i));
		
		return isToken(++i, ";") ? matchParametersList(i + 1) : i;
	}
	
	private int matchCompoundCommand(int i)
	{
		if (!isToken(i, "begin"))
			throw new SyntacticException("Missing 'begin' command", get(i));
		
		this.listener.onBlockBegin(i, get(i));
		
		i = matchCommandList(i + 1);
		
		if (!isToken(i, "end"))
			throw new SyntacticException("Missing 'end' command", get(i));
		
		if (this.listener != null) this.listener.onBlockEnd(i, get(i));
		
		return i + 1;
	}
	
	private int matchCommandList(int i)
	{
		int status = i;
		
		if ((i = matchCommand(i)) <= status)
			return status;
		
		if (isToken(i, ";"))
			return matchCommandList(i + 1);
			
		return i;
	}
	
	private int matchCommand(int i)
	{
		if (isType(i, TokenType.Identifier))
		{
			Log.d(1, "Command assignment BEGIN (" + (i - 1) + ")");
			this.listener.onExpressionBegin(i - 1, get(i - 1));
			
			i = matchIdentifier(i);
			
			if (!isType(i, TokenType.AssignmentCommand))
				return matchProcedureCall(i - 1);
			
			this.listener.onVariable(i - 1, get(i - 1));
			this.listener.onOperator(i, get(i));
			
			i = matchExpression(i + 1);
			
			Log.d(1, "Command assignment END (" + i + ")");
			this.listener.onExpressionEnd(i, get(i));
			
			return i;
		}
		
		// Inner compound command
		try {
			return matchCompoundCommand(i);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
		}
		
		// If-then-else
		try {
			int inner = i;
			
			if (!isToken(inner, "if"))
				throw new SyntacticException("Missing 'if' statement", get(inner));
			
			inner = matchExpression(inner + 1);
			
			if (!isToken(inner, "then"))
				throw new SyntacticException("Missing 'then' statement", get(inner));
			
			inner = matchCommand(inner + 1);
			return matchElse(inner);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
		}
		
		// While-do
		try {
			int inner = i;
			
			if (!isToken(inner, "while"))
				throw new SyntacticException("Missing 'while' statement", get(inner));
			
			inner = matchExpression(inner + 1);
			
			if (!isToken(inner, "do"))
				throw new SyntacticException("Missing 'then' statement", get(inner));
			
			return matchCommand(inner + 1);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
		}
		
		// Do-while
		try {
			int inner = i;
			
			if (!isToken(inner, "do"))
				throw new SyntacticException("Missing 'do' statement", get(inner));
			
			inner = matchCommand(inner + 1);
			
			if (!isToken(inner, "while"))
				throw new SyntacticException("Missing 'then' statement", get(inner));
			
			return  matchExpression(inner + 1);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
		}
		
		return i;
	}
	
	private int matchIdentifier(int i)
	{
		if (!has(i) || !isType(i, TokenType.Identifier))
			throw new SyntacticException("Missing identifier", last(i));
		
		return i + 1;
	}
	
	private int matchExpression(int i)
	{
		i = matchSimpleExpression(i);
		
		try {
			int inner = i;
			
			if (!has(inner) || !(
					   Rules.OPERATORS_RELATIONAL.contains(getToken(inner))
					|| Rules.OPERATORS_LOGICAL.contains(getToken(inner))))
				throw new SyntacticException("Missing relational operator", last(inner));
			
			this.listener.onOperator(inner, get(inner));
			
			return matchSimpleExpression(inner + 1);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
			return i;
		}
	}
	
	private int matchProcedureCall(int i)
	{
		try {
			i = matchIdentifier(i);
			
			this.listener.onProcedure(i - 1, get(i - 1));
			
			Log.d(1, "Procedure parameters BEGIN (" + (i) + ")");
			this.listener.onProcedureArgumentsBegin(i, get(i));
			
			if (isToken(i, "("))
			{
				if (isToken(i + 1, ")")) {
					i += 2;
				}
				else {
					i = matchExpressionList(i + 1);
					
					if (!isToken(i++, ")"))
						throw new SyntacticException("Missing delimitier ')' from procedure call", get(i - 1));
				}
			}
			
			Log.d(1, "Procedure parameters END (" + (i) + ")");
			this.listener.onProcedureArgumentsEnd(i, get(i));
			
			return i;
		}
		catch (SyntacticException e) {
			throw new SyntacticException("Missing procedure call identifier", get(i));
		}
	}
	
	private int matchElse(int i) {
		return isToken(i, "else") ? matchCommand(i + 1) : i;
	}
	
	private int matchSimpleExpression(int i)
	{
		if (!has(i)) throw new SyntacticException("Missing expression", last(i));
		
		Log.d(1, "Simple expression BEGIN (" + (i - 1) + ")");
		this.listener.onExpressionBegin(i - 1, get(i - 1));
		
		if (isToken(i, "+") || isToken(i, "-")) i++;
		
		i = matchTerm(i);
		i = matchSimpleExpressionComplement(i);
		
		Log.d(1, "Simple expression END (" + i + ")");
		this.listener.onExpressionEnd(i, get(i));
		
		return i;
	}
	
	private int matchSimpleExpressionComplement(int i)
	{
		if (has(i) && Rules.OPERATORS_ADDITIVE.contains(getToken(i)))
		{
			this.listener.onOperator(i, get(i));
			
			Log.d(1, "Simple expression complement BEGIN (" + (i - 1) + ")");
			this.listener.onExpressionBegin(i, get(i - 1));
			
			i = matchTerm(i + 1);
			i = matchSimpleExpressionComplement(i);
			
			Log.d(1, "Simple expression complement END (" + i + ")");
			this.listener.onExpressionEnd(i, get(i));
		}
		
		return i;
	}
	
	private int matchTerm(int i)
	{
		i = matchFactor(i);
		
		if (Rules.OPERATORS_MULTIPLICATIVE.contains(getToken(i)))
		{
			this.listener.onOperator(i, get(i));
			return matchTerm(i + 1);
		}
		
		return i;
	}
	
	private int matchFactor(int i)
	{
		if (!has(i)) throw new SyntacticException("Missing factor", last(i));
		
		if (isType(i, TokenType.Identifier))
		{
			int inner = matchIdentifier(i);
			
			this.listener.onVariable(i, get(i));
			
			return inner;
		}
		else if (isToken(i, "("))
		{
			this.listener.onExpressionBegin(i, get(i));
			
			i = matchExpression(i + 1);
			
			if (!isToken(i, ")"))
				throw new SyntacticException("Missing ')'", get(i));
			
			this.listener.onExpressionEnd(i, get(i));
			
			return i + 1;
		}
		else if (isToken(i, "not"))
		{
			return matchFactor(i + 1);
		}
		else
		{
			if (!isType(i, TokenType.Integer) && !isType(i, TokenType.Real)
					&& !isType(i, TokenType.Boolean))
				throw new SyntacticException("Didn't match any factor possibility", get(i));
			
			this.listener.onValue(i, get(i));
			
			return i + 1;
		}
	}
	
	private int matchExpressionList(int i)
	{
		Log.d(1, "Parameter expression BEGIN (" + (i - 1) + ")");
		this.listener.onExpressionBegin(i - 1, get(i - 1));
		
		i = matchExpression(i);
		
		Log.d(1, "Parameter expression END (" + i + ")");
		this.listener.onExpressionEnd(i, get(i));
		this.listener.onProcedureArgument(i, get(i));
		
		return isToken(i, ",") ? matchExpressionList(i + 1) : i;
	}
}
