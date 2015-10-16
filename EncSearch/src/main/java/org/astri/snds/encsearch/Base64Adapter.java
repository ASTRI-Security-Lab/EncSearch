package org.astri.snds.encsearch;
import java.util.Base64;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Base64Adapter extends XmlAdapter<String, byte[]> {

	public static Base64.Decoder dec = Base64.getUrlDecoder();
	public static Base64.Encoder enc = Base64.getUrlEncoder();
	
    @Override
    public String marshal(byte[] v) throws Exception {
        return enc.encodeToString(v);
    }

    @Override
    public byte[] unmarshal(String v) throws Exception {
    	return dec.decode(v);
    }

}
