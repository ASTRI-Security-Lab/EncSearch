package org.astri.snds.encsearch.rest.params;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

@XmlRootElement
public class IndexAddParams {
	
	public static class OccurenceParams {
		@XmlTransient
		public String doc_id;
		
		@XmlValue
		public Integer count;
		
		public OccurenceParams() { }
	}
	
	public static class KeywordParams {
		@XmlTransient
		public String keyword;
		
		@XmlValue
		@XmlVariableNode("doc_id")
		public List<OccurenceParams> value;
		
		public KeywordParams() {}
	}
	
	public List<String> doc_ids;
	
	@XmlPath("keywords")
	@XmlVariableNode("keyword")
	public List<KeywordParams> keywords;

	public IndexAddParams() {}
}
