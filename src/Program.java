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
		try
		{
			List<Symbol> symbols = LexiconAnalyser.process(
					Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8));
			
			new SyntacticAnalyser(symbols).analyse();
			System.out.println("Success, no errors found!");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
