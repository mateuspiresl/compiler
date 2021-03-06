package lexical;

import utils.Log;

/**
 * Represents all token types.
 */
public enum TokenType {
	KeyWord("Key-word"),
	Program("Program"),
	Procedure("Procedure"),
	Identifier("Identifier"),
	Integer("Integer"),
	Real("Real"),
	Boolean("Booleano"),
	Complex("Complex number"),
	Delimiter("Delimiter"),
	AssignmentCommand("Assignment command"),
	RelationalOperator("Relational operator"),
	AdditiveOperator("Additive operator"),
	MultiplicativeOperator("Multiplicative operator"),
	LogicalOperator("Logical operator");

	private final String asString;

	private TokenType(String asString) {
		this.asString = asString;
	}

	@Override
	public String toString()
	{
		if (Log.DEBUG)
		{
			String[] words = this.asString.split(" ");
			StringBuilder builder = new StringBuilder();
			
			for (String word : words)
				builder.append(word.charAt(0));
			
			return builder.toString();
		}
		else return this.asString;
	}
}
