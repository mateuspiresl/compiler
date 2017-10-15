package syntactic;

import lexical.Symbol;

public interface SyntacticListener
{
	public void matchIndex(int i);
	
	// Scope
	public void onScopeBegin(int i, int line);
	public void onScopeEnd(int i, int line);
	
	// Block
	public void onBlockBegin(int i, Symbol symbol);
	public void onBlockEnd(int i, Symbol symbol);
	
	// Variables
	public void onVariableDeclaration(int i, Symbol symbol);
	public void onTypeDefinition(int i, Symbol symbol);
	
	// Expressions
	public void onVariable(int i, Symbol symbol);
	public void onValue(int i, Symbol symbol);
	public void onOperator(int i, Symbol symbol);
	public void onExpressionBegin(int i, Symbol symbol);
	public void onExpressionEnd(int i, Symbol symbol);
	
	// Procedures
	public void onProcedureDeclaration(int i, Symbol symbol);
	public void onProcedureParametersDeclarationBegin(int i, Symbol symbol);
	public void onProcedureParametersDeclarationEnd(int i, Symbol symbol);
	public void onProcedure(int i, Symbol symbol);
	public void onProcedureArgumentsBegin(int i, Symbol symbol);
	public void onProcedureArgumentsEnd(int i, Symbol symbol);
	public void onProcedureArgument(int i, Symbol symbol);
	
	// Control
	public void onControlCondition(int i, Symbol symbol);
}
