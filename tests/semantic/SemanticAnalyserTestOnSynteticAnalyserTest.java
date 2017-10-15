package semantic;
import static org.junit.Assert.fail;

import org.junit.Test;

import lexical.LexicalAnalyser;
import syntactic.SyntacticAnalyser;
import syntactic.SyntacticException;
import utils.Log;

public class SemanticAnalyserTestOnSynteticAnalyserTest
{
	private static void testCode(String code) {
		new SyntacticAnalyser(new LexicalAnalyser().processCode(code).done(), new SemanticAnalyser()).analyse();
	}
	
	private static void failCode(String code) {
		try {
			testCode(code);
			fail("Should have throw an exception");
		} catch (SemanticException | SyntacticException e) {
			Log.d(0, "Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testProgramDeclaration()
	{
		testCode("program prog; begin end.");
		
		failCode("program prog; begin end;.");
		failCode("progra id; begin end.");
		failCode("program begin end.");
		failCode("program id begin end.");
		failCode("program ; begin end.");
	}
	
	@Test
	public void testVariableDeclaration()
	{
		testCode("program prog; var id: integer; begin end.");
		testCode("program prog; var id: integer; id2: real; id3: boolean; begin end.");
		testCode("program prog; var id, id2: integer; begin end.");
		testCode("program prog; var id, id2: integer; id3: real; id4: boolean; begin end.");
		testCode("program prog; var id, id2: integer; id3, id4: real; begin end.");
		testCode("program prog; var id, id2: integer; id3, id4: real; id5: boolean; begin end.");
		
		failCode("program prog; var begin end.");
		failCode("program prog; var id begin end.");
		failCode("program prog; var : begin end.");
		failCode("program prog; var id: begin end.");
		failCode("program prog; var id ; begin end.");
		failCode("program prog; var id: ; begin end.");
		failCode("program prog; var id: anything begin end.");
		failCode("program prog; var id: integer; id2 begin end.");
		failCode("program prog; var id: integer; id2: begin end.");
		failCode("program prog; var id: integer; id2 ; begin end.");
		failCode("program prog; var id: integer; id2: anything begin end.");
	}
	
	@Test
	public void testVariables()
	{
		testCode("program prog; var id: integer; begin id := 1 end.");
		testCode("program prog; var id: integer; id2: real; id3: boolean; begin id := 1; id2 := id; id3 := id2 = id; end.");
		testCode("program prog; var id: integer; procedure proc; var id2: real; begin id2 := 1.0; id := 1; end; begin id := 1; end.");
		
		failCode("program prog; var id: integer; procedure proc; begin end; begin id := proc; end.");
		failCode("program prog; var id: integer; begin id := real; end.");
	}
	
	@Test
	public void testProcedureDeclaration()
	{
		testCode("program prog; procedure proc; begin end; begin end.");
		testCode("program prog; procedure proc (id: integer); begin end; begin end.");
		testCode("program prog; procedure proc (id, id2: integer); begin end; begin end.");
		testCode("program prog; procedure proc (id: integer; id2: real); begin end; begin end.");
		testCode("program prog; procedure proc (id: integer; id2: real); var id3: integer; begin end; begin end.");
		testCode("program prog; procedure proc (id: integer; id2: real); var id3: integer; procedure proc (id4: integer); var id: integer; begin end; begin end; begin end.");
		testCode("program prog; procedure proc (id: integer); var id2: integer; begin id2 := 1 end; procedure proc2 (id3: integer); var id4: integer; begin id4 := 1 end; begin end.");
		
		failCode("program prog; procedure proc; begin end.");
		failCode("program prog; procedure proc; begin begin end.");
		failCode("program prog; procedure proc; anything end begin end.");
		failCode("program prog; procedure proc; begin end begin end.");
		failCode("program prog; procedure proc (id, id2: integer, real); begin end; begin end.");
		failCode("program prog; procedure proc (id: integer; id2: real); var id3: integer; procedure proc (id4: integer); var id5: integer; begin end; begin end.");
	}
	
	@Test
	public void testProcedures()
	{
		testCode("program prog; procedure proc; begin end; begin proc; end.");
		testCode("program prog; procedure proc; begin proc; end; begin proc; end.");
		
		failCode("program prog; procedure proc; begin end; begin proc0; end.");
		failCode("program prog; procedure proc; begin proc0; end; begin proc0; end.");
	}

	@Test
	public void testCommand()
	{
		testCode("program prog; var id: integer; begin id := id end.");
		testCode("program prog; var id: integer; begin id := 1 end.");
		testCode("program prog; var id: boolean; begin id := true end.");
		testCode("program prog; var id: boolean; begin id := not true end.");
		testCode("program prog; var id: integer; begin id := id + 1 end.");
		testCode("program prog; var id: integer; begin id := id + (1 + 1) end.");
		testCode("program prog; var id: integer; begin id := id + (1 + 1) end.");
		testCode("program prog; var id: integer; begin id := not (1 + 1) end.");
		testCode("program prog; var id: integer; begin id := id + ((id + 1) + 1) end.");
		testCode("program prog; var id: integer; begin id := 1; id := 1 end.");
		testCode("program prog; var id: integer; begin id := 1; id := id + ((id + 1) + 1) end.");
		testCode("program prog; var id: integer; begin id := id + 1 + 1 end.");
		
		testCode("program prog; var id: integer; procedure proc; begin id := id end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin id := 1 end; begin end.");
		testCode("program prog; var id: boolean; procedure proc; begin id := true end; begin end.");
		testCode("program prog; var id: boolean; procedure proc; begin id := not true end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin id := id + 1 end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin id := id + (1 + 1) end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin id := id + (1 + 1) end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin id := not (1 + 1) end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin id := id + ((id + 1) + 1) end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin id := 1; id := 1 end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin id := 1; id := id + ((id + 1) + 1) end; begin end.");
		
		failCode("program prog; var id: integer; procedure proc; begin id := proc( end; begin end.");
		failCode("program prog; var id: integer; procedure proc; begin id := proc(id)) end; begin end.");
		failCode("program prog; var id: integer; procedure proc; begin id := (; begin end.");
		failCode("program prog; var id: integer; procedure proc; begin id := proc); begin end.");
		failCode("program prog; var id: integer; procedure proc; begin id := (proc); begin end.");
	}
	
	@Test
	public void testControlStatements()
	{
		testCode("program prog; var id: integer; begin if id > 1 then id := 1 end.");
		testCode("program prog; var id: integer; begin if id > 1 then id := 1 else id := 1 end.");
		testCode("program prog; var id: integer; begin while id > 1 do id := 1 end.");
		
		testCode("program prog; var id: integer; procedure proc; begin if id > 1 then id := 1 end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin if id > 1 then id := 1 else id := 1 end; begin end.");
		testCode("program prog; var id: integer; procedure proc; begin while id > 1 do id := 1 end; begin end.");

		failCode("program prog; var id: integer; procedure proc; begin if id > 1 id := 1 end; begin end.");
		failCode("program prog; var id: integer; procedure proc; begin if id > 1 else id := 1 end; begin end.");
		failCode("program prog; var id: integer; procedure proc; begin while id > 1 id := 1 end; begin end.");
		failCode("program prog; var id: integer; procedure proc; begin id > 1 do id := 1 end; begin end.");
	}
	
	@Test
	public void fullTest1()
	{	
		testCode("program teste; {programa exemplo}\n" + 
				"var\n" + 
				"	valor1: integer;\n" + 
				"	valor2: real;\n" + 
				"	valor: integer;\n" + 
				"	NUMERO: integer;\n" + 
				"	valor3: integer;\n" + 
				"\n" + 
				"procedure calcula_percentual (n1:real; per1:real); \n" + 
				"begin \n" + 
				"    per1 := n1 * (21/100); \n" + 
				"end; \n" + 
				"\n" + 
				"procedure calcula_2 (n1:integer; per1:real); \n" + 
				"begin \n" + 
				"    per1 := n1 * (54*33); \n" + 
				"end; \n" + 
				"\n" + 
				"begin\n" +
				"   calcula_percentual;\n" +
				"	valor1 := 10;\n" + 
				"	valor2 := valor1 + 5;\n" + 
				"	NUMERO := 3 + 5 + 7 - 9;\n" + 
				"\n" + 
				"	if 3 > 4 then\n" + 
				"	begin\n" + 
				"		valor := 30;\n" + 
				"	end;\n" + 
				"\n" + 
				"	if 3 > 4\n" + 
				"	then begin\n" + 
				"		valor := 30;\n" + 
				"	end\n" + 
				"	else begin\n" + 
				"		valor2 := valor2+1;\n" + 
				"	end;\n" + 
				"\n" + 
				"	while 3 > 2 do\n" + 
				"	begin\n" + 
				"		valor3 := 4 + 3;\n" + 
				"	end;\n" + 
				"end.\n" + 
				"\n");
	}
	
	@Test
	public void fullTest2()
	{
		testCode("program teste; {programa exemplo}\n" + 
				"var\n" + 
				"	valor1: integer;\n" + 
				"  valor2: real;\n" + 
				"\n" + 
				"begin\n" + 
				"	valor1 := 1;\n" + 
				"  valor2 := 2.0;\n" + 
				"  valor2 := valor1 + 1;\n" + 
				"end.");
	}

	@Test
	public void fullTest3()
	{
		testCode("program Test2;\n" + 
				"\n" + 
				"var\n" + 
				"	NUMERO, n2: integer;\n" + 
				"	final: integer;\n" + 
				"\n" + 
				"begin\n" + 
				"	NUMERO := 3 / 5 + 7 - 9;\n" + 
				"	\n" + 
				"	if (NUMERO >= 20) or (NUMERO <= 90) then\n" + 
				"	begin\n" + 
				"		NUMERO := -10 / (-3);\n" + 
				"	end;\n" + 
				"	\n" + 
				"	final := NUMERO + 1;\n" + 
				"end.");
	}
}
