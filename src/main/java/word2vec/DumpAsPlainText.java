package word2vec;

import java.io.PrintWriter;

import word2vec.Vectors;

public class DumpAsPlainText
{
	public static void main(String[] args)
	{
		Vectors v = Vectors.readFromFile(args[0]);
		v.printAsText(new PrintWriter(System.out));
	}
}
