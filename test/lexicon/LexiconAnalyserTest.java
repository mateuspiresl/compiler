package lexicon;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class LexiconAnalyserTest {

	@Test
	public void test()
	{
		List<Symbol> symbols = LexiconAnalyser.process(
				"program teste; {programa exemplo}\n" + 
				"var\n" + 
				"	valor1: integer;\n" + 
				"	valor2: real;\n" + 
				"begin\n" + 
				"	valor1 := 10;\n" + 
				"end."
			);
		int index = 0;
		
		assertEquals(19, symbols.size());
		
		assertEquals(new Symbol("program", TokenType.KeyWord, 0), symbols.get(index++));
		assertEquals(new Symbol("teste", TokenType.Identifier, 0), symbols.get(index++));
		assertEquals(new Symbol(";", TokenType.Delimiter, 0), symbols.get(index++));
		
		assertEquals(new Symbol("var", TokenType.KeyWord, 1), symbols.get(index++));
		
		assertEquals(new Symbol("valor1", TokenType.Identifier, 2), symbols.get(index++));
		assertEquals(new Symbol(":", TokenType.Delimiter, 2), symbols.get(index++));
		assertEquals(new Symbol("integer", TokenType.KeyWord, 2), symbols.get(index++));
		assertEquals(new Symbol(";", TokenType.Delimiter, 2), symbols.get(index++));
		
		assertEquals(new Symbol("valor2", TokenType.Identifier, 3), symbols.get(index++));
		assertEquals(new Symbol(":", TokenType.Delimiter, 3), symbols.get(index++));
		assertEquals(new Symbol("real", TokenType.KeyWord, 3), symbols.get(index++));
		assertEquals(new Symbol(";", TokenType.Delimiter, 3), symbols.get(index++));
		
		assertEquals(new Symbol("begin", TokenType.KeyWord, 4), symbols.get(index++));
		
		assertEquals(new Symbol("valor1", TokenType.Identifier, 5), symbols.get(index++));
		assertEquals(new Symbol(":=", TokenType.AssignmentCommand, 5), symbols.get(index++));
		assertEquals(new Symbol("10", TokenType.Integer, 5), symbols.get(index++));
		assertEquals(new Symbol(";", TokenType.Delimiter, 5), symbols.get(index++));
		
		assertEquals(new Symbol("end", TokenType.KeyWord, 6), symbols.get(index++));
		assertEquals(new Symbol(".", TokenType.Delimiter, 6), symbols.get(index++));
	}

}
