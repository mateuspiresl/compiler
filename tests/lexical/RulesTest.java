package lexical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Test;

import lexical.Rules;

public class RulesTest {
	
	@Test
	public void testIndentifierPattern()
	{
		assertTrue(Rules.IDENTIFIER_PATTERN.matcher("asdfgh").matches());
		assertTrue(Rules.IDENTIFIER_PATTERN.matcher("asdfgh_").matches());
		assertTrue(Rules.IDENTIFIER_PATTERN.matcher("asdfgh123213").matches());
		assertTrue(Rules.IDENTIFIER_PATTERN.matcher("asdfgh_123213").matches());
		
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("1asdfgh").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("122133").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("____").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("1asdfgh").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("1342__asdfgh").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("_asdfgh").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh-").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh!").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh#").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh$").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh&").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh.").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh,").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh;").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh?").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("asdfgh^").matches());
		assertFalse(Rules.IDENTIFIER_PATTERN.matcher("").matches());
	}
	
	@Test
	public void testIntegerPattern()
	{
		assertTrue(Rules.INTEGER_PATTERN.matcher("1").matches());
		assertTrue(Rules.INTEGER_PATTERN.matcher("12").matches());
		assertTrue(Rules.INTEGER_PATTERN.matcher("123").matches());
		
		assertFalse(Rules.INTEGER_PATTERN.matcher("1,").matches());
		assertFalse(Rules.INTEGER_PATTERN.matcher("1,12").matches());
		assertFalse(Rules.INTEGER_PATTERN.matcher("1.").matches());
		assertFalse(Rules.INTEGER_PATTERN.matcher("1.12").matches());
		assertFalse(Rules.INTEGER_PATTERN.matcher(".1").matches());
	}

	@Test
	public void testRealPattern()
	{
		assertTrue(Rules.REAL_PATTERN.matcher("1.1").matches());
		assertTrue(Rules.REAL_PATTERN.matcher("01.102").matches());
		
		assertFalse(Rules.REAL_PATTERN.matcher("1").matches());
		assertFalse(Rules.REAL_PATTERN.matcher("12").matches());
		assertFalse(Rules.REAL_PATTERN.matcher("123").matches());
		assertFalse(Rules.REAL_PATTERN.matcher("1,").matches());
		assertFalse(Rules.REAL_PATTERN.matcher("1,12").matches());
		assertFalse(Rules.REAL_PATTERN.matcher("1.").matches());
		assertFalse(Rules.REAL_PATTERN.matcher(".1").matches());
	}
	
	@Test
	public void testComplexPattern()
	{
		assertTrue(Rules.COMPLEX_PATTERN.matcher("1i+1").matches());
		assertTrue(Rules.COMPLEX_PATTERN.matcher("01i+102").matches());
		
		assertFalse(Rules.COMPLEX_PATTERN.matcher("1").matches());
		assertFalse(Rules.COMPLEX_PATTERN.matcher("12").matches());
		assertFalse(Rules.COMPLEX_PATTERN.matcher("1i23").matches());
		assertFalse(Rules.COMPLEX_PATTERN.matcher("1+").matches());
		assertFalse(Rules.COMPLEX_PATTERN.matcher("1i12").matches());
		assertFalse(Rules.COMPLEX_PATTERN.matcher("i-1").matches());
		assertFalse(Rules.COMPLEX_PATTERN.matcher("-1").matches());
	}
	
	@Test
	public void testGeneralPattern()
	{
		Matcher a = Rules.GENERAL_PATTERN.matcher("12i+34");
		
		assertTrue(a.find());
		assertEquals("12i+34", a.group());
		assertFalse(a.find());
		
		Matcher b = Rules.GENERAL_PATTERN.matcher("12.34");
		
		assertTrue(b.find());
		assertEquals("12.34", b.group());
		assertFalse(b.find());
		
		Matcher c = Rules.GENERAL_PATTERN.matcher("12.i+34");
		
		assertTrue(c.find());
		assertEquals("12", c.group());
		assertTrue(c.find());
		assertEquals(".", c.group());
		assertTrue(c.find());
		assertEquals("i", c.group());
		assertTrue(c.find());
		assertEquals("+", c.group());
		assertTrue(c.find());
		assertEquals("34", c.group());
		assertFalse(c.find());
		
		Matcher d = Rules.GENERAL_PATTERN.matcher("1=2<=i//4");
		
		assertTrue(d.find());
		assertEquals("1", d.group());
		assertTrue(d.find());
		assertEquals("=", d.group());
		assertTrue(d.find());
		assertEquals("2", d.group());
		assertTrue(d.find());
		assertEquals("<=", d.group());
		assertTrue(d.find());
		assertEquals("i", d.group());
		assertTrue(d.find());
		assertEquals("//", d.group());
		assertTrue(d.find());
		assertEquals("4", d.group());
		assertFalse(d.find());
	}
	
}
