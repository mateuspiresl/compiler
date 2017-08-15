package lexicon;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class RulesTest {

	private static Object getField(String name)
	{
		try {
			Field field = Rules.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(null);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private int find(Pattern pattern, String sequence)
	{
		try {
			Matcher matcher = pattern.matcher(sequence);
			matcher.find();
			return matcher.start();
		}
		catch (IllegalStateException ise) {
			return -1;
		}
	}
	
	@Test
	public void testIndentifierPattern()
	{
		Pattern pattern = (Pattern) getField("IDENTIFIER_PATTERN");
		
		assertEquals(true, pattern.matcher("asdfgh").matches());
		assertEquals(true, pattern.matcher("asdfgh_").matches());
		assertEquals(true, pattern.matcher("asdfgh123213").matches());
		assertEquals(true, pattern.matcher("asdfgh_123213").matches());
		
		assertEquals(false, pattern.matcher("1asdfgh").matches());
		assertEquals(false, pattern.matcher("122133").matches());
		assertEquals(false, pattern.matcher("____").matches());
		assertEquals(false, pattern.matcher("1asdfgh").matches());
		assertEquals(false, pattern.matcher("1342__asdfgh").matches());
		assertEquals(false, pattern.matcher("_asdfgh").matches());
		assertEquals(false, pattern.matcher("asdfgh-").matches());
		assertEquals(false, pattern.matcher("asdfgh!").matches());
		assertEquals(false, pattern.matcher("asdfgh#").matches());
		assertEquals(false, pattern.matcher("asdfgh$").matches());
		assertEquals(false, pattern.matcher("asdfgh&").matches());
		assertEquals(false, pattern.matcher("asdfgh.").matches());
		assertEquals(false, pattern.matcher("asdfgh,").matches());
		assertEquals(false, pattern.matcher("asdfgh;").matches());
		assertEquals(false, pattern.matcher("asdfgh?").matches());
		assertEquals(false, pattern.matcher("asdfgh^").matches());
		assertEquals(false, pattern.matcher("").matches());
	}
	
	@Test
	public void testIntegerPattern()
	{
		Pattern pattern = (Pattern) getField("INTEGER_PATTERN");
		
		assertEquals(true, pattern.matcher("1").matches());
		assertEquals(true, pattern.matcher("12").matches());
		assertEquals(true, pattern.matcher("123").matches());
		
		assertEquals(false, pattern.matcher("1,").matches());
		assertEquals(false, pattern.matcher("1,12").matches());
		assertEquals(false, pattern.matcher("1.").matches());
		assertEquals(false, pattern.matcher("1.12").matches());
		assertEquals(false, pattern.matcher(".1").matches());
	}

	@Test
	public void testRealPattern()
	{
		Pattern pattern = (Pattern) getField("REAL_PATTERN");
		
		assertEquals(true, pattern.matcher("1.1").matches());
		assertEquals(true, pattern.matcher("01.102").matches());
		
		assertEquals(false, pattern.matcher("1").matches());
		assertEquals(false, pattern.matcher("12").matches());
		assertEquals(false, pattern.matcher("123").matches());
		assertEquals(false, pattern.matcher("1,").matches());
		assertEquals(false, pattern.matcher("1,12").matches());
		assertEquals(false, pattern.matcher("1.").matches());
		assertEquals(false, pattern.matcher(".1").matches());
	}
	
	@Test
	public void testSeparatorsPattern()
	{
		Pattern pattern = (Pattern) getField("SEPARATORS_PATTERN");
	
		assertEquals(0, find(pattern, ".aa"));
		assertEquals(1, find(pattern, "a,a"));
		assertEquals(2, find(pattern, "aa;"));
		assertEquals(1, find(pattern, "a:a;"));
		assertEquals(1, find(pattern, "a(a;"));
		assertEquals(1, find(pattern, "a)a;"));
		
		assertEquals(-1, find(pattern, "aaa"));
		assertEquals(-1, find(pattern, "102"));
		assertEquals(-1, find(pattern, "a_aa"));
	}
	
}
