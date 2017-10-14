package semantic;

import static org.junit.Assert.fail;

import org.junit.Test;

import lexical.LexicalAnalyser;
import lexical.Symbol;
import lexical.TokenType;
import syntactic.SyntacticAnalyser;
import syntactic.SyntacticException;
import utils.Log;

public class SemanticAnalyserTest
{
	private int i = 0, l = 0;
	private Symbol s = new Symbol("s", TokenType.Complex, -1);
	
	@Test
	public void test()
	{
//		testCode("program id; begin end.");
//		testCode("program pr; var v1: integer; v2: real; v3: boolean; begin end.");
//		
//		failCode("program pr; var v1: integer; v1: real; begin end.");
//		failCode("program pr; var id: integer; procedure p1; var in: real; begin in := 1; end; begin in := 1; end.");
//		failCode("progra id; begin end.");
//		failCode("program begin end.");
//		failCode("program id begin end.");
//		failCode("program ; begin end.");
	}
	
	@Test
	public void testScope()
	{
		SemanticAnalyser analyser = new SemanticAnalyser();
		
		analyser.onBlockBegin(i++, s);
		analyser.onBlockEnd(i++, s);
		
		try { analyser.onBlockEnd(i++, s); fail(); }
		catch (SemanticException e) { }
		
		analyser.onBlockBegin(i++, s);
		analyser.onBlockBegin(i++, s);
		analyser.onBlockBegin(i++, s);
		analyser.onBlockEnd(i++, s);
		analyser.onBlockEnd(i++, s);
		analyser.onBlockEnd(i++, s);
		
		try { analyser.onBlockEnd(i++, s); fail(); }
		catch (SemanticException e) { }
	}
	
	@Test
	public void testBlock()
	{
		SemanticAnalyser analyser = new SemanticAnalyser();
		
		analyser.onScopeBegin(i++, l++);
		analyser.onScopeEnd(i++, l++);
		
		try { analyser.onScopeEnd(i++, l++); fail(); }
		catch (SemanticException e) { }
		
		analyser.onScopeBegin(i++, l++);
		analyser.onScopeBegin(i++, l++);
		analyser.onScopeBegin(i++, l++);
		analyser.onScopeEnd(i++, l++);
		analyser.onScopeEnd(i++, l++);
		analyser.onScopeEnd(i++, l++);
		
		try { analyser.onScopeEnd(i++, l++); fail(); }
		catch (SemanticException e) { }
	}
	
	@Test
	public void testIndexMatching()
	{
		SemanticAnalyser analyser = new SemanticAnalyser();
		
		analyser.onScopeBegin(i++, l++);
		analyser.matchIndex(i - 1);
		
		try { analyser.onScopeEnd(i++, l++); fail(); }
		catch (SemanticException e) { }
		
		analyser.onScopeBegin(i, l++);
		analyser.matchIndex(i);
	
		try { analyser.onScopeEnd(i++, l++); fail(); }
		catch (SemanticException e) { }
	}
	
	@Test
	public void testAutoIndexMatching()
	{
		SemanticAnalyser analyser = new SemanticAnalyser();
		
		analyser.onScopeBegin(i++, l++);
		analyser.onScopeBegin(i, l++);
		
		analyser.onScopeEnd(i, l++);
		analyser.onScopeEnd(i, l++);
		
		try { analyser.onScopeEnd(i + 1, l++); fail(); }
		catch (SemanticException e) { }
		
		analyser.onScopeBegin(++i, l++);
		analyser.onScopeBegin(i + 1, l++);
		
		analyser.onScopeEnd(i, l++);
		
		try { analyser.onScopeEnd(i + 1, l++); fail(); }
		catch (SemanticException e) { }
		
		analyser.onBlockBegin(++i, s);
		analyser.onBlockBegin(i + 1, s);
		
		analyser.onBlockEnd(i + 1, s);
		
		try { analyser.onBlockEnd(i + 1, s); fail(); }
		catch (SemanticException e) { }
	}
	
	@Test
	public void testVariables()
	{
		SemanticAnalyser analyser = new SemanticAnalyser();
		
		Symbol var0 = new Symbol("var0", TokenType.Identifier, 0);
		Symbol var0b = new Symbol("var0b", TokenType.Identifier, 0);
		Symbol type0 = new Symbol("integer", TokenType.KeyWord, 0);
		
		Symbol var1 = new Symbol("var1", TokenType.Identifier, 1);
		Symbol var1b = new Symbol("var1b", TokenType.Identifier, 1);
		Symbol type1 = new Symbol("integer", TokenType.KeyWord, 1);
		
		analyser.onScopeBegin(i++, l++);
		{
			try { analyser.onVariable(i++, var0); fail(); }
			catch (SemanticException e) { }
			
			analyser.onVariableDeclaration(i++, var0);
			analyser.onVariableDeclaration(i++, var0b);
			analyser.onTypeDefinition(i++, type0);
			analyser.onBlockBegin(i++, s);
			analyser.onVariable(i++, var0);
			analyser.onVariable(i++, var0b);
			analyser.onBlockEnd(i++, s);
			
			try { analyser.onVariable(i++, var1); fail(); }
			catch (SemanticException e) { }
			
			analyser.onScopeBegin(i++, l++);
			{
				analyser.onVariableDeclaration(i++, var1);
				analyser.onVariableDeclaration(i++, var1b);
				analyser.onTypeDefinition(i++, type1);
				analyser.onBlockBegin(i++, s);
				analyser.onVariable(i++, var0);
				analyser.onVariable(i++, var0b);
				analyser.onVariable(i++, var1);
				analyser.onVariable(i++, var1b);
				analyser.onBlockEnd(i++, s);
				
				try { analyser.onVariable(i++, var1); fail(); }
				catch (SemanticException e) { }
				
				analyser.onBlockBegin(i++, s);
				analyser.onVariable(i++, var0);
				analyser.onVariable(i++, var1);
				analyser.onBlockEnd(i++, s);
				analyser.onScopeEnd(i++, l++);
			}
			
			analyser.onBlockBegin(i++, s);
			analyser.onVariable(i++, var0);
			analyser.onVariable(i++, var0b);
			
			try { analyser.onVariable(i++, var1); fail(); }
			catch (SemanticException e) { }
			
			analyser.onBlockEnd(i++, s);
			analyser.onScopeEnd(i++, l++);
		}
		
		analyser.onBlockBegin(i++, s);
		
		try { analyser.onVariable(i++, var0); fail(); }
		catch (SemanticException e) { }
		
		try { analyser.onVariable(i++, var0b); fail(); }
		catch (SemanticException e) { }
	}
	
	@Test
	public void testExpressions()
	{
		SemanticAnalyser analyser = new SemanticAnalyser();
		
		Symbol integer = new Symbol("integer", TokenType.KeyWord, 0);
		Symbol int0 = new Symbol("int0", TokenType.Identifier, 0);
		Symbol int1 = new Symbol("int1", TokenType.Identifier, 0);
		
		Symbol real = new Symbol("real", TokenType.KeyWord, 2);
		Symbol real0 = new Symbol("real0", TokenType.Identifier, 2);
		Symbol real1 = new Symbol("real1", TokenType.Identifier, 2);
		
		Symbol bool = new Symbol("boolean", TokenType.KeyWord, 3);
		Symbol bool0 = new Symbol("bool0", TokenType.Identifier, 3);
		Symbol bool1 = new Symbol("bool1", TokenType.Identifier, 3);
		
		Symbol intval = new Symbol("1", TokenType.Integer, 4);
		Symbol realval = new Symbol("0.1", TokenType.Real, 4);
		Symbol boolval = new Symbol("true", TokenType.Boolean, 4);
		
		Symbol assign = new Symbol(":=", TokenType.AssignmentCommand, 5);
		Symbol plus = new Symbol("+", TokenType.AdditiveOperator, 5);
		Symbol multi = new Symbol("*", TokenType.MultiplicativeOperator, 5);
		Symbol equal = new Symbol("==", TokenType.RelationalOperator, 5);
		
		analyser.onScopeBegin(i++, l++);
		
		analyser.onVariableDeclaration(i++, int0);
		analyser.onVariableDeclaration(i++, int1);
		analyser.onTypeDefinition(i++, integer);
		
		analyser.onVariableDeclaration(i++, real0);
		analyser.onVariableDeclaration(i++, real1);
		analyser.onTypeDefinition(i++, real);
		
		analyser.onVariableDeclaration(i++, bool0);
		analyser.onVariableDeclaration(i++, bool1);
		analyser.onTypeDefinition(i++, bool);
		
		analyser.onBlockBegin(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, plus);
		analyser.onVariable(i++, int1);
		analyser.onOperator(i++, plus);
		analyser.onValue(i++, intval);
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, plus);
		analyser.onVariable(i++, int1);
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, multi);
		analyser.onVariable(i++, int1);
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, plus);
		analyser.onValue(i++, realval);
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int1);
		analyser.onOperator(i++, multi);
		analyser.onVariable(i++, real1);
		analyser.onOperator(i++, plus);
		analyser.onValue(i++, intval);
		try { analyser.onExpressionEnd(i++, s); fail(); }
		catch (SemanticException e) { }
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, plus);
		analyser.onVariable(i++, real1);
		try { analyser.onExpressionEnd(i++, s); fail(); }
		catch (SemanticException e) { }
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, bool0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, equal);
		analyser.onVariable(i++, int1);
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, bool0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, equal);
		analyser.onVariable(i++, real1);
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, bool0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, equal);
		analyser.onVariable(i++, real1);
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, equal);
		analyser.onVariable(i++, int1);
		try { analyser.onExpressionEnd(i++, s); fail(); }
		catch (SemanticException e) { }
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, bool0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, equal);
		analyser.onVariable(i++, bool1);
		try { analyser.onExpressionEnd(i++, s); fail(); }
		catch (SemanticException e) { }
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, bool0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, bool0);
		analyser.onOperator(i++, multi);
		analyser.onValue(i++, boolval);
		try { analyser.onExpressionEnd(i++, s); fail(); }
		catch (SemanticException e) { }
		analyser.onExpressionEnd(i++, s);
		
		analyser.onBlockEnd(i++, s);
	}
	
	@Test
	public void testComplexExpressions()
	{
		SemanticAnalyser analyser = new SemanticAnalyser();
		
		Symbol integer = new Symbol("integer", TokenType.KeyWord, 0);
		Symbol int0 = new Symbol("int0", TokenType.Identifier, 0);
		Symbol int1 = new Symbol("int1", TokenType.Identifier, 0);
		
		Symbol real = new Symbol("real", TokenType.KeyWord, 2);
		Symbol real0 = new Symbol("real0", TokenType.Identifier, 2);
		Symbol real1 = new Symbol("real1", TokenType.Identifier, 2);
		
		Symbol bool = new Symbol("boolean", TokenType.KeyWord, 3);
		Symbol bool0 = new Symbol("bool0", TokenType.Identifier, 3);
		Symbol bool1 = new Symbol("bool1", TokenType.Identifier, 3);
		
		Symbol assign = new Symbol(":=", TokenType.AssignmentCommand, 5);
		Symbol plus = new Symbol("+", TokenType.AdditiveOperator, 5);
		Symbol multi = new Symbol("*", TokenType.MultiplicativeOperator, 5);
		Symbol equal = new Symbol("==", TokenType.RelationalOperator, 5);
		
		analyser.onScopeBegin(i++, l++);
		
		analyser.onVariableDeclaration(i++, int0);
		analyser.onVariableDeclaration(i++, int1);
		analyser.onTypeDefinition(i++, integer);
		
		analyser.onVariableDeclaration(i++, real0);
		analyser.onVariableDeclaration(i++, real1);
		analyser.onTypeDefinition(i++, real);
		
		analyser.onVariableDeclaration(i++, bool0);
		analyser.onVariableDeclaration(i++, bool1);
		analyser.onTypeDefinition(i++, bool);
		
		analyser.onBlockBegin(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, plus);
		{
			analyser.onExpressionBegin(i++, s);
			analyser.onVariable(i++, int0);
			analyser.onOperator(i++, multi);
			analyser.onVariable(i++, int1);
			analyser.onExpressionEnd(i++, s);
		}
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, plus);
		{
			analyser.onExpressionBegin(i++, s);
			{
				analyser.onExpressionBegin(i++, s);
				analyser.onVariable(i++, int0);
				analyser.onOperator(i++, multi);
				analyser.onVariable(i++, int1);
				analyser.onExpressionEnd(i++, s);
			}
			analyser.onOperator(i++, multi);
			analyser.onVariable(i++, int1);
			analyser.onExpressionEnd(i++, s);
		}
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, multi);
		{
			analyser.onExpressionBegin(i++, s);
			analyser.onVariable(i++, int0);
			analyser.onOperator(i++, multi);
			analyser.onVariable(i++, real1);
			analyser.onExpressionEnd(i++, s);
		}
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, int0);
		analyser.onOperator(i++, multi);
		{
			analyser.onExpressionBegin(i++, s);
			{
				analyser.onExpressionBegin(i++, s);
				analyser.onVariable(i++, int0);
				analyser.onOperator(i++, multi);
				analyser.onVariable(i++, real1);
				analyser.onExpressionEnd(i++, s);
			}
			analyser.onOperator(i++, multi);
			analyser.onVariable(i++, int0);
			analyser.onExpressionEnd(i++, s);
		}
		try { analyser.onExpressionEnd(i++, s); fail(); }
		catch (SemanticException e) { }
		analyser.onExpressionEnd(i++, s);
		
		analyser.onExpressionBegin(i++, s);
		analyser.onVariable(i++, real0);
		analyser.onOperator(i++, assign);
		analyser.onVariable(i++, real1);
		analyser.onOperator(i++, plus);
		{
			analyser.onExpressionBegin(i++, s);
			analyser.onVariable(i++, int0);
			analyser.onOperator(i++, equal);
			analyser.onVariable(i++, real1);
			analyser.onExpressionEnd(i++, s);
		}
		try { analyser.onExpressionEnd(i++, s); fail(); }
		catch (SemanticException e) { }
		analyser.onExpressionEnd(i++, s);
		
		analyser.onBlockEnd(i++, s);
	}
	
	private static void testCode(String code) {
		Log.d(0, "Test: " + code);
		new SyntacticAnalyser(new LexicalAnalyser().processCode(code).done(), new SemanticAnalyser()).analyse();
	}
	
	private static void failCode(String code) {
		try {
			testCode(code);
			fail("Should have throw an exception");
		} catch (SemanticException | SyntacticException e) { }
	}
	
	@Test
	public void testExpressionsOnCode()
	{
		testCode("program prog; var a: integer; begin a := 1; end.");
		testCode("program prog; var a: integer; begin a := ((1)); end.");
		testCode("program prog; var a: real; begin a := 1; end.");
		testCode("program prog; var a: real; begin a := ((1.0)); end.");
		testCode("program prog; var a: boolean; begin a := true; end.");
		
		failCode("program prog; var a: integer; begin a := 1.0; end.");
		failCode("program prog; var a: integer; begin a := true; end.");
		failCode("program prog; var a: boolean; begin a := 1.0; end.");
		failCode("program prog; var a: boolean; begin a := 1; end.");
		
		// Additive operators
		testCode("program prog; var a: integer; begin a := 1 + 1; end.");
		testCode("program prog; var a: integer; begin a := (1 + (1 + 1)) + 1; end.");
		testCode("program prog; var a: real; begin a := 1 + 1; end.");
		testCode("program prog; var a: real; begin a := (1 + (1 + 1)) + 1; end.");
		testCode("program prog; var a: real; begin a := (1 + (1 + 1.0)) + 1; end.");
		
		failCode("program prog; var a: integer; begin a := 1 + 1.0; end.");
		failCode("program prog; var a: integer; begin a := (1 + (1 + 1.0)) + 1.0; end.");
		
		// Multiplicative operators
		testCode("program prog; var a: integer; begin a := 1 * 1; end.");
		testCode("program prog; var a: integer; begin a := (1 + (1 * 1)) * 1; end.");
		testCode("program prog; var a: real; begin a := 1 * 1; end.");
		testCode("program prog; var a: real; begin a := (1 + (1 * 1)) + 1; end.");
		testCode("program prog; var a: real; begin a := (1 * (1 + 1.0)) * 1; end.");
		
		failCode("program prog; var a: integer; begin a := 1 * 1.0; end.");
		failCode("program prog; var a: integer; begin a := (1 + (1 * 1.0)) * 1.0; end.");
		
		// Relational operators
		testCode("program prog; var a: boolean; begin a := 1 * 1 >= 0; end.");
		testCode("program prog; var a: boolean; begin a := 1 <= (1 + (1 * 1)) * 1; end.");
		testCode("program prog; var a: boolean; begin a := 1 = 1; end.");
		testCode("program prog; var a: boolean; begin a := (1 + (1 * 1)) > 1; end.");
		testCode("program prog; var a: boolean; begin a := (1 * (1 + 1.0)) < 1; end.");
		
		failCode("program prog; var a: integer; begin a := 1 > 1.0; end.");
		failCode("program prog; var a: integer; begin a := (1 + (1 < 1.0)) * 1.0; end.");
		
		// Logical operators
		testCode("program prog; var a: boolean; begin a := true and false; end.");
		testCode("program prog; var a: boolean; begin a := (1 > 2) or (1 <= ((1 + (1 * 1)) * 1)); end.");
		testCode("program prog; var a: boolean; begin a := (1 = 1) and (1 < 1); end.");
		testCode("program prog; var a: boolean; begin a := true or ((1 + (1 * 1)) > 1); end.");
		testCode("program prog; var a: boolean; begin a := ((1 + 1 * 1) > 1) or ((1 * 1 + 1.0) < 1); end.");
		
		failCode("program prog; var a: integer; begin a := true and 1; end.");
		failCode("program prog; var a: integer; begin a := (1 + (1 < 1.0)) or false; end.");
	}
}
