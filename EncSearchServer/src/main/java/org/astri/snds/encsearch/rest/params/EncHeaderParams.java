package org.astri.snds.encsearch.rest.params;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

@XmlRootElement
public class EncHeaderParams {
	
	public String docName;
	public String iv;
	public int ver;
	public String salt;
	public int iterations;
	public String hmac;
	public String name_hmac;
	
	// empty constructor needed for deserialization by JAXB
	public EncHeaderParams() {} 
	
}