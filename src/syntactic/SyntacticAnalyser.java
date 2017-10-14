package syntactic;

import java.util.List;

import lexicon.Rules;
import lexicon.Symbol;
import lexicon.TokenType;

public class SyntacticAnalyser
{
	private List<Symbol> symbols;
	
	public SyntacticAnalyser(List<Symbol> symbols) {
		this.symbols = symbols;
	}
	
	public void analyse()
	{
		int i = 0;
		
		if (!has(i) || !get(i++).equals(Types.PROGRAM))
			throw new SyntacticException("Missing key word 'program'", previous(i));
		
		if (!has(i) || getType(i++) != TokenType.Identifier)
			throw new SyntacticException("Missing program identifier", previous(i));
		
		if (!has(i) || !get(i++).equals(Types.INSTRUCTION_END))
			throw new SyntacticException("Missing ';'", previous(i));
		
		i = matchVariableDeclarations(i);
		i = matchProcedureDeclarations(i);
		i = matchCompoundCommand(i);
		
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
		if (has(i) && get(i).equals(Types.VAR))
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
			
			if (!has(i) || !Types.TYPES.contains(get(i++)))
				throw new SyntacticException("Invalid or missing type", previous(i - 1));
			
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
		if (!get(i++).equals(Types.PROCEDURE))
			return i - 1;
		
		if (!has(i) || getType(i++) != TokenType.Identifier)
			throw new SyntacticException("Missing procedure identifier", previous(i - 1));
		
		i = matchParameters(i);
		
		if (!has(i) || !get(i++).equals(";"))
			throw new SyntacticException("Missing ';'", previous(i - 1));
		
		i = matchVariableDeclarations(i);
		i = matchProcedureDeclarations(i);
		i = matchCompoundCommand(i);
		
		return i;
	}
	
	private int matchParameters(int i)
	{
		if (get(i++).equals(Types.PROCEDURE_OPEN))
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
		
		if (!has(i) || !Types.TYPES.contains(get(i++)))
			throw new SyntacticException("Invalid or missing type", previous(i - 1));
		
		if (has(i) && get(i).equals(";"))
			i = matchParametersList(i + 1);
		
		return i;
	}
	
	private int matchCompoundCommand(int i)
	{
		if (!has(i) || !get(i++).equals("begin"))
			throw new SyntacticException("Missing 'begin' command", previous(i));
		
		i = matchOptionalCommands(i);
		
		if (!has(i) || !get(i++).equals("end"))
			throw new SyntacticException("Missing 'end' command", previous(i));
		
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
			int inner = matchVariable(i);
			
			if (!has(inner) || getType(inner++) != TokenType.AssignmentCommand)
				throw new SyntacticException("Missing assignment command", previous(inner - 1));
			
			return matchExpression(inner);
		}
		catch (SyntacticException e) { }
		
		// Procedure call
		try {
			return matchProcedureCall(i);
		}
		catch (SyntacticException e) { }
		
		// Inner compound command
		try {
			return matchCompoundCommand(i);
		}
		catch (SyntacticException e) { }
		
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
		catch (SyntacticException e) { }
		
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
		catch (SyntacticException e) { }
		
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
		catch (SyntacticException e) { }
		
		return i;
	}
	
	private int matchVariable(int i)
	{
		if (!has(i) || getType(i) != TokenType.Identifier)
			throw new SyntacticException("Missing identifier", previous(i));
		
		return i + 1;
	}
	
	private int matchExpression(int i)
	{
		i = matchSimpleExpression(i);
		
		try {
			int inner = i;
			
			if (!has(inner) || !Rules.OPERATORS_RELATIONAL.contains(get(inner++)))
				throw new SyntacticException("Missing relational operator", previous(inner - 1));
			
			return matchSimpleExpression(inner);
		}
		catch (SyntacticException e) {
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
		
		if (get(i).equals("+") || get(i).equals("-")) i++;
		
		i = matchTerm(i);
		return matchSimpleExpressionComplement(i);
	}
	
	private int matchSimpleExpressionComplement(int i)
	{
		if (has(i) && Rules.OPERATORS_ADDITIVE.contains(get(i)))
		{
			i = matchTerm(i + 1);
			return matchSimpleExpressionComplement(i);
		}
		
		return i;
	}
	
	private int matchTerm(int i)
	{
		i = matchFactor(i);
		
		if (has(i) && Rules.OPERATORS_MULTIPLICATIVE.contains(get(i)))
			return matchTerm(i + 1);
		
		return i;
	}
	
	private int matchFactor(int i)
	{
		if (!has(i)) throw new SyntacticException("Missing factor", previous(i));
		
		if (getType(i) == TokenType.Identifier)
		{
			int inner = i + 1;
			
			// Procedure list of expressions
			if (get(inner).equals("("))
			{
				inner = matchExpressionList(inner + 1);
				
				if (!has(inner) || !get(inner++).equals(")"))
					throw new SyntacticException("Missing delimitier ')' from procedure list of expressions", previous(inner - 1));
			}
			
			return inner;
		}
		else if (get(i).equals("("))
		{
			i = matchExpression(i + 1);
			
			if (!has(i) || !get(i).equals(")"))
				throw new SyntacticException("Missing ')'", previous(i));
			
			return i + 1;
		}
		else if (get(i).equals("not"))
		{
			return matchFactor(i + 1);
		}
		else
		{
			if (   		getType(i) != TokenType.Integer
					&&	getType(i) != TokenType.Real
					&& !get(i).equals("true")
					&& !get(i).equals("false"))
				throw new SyntacticException("Didn't match any factor possibility", previous(i));
			
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
