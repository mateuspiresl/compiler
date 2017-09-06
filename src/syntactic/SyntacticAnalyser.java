package syntactic;

import java.util.List;
import java.util.ListIterator;

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
		
		if (has(i)) new RuntimeException("??????????????");
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
	
	private int matchVariableDeclarationList(int i, boolean isFirst)
	{
		int state = i;
		
		if ((i = matchIdentifiersList(i)) <= state) {
			if (isFirst) throw new SyntacticException("Missing identifier", previous(i));
		}
		else
		{
			if (!has(i) || !get(i++).equals(":"))
				throw new SyntacticException("Missing ':'", previous(i - 1));
			
			if (!has(i) || !Types.TYPES.contains(get(i++)))
				throw new SyntacticException("Invalid or missing type", previous(i - 1));
			
			if (!has(i) || !get(i++).equals(";"))
				throw new SyntacticException("Missing ';'", previous(i - 1));
			
			matchVariableDeclarationList(i, false);
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
			i = matchProcedureDeclarations(i);
		
		return i;
	}
	
	private int matchProcedureDeclaration(int i)
	{
		if (!get(i).equals(Types.PROCEDURE))
			return i;
		
		if (!has(i) || getType(i++) != TokenType.Identifier)
			throw new SyntacticException("Missing procedure identifier", previous(i - 1));
		
		matchParameters(i);
		
		if (!has(i) || !get(i++).equals(";"))
			throw new SyntacticException("Missing ';'", previous(i - 1));
		
		i = matchVariableDeclarations(i);
		i = matchProcedureDeclarations(i);
		// i = matchCompoundCommand();
		
		return i;
	}
	
	private int matchParameters(int i)
	{
		if (get(i).equals(Types.PROCEDURE_OPEN))
		{
			i = matchParametersList(i);
			
			if (!has(i) || !get(i).equals(")"))
				throw new SyntacticException("Missing ')'", previous(i));
		}
		
		return i;
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
	
	private void matchCompoundCommand()
	{
		if (!has(i) || !get(i).equals("begin"))
			throw new SyntacticException("Missing 'begin' command", previous(i));
		
		matchOptionalCommands();
		
		if (!has(i) || !get(i).equals("end"))
			throw new SyntacticException("Missing 'end' command", previous(i));
	}
	
	private void matchOptionalCommands()
	{
		if (has(i)) matchCommandList();
	}
	
	private void matchCommandList()
	{
		matchCommand();
		
		if (has(i))
		{
			if (get(i).equals(";"))
				matchCommandList();
			else
				this.it.previous();
		}
	}
	
	private void matchCommand()
	{
		matchVariable();
	}
	
	private void matchVariable()
	{
		if (!has(i) || getType(i) != TokenType.Identifier)
			throw new SyntacticException("Missing identifier", previous(i));
	}
	
	private void matchExpression()
	{
		
	}
	
	private void matchSimpleExpression()
	{
		
	}
	
	private void matchExpressionList()
	{
		
	}
	
	private void matchTerm()
	{
		
	}
	
	private void matchFactor()
	{
		if (!has(i)) throw new SyntacticException("Missing factor", previous(i));
		
		Symbol sym = this.it.get(i);
		
		if (sym.getType() == TokenType.Identifier)
		{
			if (get(i).equals("("))
			{
				matchExpressionList();
				
				if (!has(i) || !get(i).equals(")"))
					throw new SyntacticException("Missing ')'", previous(i));
			}
			else this.it.previous();
		}
		else if (sym.getToken().equals("("))
		{
			matchExpression();
			
			if (!has(i) || !get(i).equals(")"))
				throw new SyntacticException("Missing ')'", previous(i));
		}
		else if (sym.getToken().equals("not"))
		{
			matchFactor();
		}
		else if (   sym.getType() != TokenType.Integer
				||  sym.getType() != TokenType.Real
				|| !sym.getToken().equals("true")
				|| !sym.getToken().equals("false"))
		{
			
		}
			
	}
}
