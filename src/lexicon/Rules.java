package lexicon;

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
public abstract class Rules {

	public static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z][\\w\\d]*$");
	public static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");
	public static final Pattern REAL_PATTERN = Pattern.compile("^\\d+\\.\\d+$");
	public static final Pattern SEPARATORS_PATTERN;
	
	public static final String ASSIGNMENT_COMMAND = ":=";
	public static final String COMMENT_OPEN = "{";
	public static final String COMMENT_CLOSE = "}";
	
	public static final Set<String> KEY_WORDS;
	public static final Set<String> DELIMITERS = new HashSet<String>();
	public static final Set<String> OPERATORS_RELATIONAL = new HashSet<String>();
	public static final Set<String> OPERATORS_ADDITIVE = new HashSet<String>();
	public static final Set<String> OPERATORS_MULTIPLICATIVE = new HashSet<String>();
	
	static {
		KEY_WORDS = new HashSet<String>() {{
			add("program");	add("var");		add("integer");
			add("real");	add("boolean");	add("procedure");
			add("begin");	add("end");		add("if");
			add("then");	add("else");	add("while");
			add("do");		add("not");
		}};
		
		List<String> DELIMITERS_LIST = Arrays.asList(new String[] { ";", ".", ":", ",", "(", ")" });
		List<String> OPERATORS_RELATIONAL_LIST = Arrays.asList(new String[] { "<=", ">=", "<>", "=", "<", ">" });
		List<String> OPERATORS_ADDITIVE_LIST = Arrays.asList(new String[] { "+", "-" });
		List<String> OPERATORS_MULTIPLICATIVE_LIST = Arrays.asList(new String[] { "*", "/" });
		
		DELIMITERS.addAll(DELIMITERS_LIST);
		OPERATORS_RELATIONAL.addAll(OPERATORS_RELATIONAL_LIST);
		OPERATORS_ADDITIVE.addAll(OPERATORS_ADDITIVE_LIST);
		OPERATORS_MULTIPLICATIVE.addAll(OPERATORS_MULTIPLICATIVE_LIST);
		
		OPERATORS_ADDITIVE.add("or");
		OPERATORS_MULTIPLICATIVE.add("and");
		
		SEPARATORS_PATTERN = Pattern.compile("\\" + String.join("|\\", new ArrayList<String>() {{
			add(ASSIGNMENT_COMMAND);
			add(COMMENT_OPEN);
			add(COMMENT_CLOSE);
			addAll(DELIMITERS_LIST);
			addAll(OPERATORS_RELATIONAL_LIST);
			addAll(OPERATORS_ADDITIVE_LIST);
			addAll(OPERATORS_MULTIPLICATIVE_LIST);
		}}.toArray(new String[0])));
	}
	
}
