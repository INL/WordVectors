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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;



/**
 * This program takes vectors are produced by the C program word2vec and transforms them into a Java binary file to be
 * read by the Vectors class
 *
 * (Slightly optimized by me, notably by simply using a BufferedInputStream)
 * Also (probably) reading the word2vec file is  faster than reading the java serialized object?
 */
public class ConvertVectors {

	/**
	 * @param args
	 *            the input C vectors file, output Java vectors file
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		Vectors v = Vectors.readFromFile(args[0]);
		File outputFile = new File(args[1]);
		FileOutputStream fos = new FileOutputStream(outputFile);
		v.writeTo(fos);
	}
    @SuppressWarnings("unused")
	@Deprecated
	private static float[] readFloatsOrig(int size, BufferedInputStream fis) throws IOException
	{
		float[] m = new float[size];
		byte[] orig = new byte[4];
		byte[] buf = new byte[4];
		for (int i = 0; i < size; i++)
		{
			// read a little endian floating point number and interpret it as a big endian one, see
			// http://stackoverflow.com/questions/2782725/converting-float-values-from-big-endian-to-little-endian/2782742#2782742
			// NB: this code assumes amd64 architecture
			for (int j = 0; j < 4; j++)
				orig[j] = (byte) fis.read(); // silly: no buffering....
			buf[2] = orig[0];
			buf[1] = orig[1];
			buf[0] = orig[2];
			buf[3] = orig[3];
			// this code can be made more efficient by reusing the ByteArrayInputStream
			//DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
			//float f1 = dis.readFloat();
			float f2 =  ByteBuffer.wrap(buf).getFloat();
			//System.err.println(f2);
			//if (f1 != f2)
			//{
			//nl.openconvert.log.ConverterLog.defaultLog.println("Niet gelijk: "+ f1 + " != " + f2);
			//}
			m[i] = f2;
			//dis.close();
		}
		return m;
	}
}
