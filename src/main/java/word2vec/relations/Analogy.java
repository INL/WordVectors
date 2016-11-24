package word2vec.relations;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import word2vec.Distance;
import word2vec.Util;
import word2vec.Vectors;
import word2vec.Distance.ScoredTerm;

public class Analogy 
{
	Vectors vectors;

	public float[] guess(float[] a, float[] b, float[] c)
	{
		float[] diff = Distance.subtract(b, a);
		//Util.normalize(diff);
		float[] r =  Distance.add(c, diff);
		Util.normalize(r);
		return r;
	}

	public void guess(String a, String b, String c, String d)
	{
		try
		{
			float[] A = vectors.getVector(a);
			float[] B = vectors.getVector(b);
			float[] C = vectors.getVector(c);
			float[] D = guess(A,B,C);
			
			String[] known = {a,b,c};
			Set<String> knownSet = new HashSet<String>();
			knownSet.add(a); knownSet.add(b); knownSet.add(c);

			List<ScoredTerm> close = Distance.getClosestTerms(vectors, 10, D);
			
			int k=0;
			int matchAt = -1;
			
			boolean selfMatch = false;
			
			String neighbours = "";
			
			for (ScoredTerm st: close)
			{
				if (knownSet.contains(st.getTerm()))
					continue;
				if (d != null && st.getTerm().equals(d))
				{
					matchAt = k;
				}
				neighbours += st.getTerm() + "/" + st.getScore() + " ";
				k++;
			}
			
			System.out.println( "   selfMatch: " + matchAt + " " + neighbours);
		} catch (Exception e)
		{

		}
	}
	
	public static void main(String[] args)
	{
		Analogy a = new Analogy();
		a.vectors = Vectors.readFromFile(args[0]);
		System.err.println("enter words...");
		//int nof = Integer.parseInt(args[1]);
		String line;
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while ((line = in.readLine()) != null)
			{
				System.out.println("#### "  + line + " ####");
				String[] tokens = line.split("\\s+");
				String d = tokens.length > 3? tokens[3]:null;
				try
				{
					
					a.guess(tokens[0], tokens[1], tokens[2], d);
				
				} catch (Exception e)
				{

				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
