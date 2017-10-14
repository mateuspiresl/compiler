package syntactic;

import java.util.ListIterator;

import lexical.Symbol;

@SuppressWarnings("serial")
public class SyntacticException extends RuntimeException
{
	public SyntacticException(String msg) {
		super(msg);
	}
	
	public SyntacticException(String msg, int line) {
		this(msg + ", at line " + line);
	}
	
	public SyntacticException(String msg, Symbol sym) {
		this(msg + ", token '" + sym.getToken() + "', at line " + sym.getAt());
	}
	
	public SyntacticException(String msg, ListIterator<Symbol> it) {
		this(msg, it.previous());
	}
}
