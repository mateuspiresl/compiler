package syntactic;

import java.util.List;
import java.util.ListIterator;

import lexicon.Symbol;
import lexicon.TokenType;

public class SyntacticAnalyser
{
	private ListIterator<Symbol> it;
	
	public SyntacticAnalyser(List<Symbol> symbols) {
		this.it = symbols.listIterator();
	}
	
	public void analyse()
	{
		if (!has())
			throw new SyntacticException("Missing key word 'program'");
		
		if (!next().equals(Types.PROGRAM))
			throw new SyntacticException("Missing key word 'program'", this.it);
		
		if (!has() || nextType() != TokenType.Identifier)
			throw new SyntacticException("Missing program identifier", this.it);
		
		if (!has() || !next().equals(Types.INSTRUCTION_END))
			throw new SyntacticException("Missing ';'", this.it);
		
		if (has()) matchVariableDeclarations();
		if (has()) matchProcedureDeclarations();
		
		if (has()) System.out.println("MAYBE AN ERROR");
	}
	
	private boolean has() { return this.it.hasNext(); }
	private String next() { return this.it.next().getToken(); }
	private TokenType nextType() { return this.it.next().getType(); }
	// private void previous() { this.it.previous(); }
	
	private void matchVariableDeclarations()
	{
		if (next().equals(Types.VAR))
			matchVariableDeclarationList(true);
		else
			this.it.previous();
	}
	
	private void matchVariableDeclarationList(boolean isFirst)
	{
		if (!matchIdentifiersList())
		{
			if (isFirst) throw new SyntacticException("Missing identifier", this.it);
			this.it.previous();
		}
		else
		{
			if (!has() || !next().equals(":"))
				throw new SyntacticException("Missing ':'", this.it);
			
			if (!has() || !Types.TYPES.contains(next()))
				throw new SyntacticException("Invalid or missing type", this.it);
			
			if (!has() || !next().equals(";"))
				throw new SyntacticException("Missing ';'", this.it);
			
			if (has()) matchVariableDeclarationList(false);
		}
	}
	
	private boolean matchIdentifiersList()
	{
		if (!has() || nextType() != TokenType.Identifier)
			return false;
		
		if (has())
		{
			if (next().equals(","))
			{
				if (!matchIdentifiersList())
					throw new SyntacticException("Missing identifier", this.it);
			}
			else this.it.previous();
		}
		
		return true;
	}
	
	private void matchProcedureDeclarations()
	{
		if (matchProcedureDeclaration() && has())
			matchProcedureDeclarations();
	}
	
	private boolean matchProcedureDeclaration()
	{
		if (!next().equals(Types.PROCEDURE))
		{
			// Offer unmatched token to the iterator
			this.it.previous();
			return false;
		}
		
		if (!has() || nextType() != TokenType.Identifier)
			throw new SyntacticException("Missing procedure identifier", this.it);
		
		if (has()) matchParameters();
		
		if (!has() || !next().equals(";"))
			throw new SyntacticException("Missing ';'", this.it);
		
		if (has()) matchVariableDeclarations();
		if (has()) matchProcedureDeclarations();
		matchCompoundCommand();
		
		return true;
	}
	
	private void matchParameters()
	{
		if (next().equals(Types.PROCEDURE_OPEN))
		{
			matchParametersList();
			
			if (!has() || !next().equals(")"))
				throw new SyntacticException("Missing ')'", this.it);
		}
		else this.it.previous();
	}
	
	private void matchParametersList()
	{
		matchIdentifiersList();
		
		if (!has() || !next().equals(":"))
			throw new SyntacticException("Missing ':'", this.it);
		
		if (!has() || !Types.TYPES.contains(next()))
			throw new SyntacticException("Invalid or missing type", this.it);
		
		if (has())
		{
			if (next().equals(";"))
				matchParametersList();
			else
				this.it.previous();
		}
	}
	
	private void matchCompoundCommand()
	{
		if (!has() || !next().equals("begin"))
			throw new SyntacticException("Missing 'begin' command", this.it);
		
		matchOptionalCommands();
		
		if (!has() || !next().equals("end"))
			throw new SyntacticException("Missing 'end' command", this.it);
	}
	
	private void matchOptionalCommands()
	{
		if (has()) matchCommandList();
	}
	
	private void matchCommandList()
	{
		matchCommand();
		
		if (has())
		{
			if (next().equals(";"))
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
		if (!has() || nextType() != TokenType.Identifier)
			throw new SyntacticException("Missing identifier", this.it);
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
		if (!has()) throw new SyntacticException("Missing factor", this.it);
		
		Symbol sym = this.it.next();
		
		if (sym.getType() == TokenType.Identifier)
		{
			if (next().equals("("))
			{
				matchExpressionList();
				
				if (!has() || !next().equals(")"))
					throw new SyntacticException("Missing ')'", this.it);
			}
			else this.it.previous();
		}
		else if (sym.getToken().equals("("))
		{
			matchExpression();
			
			if (!has() || !next().equals(")"))
				throw new SyntacticException("Missing ')'", this.it);
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
