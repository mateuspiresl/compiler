import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lexicon.LexiconAnalyser;
import lexicon.Symbol;
import syntactic.SyntacticAnalyser;

public class Program
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 0) args = new String[] { "test-files/test1.pas" };
		
		try
		{
			List<Symbol> symbols = LexiconAnalyser.process(
					Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8));
			
			for (Symbol symbol : symbols)
				System.out.println(symbol.toFormatedString());
	
			new SyntacticAnalyser(symbols).analyse();
			System.out.println("Success");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
