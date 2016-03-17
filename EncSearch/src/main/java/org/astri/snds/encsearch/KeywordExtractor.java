package org.astri.snds.encsearch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.bouncycastle.util.encoders.Hex;


public class KeywordExtractor implements IKeywordExtractor {
	
	private Mac mac;
	private Encoder urlEncoder = Base64.getUrlEncoder();
	protected final String ENCODING = "UTF-8";
	
	// keyword -> (docName -> number of occurrence of that word in that document)
	public HashMap<String, HashMap<String, Integer>> index = new HashMap<>();
	
	
	public KeywordExtractor(byte[] rawkey) throws NoSuchAlgorithmException, InvalidKeyException {
		final String HMAC_ALG = "HmacSHA256";

		//System.out.print("hmac key(hex)=");
		//System.out.println(Hex.toHexString( rawkey ));

		mac = Mac.getInstance(HMAC_ALG);
		SecretKeySpec keyspec = new SecretKeySpec(rawkey, HMAC_ALG);
		mac.init(keyspec);
	}

	/* (non-Javadoc)
	 * @see org.astri.snds.encsearch.IKeywordExtractor#extractFromDocument(java.lang.String, java.lang.String)
	 */
	@Override
	public void extractFromDocument(String docName, String contents) throws IOException {
		
		Analyzer analyzer = new CustomAnalyzer();
		TokenStream ts = analyzer.tokenStream("body", contents);
		//OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);

		try {
			ts.reset(); // Resets this stream to the beginning. (Required)
			while (ts.incrementToken()) {
				String term = ts.getAttribute(CharTermAttribute.class).toString();
				
				addKeyword(docName, term);
			}
			ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
		} finally {
			ts.close(); // Release resources associated with this stream.
			analyzer.close();
		}
	}
	
	public void addKeyword(String docName, String term) throws IllegalStateException, UnsupportedEncodingException {
		/*
		System.out.print(docName);
		System.out.print(": ");
		System.out.println(term);
		*/

		byte[] termBytes = term.getBytes(ENCODING);
		byte[] macBytes = mac.doFinal(termBytes);

		/*
		System.out.print("keyword(hex)=");
		System.out.print(Hex.toHexString(termBytes));
		System.out.print(" hmac=");
		System.out.println(Hex.toHexString(macBytes));
		*/

		String hmacB64 = urlEncoder.encodeToString(macBytes);
		
		// increment occurrences
		HashMap<String, Integer> docs = null;
		if (index.containsKey(hmacB64)) {
			docs = index.get(hmacB64);
		} else {
			docs = new HashMap<String, Integer>();
			index.put(hmacB64, docs);
		}
		
		int cnt = 0;
		if (docs.containsKey(docName)) {
			cnt = docs.get(docName);
		}
		docs.put(docName, cnt + 1);
	}
	
}
