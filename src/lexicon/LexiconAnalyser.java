package lexicon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class LexiconAnalyser {
	
	private List<Symbol> symbols;
	private boolean commenting = false;
	private int at;

	public LexiconAnalyser() {
		this.symbols = new ArrayList<>();
	}

	public LexiconAnalyser processCode(String code) {
		return processLines(code.split("\n"));
	}
	
	public LexiconAnalyser processLines(List<String> lines) {
		return processLines(lines.toArray(new String[0]));
	}

	public LexiconAnalyser processLines(String[] lines)
	{
		for (int i = 0; i < lines.length; i++)
			processLine(lines[i]);
		
		return this;
	}

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
	
	private List<String> processToken(String token)
	{
		List<String> subTokens = new ArrayList<>(); 
		boolean partialReal = false;
		Matcher matcher = Rules.SEPARATORS_PATTERN.matcher(token);
		int lastIndex = 0;
		
		while (!matcher.hitEnd() && matcher.find())
		{
			if (matcher.start() > lastIndex)
				subTokens.add(token.substring(lastIndex, matcher.start()));
			
			if (matcher.group() == ".")
			{
				if (Rules.INTEGER_PATTERN.matcher(matcher.group()).matches())
					partialReal = true;
			}
			else if (partialReal)
			{
				fixReal(subTokens);
				partialReal =false;
			}
			
			subTokens.add(token.substring(matcher.start(), matcher.end()));
			lastIndex = matcher.end();
		}
		
		if (lastIndex < token.length())
		{
			subTokens.add(token.substring(lastIndex, token.length()));
			if (partialReal) fixReal(subTokens);
		}
		
		return subTokens;
	}

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
	
	public List<Symbol> done()
	{
		if (commenting) throw new RuntimeException("Comment not closed, at EOF");
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
