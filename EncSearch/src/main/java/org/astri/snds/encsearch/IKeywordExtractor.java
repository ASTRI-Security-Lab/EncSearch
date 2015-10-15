package org.astri.snds.encsearch;

import java.io.IOException;

public interface IKeywordExtractor {

	void extractFromDocument(String docName, String contents) throws IOException;

}