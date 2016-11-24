package word2vec;




public class PrintAverageVectors
{
	public static void main(String[] args)
	{
		Vectors vectors = Vectors.readFromFile(args[0]);
		Distance.printAverageVectorsForLabeledSentences(vectors,args[1]);
	}
}
