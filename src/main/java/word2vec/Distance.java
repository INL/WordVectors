/*
 * Copyright 2014 Radialpoint SafeCare Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package word2vec;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * this is much faster in Mikolov's code. Difference?
 */
public class Distance
{

	public static class ScoredTerm
	{
		private String term;
		private float score;

		public ScoredTerm(String term, float score)
		{
			super();
			this.term = term;
			this.score = score;
		}

		public String getTerm() {
			return term;
		}

		public float getScore() {
			return score;
		}
	}

	public static List<ScoredTerm> getClosestTermsOld(Vectors vectors, int wordsToReturn, float[] vec)
	{
		float[][]allVec = vectors.getVectors();
		double similarity = 0;

		int size = vectors.vectorSize();

		float[] bestSimilarity = new float[wordsToReturn];

		for (int i=0; i < wordsToReturn; i++)
		{
			bestSimilarity[i] = Float.NEGATIVE_INFINITY;
		}


		String[] bestWords = new String[wordsToReturn];

		for (int c = 0; c < vectors.wordCount(); c++)
		{
			similarity = 0;
			for (int i = 0; i < size; i++)
				similarity += vec[i] * allVec[c][i];
			for (int i = 0; i < wordsToReturn; i++)  // this is slowish
			{
				if (similarity > bestSimilarity[i])
				{
					for (int d = wordsToReturn - 1; d > i; d--)
					{
						bestSimilarity[d] = bestSimilarity[d - 1];
						bestWords[d] = bestWords[d - 1];
					}
					bestSimilarity[i] = (float) similarity;
					bestWords[i] = vectors.getTerm(c);
					break;
				}
			}
		}
		List<ScoredTerm> result = new ArrayList<>(wordsToReturn);
		for (int i = 0; i < wordsToReturn; i++)
			result.add(new ScoredTerm(bestWords[i], bestSimilarity[i]));
		return result;
	}

	static class DistanceTo implements java.util.function.IntToDoubleFunction
	{
		float[] reference;
		float[][] allVec;

		public DistanceTo(float[] reference, float[][] allVec)
		{
			this.reference = reference;
			this.allVec = allVec;
		}
		@Override
		public double applyAsDouble(int value)
		{
			double similarity = 0;
			for (int i = 0; i < reference.length; i++)
				similarity += reference[i] * allVec[value][i];
			// TODO Auto-generated method stub
			return similarity;
		}

	}

	public static List<ScoredTerm> getClosestTerms(Vectors vectors, int wordsToReturn, float[] vec)
	{
		float[][]allVec = vectors.getVectors();
		double similarity = 0;

		// int size = vectors.vectorSize();

		float[] bestSimilarity = new float[wordsToReturn];

		for (int i=0; i < wordsToReturn; i++)
		{
			bestSimilarity[i] = Float.NEGATIVE_INFINITY;
		}


		String[] bestWords = new String[wordsToReturn];
		double[] similarities = new double[vectors.wordCount()];
		DistanceTo dt = new DistanceTo(vec, allVec);
		java.util.Arrays.parallelSetAll(similarities, dt); // ahem...


		for (int c = 0; c < vectors.wordCount(); c++)
		{
			similarity = similarities[c];
			for (int i = 0; i < wordsToReturn; i++)  // this is slowish
			{
				if (similarity > bestSimilarity[i])
				{
					for (int d = wordsToReturn - 1; d > i; d--)
					{
						bestSimilarity[d] = bestSimilarity[d - 1];
						bestWords[d] = bestWords[d - 1];
					}
					bestSimilarity[i] = (float) similarity;
					bestWords[i] = vectors.getTerm(c);
					break;
				}
			}
		}
		List<ScoredTerm> result = new ArrayList<>(wordsToReturn);
		for (int i = 0; i < wordsToReturn; i++)
			result.add(new ScoredTerm(bestWords[i], bestSimilarity[i]));
		return result;
	}

	public static void checkNAN(float[] vec)
	{
		for (int i=0; i < vec.length; i++)
		{
			if (Float.isNaN(vec[i]))
			{
				System.err.println("NaN at " + i);
			}
		}
	}

	public static void printAverageVectorsForLabeledSentences(Vectors vectors, String filename)
	{
		String line;
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null)
			{
				String[] tokens = line.split("\t");
				String lemma = tokens[0];
				String sense = tokens[1];
				String example = tokens[2];
				String[] words = example.split("\\s+");
				float [] vec = Util.getAverageVector(vectors, words, 0);
				System.out.print(sense + "\t" + lemma + "\t");
				for (int i=0; i < vec.length; i++)
				{
					System.out.print(i==0?"":" " + vec[i]);
				}
				System.out.println();
			}
			in.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<ScoredTerm> measure(Vectors vectors, int wordsToReturn, String[] tokens) throws OutOfVocabularyException
	{
		return measure(vectors, wordsToReturn, tokens, null);
	}

	public static List<ScoredTerm> measure(Vectors vectors, int wordsToReturn, String[] tokens, Predicate<String> predicate) throws OutOfVocabularyException
	{
		float distance;
		//float length;
		float[] bestDistance = new float[wordsToReturn];
		String[] bestWords = new String[wordsToReturn];
		int d;
		int size = vectors.vectorSize();
		float[] vec = new float[size]; // average vector of input tokens
		float[][]allVec = vectors.getVectors();
		//long startTime = System.currentTimeMillis();
		// nl.openconvert.log.ConverterLog.defaultLog.println("1:" + startTime);
		Set<Integer> wordIdx = new TreeSet<>();

		int tokenCount = tokens.length;
		// boolean outOfDict = false;
		String outOfDictWord = null;
		Arrays.fill(vec, 0.0f);
		wordIdx.clear();

		boolean foundAtLeastOneToken = false;
		for (int i = 0; i < tokenCount; i++)
		{
			Integer idx = vectors.getIndexOrNull(tokens[i]);
			if (idx == null)
			{
				outOfDictWord = tokens[i];
				// outOfDict = true;
				continue;
			} else
			{
				foundAtLeastOneToken = true;
			}
			wordIdx.add(idx);
			float[] vect1 = allVec[idx];
			for (int j = 0; j < size; j++)
				vec[j] += vect1[j];
		}

		if (!foundAtLeastOneToken)
			throw new OutOfVocabularyException(outOfDictWord);

		Util.normalize(vec);

		for (int i = 0; i < wordsToReturn; i++)
		{
			bestDistance[i] = Float.MIN_VALUE;
			bestWords[i] = "";
		}

		//nl.openconvert.log.ConverterLog.defaultLog.println("2:" +  ( System.currentTimeMillis() - startTime));
		int wc = vectors.wordCount();

		//String[] bestWords = new String[wordsToReturn];
		boolean runInParallel = true;
		double[] similarities = null;

		if (runInParallel)
		{
		  similarities = new double[vectors.wordCount()];
		  DistanceTo dt = new DistanceTo(vec, allVec);
		  java.util.Arrays.parallelSetAll(similarities, dt); // ahem...
		}
		for (int c = 0; c < wc; c++)
		{
			if (wordIdx.contains(c)) continue;
			if (predicate != null && !predicate.test(vectors.getTerm(c)))
				continue;

			distance = runInParallel?(float) similarities[c]:0;

			if (!runInParallel)
			{
				float[] vc = allVec[c];

				for (int i = 0; i < size; i++) // this is simply much slower than C..
				{
					distance += vec[i] * vc[i];
				}
			}
			for (int i = 0; i < wordsToReturn; i++)
			{
				if (distance > bestDistance[i])
				{
					for (d = wordsToReturn - 1; d > i; d--) // schuif op (dit zou sneller moeten kunnen meet een priority queue)
					{
						bestDistance[d] = bestDistance[d - 1];
						bestWords[d] = bestWords[d - 1];
					}
					bestDistance[i] = distance;
					bestWords[i] = vectors.getTerm(c);
					break;
				}
			}
		}
		//nl.openconvert.log.ConverterLog.defaultLog.println("best:" + bestDistance[0]);
		//nl.openconvert.log.ConverterLog.defaultLog.println("3:" +  ( System.currentTimeMillis() - startTime));
		List<ScoredTerm> result = new ArrayList<>(wordsToReturn);
		for (int i = 0; i < wordsToReturn; i++)
			result.add(new ScoredTerm(bestWords[i], bestDistance[i]));
		return result;
	}


	public static double cosineSimilarity(float[] a, float[] b)
	{
		double dotProduct = 0.0;
		double aMagnitude = 0.0;
		double bMagnitude = 0.0;
		for (int i = 0; i < b.length ; i++)
		{
			double aValue = a[i];
			double bValue = b[i];
			aMagnitude += aValue * aValue;
			bMagnitude += bValue * bValue;
			dotProduct += aValue * bValue;
		}
		aMagnitude = Math.sqrt(aMagnitude);
		bMagnitude = Math.sqrt(bMagnitude);
		return (aMagnitude == 0 || bMagnitude == 0)
				? 0: Math.abs(dotProduct / (aMagnitude * bMagnitude));
	}

	public static double angle(float[] a, float[] b)
	{
		double cos = cosineSimilarity(a,b);
		return Math.acos(cos);
	}

	public static void main(String[] args)
	{
		Vectors v = Vectors.readFromFile(args[0]);
		System.err.println("enter word...");
		int nof = Integer.parseInt(args[1]);
		String line;
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while ((line = in.readLine()) != null)
			{
				System.out.println("#### "  + line + " ####");
				String[] tokens = line.split("\\s+");
				float[] matchMe;
				if (tokens[0].equals("*"))
				{
					tokens = Arrays.copyOfRange(tokens, 1,tokens.length);
					matchMe = Util.getProductVector(v, tokens);
				} else
				{
					matchMe = Util.getAverageVector(v, tokens,0);
				}
				try
				{
					long l1 = System.currentTimeMillis();
					List<ScoredTerm> l = Distance.getClosestTerms(v, nof, matchMe);  // .measure(v, nof, tokens, null);
					long l2 = System.currentTimeMillis();
					System.err.println("Time:" + (l2-l1));
					for (ScoredTerm t: l)
					{
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

	public static float[] subtract(float[] v1, float[] v2)
	{

		if (v1 == null)
			return v2;
		if (v2== null)
			return v1;
		if (v1.length != v2.length)
			return null;
		float[] r = new float[v1.length];
		for (int i=0; i < r.length; i++)
			r[i] = v1[i]- v2[i];
		return r;
	}

	public static float[] add(float[] v1, float[] v2)
	{

		if (v1 == null)
			return v2;
		if (v2== null)
			return v1;
		if (v1.length != v2.length)
			return null;
		float[] r = new float[v1.length];
		for (int i=0; i < r.length; i++)
			r[i] = v1[i]+ v2[i];
		return r;
	}

	public static float[] mult(float[] v1, float[] v2)
	{

		if (v1 == null)
			return v2;
		if (v2== null)
			return v1;
		if (v1.length != v2.length)
			return null;
		float[] r = new float[v1.length];
		for (int i=0; i < r.length; i++)
			r[i] = v1[i]*v2[i];
		return r;
	}
}
