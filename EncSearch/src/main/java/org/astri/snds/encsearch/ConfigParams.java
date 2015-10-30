package org.astri.snds.encsearch;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.oxm.MediaType;

@XmlRootElement
public class ConfigParams {
	
	public URL search_server;
	public String docs_path;
	public String encrypted_path;
	public String decrypt_path;
	public List<String> done;
	public String username;
	
	
	@XmlJavaTypeAdapter(Base64Adapter.class)
	public byte[] salt;
	
	private static JAXBContext jaxb = null;
	
	private static JAXBContext getJaxb() throws JAXBException {
		if (jaxb == null) jaxb = JAXBContext.newInstance(ConfigParams.class);
		return jaxb;
	}

	public static ConfigParams read(String filename) throws JAXBException {
		Unmarshaller unm = getJaxb().createUnmarshaller();
		unm.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
		unm.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);

		StreamSource inf = new StreamSource(filename);
		return (ConfigParams) unm.unmarshal(inf, ConfigParams.class).getValue();
	}
	
	public void write(String filename) throws FileNotFoundException, JAXBException {
		FileOutputStream outf = new FileOutputStream(filename);
		Marshaller marshaller = getJaxb().createMarshaller();
        marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
        marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
		marshaller.marshal(this, outf);
	}

}
