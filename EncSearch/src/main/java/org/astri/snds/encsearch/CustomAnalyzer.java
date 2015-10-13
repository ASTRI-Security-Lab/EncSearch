package org.astri.snds.encsearch;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

public class CustomAnalyzer extends StopwordAnalyzerBase {

	public static final CharArraySet ENGLISH_STOP_WORDS_SET;

	static {
		List<String> stopWords = Arrays.asList(new String[] { "a", "an", "and", "are", "as", "at", "be", "but", "by",
				"for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "s", "that", "the",
				"their", "then", "there", "these", "they", "this", "to", "was", "will", "with" });

		CharArraySet stopSet = new CharArraySet(stopWords, false);
		ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
	}

	public CustomAnalyzer() {
		this(ENGLISH_STOP_WORDS_SET);
	}

	public CustomAnalyzer(CharArraySet stopWords) {
		super(stopWords);
	}

	public CustomAnalyzer(Path stopwordsFile) throws IOException {
		this(loadStopwordSet(stopwordsFile));
	}

	public CustomAnalyzer(Reader stopwords) throws IOException {
		this(loadStopwordSet(stopwords));
	}

	@Override
	protected TokenStreamComponents createComponents(String arg0) {
		Tokenizer source = new LowerCaseTokenizer();
		TokenStream filter = new StopFilter(source, this.stopwords);
		filter = new EnglishPossessiveFilter(filter);
		filter = new EnglishMinimalStemFilter(filter);

		return new Analyzer.TokenStreamComponents(source, filter);
	}

}
