package word2vec;
import java.util.*;



import word2vec.Distance.ScoredTerm;


public class PrintNeighbours
{
	Vectors vectors;
	int wordsToReturn = 50;

	public PrintNeighbours(String vectorFilename, int n)
	{
		vectors = Vectors.readFromFile(vectorFilename);
		this.wordsToReturn = n;
	}

	public void printNeighbours(String[] words)
	{

		Set<String> selected = new HashSet<String>();
		for (String w: words)
		{		
			selected.add(w);
			String[] w0 = {w};
			try
			{
				List<ScoredTerm> n = Distance.measure(vectors, wordsToReturn, w0);
				for (ScoredTerm t: n)
					selected.add(t.getTerm());
			} catch (Exception e)
			{

			}
		}
		for (String w: selected)
		{

			{
				float[] v;
				try 
				{
					v = vectors.getVector(w);
					System.out.print(w);
					for (float c: v)
					{
						System.out.print("\t" + c);
					}
					System.out.println();
				} catch (OutOfVocabularyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	public static void main(String[] args)
	{
		PrintNeighbours p = new PrintNeighbours(args[0],Integer.parseInt(args[1]));
		p.printNeighbours( Arrays.copyOfRange(args, 2, args.length));
	}
}
