package lexicon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Process the a source code lexicographically.
 */
public class LexiconAnalyser {
	
	private List<Symbol> symbols;
	// Indicates a comment opening symbol was found, but no comment
	// closing symbol after it.
	private boolean commenting = false;
	// The current line.
	private int at = 0;

	/**
	 * The constructor.
	 */
	public LexiconAnalyser() {
		this.symbols = new ArrayList<>();
	}

	/**
	 * Process an entire code.
	 * @param code the entire code.
	 * @return itself.
	 */
	public LexiconAnalyser processCode(String code) {
		return processLines(code.split("\n"));
	}
	
	/**
	 * Process list of lines.
	 * @param lines the list of lines.
	 * @return itself.
	 */
	public LexiconAnalyser processLines(List<String> lines) {
		return processLines(lines.toArray(new String[0]));
	}

	/**
	 * Process an array of lines.
	 * @param lines the array of lines.
	 * @return itself.
	 */
	public LexiconAnalyser processLines(String[] lines)
	{
		for (int i = 0; i < lines.length; i++)
			processLine(lines[i]);
		
		return this;
	}

	/**
	 * Process a line.
	 * @param line the line.
	 * @return itself.
	 */
	public LexiconAnalyser processLine(String line)
	{
		this.at++;
		
		String[] tokens = line.split(" ");
		for (String token : tokens)
		{
			List<String> subTokens = processToken(token.trim());
			for (String subToken : subTokens)
			{
				String lower = subToken.toLowerCase();
				TokenType type = null;
				
				// Ignores everything if it's commenting, unless it's a
				// comment closing symbol.
				if (commenting) {
					if (lower.equals(Rules.COMMENT_CLOSE))
						commenting = false;
				}
				
				else if (lower.equals(Rules.COMMENT_OPEN))
					commenting = true;
				
				else if (lower.equals(Rules.COMMENT_CLOSE))
					throw new LexiconException("Closing comment without open, at " + at);
				
				else if (lower.equals(Rules.COMMENT_INLINE))
					// Ignores everything after an inline comment
					return this;
				
				else if (lower.equals(Rules.ASSIGNMENT_COMMAND))
					type = TokenType.AssignmentCommand;
				
				else if (Rules.INTEGER_PATTERN.matcher(lower).matches())
					type = TokenType.Integer;
				
				else if (Rules.REAL_PATTERN.matcher(lower).matches())
					type = TokenType.Real;
				
				else if (Rules.COMPLEX_PATTERN.matcher(lower).matches())
					type = TokenType.Real;
				
				else if (Rules.DELIMITERS.contains(lower))
					type = TokenType.Delimiter;
				
				else if (Rules.OPERATORS_RELATIONAL.contains(lower))
					type = TokenType.RelationalOperator;
				
				else if (Rules.OPERATORS_ADDITIVE.contains(lower))
					type = TokenType.AdditiveOperator;
				
				else if (Rules.OPERATORS_MULTIPLICATIVE.contains(lower))
					type = TokenType.MultiplicativeOperator;
				
				else if (Rules.KEY_WORDS.contains(lower))
					type = TokenType.KeyWord;
				
				else if (Rules.IDENTIFIER_PATTERN.matcher(lower).matches())
					type = TokenType.Identifier;
				
				else throw new LexiconException("The symbol '" + subToken + "' does not belong to this language, at " + at);
				
				if (type != null) this.symbols.add(new Symbol(lower, type, at));
			}
		}
		
		return this;
	}
	
	/**
	 * Process tokens.
	 * This method receives a string without spaces and breaks
	 * into simpler tokens.
	 * @param token the token.
	 * @return the list of tokens.
	 */
	private List<String> processToken(String token)
	{
		List<String> subTokens = new ArrayList<>();
		Matcher matcher = Rules.GENERAL_PATTERN.matcher(token);
		int lastIndex = 0;
		
		// Finds a separator
		while (!matcher.hitEnd() && matcher.find())
		{
			// It there is something that wasn't catch by the 
			// general pattern, it's a symbol that does not belong
			// to the language and must be added to be recognized
			// in the processor.
			if (matcher.start() > lastIndex)
				subTokens.add(token.substring(lastIndex, matcher.start()));
			
			subTokens.add(matcher.group());
			lastIndex = matcher.end();
		}
		
		// There's a string after the last substring found.
		if (lastIndex < token.length())
			subTokens.add(token.substring(lastIndex, token.length()));
		
		return subTokens;
	}
	
	/**
	 * Returns the list of symbols.
	 * @return the list of symbols.
	 * @throws LexiconException if a comment wasn't closed.
	 */
	public List<Symbol> done()
	{
		if (this.commenting) throw new LexiconException("Comment not closed, at EOF");
		return this.symbols;
	}
	
	public static List<Symbol> process(String code) {
		return new LexiconAnalyser().processCode(code).done();
	}
	
	public static List<Symbol> process(String[] lines) {
		return new LexiconAnalyser().processLines(lines).done();
	}
	
	public static List<Symbol> process(List<String> lines) {
		return new LexiconAnalyser().processLines(lines).done();
	}
	
}
