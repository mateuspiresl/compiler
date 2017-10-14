package semantic;

import lexicon.Symbol;

@SuppressWarnings("serial")
public class SemanticException extends RuntimeException
{
	public SemanticException(String msg) {
		super(msg);
	}
	
	public SemanticException(String msg, int line) {
		this(msg + ", at line " + line);
	}
	
	public SemanticException(String msg, Symbol sym) {
		this(msg + ", token '" + sym.getToken() + "', at line " + sym.getAt());
	}
}
