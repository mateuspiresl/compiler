import static org.junit.Assert.*;

import org.junit.Test;

import lexicon.LexiconAnalyser;
import syntactic.SyntacticAnalyser;
import syntactic.SyntacticException;

public class SyntacticAnalyserTest {

	private static void testCode(String code) {
		new SyntacticAnalyser(new LexiconAnalyser().processCode(code).done()).analyse();
	}
	
	private static void failProgram(String code) {
		try {
			testCode(code);
			fail("Should have throw an exception");
		} catch (SyntacticException e) { }
	}
	
	@Test
	public void testProgramDeclaration()
	{
		testCode("program id;");
		
		failProgram("progra id;");
		failProgram("program");
		failProgram("program id");
		failProgram("program ;");
	}
	
	@Test
	public void testVariableDeclaration()
	{
		testCode("program id; var id: integer;");
		testCode("program id; var id: integer; id: real; id: boolean;");
		testCode("program id; var id, id2: integer;");
		testCode("program id; var id, id2: integer; id: real; id: boolean;");
		testCode("program id; var id, id2: integer; id, id2: real;");
		testCode("program id; var id, id2: integer; id, id2: real; id: boolean;");
		
		failProgram("program id; var");
		failProgram("program id; var id");
		failProgram("program id; var :");
		failProgram("program id; var id:");
		failProgram("program id; var id ;");
		failProgram("program id; var id: ;");
		failProgram("program id; var id: anything");
		failProgram("program id; var id: integer; id");
		failProgram("program id; var id: integer; id:");
		failProgram("program id; var id: integer; id ;");
		failProgram("program id; var id: integer; id: anything");
	}
	
	@Test
	public void testProcedureDeclaration()
	{
		testCode("program id; procedure proc;");
		testCode("program id; procedure proc (id: integer);");
		testCode("program id; procedure proc (id, id2: integer);");
		testCode("program id; procedure proc (id: integer; id: real);");
		testCode("program id; procedure proc (id: integer; id: real); var id: integer;");
		testCode("program id; procedure proc (id: integer; id: real); var id: integer; procedure proc (id: integer); var id: integer;");
	}

}
