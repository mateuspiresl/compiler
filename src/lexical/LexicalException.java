package lexical;

/**
 * Thrown to indicate a lexicographic error.
 */
@SuppressWarnings("serial")
public class LexicalException extends RuntimeException {
	public LexicalException(String msg) { super(msg); }
}
