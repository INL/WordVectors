package word2vec;

import java.io.BufferedReader;
import java.util.*;
import java.io.InputStreamReader;
import java.util.List;

import word2vec.Distance.ScoredTerm;

public class NNBN
{


	private static void findCorrespondingWord(Vectors v, int nof)
	{
		String line;
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while ((line = in.readLine()) != null)
			{

				final String lookFor = line.endsWith("_nn")?"_bn":"_nn";

				System.out.println("#### "  + line + " ####");
				String[] tokens = line.split("\\s+");
				try
				{
					long l1 = System.currentTimeMillis();
					List<ScoredTerm> l = Distance.measure(v, nof, tokens, s -> s.endsWith((lookFor)));
					long l2 = System.currentTimeMillis();
					System.err.println("Time:" + (l2-l1));
					for (ScoredTerm t: l)
					{
						String w = t.getTerm();
						if (w.endsWith(lookFor))
							System.out.println(t.getTerm() + "\t" + t.getScore());
					}
				} catch (Exception e)
				{

				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static class Item
	{
		String w;
		ScoredTerm other;
		double d;
		
		public Item(String w, double d)
		{
			this.w = w;
			this.d = d;
		}
	}

	public static void findLeastSelfSimilar(Vectors vectors)
	{
		Map<String, Integer> v = vectors.getVocabulary();
		String[] suffixes = {"_nn", "_bn"};
		List<Item> items = new ArrayList<>();
		for (String s: v.keySet())
		{
			for (int i=0; i < 1; i++ )
			{
				if (s.endsWith(suffixes[i]) && s.matches("^[a-z]+_(nn|bn)$"))
				{
					try
					{
						String s1 = s.replaceAll(suffixes[i], suffixes[1-i]);
						float[] v0 = vectors.getVector(s);
						float[] v1 = vectors.getVector(s1);
						double d = Distance.cosineSimilarity(v0, v1);
						items.add(new Item(s,d));
					} catch (Exception e)
					{
						//e.printStackTrace();
					}
				}
			}
		}
		Collections.sort(items, (i1,i2) -> Double.compare(i1.d, i2.d));
		int k=0;
		for (Item i: items)
		{
			try
			{
				i.other = findOther(vectors,i.w);
				System.out.println(i.w + "\t"  + i.d + "\t"  + i.other.getTerm());
			} catch (Exception e)
			{
				
			}
			if (k++ > 100)
				break;
		}
	}

	public static ScoredTerm findOther(Vectors v, String s) throws OutOfVocabularyException
	{
		String[] suffixes = {"_nn", "_bn"};
		for (int i=0; i < 1; i++ )
		{
			if (s.endsWith(suffixes[i]))
			{
				String[] tokens = {s};
				final String otherSuf = suffixes[1-i];
				List<ScoredTerm> others = Distance.measure(v, 1, tokens, str->str.endsWith(otherSuf));
				return others.get(0);
			}
		}
		return null;
	}
	public static void main(String[] args)
	{
		Vectors v = Vectors.readFromFile(args[0]);
		findLeastSelfSimilar(v);
		System.err.println("enter word...");
		int nof = Integer.parseInt(args[1]);
		findCorrespondingWord(v, nof);
	}
}
