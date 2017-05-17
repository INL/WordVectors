package word2vec;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



//import diamant.mds.WordInContext;

public class Util 
{

	public static void writeFloats(FileOutputStream fw, float[] v) throws IOException 
	{
		for (float f: v)
		{
			byte[] bytes = new byte[4];
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			buffer.putFloat(f);
			buffer.position(0);
			buffer.get(bytes);


			byte[] swap = new byte[4];

				swap[0] = bytes[3];
				swap[1] = bytes[2];
				swap[2] = bytes[1];
				swap[3] = bytes[0];
			


			fw.write(swap);
		}
	}

	public static float[] readFloats(int size, BufferedInputStream fis, boolean fromLittleEndian) throws IOException 
	{
		float[] readback=new float[size];
		try
		{
			byte[] buffer = new byte[4 * size];
			fis.read(buffer);

			

			if (fromLittleEndian)
			{
				byte[] swap = new byte[4];

				for (int j=0; j < size; j++)
				{
					int d = 4*j;

					
						swap[0] = buffer[d+3];
						swap[1] = buffer[d+2];
						swap[2] = buffer[d+1];
						swap[3] = buffer[d];

						buffer[d] = swap[0];
						buffer[d+1] = swap[1];
						buffer[d+2] = swap[2];
						buffer[d+3] = swap[3];
					
				}
			}

			// System.err.println(r);

			ByteBuffer buf_in = ByteBuffer.wrap(buffer);


			buf_in.rewind();
			buf_in.asFloatBuffer().get(readback);
		

		} catch (IOException ex) 
		{
			System.err.println(ex.getMessage());
		}
		return readback;
	}

	public static void test() throws Exception
	{
		float[] f = new float[20];

		for (int i=0; i < 20; i++)
			f[i] = i;
		normalize(f);
		FileOutputStream fos = new FileOutputStream("/tmp/aap");
		writeFloats(fos, f);
		fos.close();

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream("/tmp/aap"));
		float[] f1 = readFloats(20, bis, true);
		for (int i=0; i < 20; i++)
		{
			if (f1[i] != f[i])
			{
				System.out.println(f1[i] + " != " + f[i]);
			}
		}
	}


	public static void test2() throws Exception
	{
		int dim = 20;
		float[] f = new float[dim];

		for (int i=0; i < dim; i++)
			f[i] = i;
		normalize(f);
		Map<String, float[]> senseMap = new HashMap<String, float[]>();

		senseMap.put("aap",f);
		senseMap.put("noot",f);
		senseMap.put("wim",f);
		writeAsWord2VecFile(dim, senseMap, "/tmp/aap");


		Vectors v = Vectors.readFromFile("/tmp/aap");
		System.err.println(v.getVocabulary());
		float[] f1 = v.getVector("aap");

		for (int i=0; i < dim; i++)
		{
			if (f1[i] != f[i])
			{
				System.out.println(f1[i] + " != " + f[i]);
			}
		}
	}

	public static void normalize( float[] vec)
	{
		int size = vec.length;
		double length;
		length = 0;
		for (int i = 0; i < size; i++)
			length += vec[i] * vec[i];
		//double length2 = length;
		if (length == 0)
			return;
		length = (float) Math.sqrt(length);
		for (int i = 0; i < size; i++)
		{
			double d = vec[i] / length;
			float df = (float) d;
			if (Float.isNaN(df))
			{
				//System.err.println("NaN at " +  i + " vec[i]= " + vec[i] + " d= " + d + " len²=" + length2);
			}
			vec[i] = df;
		}
	}

	public static float[] getAverageVector(Vectors vectors, String[] tokens, float alpha)
	{
		int size = vectors.vectorSize();
		float[] vec = new float[size];
		Arrays.fill(vec, 0.0f);
		int tokenCount = tokens.length;
		float[][]allVec = vectors.getVectors();

		for (int i = 0; i < tokenCount; i++) 
		{
			Integer idx = vectors.getIndexOrNull(tokens[i]);

			if (idx == null) 
			{	
				continue;
			}

			float[] vect1 = allVec[idx];

			for (int j = 0; j < size; j++)
				vec[j] += vect1[j];
		}


		normalize(vec);
		Distance.checkNAN(vec);

		return vec;
	}

	public static float[] getProductVector(Vectors vectors, String[] tokens)
	{
		int size = vectors.vectorSize();
		float[] vec = new float[size];
		for (int i=0; i < vec.length; i++)
			vec[i] = 1;
		
		int tokenCount = tokens.length;
		float[][]allVec = vectors.getVectors();

		int found=0;
		for (int i = 0; i < tokenCount; i++) 
		{
			Integer idx = vectors.getIndexOrNull(tokens[i]);

			if (idx == null) 
			{	
				continue;
			}

			found++;
			float[] vect1 = allVec[idx];
			System.err.println(idx +  " vect1:  " + toList(vect1));
			for  (int j = 0; j < size; j++)
				vec[j] *= (1 + vect1[j]);
		}

		for (int i=0; i < vec.length; i++)
		{
			// boolean neg = false;
			
			vec[i] = (float)  Math.pow((double) vec[i], 1/ (double) found);
			vec[i] = vec[i] - 1;
			// System.err.println(i + " "  + vec[i]);
		}

		normalize(vec);
		Distance.checkNAN(vec);
		System.err.println("Product of"  + Arrays.asList(tokens) +  " :  " + toList(vec));
		return vec;
	}
	
	static List<Float> toList(float[] a)
	{
		List<Float> r = new ArrayList<Float>();
		for (float x: a)
		{
			r.add(x);
		}
		return r;
	}
	public static float[] getAverageVector(Vectors vectors, Collection<String> tokens)
	{
		return getAverageVector(vectors, tokens, 0);
	}
	
	public static float[] getAverageVector(Vectors vectors, Collection<String> tokens, float alpha)
	{
		int size = vectors.vectorSize();
		float[] vec = new float[size];
		Arrays.fill(vec, 0.0f);
		// int tokenCount = tokens.size();
		float[][]allVec = vectors.getVectors();

		for (String token: tokens) 
		{
			Integer idx = vectors.getIndexOrNull(token);

			if (idx == null) 
			{	
				continue;
			}

			float[] vect1 = allVec[idx];

			for (int j = 0; j < size; j++)
				vec[j] += vect1[j];
		}


		normalize(vec);
		Distance.checkNAN(vec);

		return vec;
	}
/*
 * Weight the average vector by rank in the word2vec file (which should be sorted on descending frequency)
 */
	
	public static float[] getRankWeightedAverageVector(Vectors vectors, String[] tokens)
	{
		int size = vectors.vectorSize();
		float[] vec = new float[size];
		Arrays.fill(vec, 0.0f);
		int tokenCount = tokens.length;
		float[][]allVec = vectors.getVectors();

		for (int i = 0; i < tokenCount; i++) 
		{
			Integer idx = vectors.getIndexOrNull(tokens[i]);

			if (idx == null) 
			{	
				continue;
			}

			double weight = Math.log(1+idx);
			float[] vect1 = allVec[idx];

			for (int j = 0; j < size; j++)
				vec[j] += weight * vect1[j];
		}


		normalize(vec);
		Distance.checkNAN(vec);

		return vec;
	}
	
	public static float[] getRankAndDistanceWeightedAverageVector(Vectors vectors, WordInContext wic)
	{
		List<String> tokens = wic.sentence;
		int focusPosition = wic.focusPosition;
		
		float[] vec = getRankedAndDistanceWeightedAverageVector(vectors, tokens, focusPosition);

		return vec;
	}

	private static float[] getRankedAndDistanceWeightedAverageVector(Vectors vectors, List<String> tokens,
			int focusPosition) 
	{
		int size = vectors.vectorSize();
		float[] vec = new float[size];
		Arrays.fill(vec, 0.0f);
		
		
		
		int tokenCount = tokens.size();
		int T = tokenCount;
		
		//T = 10;
		float[][]allVec = vectors.getVectors();
		//String lemma = wic.lemma.replaceAll(":.*", "").toLowerCase();
		//float[] vLem = null;
		
		/**
		try
		{
		   vLem = vectors.getVector(lemma);
		} catch (OutOfVocabularyException e)
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		*/
		
		for (int i = 0; i < tokenCount; i++) 
		{
			Integer idx = vectors.getIndexOrNull(tokens.get(i));

			if (idx == null) 
			{	
				continue;
			}
			float[] vect1 = allVec[idx];
			
			double relevanceWeight = 1;
			//if (vLem != null) relevanceWeight = Distance.cosineSimilarity(vLem, vect1); // dit werkt dus totaal NIET...
			
			double rankWeight = Math.log(1+idx);
			double distanceWeight = (1 / (double) T) * Math.max(0, T - Math.abs(i-focusPosition));
			//System.err.println(tokenCount + ": " + i +  " f=" + wic.focusPosition + " dw=" + distanceWeight);
			
			
			for (int j = 0; j < size; j++)
				vec[j] += rankWeight * distanceWeight * relevanceWeight * vect1[j];
		}


		normalize(vec);
		Distance.checkNAN(vec);
		return vec;
	}
	
	public static float[] getRankWeightedAverageVector(Vectors vectors,
			List<String> tokens)
	{
		// TODO Auto-generated method stub
		int size = vectors.vectorSize();
		float[] vec = new float[size];
		Arrays.fill(vec, 0.0f);
		// int tokenCount = tokens.size();
		float[][]allVec = vectors.getVectors();

		for (String token: tokens) 
		{
			Integer idx = vectors.getIndexOrNull(token);

			if (idx == null) 
			{	
				continue;
			}

			double weight = Math.log(1+idx);
			float[] vect1 = allVec[idx];

			for (int j = 0; j < size; j++)
				vec[j] += weight * vect1[j];
		}


		normalize(vec);
		Distance.checkNAN(vec);

		return vec;
	}


	public static void writeAsWord2VecFile(int dimension, Map<String, float[]> senseMap, String fileName) throws IOException
	{
		int words = senseMap.size();
		FileOutputStream fw = new FileOutputStream(fileName);
		fw.write((words + " " + dimension + "\n").getBytes());
		for (String s: senseMap.keySet())
		{
			fw.write(s.getBytes());
			fw.write(' ');
			//fw.write(16);
			fw.flush();
			float[] v = senseMap.get(s);
			// System.err.println(Arrays.asList(v));
			writeFloats(fw, v);
			fw.flush();
			fw.write("\n".getBytes());
		}
		fw.close();
	}

	public static void main(String[] args) throws Exception
	{
		test2();
	}

	public static void readToNewline(BufferedInputStream fis, StringBuilder sb) throws IOException 
	{
		char ch = (char) fis.read();
		while (ch != '\n') 
		{
			sb.append(ch);
			ch = (char) fis.read();
		}
	}

	public static String readToWhiteSpace(BufferedInputStream fis, StringBuilder sb)
			throws IOException, UnsupportedEncodingException 
	{
		char ch; 
		sb.setLength(0);
		ch = (char) fis.read();
		while (!Character.isWhitespace(ch) && ch >= 0 && ch <= 256) 
		{
			sb.append((char) ch);
			ch = (char) fis.read();
		}
		//ch = (char) fis.read(); // dit slaat nergens op!
		String st = sb.toString();
		byte[] b0 = st.getBytes("iso-8859-1");
		st = new String(b0, "utf-8");
		return st;
	}

	
}
