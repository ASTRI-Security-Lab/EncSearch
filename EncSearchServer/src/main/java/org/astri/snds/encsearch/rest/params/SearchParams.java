package org.astri.snds.encsearch.rest.params;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SearchParams {
	
	public List<String> keywords;

	public SearchParams() { }
	public SearchParams(List<String> keywords_) { keywords = keywords_; }
}
