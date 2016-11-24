package word2vec;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import word2vec.Distance.ScoredTerm;

public class DocDistance
{


	private static void findCorrespondingWord(Vectors v, int nof)
	{
		String line;
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while ((line = in.readLine()) != null)
			{

				final boolean lookForDoc = line.contains("_") || line.contains(" ");

				System.out.println("#### "  + line + " ####");
				String[] tokens = line.split("\\s+");
				try
				{

					List<ScoredTerm> l = Distance.measure(v, nof, tokens, s -> x(lookForDoc, s.startsWith("_")));

					for (ScoredTerm t: l)
					{
						String w = t.getTerm();
						System.out.println(w + "\t" + t.getScore());
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static boolean x(boolean a, boolean b)
	{
		return a?b:!b;
	}


	public static void main(String[] args)
	{
		Vectors v = Vectors.readFromFile(args[0]);
		System.err.println("enter word...");
		int nof = Integer.parseInt(args[1]);
		findCorrespondingWord(v, nof);
	}
}
