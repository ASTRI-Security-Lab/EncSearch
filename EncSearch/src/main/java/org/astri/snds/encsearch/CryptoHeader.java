package org.astri.snds.encsearch;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class CryptoHeader {

	@XmlJavaTypeAdapter(Base64Adapter.class)
	public byte[] iv;
	public int version;
	
	@XmlJavaTypeAdapter(Base64Adapter.class)
	public byte[] salt;
	public int iterations;
	
	@XmlJavaTypeAdapter(Base64Adapter.class)
	public byte[] hmac;

	@XmlJavaTypeAdapter(Base64Adapter.class)
	public byte[] name_hmac;

}
