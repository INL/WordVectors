package word2vec.relations;


import java.util.ArrayList;
import java.util.List;

import la.decomposition.QRDecomposition;
import la.decomposition.SingularValueDecomposition;
import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import la.vector.DenseVector;
import ml.options.Options;
import ml.regression.LASSO;
import ml.regression.Regression;
import ml.utils.Printer;
import word2vec.Distance;
import word2vec.Distance.ScoredTerm;
import word2vec.Util;
import word2vec.Vectors;
/**
 *
 * Test of met lineare relatie tussen twee ruimtes goede woordparen kunnen vinden.
 * Wellicht beter te maken door per woord / groepje woorden
 * andere lineaire afbeelding te kiezen, meer bepaald door de woorden waarmee het te mappen woord een relatie heeft
 *
 * <pre>
 *
 * LASSO is a Java implementation of LASSO, which solves the following convex optimization problem:
min_W 2\1 || Y - X * W ||_F^2 + lambda * || W ||_1
where X is an n-by-p data matrix with each row bing a p dimensional data vector and Y is an n-by-ny dependent variable matrix.
</pre>
 * @author does
 *
 */
public class VectorRegression
{
	double p = 0.01; // is dit slim?
	int max = 8000;
	boolean useP = false;
	boolean useMax = true;
	int N_TERMS=10;

	private static Regression getLasso()
	{
		Options options = new Options();
		options.maxIter = 30;
		options.lambda = 0.01;  // regularization
		options.verbose = !true;
		options.epsilon = 1e-5;

		Regression lasso = new LASSO(options);
		return lasso;
	}

	@SuppressWarnings("unused")
	private static void test()
	{
		double[][] data =
			{
					{1, 2, 3, 2},
					{4, 2, 3, 6},
					{5, 1, 2, 1}
			};

		double[][] depVars = {{3, 2}, {2, 3}, {1, 4}};

		Options options = new Options();
		options.maxIter = 600;
		options.lambda = 0.05;
		options.verbose = !true;
		options.epsilon = 1e-5;
		Regression LASSO = new LASSO(options);
		LASSO.feedData(data);
		LASSO.feedDependentVariables(depVars);
		LASSO.train();
		System.out.printf("Projection matrix:\n");
		Printer.display(LASSO.W);
		Matrix Yt =
				LASSO.predict(data);
		//
		System.out.printf("Predicted dependent variables:\n");
		Printer.display(Yt);
	}


	public static float[] doubleToFloat(double[] x)
	{
		float[] f = new float[x.length];
		for (int i=0; i < x.length; i++)
			f[i] = (float) x[i];
		return f;
	}

	public static double[] floatToDouble(float[] x)
	{
		double[] f = new double[x.length];
		for (int i=0; i < x.length; i++)
			f[i] = x[i];
		return f;
	}

	public static double[][] floatToDouble(float[][] x)
	{
		double[][] f = new double[x.length][];
		for (int i=0; i < x.length; i++)
			f[i] = floatToDouble(x[i]);
		return f;
	}

	public static double[][] floatToDouble(List<float[]> x)
	{
		int d = x.get(0).length;
		double[][] f = new double[d][];
		for (int i=0; i < d; i++)
		{
			f[i] = new double[x.size()];
			for (int j=0; j < x.size(); j++)
			{
				f[i][j] = x.get(j)[i];
			}
		}

		return f;
	}
	public static String[] getCommonVocabulary(Vectors v1, Vectors v2)
	{
		List<String> common = new ArrayList<>();
		for (int i=0; i < v1.wordCount(); i++)
		{
			String w = v1.getTerm(i);
			if (v2.hasTerm(w))
			{
				common.add(w);
			}
		}
		String[] x = (String[]) common.toArray();
		return x;
	}

	public void testMapping(Vectors v1, Vectors v2)
	{
		Matrix W = getLinearMappingBetweenSpaces(v1,v2);
		//String[] common = getCommonVocabulary(v1,v2);
		Matrix Wt = W.transpose();
		for (int i=0; i < v1.wordCount(); i++)
		{
			String w = v1.getTerm(i);
			if (v2.hasTerm(w))
			{
				try
				{
					float[] vec1 = v1.getVector(i);
					float[] vec2 = v2.getVector(w);
					double[][] d1 = new double[1][];
					d1[0] = floatToDouble(vec1);

					DenseVector v = new DenseVector( floatToDouble(vec1));
					DenseVector img = (DenseVector) Wt.operate(v);

					float[] y1 = doubleToFloat(img.getPr());
					Util.normalize(y1);

					// now measure distance between y1 and vec2. waarom zit dat verdarrie niet in de interface??

					double d = Distance.cosineSimilarity(y1,vec2);

					System.out.print(w + " selfdist:"  + d + "  ");

					List<ScoredTerm> close = Distance.getClosestTerms(v2, N_TERMS, y1);

					int k=0;
					int matchAt = -1;

					//boolean selfMatch = false;

					String neighbours = "";

					for (ScoredTerm st: close)
					{
						if (st.getTerm().equals(w))
						{
							matchAt = k;
						}
						neighbours += st.getTerm() + "/" + st.getScore() + " ";
						k++;
					}

					System.out.println( "   selfMatch: " + matchAt + " " + neighbours);

				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	public Matrix getLinearMappingBetweenSpaces(Vectors v1, Vectors v2)
	{
		//long s = System.currentTimeMillis();
		List<String> words = new ArrayList<>();
		List<float[]> vectors1 = new ArrayList<>();
		List<float[]> vectors2 = new ArrayList<>();
		int k=0;
		for (int i=0 ; i < v1.wordCount(); i++)
		{
			if ((!useP  ||  Math.random() < p) && (!useMax ||  k < max))
			{
				String w = v1.getTerm(i);
				float[] vec1 = v1.getVector(i);
				if (v2.hasTerm(w)) try
				{
					// nl.openconvert.log.ConverterLog.defaultLog.println("selected " + w);
					float[] vec2 = v2.getVector(w);
					if (vec2 != null)
					{
						words.add(v1.getTerm(i));
						vectors1.add(vec1);
						vectors2.add(vec2);
						k++;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		Matrix r = fitLinearMapping(vectors1, vectors2);

		// long f = System.currentTimeMillis();

		// nl.openconvert.log.ConverterLog.defaultLog.println("Computed linear mapping in " +(f-s) + " milliseconds ");
		return r;
	}

	public static float[] apply(Matrix mT, float[] x)
	{
		double[] V1 = VectorRegression.floatToDouble(x);
		DenseVector img = (DenseVector) mT.operate(new DenseVector(V1));
		float[] y1 = VectorRegression.doubleToFloat(img.getPr());
		word2vec.Util.normalize(y1);
		return y1;
	}

	public static Matrix Identity(int dim)
	{
		double[][] x = new double[dim][dim];
		for (int i=0; i < dim; i++)
			for (int j=0; j < dim; j++)
				x[i][j] = (i==j)?1:0;
		return new DenseMatrix(x);
	}


	/**
	 * ? arg min<sub>R</sub> || RA - B || <br>
	 * Oplossing: <br>
	 * X = A * B<sup>T</sup> <br>
	 * X = UΣV <br>
	 * R = U * V<sup>T</sup>
	 * <br>
	 * https://en.wikipedia.org/wiki/Orthogonal_Procrustes_problem
	 */

	public static Matrix Procrustes(Matrix A, Matrix B)
	{
		Matrix X = B.mtimes(A.transpose());
		Matrix[] USV = SingularValueDecomposition.decompose(X);
		Matrix U = USV[0];
		//Matrix S = USV[1];
		Matrix V = USV[2];
		return U.mtimes(V.transpose());
	}

	public static Matrix Procrustes(List<float[]> x, List<float[]> y)
	{
		Matrix A = new DenseMatrix(floatToDouble(x));
		Matrix B = new DenseMatrix(floatToDouble(y));
		return Procrustes(A,B);
	}

	public static Matrix fitLinearMapping(List<float[]> x, List<float[]> y, int reduceTo)
	{
		int dim = x.get(0).length;

		Matrix m = fitLinearMapping(x,y);

		Matrix A = new DenseMatrix(floatToDouble(x));

		Matrix[] QR = QRDecomposition.decompose(A);


		Matrix Q = QR[0];

		// System.err.println(Q.getColumnDimension() + " x " + Q.getRowDimension());

		Matrix P = Q.mtimes(Q.transpose());



		Matrix Q2 = Identity(dim).minus(P); // waarom werkte de oude foute keuze beter???

		//Matrix Q1 = Q.getColumns(0,x.size()-1);

		Matrix I_A = Identity(dim).getColumns(0, x.size()-1);

		//Matrix Q1T = Q1.transpose();


		Matrix MP = m.mtimes(P);



		boolean reduce = false;
		if (reduce)
		{
			Matrix[] USV = SingularValueDecomposition.decompose(MP.minus(I_A), true);

			@SuppressWarnings("unused")
			Matrix U = USV[0];
			Matrix S = USV[1];
			@SuppressWarnings("unused")
			Matrix V = USV[2];

			//Printer.print(V);

			//Printer.printMatrix(SVU[1]);
			for (int i = reduceTo; i < S.getColumnDimension(); i++)
			{
				S.setEntry(i, i, 0);
			}

			// Matrix r1 = U.mtimes(S.mtimes(V.transpose())).plus(I_A);
			// // Matrix r = r1.mtimes(Q1T);
		}

		return MP.plus(Q2); // waarom maakt dit niks uit
		//return m;
	}

	/**
	 * !Use Procrustes instead of this.... (cf. Hamilton Leskovec Jurafsky 2016)
	 * Probably better results ....
	 * @param x
	 * @param y
	 * @return
	 */
	public static Matrix fitLinearMapping(List<float[]> x, List<float[]> y)
	{
		double[][] X  = new double[x.size()][];
		double[][] Y  = new double[x.size()][];

		//nl.openconvert.log.ConverterLog.defaultLog.println("Selected + " + x.size());

		for (int i=0; i < x.size(); i++)
		{
			float[] vec1 = x.get(i);
			float[] vec2 = y.get(i);
			int nx = vec1.length;
			int ny = vec2.length;

			X[i] = new double[nx];
			Y[i] = new double[ny];
			for (int j=0; j < nx; j++)
			{
				X[i][j]= vec1[j];
			}
			for (int j=0; j < ny; j++)
			{
				Y[i][j]= vec2[j];
			}
		}
		Regression lasso = getLasso();
		lasso.feedData(X);
		lasso.feedDependentVariables(Y);
		lasso.train();
		//Printer.display(lasso.W);a

		Matrix r = lasso.W;
		return r;
	}

	public static void main(String[] args)
	{
		//test();
		VectorRegression l = new VectorRegression();
		Vectors v1 = Vectors.readFromFile(args[0]);
		Vectors v2 = Vectors.readFromFile(args[1]);
		l.testMapping(v1, v2);
	}
}
