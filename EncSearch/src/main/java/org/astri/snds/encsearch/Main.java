package org.astri.snds.encsearch;

import java.net.URL;
import java.nio.file.Paths;

public class Main {
	
	public static void main(String[] args) {
		try {
			URL searchHost = new URL("http://localhost:8080/EncSearchServer/rest");

			// build index
			FileCrypto crypt = new FileCrypto("abcd", Paths.get("../data/encrypted"));
			KeywordExtractor kwExtractor = new KeywordExtractor(crypt.getKwKey());
			DocManager mgr = new DocManager(Paths.get("../data"), kwExtractor, crypt);
			mgr.search();
			
			IndexUploader up = new IndexUploader(searchHost);
			up.upload(kwExtractor.index);
			
			// query
			KeywordExtractor queryEx = new KeywordExtractor(crypt.getKwKey());
			queryEx.extractFromDocument("query", "bank China");
			
			RemoteQuery query = new RemoteQuery(searchHost);
			query.searchKeywords(queryEx.index.keySet()).forEach(i -> System.out.println(i));
			
			crypt.destroy();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
