package lexicon;

/**
 * Thrown to indicate a lexicography error.
 */
@SuppressWarnings("serial")
public class LexiconException extends RuntimeException {
	public LexiconException(String msg) { super(msg); }
}
