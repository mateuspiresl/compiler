package lexicon;

public enum TokenType {
	KeyWord("Palavra-chave"),
	Identifier("Identificador"),
	Integer("Número inteiro"),
	Real("Número real"),
	Delimiter("Delimitador"),
	AssignmentCommand("Comando de atribuição"),
	RelationalOperator("Operador relacional"),
	AdditiveOperator("Operador aditivo"),
	MultiplicativeOperator("Operador multiplicativo");

	private final String asString;

	private TokenType(String asString) {
		this.asString = asString;
	}
	
	@Override
	public String toString() {
		return this.asString;
	}
}
