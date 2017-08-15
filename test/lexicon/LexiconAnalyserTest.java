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
		
		assertEquals(new Symbol("program", TokenType.KeyWord, 1), symbols.get(index++));
		assertEquals(new Symbol("teste", TokenType.Identifier, 1), symbols.get(index++));
		assertEquals(new Symbol(";", TokenType.Delimiter, 1), symbols.get(index++));
		
		assertEquals(new Symbol("var", TokenType.KeyWord, 2), symbols.get(index++));
		
		assertEquals(new Symbol("valor1", TokenType.Identifier, 3), symbols.get(index++));
		assertEquals(new Symbol(":", TokenType.Delimiter, 3), symbols.get(index++));
		assertEquals(new Symbol("integer", TokenType.KeyWord, 3), symbols.get(index++));
		assertEquals(new Symbol(";", TokenType.Delimiter, 3), symbols.get(index++));
		
		assertEquals(new Symbol("valor2", TokenType.Identifier, 4), symbols.get(index++));
		assertEquals(new Symbol(":", TokenType.Delimiter, 4), symbols.get(index++));
		assertEquals(new Symbol("real", TokenType.KeyWord, 4), symbols.get(index++));
		assertEquals(new Symbol(";", TokenType.Delimiter, 4), symbols.get(index++));
		
		assertEquals(new Symbol("begin", TokenType.KeyWord, 5), symbols.get(index++));
		
		assertEquals(new Symbol("valor1", TokenType.Identifier, 6), symbols.get(index++));
		assertEquals(new Symbol(":=", TokenType.AssignmentCommand, 6), symbols.get(index++));
		assertEquals(new Symbol("10", TokenType.Integer, 6), symbols.get(index++));
		assertEquals(new Symbol(";", TokenType.Delimiter, 6), symbols.get(index++));
		
		assertEquals(new Symbol("end", TokenType.KeyWord, 7), symbols.get(index++));
		assertEquals(new Symbol(".", TokenType.Delimiter, 7), symbols.get(index++));
	}

	@Test
	public void testReal()
	{
		List<Symbol> symbols = LexiconAnalyser.process(
				"1\n" + 
				".\n" + 
				"2\n" + 
				"3.4\n" + 
				"5.6 a\n" +
				"c 9.10 d"
			);
		int index = 0;
		
		assertEquals(9, symbols.size());
		
		assertEquals(new Symbol("1", TokenType.Integer, 1), symbols.get(index++));
		assertEquals(new Symbol(".", TokenType.Delimiter, 2), symbols.get(index++));
		assertEquals(new Symbol("2", TokenType.Integer, 3), symbols.get(index++));
		assertEquals(new Symbol("3.4", TokenType.Real, 4), symbols.get(index++));
		assertEquals(new Symbol("5.6", TokenType.Real, 5), symbols.get(index++));
		assertEquals(new Symbol("a", TokenType.Identifier, 5), symbols.get(index++));
		assertEquals(new Symbol("c", TokenType.Identifier, 6), symbols.get(index++));
		assertEquals(new Symbol("9.10", TokenType.Real, 6), symbols.get(index++));
		assertEquals(new Symbol("d", TokenType.Identifier, 6), symbols.get(index++));
	}
	
	@Test
	public void testSpecialOperators()
	{
		List<Symbol> symbols = LexiconAnalyser.process(
				"1 and 2\n" + 
				"3 or 4\n" + 
				"a and 5\n" + 
				"6 or b\n" + 
				"c and d"
			);
		int index = 0;
		
		assertEquals(15, symbols.size());
		
		assertEquals(new Symbol("1", TokenType.Integer, 1), symbols.get(index++));
		assertEquals(new Symbol("and", TokenType.MultiplicativeOperator, 1), symbols.get(index++));
		assertEquals(new Symbol("2", TokenType.Integer, 1), symbols.get(index++));
		
		assertEquals(new Symbol("3", TokenType.Integer, 2), symbols.get(index++));
		assertEquals(new Symbol("or", TokenType.AdditiveOperator, 2), symbols.get(index++));
		assertEquals(new Symbol("4", TokenType.Integer, 2), symbols.get(index++));
		
		assertEquals(new Symbol("a", TokenType.Identifier, 3), symbols.get(index++));
		assertEquals(new Symbol("and", TokenType.MultiplicativeOperator, 3), symbols.get(index++));
		assertEquals(new Symbol("5", TokenType.Integer, 3), symbols.get(index++));
		
		assertEquals(new Symbol("6", TokenType.Integer, 4), symbols.get(index++));
		assertEquals(new Symbol("or", TokenType.AdditiveOperator, 4), symbols.get(index++));
		assertEquals(new Symbol("b", TokenType.Identifier, 4), symbols.get(index++));
		
		assertEquals(new Symbol("c", TokenType.Identifier, 5), symbols.get(index++));
		assertEquals(new Symbol("and", TokenType.MultiplicativeOperator, 5), symbols.get(index++));
		assertEquals(new Symbol("d", TokenType.Identifier, 5), symbols.get(index++));
	}
	
}
