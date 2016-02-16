package org.astri.snds.encsearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.function.Consumer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/** Keeps track of documents in a directory */
public class DocManager implements Consumer<Path> {
	
	private Path homePath;
	private IKeywordExtractor kwExtractor;
	private FileCrypto encrypter;
	private URL host;
	
	// Take in additional host variable 
	public DocManager(Path folder, IKeywordExtractor kwExtractor_, FileCrypto encrypter_, URL host_) throws URISyntaxException {
		homePath = folder;
		kwExtractor = kwExtractor_;
		encrypter = encrypter_;
		host = host_;
	}
	
	public void search() throws IOException {
		Files.walk(homePath).forEach(this);
	}

	/// Found a document
	public void accept(Path t) {
		if (!Files.isRegularFile(t)) return;
		int ps = t.toString().lastIndexOf('.');
		if (ps <= 0) return;

		String ext = t.toString().substring(ps).toLowerCase();
		try {
			String encName = t.toString();
			if (encrypter != null) {
				if (!(ext.equals(FileCrypto.EXT_DATA) || ext.equals(FileCrypto.EXT_HEADER))) {
					//encName = encrypter.onFileFound(t); 
					encName = encrypter.onFileFound(t,host);
				}
			}

			if (kwExtractor != null) {
				if (ext.equals(".doc")) {
					acceptMsWord(t, encName);
				} else if (ext.equals(".docx")) {
					acceptMsWord(t, encName);
				} else if (ext.equals(".pdf")) {
					acceptPdf(t, encName);
				} else if (ext.equals(".txt")) {
					acceptPlain(encName, new String(Files.readAllBytes(t)));
				}
			}
			
		} catch (Exception e) {
			// TODO: probably more like skipping these files
			throw new RuntimeException(e);
		}
	}

	private void acceptPlain(String encName, String contents) throws IOException {
		kwExtractor.extractFromDocument(encName, contents);
	}

	private void acceptPdf(Path t, String encName) throws IOException {
		PDFTextStripper stripper = new PDFTextStripper();
		PDDocument document = PDDocument.load(t.toFile());
		try {
			String result = stripper.getText(document); 
			acceptPlain(encName, result);
		} finally {
			document.close();
		}
	}

	private void acceptMsWord(Path t, String encName) throws IOException, InvalidFormatException, OpenXML4JException, XmlException {
		InputStream inputStream = Files.newInputStream(t);
		POITextExtractor extractor = null;
		try {
			extractor = ExtractorFactory.createExtractor(t.toFile());
			String result = extractor.getText();
			acceptPlain(encName, result);
		} finally {
			inputStream.close();
			if (extractor != null) extractor.close();
		}
	}

}
