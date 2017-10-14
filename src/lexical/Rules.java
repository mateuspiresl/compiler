package lexical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents all lexicographic processing rules.
 */
@SuppressWarnings({ "serial" })
public abstract class Rules
{
	public static final Pattern IDENTIFIER_PATTERN;
	public static final Pattern INTEGER_PATTERN;
	public static final Pattern REAL_PATTERN;
	public static final Pattern COMPLEX_PATTERN;
	public static final Pattern GENERAL_PATTERN;
	
	public static final String ASSIGNMENT_COMMAND = ":=";
	public static final String COMMENT_OPEN = "{";
	public static final String COMMENT_CLOSE = "}";
	public static final String COMMENT_INLINE = "//";
	
	public static final Set<String> KEY_WORDS = new HashSet<String>();
	public static final Set<String> BOOLEAN_VALUES = new HashSet<String>();
	public static final Set<String> DELIMITERS = new HashSet<String>();
	public static final Set<String> OPERATORS_RELATIONAL = new HashSet<String>();
	public static final Set<String> OPERATORS_ADDITIVE = new HashSet<String>();
	public static final Set<String> OPERATORS_MULTIPLICATIVE = new HashSet<String>();
	public static final Set<String> OPERATORS_LOGICAL = new HashSet<String>();
	
	static {
		String identifierPattern = "[a-zA-Z][\\w\\d]*";
		String integerPattern = "\\d+";
		String realPattern = "\\d+\\.\\d+";
		String complexPattern = "\\d+i[-+]\\d+";
		
		IDENTIFIER_PATTERN = Pattern.compile(wrapPattern(identifierPattern));
		INTEGER_PATTERN = Pattern.compile(wrapPattern(integerPattern));
		REAL_PATTERN = Pattern.compile(wrapPattern(realPattern));
		COMPLEX_PATTERN = Pattern.compile(wrapPattern(complexPattern));
		
		List<String> booleanValues = Arrays.asList(new String[] { "true", "false" });
		List<String> keyWords = Arrays.asList(new String[] { "program", "var", "integer", "real", "boolean",
				"procedure", "begin", "end", "if", "then", "else", "while", "do", "not" });
		List<String> delimiters = Arrays.asList(new String[] { ";", ".", ":", ",", "(", ")" });
		List<String> relationalOperators = Arrays.asList(new String[] { "<=", ">=", "<>", "=", "<", ">" });
		List<String> additiveOperators = Arrays.asList(new String[] { "+", "-" });
		List<String> multiplicativeOperators = Arrays.asList(new String[] { "*", "/" });
		List<String> logicalOperators = Arrays.asList(new String[] { "and", "or" });
		
		BOOLEAN_VALUES.addAll(booleanValues);
		KEY_WORDS.addAll(keyWords);
		DELIMITERS.addAll(delimiters);
		OPERATORS_RELATIONAL.addAll(relationalOperators);
		OPERATORS_ADDITIVE.addAll(additiveOperators);
		OPERATORS_MULTIPLICATIVE.addAll(multiplicativeOperators);
		OPERATORS_LOGICAL.addAll(logicalOperators);
		
//		OPERATORS_ADDITIVE.add("or");
//		OPERATORS_MULTIPLICATIVE.add("and");
		
		GENERAL_PATTERN = Pattern.compile(String.join("|", new ArrayList<String>() {{
			add(regexScape(COMMENT_OPEN));
			add(regexScape(COMMENT_CLOSE));
			add(regexScape(COMMENT_INLINE));
			add(regexScape(ASSIGNMENT_COMMAND));
			add(patternFromList(delimiters));
			add(patternFromList(relationalOperators));
			add(patternFromList(additiveOperators));
			add(patternFromList(multiplicativeOperators));
			add("and|or");
			add(complexPattern);
			add(realPattern);
			add(integerPattern);
			add(identifierPattern);
		}}.toArray(new String[0])));
	}
	
	private static String patternFromList(List<String> list) {
		return "\\" + String.join("|\\", list);
	}
	
	private static String wrapPattern(String pattern) {
		return "^" + pattern + "$";
	}
	
	private static String regexScape(String pattern) {
		return "\\" + String.join("\\", pattern.split(""));
	}
}
