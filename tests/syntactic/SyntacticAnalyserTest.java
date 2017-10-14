package syntactic;
import static org.junit.Assert.*;

import org.junit.Test;

import lexical.LexicalAnalyser;
import syntactic.SyntacticAnalyser;
import syntactic.SyntacticException;

public class SyntacticAnalyserTest
{
	private static void testCode(String code) {
		new SyntacticAnalyser(new LexicalAnalyser().processCode(code).done()).analyse();
	}
	
	private static void failCode(String code) {
		try {
			testCode(code);
			fail("Should have throw an exception");
		} catch (SyntacticException e) { }
	}
	
	@Test
	public void testProgramDeclaration()
	{
		testCode("program id; begin end.");
		
		failCode("program id; begin end;.");
		failCode("progra id; begin end.");
		failCode("program begin end.");
		failCode("program id begin end.");
		failCode("program ; begin end.");
	}
	
	@Test
	public void testVariableDeclaration()
	{
		testCode("program id; var id: integer; begin end.");
		testCode("program id; var id: integer; id: real; id: boolean; begin end.");
		testCode("program id; var id, id2: integer; begin end.");
		testCode("program id; var id, id2: integer; id: real; id: boolean; begin end.");
		testCode("program id; var id, id2: integer; id, id2: real; begin end.");
		testCode("program id; var id, id2: integer; id, id2: real; id: boolean; begin end.");
		
		failCode("program id; var begin end.");
		failCode("program id; var id begin end.");
		failCode("program id; var : begin end.");
		failCode("program id; var id: begin end.");
		failCode("program id; var id ; begin end.");
		failCode("program id; var id: ; begin end.");
		failCode("program id; var id: anything begin end.");
		failCode("program id; var id: integer; id begin end.");
		failCode("program id; var id: integer; id: begin end.");
		failCode("program id; var id: integer; id ; begin end.");
		failCode("program id; var id: integer; id: anything begin end.");
	}
	
	@Test
	public void testProcedureDeclaration()
	{
		testCode("program id; procedure proc; begin end; begin end.");
		testCode("program id; procedure proc (id: integer); begin end; begin end.");
		testCode("program id; procedure proc (id, id2: integer); begin end; begin end.");
		testCode("program id; procedure proc (id: integer; id: real); begin end; begin end.");
		testCode("program id; procedure proc (id: integer; id: real); var id: integer; begin end; begin end.");
		testCode("program id; procedure proc (id: integer; id: real); var id: integer; procedure proc (id: integer); var id: integer; begin end; begin end; begin end.");
		testCode("program id; procedure proc (id: integer); var id2: integer; begin id3 := 1 end; procedure proc2 (id: integer); var id4: integer; begin id5 := 1 end; begin end.");
		
		failCode("program id; procedure proc; begin end.");
		failCode("program id; procedure proc; begin begin end.");
		failCode("program id; procedure proc; anything end begin end.");
		failCode("program id; procedure proc; begin end begin end.");
		failCode("program id; procedure proc (id, id2: integer, real); begin end; begin end.");
		failCode("program id; procedure proc (id: integer; id: real); var id: integer; procedure proc (id: integer); var id: integer; begin end; begin end.");
	}

	@Test
	public void testCommand()
	{
		testCode("program id; begin id := id end.");
		testCode("program id; begin id := 1 end.");
		testCode("program id; begin id := true end.");
		testCode("program id; begin id := not true end.");
		testCode("program id; begin id := proc(id) end.");
		testCode("program id; begin id := id + 1 end.");
		testCode("program id; begin id := id + (1 + 1) end.");
		testCode("program id; begin id := id + (1 + 1) end.");
		testCode("program id; begin id := not (1 + 1) end.");
		testCode("program id; begin id := id + ((id + 1) + 1) end.");
		testCode("program id; begin id := 1; id := 1 end.");
		testCode("program id; begin id := 1; id := id + ((id + 1) + 1) end.");
		testCode("program id; begin id := id + 1 + 1 end.");
		
		testCode("program id; procedure proc; begin id := id end; begin end.");
		testCode("program id; procedure proc; begin id := 1 end; begin end.");
		testCode("program id; procedure proc; begin id := true end; begin end.");
		testCode("program id; procedure proc; begin id := not true end; begin end.");
		testCode("program id; procedure proc; begin id := proc(id) end; begin end.");
		testCode("program id; procedure proc; begin id := id + 1 end; begin end.");
		testCode("program id; procedure proc; begin id := id + (1 + 1) end; begin end.");
		testCode("program id; procedure proc; begin id := id + (1 + 1) end; begin end.");
		testCode("program id; procedure proc; begin id := not (1 + 1) end; begin end.");
		testCode("program id; procedure proc; begin id := id + ((id + 1) + 1) end; begin end.");
		testCode("program id; procedure proc; begin id := 1; id := 1 end; begin end.");
		testCode("program id; procedure proc; begin id := 1; id := id + ((id + 1) + 1) end; begin end.");
		
		failCode("program id; procedure proc; begin id := proc( end; begin end.");
		failCode("program id; procedure proc; begin id := proc(id)) end; begin end.");
		failCode("program id; procedure proc; begin id := (; begin end.");
		failCode("program id; procedure proc; begin id := proc); begin end.");
		failCode("program id; procedure proc; begin id := (proc); begin end.");
	}
	
	@Test
	public void testControlStatements()
	{
		testCode("program id; begin if id > 1 then id := 1 end.");
		testCode("program id; begin if id > 1 then id := 1 else id := 1 end.");
		testCode("program id; begin while id > 1 do id := 1 end.");
		testCode("program id; begin do id := 1 while id > 1 end.");
		
		testCode("program id; procedure proc; begin if id > 1 then id := 1 end; begin end.");
		testCode("program id; procedure proc; begin if id > 1 then id := 1 else id := 1 end; begin end.");
		testCode("program id; procedure proc; begin while id > 1 do id := 1 end; begin end.");

		failCode("program id; procedure proc; begin if id > 1 id := 1 end; begin end.");
		failCode("program id; procedure proc; begin if id > 1 else id := 1 end; begin end.");
		failCode("program id; procedure proc; begin while id > 1 id := 1 end; begin end.");
		failCode("program id; procedure proc; begin id > 1 do id := 1 end; begin end.");
	}
	
	@Test
	public void fullTest()
	{
		testCode("program teste;\n" + 
				"var\n" + 
				"	valor1: integer;\n" + 
				"	valor2: real;\n" + 
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
				"	\n" + 
				"	valor1 := 10;\n" + 
				"	valor2 := valor1 + 5;\n" + 
				"	valor := 1 + 5;\n" + 
				"	NUMERO := 3 + 5 + 7 - 9;\n" + 
				"\n" + 
				"	if 3 > 4 then\n" + 
				"	begin\n" + 
				"	valor := 30;\n" + 
				"	end;\n" + 
				"\n" + 
				"	if 3 > 4 then\n" + 
				"	begin\n" + 
				"		valor := 30;\n" + 
				"	end\n" + 
				"	else\n" + 
				"	begin\n" + 
				"		valor2 := valor2+1;\n" + 
				"	end;\n" + 
				"\n" + 
				"	while 3 > 2 do\n" + 
				"	begin\n" + 
				"		valor3 := 4 + 3;\n" + 
				"	end;\n" + 
				"end.\n" + 
				"\n");
		
		testCode("program Test3; {programa para teste do lexico}\n" + 
				"var\n" + 
				"   NUMERO, n2  : integer;\n" + 
				"   final   : integer;\n" + 
				"begin  {tente gerar um erro usando um caracter nao permitido.. tipo &}\n" + 
				"   NUMERO := 3 / 5 + 7 - 9;\n" + 
				"   if (NUMERO >= 20) or (NUMERO <= 90) then\n" + 
				"   begin\n" + 
				"      NUMERO := -10 / (-3);\n" + 
				"    end;\n" + 
				"   final := NUMERO + 1;\n" + 
				"end.");
		
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
				"\n" + 
				"	do NUMERO := -10 / (-3)\n" + 
				"	while (NUMERO >= 20);\n" + 
				"\n" + 
				"	final := NUMERO + 1;\n" + 
				"end.");
	}
}
