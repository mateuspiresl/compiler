import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import lexicon.LexiconAnalyser;
import lexicon.LexiconException;
import lexicon.Symbol;

public class Program {
	
	public static void main(String[] args) throws IOException
	{
		try
		{
			List<Symbol> symbols = LexiconAnalyser.process(
					Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8));
			
			for (Symbol symbol : symbols)
				System.out.println(symbol.toFormatedString());
		}
		catch (LexiconException le)
		{
			System.out.println(le.getMessage());
		}
	}
	
}
