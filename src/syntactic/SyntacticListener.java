package syntactic;

import lexical.Symbol;

public interface SyntacticListener
{
	public void matchIndex(int i);
	public void onScopeBegin(int i, int line);
	public void onScopeEnd(int i, int line);
	public void onBlockBegin(int i, Symbol symbol);
	public void onBlockEnd(int i, Symbol symbol);
	public void onProcedureDeclaration(int i, Symbol symbol);
	public void onVariableDeclaration(int i, Symbol symbol);
	public void onTypeDefinition(int i, Symbol symbol);
	public void onProcedure(int i, Symbol symbol);
	public void onVariable(int i, Symbol symbol);
	public void onValue(int i, Symbol symbol);
	public void onOperator(int i, Symbol symbol);
	public void onExpressionBegin(int i, Symbol symbol);
	public void onExpressionEnd(int i, Symbol symbol);
}
