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
		this(symbols, null);
	}
	
	public void analyse()
	{
		int i = 0;
		
		if (!has(i) || !get(i++).equals("program"))
			throw new SyntacticException("Missing key word 'program'", previous(i));
		
		if (!has(i) || getType(i++) != TokenType.Identifier)
			throw new SyntacticException("Missing program identifier", previous(i));
		
		if (this.listener != null) this.listener.onScopeBegin(i - 1, this.symbols.get(i - 1).getAt());
		
		if (!has(i) || !get(i++).equals(";"))
			throw new SyntacticException("Missing ';'", previous(i));
		
		i = matchVariableDeclarations(i);
		i = matchProcedureDeclarations(i);
		i = matchCompoundCommand(i);
		
		if (this.listener != null) this.listener.onScopeEnd(i - 1, this.symbols.get(i - 1).getAt());
		
		if (!has(i) || !get(i++).equals("."))
			throw new SyntacticException("Missing '.' at end of file", previous(i));
		
		if (has(i)) new RuntimeException("Remaining");
	}
	
	private boolean has(int i) { return this.symbols.size() > i; }
	private String get(int i) { return this.symbols.get(i).getToken(); }
	private TokenType getType(int i) { return this.symbols.get(i).getType(); }
	private Symbol previous(int i) { return this.symbols.get(has(i) ? i : i - 1); }
	
	private int matchVariableDeclarations(int i)
	{
		if (has(i) && get(i).equals("var"))
			return matchVariableDeclarationList(i + 1, true);
		
		return i;
	}
	
	private int matchVariableDeclarationList(int i, boolean primary)
	{
		int state = i;
		
		if ((i = matchIdentifiersList(i)) <= state) {
			if (primary) throw new SyntacticException("Missing identifier", previous(i));
		}
		else
		{
			if (!has(i) || !get(i++).equals(":"))
				throw new SyntacticException("Missing ':'", previous(i - 1));
			
			if (!has(i) || !TYPES.contains(get(i++)))
				throw new SyntacticException("Invalid or missing type", previous(i - 1));
			
			if (this.listener != null) this.listener.onTypeDefinition(i, this.symbols.get(i - 1));
			
			if (!has(i) || !get(i++).equals(";"))
				throw new SyntacticException("Missing ';'", previous(i - 1));
			
			return matchVariableDeclarationList(i, false);
		}
		
		return i;
	}
	
	private int matchIdentifiersList(int i)
	{
		if (!has(i) || getType(i++) != TokenType.Identifier)
			return i - 1;

		if (this.listener != null) this.listener.onVariableDeclaration(i - 1, this.symbols.get(i - 1));
		
		int state = i;
		
		if (has(i) && get(i).equals(",") && (i = matchIdentifiersList(i + 1)) <= state + 1)
			throw new SyntacticException("Missing identifier", previous(i + 1));
		
		return i;
	}
	
	private int matchProcedureDeclarations(int i)
	{
		int state = i;
		
		if (has(i) && (i = matchProcedureDeclaration(i)) > state)
		{
			if (!has(i) || !get(i++).equals(";"))
				throw new SyntacticException("Missing ';'", previous(i - 1));
			
			i = matchProcedureDeclarations(i);
		}
		
		return i;
	}
	
	private int matchProcedureDeclaration(int i)
	{
		if (!get(i++).equals("procedure"))
			return i - 1;
		
		if (!has(i) || getType(i++) != TokenType.Identifier)
			throw new SyntacticException("Missing procedure identifier", previous(i - 1));
		
		if (this.listener != null) this.listener.onScopeBegin(i - 1, this.symbols.get(i - 1).getAt());
		
		i = matchParameters(i);
		
		if (!has(i) || !get(i++).equals(";"))
			throw new SyntacticException("Missing ';'", previous(i - 1));
		
		i = matchVariableDeclarations(i);
		i = matchProcedureDeclarations(i);
		i = matchCompoundCommand(i);
		
		if (this.listener != null) this.listener.onScopeEnd(i - 1, this.symbols.get(i - 1).getAt());
		
		return i;
	}
	
	private int matchParameters(int i)
	{
		if (get(i++).equals("("))
		{
			i = matchParametersList(i);
			
			if (!has(i) || !get(i++).equals(")"))
				throw new SyntacticException("Missing ')'", previous(i));
			
			return i;
		}
		else return i - 1;
	}
	
	private int matchParametersList(int i)
	{
		i = matchIdentifiersList(i);
		
		if (!has(i) || !get(i++).equals(":"))
			throw new SyntacticException("Missing ':'", previous(i - 1));
		
		if (!has(i) || !TYPES.contains(get(i++)))
			throw new SyntacticException("Invalid or missing type", previous(i - 1));
		
		if (has(i) && get(i).equals(";"))
			i = matchParametersList(i + 1);
		
		return i;
	}
	
	private int matchCompoundCommand(int i)
	{
		if (!has(i) || !get(i++).equals("begin"))
			throw new SyntacticException("Missing 'begin' command", previous(i));
		
		if (this.listener != null) this.listener.onBlockBegin(i - 1, this.symbols.get(i - 1));
		
		i = matchOptionalCommands(i);
		
		if (!has(i) || !get(i++).equals("end"))
			throw new SyntacticException("Missing 'end' command", previous(i));
		
		if (this.listener != null) this.listener.onBlockEnd(i - 1, this.symbols.get(i - 1));
		
		return i;
	}
	
	private int matchOptionalCommands(int i)
	{
		if (has(i)) return matchCommandList(i);
		else return i;
	}
	
	private int matchCommandList(int i)
	{
		int status = i;
		
		if ((i = matchCommand(i)) <= status)
			return status;
		
		if (has(i) && get(i).equals(";"))
			return matchCommandList(i + 1);
			
		return i;
	}
	
	private int matchCommand(int i)
	{
		// Assignment
		// variable := expression
		try {
			Log.d(1, "Command assignment BEGIN (" + (i - 1) + ")");
			if (this.listener != null) this.listener.onExpressionBegin(i - 1, this.symbols.get(i - 1));
			
			int inner = matchVariable(i);
			
			if (!has(inner) || getType(inner) != TokenType.AssignmentCommand)
				throw new SyntacticException("Missing assignment command", previous(inner));
			
			if (this.listener != null) this.listener.onOperator(inner, this.symbols.get(inner));
			
			inner = matchExpression(inner + 1);
			
			Log.d(1, "Command assignment END (" + inner + ")");
			if (this.listener != null) this.listener.onExpressionEnd(inner, this.symbols.get(inner));
			
			return inner;
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
		}
		
		// Procedure call
		try {
			return matchProcedureCall(i);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
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
			
			if (!has(inner) || !get(inner++).equals("if"))
				throw new SyntacticException("Missing 'if' statement", previous(inner - 1));
			
			inner = matchExpression(inner);
			
			if (!has(inner) || !get(inner++).equals("then"))
				throw new SyntacticException("Missing 'then' statement", previous(inner - 1));
			
			inner = matchCommand(inner);
			return matchElse(inner);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
		}
		
		// While-do
		try {
			int inner = i;
			
			if (!has(inner) || !get(inner++).equals("while"))
				throw new SyntacticException("Missing 'while' statement", previous(inner - 1));
			
			inner = matchExpression(inner);
			
			if (!has(inner) || !get(inner++).equals("do"))
				throw new SyntacticException("Missing 'then' statement", previous(inner - 1));
			
			return matchCommand(inner);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
		}
		
		// Do-while
		try {
			int inner = i;
			
			if (!has(inner) || !get(inner++).equals("do"))
				throw new SyntacticException("Missing 'do' statement", previous(inner - 1));
			
			inner = matchCommand(inner);
			
			if (!has(inner) || !get(inner++).equals("while"))
				throw new SyntacticException("Missing 'then' statement", previous(inner - 1));
			
			return  matchExpression(inner);
		}
		catch (SyntacticException e) {
			if (this.listener != null) this.listener.matchIndex(i);
		}
		
		return i;
	}
	
	private int matchVariable(int i)
	{
		if (!has(i) || getType(i) != TokenType.Identifier)
			throw new SyntacticException("Missing identifier", previous(i));
		
		if (this.listener != null) this.listener.onVariable(i, this.symbols.get(i));
		
		return i + 1;
	}
	
	private int matchExpression(int i)
	{
		i = matchSimpleExpression(i);
		
		try {
			int inner = i;
			
			if (!has(inner) || !(
					   Rules.OPERATORS_RELATIONAL.contains(get(inner))
					|| Rules.OPERATORS_LOGICAL.contains(get(inner))))
				throw new SyntacticException("Missing relational operator", previous(inner));
			
			if (this.listener != null) this.listener.onOperator(inner, this.symbols.get(inner));
			
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
			i = matchVariable(i);
			
			if (get(i).equals("("))
			{
				i = matchExpressionList(i + 1);
				
				if (!has(i) || !get(i++).equals(")"))
					throw new SyntacticException("Missing delimitier ')' from procedure call", previous(i - 1));
			}
			
			return i;
		}
		catch (SyntacticException e) {
			throw new SyntacticException("Missing procedure call identifier", previous(i));
		}
	}
	
	private int matchElse(int i)
	{
		if (has(i) && get(i).equals("else"))
			return matchCommand(i + 1);
		
		return i;
	}
	
	private int matchSimpleExpression(int i)
	{
		if (!has(i)) throw new SyntacticException("Missing expression", previous(i));
		
		Log.d(1, "Simple expression BEGIN (" + (i - 1) + ")");
		if (this.listener != null) this.listener.onExpressionBegin(i - 1, this.symbols.get(i - 1));
		
		if (get(i).equals("+") || get(i).equals("-")) i++;
		
		i = matchTerm(i);
		i = matchSimpleExpressionComplement(i);
		
		Log.d(1, "Simple expression END (" + i + ")");
		if (this.listener != null) this.listener.onExpressionEnd(i, this.symbols.get(i));
		
		return i;
	}
	
	private int matchSimpleExpressionComplement(int i)
	{
		if (has(i) && Rules.OPERATORS_ADDITIVE.contains(get(i)))
		{
			if (this.listener != null)
			{
				this.listener.onOperator(i, this.symbols.get(i));
				
				Log.d(1, "Simple expression complement BEGIN (" + (i - 1) + ")");
				this.listener.onExpressionBegin(i, this.symbols.get(i - 1));
			}
			
			i = matchTerm(i + 1);
			i = matchSimpleExpressionComplement(i);
			
			Log.d(1, "Simple expression complement END (" + i + ")");
			if (this.listener != null) this.listener.onExpressionEnd(i, this.symbols.get(i));
		}
		
		return i;
	}
	
	private int matchTerm(int i)
	{
		i = matchFactor(i);
		
		if (has(i) && Rules.OPERATORS_MULTIPLICATIVE.contains(get(i)))
		{
			if (this.listener != null) this.listener.onOperator(i, this.symbols.get(i));
			return matchTerm(i + 1);
		}
		
		return i;
	}
	
	private int matchFactor(int i)
	{
		if (!has(i)) throw new SyntacticException("Missing factor", previous(i));
		
		if (getType(i) == TokenType.Identifier)
		{
			return matchVariable(i);
		}
		else if (get(i).equals("("))
		{
			if (this.listener != null) this.listener.onExpressionBegin(i, this.symbols.get(i));
			
			i = matchExpression(i + 1);
			
			if (!has(i) || !get(i).equals(")"))
				throw new SyntacticException("Missing ')'", previous(i));
			
			if (this.listener != null) this.listener.onExpressionEnd(i, this.symbols.get(i));
			
			return i + 1;
		}
		else if (get(i).equals("not"))
		{
			return matchFactor(i + 1);
		}
		else
		{
			Symbol symbol = this.symbols.get(i);
			
			if (   		symbol.getType() != TokenType.Integer
					&&	symbol.getType() != TokenType.Real
					&&	symbol.getType() != TokenType.Boolean)
				throw new SyntacticException("Didn't match any factor possibility", previous(i));
			
			if (this.listener != null) this.listener.onValue(i, this.symbols.get(i));
			
			return i + 1;
		}
	}
	
	private int matchExpressionList(int i)
	{
		i = matchExpression(i);
		
		if (has(i) && get(i).equals(","))
			return matchExpressionList(i + 1);
		
		return i;
	}
}
