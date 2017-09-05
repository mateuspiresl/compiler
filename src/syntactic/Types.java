package syntactic;

import java.util.HashSet;
import java.util.Set;

public class Types
{
	public static final String PROGRAM = "program";
	public static final String PROCEDURE = "procedure";
	public static final String INSTRUCTION_END = ";";
	public static final String VAR = "var";
	public static final String PROCEDURE_OPEN = "(";
	
	public static final Set<String> TYPES = new HashSet<String>();
	
	// Initialization
	static {
		TYPES.add("integer");
		TYPES.add("real");
		TYPES.add("boolean");
	}
}
