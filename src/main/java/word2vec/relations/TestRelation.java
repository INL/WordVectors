package word2vec.relations;

import java.util.ArrayList;
import java.util.List;

import org.ivdnt.util.TabSeparatedFile;

//import diamant.mds.WordInContext;
import la.matrix.Matrix;
import word2vec.Distance;
import word2vec.Distance.ScoredTerm;
import word2vec.Vectors;

public class TestRelation
{
	static String[] fields = {"w1", "relation", "w2"};
	Vectors vectors;

	class Instance
	{
		String w1;
		String w2;
		String relation;
		float[] v1;
		float[] v2;
		boolean ok = true;

		@Override
		public String toString()
		{
			return relation + "(" + w1 + "," + w2 + ")";
		}
	}

	List<Instance> instances = new ArrayList<>();

	public  void readInstances(String fileName)
	{
		TabSeparatedFile ts = new TabSeparatedFile(fileName, fields);
		String[] aap;
		while ((aap = ts.getLine()) != null)
		{
			System.err.println(aap[0]);
			Instance i = new Instance();
			i.w1 = ts.getField("w1");
			i.w2 = ts.getField("w2");
			i.relation = ts.getField("relation");
			instances.add(i);
			System.err.println(i);
		}
	}

	public void test()
	{
		for (Instance i: instances)
		{
			try
			{
				i.v1 = vectors.getVector(i.w1);
				i.v2 = vectors.getVector(i.w2);
			} catch (Exception e)
			{
				e.printStackTrace();
				i.ok = false;
			}
		}

		for (Instance i: instances)
		{
			leaveOneOut(i);
		}
	}

	public void leaveOneOut(Instance h)
	{
		List<float[]> l1 = new ArrayList<>();
		List<float[]> l2 = new ArrayList<>();

		for (Instance i: instances)
		{
			if (h != i)
			{
				l1.add(i.v1);
				l2.add(i.v2);
			}
		}

		Matrix m = VectorRegression.fitLinearMapping(l1, l2, Math.max(l1.size()-1,1));

		// dit gaat dus niet zo goed bij weinig (minder dan dim V) data, terwijl de simpele translatie wel werkt.
		// simpelweg te veel vrijheden.
		// oplossing: pca, en dan...

		Matrix mT = m.transpose();

		float[] y1 = VectorRegression.apply(mT, h.v1);

		double d = Distance.cosineSimilarity(y1,h.v2);

		System.out.print(h + " target_distance:"  + d + "  ");
		List<ScoredTerm> close = Distance.getClosestTerms(vectors, 50, y1);

		int k=0;
		int matchAt = -1;

		StringBuilder neighbours = new StringBuilder();

		for (ScoredTerm st: close)
		{
			if (st.getTerm().equals(h.w2))
			{
				matchAt = k;
			}
			neighbours.append(st.getTerm()).append("/").append(st.getScore()).append(" ");
			k++;
		}

		System.out.println( "   selfMatch: " + matchAt + " " + neighbours.toString());
	}

	public static void main(String[] args) throws Exception
	{
		TestRelation tr = new TestRelation();
		tr.vectors = Vectors.readFromFile(args[0]);
		tr.readInstances(args[1]);
		tr.test();
	}
}
