import java.io.File;
import java.util.List;

import lexicon.LexiconAnalyser;
import lexicon.Symbol;

public class Program {
	
	public static void main(String[] args)
	{
		File file = new File(args[1]);
		
		List<Symbol> symbols = LexiconAnalyser.process(new File(args[1]))
	}
	
}
