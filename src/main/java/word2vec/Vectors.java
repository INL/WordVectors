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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;



//import nl.openconvert.log.ConverterLog;

/**
 * This class stores the mapping of String->array of float that constitutes each vector.
 * 
 * The class can serialize to/from a stream.
 * 
 * The ConvertVectors allows to transform the C binary vectors into instances of this class.
 */
public class Vectors implements java.io.Serializable
{

	/**
	 * The vectors themselves.
	 */
	protected float[][] vectors;

	/**
	 * The words associated with the vectors
	 */
	protected String[] vocabVects;

	/**
	 * Size of each vector
	 */
	protected int size;

	/**
	 * Inverse map, word-> index
	 */
	protected Map<String, Integer> vocab;

	/**
	 * Package-level constructor, used by the ConvertVectors program.
	 * 
	 * @param vectors
	 *            , it cannot be empty
	 * @param vocabVects
	 *            , the length should match vectors
	 */
	Vectors(float[][] vectors, String[] vocabVects) throws VectorsException {
		this.vectors = vectors;
		this.size = vectors[0].length;
		if (vectors.length != vocabVects.length)
			throw new VectorsException("Vectors and vocabulary size mismatch");
		this.vocabVects = vocabVects;
		this.vocab = new HashMap<String, Integer>();
		for (int i = 0; i < vocabVects.length; i++)
			vocab.put(vocabVects[i], i);
	}

	/**
	 * Initialize a Vectors instance from an open input stream. This method closes the stream.
	 * 
	 * @param is
	 *            the open stream
	 * @throws IOException
	 *             if there are problems reading from the stream
	 */
	public Vectors(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);

		int words = dis.readInt();
		int size = dis.readInt();
		this.size = size;

		this.vectors = new float[words][];
		this.vocabVects = new String[words];

		for (int i = 0; i < words; i++) {
			this.vocabVects[i] = dis.readUTF();
			float[] vector = new float[size];
			for (int j = 0; j < size; j++)
				vector[j] = dis.readFloat();
			this.vectors[i] = vector;
		}
		this.vocab = new HashMap<String, Integer>();
		for (int i = 0; i < vocabVects.length; i++)
			vocab.put(vocabVects[i], i);
		dis.close();
	}

	/**
	 * Writes this vector to an open output stream. This method closes the stream.
	 * 
	 * @param os
	 *            the stream to write to
	 * @throws IOException
	 *             if there are problems writing to the stream
	 */
	public void writeTo(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(this.vectors.length);
		dos.writeInt(this.size);

		for (int i = 0; i < vectors.length; i++) {
			dos.writeUTF(this.vocabVects[i]);
			for (int j = 0; j < size; j++)
				dos.writeFloat(this.vectors[i][j]);
		}
		dos.close();
	}

	public void printAsText(PrintWriter out)
	{
		for (int c = 0; c < this.wordCount(); c++) 
		{    
			out.print(this.getTerm(c));
			float[] v = this.getVector(c);
			out.print("\t" + v[0]);
			for (int i=1; i < this.vectorSize(); i++)
			{
				out.print(" " + v[i]);
			}
			out.println();
		}
	}

	public float[][] getVectors() {
		return vectors;
	}

	public float[] getVector(int i) {
		return vectors[i];
	}

	public float[] getVector(String term) throws OutOfVocabularyException {
		Integer idx = vocab.get(term);
		if (idx == null)
			throw new OutOfVocabularyException("Unknown term '" + term + "'");
		return vectors[idx];
	}

	public int getIndex(String term) throws OutOfVocabularyException {
		Integer idx = vocab.get(term);
		if (idx == null)
			throw new OutOfVocabularyException("Unknown term '" + term + "'");
		return idx;
	}

	public Integer getIndexOrNull(String term) {
		return vocab.get(term);
	}

	public String getTerm(int index) {
		return vocabVects[index];
	}

	public Map<String, Integer> getVocabulary() {
		return vocab;
	}

	public boolean hasTerm(String term) {
		return vocab.containsKey(term);
	}

	public int vectorSize() {
		return size;
	}

	public int wordCount() {
		return vectors.length;
	}

	/**
	 * @param vectorFileName
	 * @return
	 */
	
	
	
	public static Vectors readFromFile(String vectorFileName)
	{
		float[][] vectors;
		String[] vocabVects;
		int words;
		int size;
	
		try
		{
			File vectorFile = new File(vectorFileName);
			if (!vectorFile.exists())
				throw new VectorsException("Vectors file not found");
	
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(vectorFile));
	
			StringBuilder sb = new StringBuilder();
			Util.readToNewline(inputStream, sb);
	
			String line = sb.toString();
			String[] parts = line.split("\\s+");
			
			words = (int) Long.parseLong(parts[0]);
			size = (int) Long.parseLong(parts[1]);
			
			vectors = new float[words][];
			vocabVects = new String[words];
			
			//nl.openconvert.log.ConverterLog.setDefaultVerbosity(true);
			//nl.openconvert.log.ConverterLog.defaultLog.println("Read " + words + " words with size " + size + " per vector from "  + vectorFileName);
	
	
			for (int w = 0; w < words; w++) 
			{
				//if (w % (words / 10) == 0) 
				//{
				//	nl.openconvert.log.ConverterLog.defaultLog.println("Read " + w + " words");
				//}
	
				String st = Util.readToWhiteSpace(inputStream, sb);
				vocabVects[w] = st;
				
				//System.err.println("<" + st + ">");
				
				float[] m = Util.readFloats(size, inputStream, true);
				Util.normalize(m);
				vectors[w] = m;
				char ch = (char) inputStream.read(); // newline
			}
			inputStream.close();
			Vectors instance = new Vectors(vectors, vocabVects);
			return instance;
		}  catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
