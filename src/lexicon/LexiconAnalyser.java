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
	private int at;

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
		String[] tokens = line.split(" ");
		for (String token : tokens)
		{
			List<String> subTokens = processToken(token.trim());
			for (String subToken : subTokens)
			{
				String lower = subToken.toLowerCase();
				TokenType type = null;
				
				// Ignores everything if it's commenting, unless it's
				// comment closing.
				if (commenting) {
					if (lower.equals(Rules.COMMENT_CLOSE))
						commenting = false;
				}
				
				else if (lower.equals(Rules.COMMENT_OPEN))
					commenting = true;
				
				else if (lower.equals(Rules.COMMENT_CLOSE))
					throw new LexiconException("Closing comment without open, at " + at);
				
				else if (Rules.KEY_WORDS.contains(lower))
					type = TokenType.KeyWord;
				
				else if (Rules.IDENTIFIER_PATTERN.matcher(lower).matches())
					type = TokenType.Identifier;
				
				else if (Rules.INTEGER_PATTERN.matcher(lower).matches())
					type = TokenType.Integer;
				
				else if (Rules.REAL_PATTERN.matcher(lower).matches())
					type = TokenType.Real;
				
				else if (lower.equals(Rules.ASSIGNMENT_COMMAND))
					type = TokenType.AssignmentCommand;
				
				else if (Rules.DELIMITERS.contains(lower))
					type = TokenType.Delimiter;
				
				else if (Rules.OPERATORS_RELATIONAL.contains(lower))
					type = TokenType.RelationalOperator;
				
				else if (Rules.OPERATORS_ADDITIVE.contains(lower))
					type = TokenType.AdditiveOperator;
				
				else if (Rules.OPERATORS_MULTIPLICATIVE.contains(lower))
					type = TokenType.MultiplicativeOperator;
				
				else throw new LexiconException("The symbol '" + subToken + "' does not belong to this language, at " + at);
				
				if (type != null) this.symbols.add(new Symbol(subToken, type, at));
			}
		}
		
		this.at++;
		return this;
	}
	
	/**
	 * Process tokens.
	 * This method receives a string without spaces and breaks it
	 * in sub tokens by separators (everything that is not alphanumeric
	 * and is a valid symbol).
	 * @param token the token.
	 * @return the list of sub tokens.
	 */
	private List<String> processToken(String token)
	{
		List<String> subTokens = new ArrayList<>();
		Matcher matcher = Rules.SEPARATORS_PATTERN.matcher(token);
		int lastIndex = 0;
		
		// Indicates a real number (found an integer followed by a dot).
		boolean partialReal = false;
		
		// Finds a separator
		while (!matcher.hitEnd() && matcher.find())
		{
			// If it's the beginning of the token:
			// 	 if the found separator does not start at the index 0,
			//   the previous substring, that goes from 0 to the start
			//   of the separator, needs to be added.
			// Otherwise:
			//   if the found separator does not start right after the
			//   end of the previous one, there is a string between both
			//   that needs to be added.
			if (matcher.start() > lastIndex)
				subTokens.add(token.substring(lastIndex, matcher.start()));
			
			// If the found separator is a dot and the previous substring
			// is an integer, it found a "partial real", so there is a
			// chance that appending these two and the next substring,
			// a real number will be formed.
			if (matcher.group().equals(".") && subTokens.size() > 0) {
				partialReal = Rules.INTEGER_PATTERN.matcher(subTokens.get(subTokens.size() - 1)).matches();
			}
			// If the previous separator formed a partial real and there
			// was a substring between the current and last separators,
			// check if the appending with this will form a real number.
			else if (partialReal)
			{
				if (matcher.start() > lastIndex)
					fixReal(subTokens);
				
				partialReal = false;
			}
			
			subTokens.add(matcher.group());
			lastIndex = matcher.end();
		}
		
		// There's a string after the last separator found
		if (lastIndex < token.length())
		{
			subTokens.add(token.substring(lastIndex, token.length()));
			if (partialReal) fixReal(subTokens);
		}
		
		return subTokens;
	}

	/**
	 * Fix the real number breaking.
	 * It considers that the second and third last tokens forms
	 * a partial token, so it only checks if the last token is
	 * an integer, and if positive, builds the real number.
	 * @param tokens the list of tokens.
	 */
	private void fixReal(List<String> tokens)
	{
		int last = tokens.size() - 1;
		
		if (Rules.INTEGER_PATTERN.matcher(tokens.get(last)).matches())
		{
			tokens.set(last - 2, tokens.get(last - 2) + tokens.get(last - 1) + tokens.get(last));
			tokens.remove(last);
			tokens.remove(last - 1);
		}
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
