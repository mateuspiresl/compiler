package lexicon;

public class Symbol {

	private final String token;
	private final TokenType type;
	private final int at;
	
	public Symbol(String token, TokenType type, int at)
	{
		this.token = token;
		this.type = type;
		this.at = at;
	}

	public String getToken() {
		return this.token;
	}

	public TokenType getType() {
		return this.type;
	}

	public int getAt() {
		return this.at;
	}

	@Override
	public String toString() {
		return "'" + this.token + "', " + this.type + ", at " + this.at;
	}
	
	@Override
	public boolean equals(Object thatObj)
	{
		if (!(thatObj instanceof Symbol)) return false;
		Symbol that = (Symbol) thatObj;
		return this.token.equals(that.token) && this.type == that.type && this.at == that.at;
	}
	
}
