package org.astri.snds.encsearch;

import java.net.URL;
import java.nio.file.Paths;

public class Main {
	
	public static void main(String[] args) {
		try {
			byte[] rawkey = new byte[] {11, 22, 33, 44, 55, 66, 77, 88, 99, 0};
			URL searchHost = new URL("http://localhost:8080/EncSearchServer/rest");

			// build index
			KeywordExtractor kwExtractor = new KeywordExtractor(rawkey);
			DocManager mgr = new DocManager(Paths.get("../data"), kwExtractor);
			mgr.search();
			
			IndexUploader up = new IndexUploader(searchHost);
			up.upload(kwExtractor.index);
			
			// query
			KeywordExtractor queryEx = new KeywordExtractor(rawkey);
			queryEx.extractFromDocument("query", "bank China");
			
			RemoteQuery query = new RemoteQuery(searchHost);
			query.searchKeywords(queryEx.index.keySet()).forEach(i -> System.out.println(i));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
