package word2vec;
import java.io.IOException;
import java.io.Writer;
import java.util.*;



import org.ivdnt.util.StringUtils;
import org.ivdnt.util.TabSeparatedFile;

public class WordInContext 
{
	public List<String> sentence;
	public int focusPosition;
	public String trueSenseId;
	public float[] vector;
	public String lemma;
	public String quotationId = null;
	static String[] basicFields = { "word", "sense", "sentence"};
	static String[] fieldsWithId =  { "id", "word", "sense", "sentence"};
	public WordInContext(List<String> l, int f)
	{
		this.sentence=l;
		this.focusPosition = f;
	}

	public String[] contextWindow(int size)
	{
		List<String> context = new ArrayList<String>();
		for (int i=focusPosition-1; i > 0 && focusPosition -i <= size; i--)
		{
			context.add(sentence.get(i));
		}
		for (int i=focusPosition+1; i < sentence.size() && i - focusPosition <= size; i++)
			context.add(sentence.get(i));
		return context.toArray(new String[context.size()]);
	}

	public boolean hasFocus()
	{
		return this.focusPosition >= 0;
	}

	public static List<WordInContext> readContextsFromTabSeparatedFile(String fileName) throws IOException
	{
		return readContextsFromTabSeparatedFile(fileName, false, true);

	}
	public static List<WordInContext> readContextsFromTabSeparatedFile(String fileName, boolean hasQuotationIds, boolean hasFocus) throws IOException
	{
		String[] fields = hasQuotationIds?fieldsWithId:basicFields ;
		TabSeparatedFile ts = new TabSeparatedFile(fileName, fields);

		List<String> sentences = ts.getColumn("sentence");
		String word = ts.getColumn("word").get(0);

		List<WordInContext> sentenceList = new ArrayList<WordInContext>();
		List<String> senseIds = ts.getColumn("sense");
		List<String> lemmata = ts.getColumn("word");
		List<String> quotationIds = hasQuotationIds?ts.getColumn("id"):null;
		
		for (int i=0; i < sentences.size(); i++)
		{
			String s = sentences.get(i);
			if (s == null) continue;
			
			List<String> words = new ArrayList<String>();
			int k=0;
			int focusPosition=-1;
			for (String w: s.split("\\s+"))
			{
				if (hasFocus)
				{
					if (w.contains("/"))
					{
						focusPosition = k;
					}
					w = w.replaceAll("/[^ ]*",  "");
				}

				w = StringUtils.stripPunctuation(w);
				w = w.trim();
				if (w.length() > 0)
				{
					k++;
					words.add(w);
				}
			}
		
			WordInContext wic = new WordInContext(words, focusPosition);
			wic.lemma = lemmata.get(i);
			wic.trueSenseId = senseIds.get(i);
			wic.quotationId = hasQuotationIds?quotationIds.get(i):null;
			
			
			sentenceList.add(wic);
		}
		return sentenceList;
	}

	public String toString()
	{
		return trueSenseId + " " + lemma + " "  + sentence;
	}

	public static List<WordInContext> makeContexts(List<String> sentences)
	{
		List<WordInContext> sentenceList = new ArrayList<WordInContext>();
		for (int i=0; i < sentences.size(); i++)
		{
			String s = sentences.get(i);
			//System.err.println(s);
			if (s == null)
				continue;
			List<String> words = new ArrayList<String>();
			int k=0;
			int focusPosition=-1;
			for (String w: s.split("\\s+"))
			{
				if (w.contains("/"))
				{
					focusPosition = k;
				}
				w = w.replaceAll("/[^ ]*",  "");
				w = StringUtils.stripPunctuation(w);
				w = w.trim();
				if (w.length() > 0)
				{
					k++;
					words.add(w);
				}
			}
			//System.err.println("Words:" + words);
			WordInContext wic = new WordInContext(words, focusPosition);
			sentenceList.add(wic);
		}
		return sentenceList;
	}
}
