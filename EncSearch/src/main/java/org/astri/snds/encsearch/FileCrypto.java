package org.astri.snds.encsearch;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.oxm.MediaType;

/** 
 * Does the work of encrypting or decrypting files.
 * It should be reused to encrypt multiple files.
*/
public class FileCrypto implements Destroyable {
	private static final String CR_ALG_HMAC = "HmacSHA256";
	private static final String CR_ALG_AES = "AES/CBC/PKCS5Padding";
	public static final String EXT_HEADER = ".ench";
	public static final String EXT_DATA = ".encd";
	private final int CR_ITERATIONS = 32000;   // TODO: more?
	private final int CR_KEY_LENGTH = 16;
	private final int CR_RAWKEY_LENGTH = CR_KEY_LENGTH * 8 * 4;  // encrypt, mac, name, keywords
	private final int CR_SALT_LENGTH = 20;
	
	private byte[] salt;
	private SecureRandom rnd = null;
	private SecretKeySpec encKey = null, hmacKey = null, nameKey = null;
	private byte[] kwKey = null;
	private JAXBContext jaxb;
	private Path outDir;

	// TODO: having password in a String is not so nice
	public FileCrypto(String password, Path outDir_) {
		this(password, null, outDir_);
	}
	
	public FileCrypto(String password, byte[] salt_, Path outDir_) {
		// this will need to be cleaned
		byte[] key = null;
		byte[] encKeyBytes = new byte[CR_KEY_LENGTH];
		byte[] hmacKeyBytes = new byte[CR_KEY_LENGTH];
		byte[] nameKeyBytes = new byte[CR_KEY_LENGTH];

		try {
			// init general purpose objects
			jaxb = JAXBContext.newInstance(CryptoHeader.class);
			outDir = outDir_;
			outDir.toFile().mkdirs();   // ensure output dir exists
			rnd = new SecureRandom();
			
			// salt can be either reused when decrypting or generated fresh for encryption
			if (salt_ == null) {
				salt = new byte[CR_SALT_LENGTH];
				rnd.nextBytes(salt);
			} else {
				salt = salt_;
			}


			PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, CR_ITERATIONS, CR_RAWKEY_LENGTH);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			key = keyFactory.generateSecret(keySpec).getEncoded();
			kwKey = new byte[CR_KEY_LENGTH];
			System.arraycopy(key, CR_KEY_LENGTH * 0, encKeyBytes, 0, CR_KEY_LENGTH);
			System.arraycopy(key, CR_KEY_LENGTH * 1, hmacKeyBytes, 0, CR_KEY_LENGTH);
			System.arraycopy(key, CR_KEY_LENGTH * 2, nameKeyBytes, 0, CR_KEY_LENGTH);
			System.arraycopy(key, CR_KEY_LENGTH * 3, kwKey, 0, CR_KEY_LENGTH);
			
			encKey = new SecretKeySpec(encKeyBytes, "AES");
			hmacKey = new SecretKeySpec(hmacKeyBytes, CR_ALG_HMAC);
			nameKey = new SecretKeySpec(nameKeyBytes, "AES");
			
		} catch (JAXBException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);

		} finally {
			destroyBytes(key);
			destroyBytes(encKeyBytes);
			destroyBytes(hmacKeyBytes);
			destroyBytes(nameKeyBytes);
		}
	}
	
	public void onFileFound(Path file) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IOException, JAXBException, IllegalBlockSizeException, BadPaddingException {
		// encrypt file name
		CryptoHeader header = prepareHeader();
		String encryptedName = encryptName(file, header);
		Path dataPath = getEncDataPath(encryptedName);
		Path headerPath = getEncHeaderPath(encryptedName);
		
		// TODO: some way to know if file has already been encrypted or not
		if (dataPath.toFile().exists() && headerPath.toFile().exists()) return;

		// if not, encrypt
		encryptFile(header, file, dataPath);

		// and save header
		OutputStream headerOut = null;
		headerOut = new FileOutputStream(headerPath.toFile());
		Marshaller marshaller = jaxb.createMarshaller();
        marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
        marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
		marshaller.marshal(header, headerOut);
		if (headerOut != null) headerOut.close();
	}

	private CryptoHeader prepareHeader() {
		CryptoHeader header = new CryptoHeader();
		header.iv = new byte[16];
		rnd.nextBytes(header.iv);
		return header;
	}
	
	private String encryptName(Path file, CryptoHeader header) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		System.out.println(String.format("encrypting name [%s] encKey [%s]",
				file.toString(),
				Base64Adapter.enc.encodeToString(encKey.getEncoded()) ));
		System.out.println(String.format("  hmacKey [%s] iv [%s]",
				Base64Adapter.enc.encodeToString(hmacKey.getEncoded()),
				Base64Adapter.enc.encodeToString(header.iv) ));

		// encrypt file name, using same IV but a different key
		Cipher cipher = Cipher.getInstance(CR_ALG_AES);
		cipher.init(Cipher.ENCRYPT_MODE, nameKey, new IvParameterSpec(header.iv));
		
		// use the same HMAC key
		Mac hmac = Mac.getInstance(CR_ALG_HMAC);
		hmac.init(hmacKey);

		byte[] nameBytes = file.toString().getBytes("UTF-8");
		byte[] crBytes = cipher.doFinal(nameBytes);
		header.name_hmac = hmac.doFinal(crBytes);

		String result = Base64Adapter.enc.encodeToString(crBytes);
		System.out.println(String.format("  result [%s]",
				result));
		return result;
	}

	private String decryptName(String filename, CryptoHeader header) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, BadMacException {
		String filenameOnly = com.google.common.io.Files.getNameWithoutExtension(filename);
		/*
		System.out.println(String.format("decrypting name [%s] encKey [%s]",
				filenameOnly,
				Base64Adapter.enc.encodeToString(encKey.getEncoded()) ));
		System.out.println(String.format("  hmacKey [%s] iv [%s]",
				Base64Adapter.enc.encodeToString(hmacKey.getEncoded()),
				Base64Adapter.enc.encodeToString(header.iv) ));
		*/

		Cipher cipher = Cipher.getInstance(CR_ALG_AES);
		cipher.init(Cipher.DECRYPT_MODE, nameKey, new IvParameterSpec(header.iv));

		Mac hmac = Mac.getInstance(CR_ALG_HMAC);
		hmac.init(hmacKey);
		
		byte[] nameRaw = Base64Adapter.dec.decode(filenameOnly);
		byte[] actualMac = hmac.doFinal(nameRaw);
		
		// this is supposed to be constant time
		if (!MessageDigest.isEqual(actualMac, header.name_hmac)) throw new BadMacException("File name " + filename);

		byte[] actualNameBytes = cipher.doFinal(nameRaw);
		String result = new String(actualNameBytes, "UTF-8");
		//System.out.println(String.format("  result [%s]", result));
		return result;
	}

	public void decryptFile(Path file) throws IOException, JAXBException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, BadMacException, IllegalBlockSizeException, BadPaddingException {
		if (!file.toString().endsWith(EXT_DATA)) return;
		
		Path dataPath = file;
		String filenameOnly = com.google.common.io.Files.getNameWithoutExtension(file.toString());
		Path headerPath = file.resolveSibling(filenameOnly + EXT_HEADER);
		
		// load the header
		StreamSource headerIn = null;
		headerIn = new StreamSource(headerPath.toFile());
		
		Unmarshaller unm = jaxb.createUnmarshaller();
        unm.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
        unm.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
        CryptoHeader header = (CryptoHeader) unm.unmarshal(headerIn, CryptoHeader.class).getValue();
		
		// setup decryption
		Cipher cipher = Cipher.getInstance(CR_ALG_AES);
		cipher.init(Cipher.DECRYPT_MODE, encKey, new IvParameterSpec(header.iv));

		Mac hmac = Mac.getInstance(CR_ALG_HMAC);
		hmac.init(hmacKey);
		
		String safeOutFilename = decryptName(filenameOnly, header).replaceAll("\\.\\.\\/", "");
		Path outPath = outDir.resolve(safeOutFilename);
		outPath.toFile().getParentFile().mkdirs();

		FileInputStream inf = null; 
		FileOutputStream outf = null; 
		CipherOutputStream outCr = null; 
		MacInputStream inMac = null;
		try {
			inf = new FileInputStream(dataPath.toFile());
			inMac = new MacInputStream(inf, hmac);
			outf = new FileOutputStream(outPath.toFile());
			outCr = new CipherOutputStream(outf, cipher);

			copy(inMac, outCr);
			
			byte[] actualMac = hmac.doFinal();
			if (MessageDigest.isEqual(actualMac, header.hmac)) throw new BadMacException("File contents of " + file.toString());
			
		} finally {
			if (outCr != null) outCr.close();
			if (inMac != null) inMac.close();
			if (outf != null) outf.close();
			if (inf != null) inf.close();
		}
	}


	private Path getEncHeaderPath(String fileName) {
		return outDir.resolve(fileName + EXT_HEADER);
	}

	private Path getEncDataPath(String fileName) {
		return outDir.resolve(fileName + EXT_DATA);
	}
	
	private void encryptFile(CryptoHeader header, Path inPath, Path outPath) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher cipher = Cipher.getInstance(CR_ALG_AES);
		cipher.init(Cipher.ENCRYPT_MODE, encKey, new IvParameterSpec(header.iv));
		
		Mac hmac = Mac.getInstance(CR_ALG_HMAC);
		hmac.init(hmacKey);
		
		header.version = 1;
		header.salt = salt;
		header.iterations = CR_ITERATIONS;

		FileInputStream inf = null; 
		FileOutputStream outf = null; 
		CipherOutputStream outCr = null; 
		MacOutputStream outMac = null;
		try {
			inf = new FileInputStream(inPath.toFile());
			outf = new FileOutputStream(outPath.toFile());
			outMac = new MacOutputStream(outf, hmac);
			outCr = new CipherOutputStream(outMac, cipher);
			
			copy(inf, outCr);
			header.hmac = hmac.doFinal();
		} finally {
			if (outCr != null) outCr.close();
			if (outMac != null) outMac.close();
			if (outf != null) outf.close();
			if (inf != null) inf.close();
		}
	}
	
	private void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[32 * 1024];
		int len;
		while ((len = in.read(buffer)) != -1) {
		    out.write(buffer, 0, len);
		}
	}
	
	public static void destroyBytes(byte[] key) {
		if (key == null) return;
		for (int i = 0; i < key.length; i++) key[i] = 0;
	}
	
	// TODO: is not actually called anywhere
	public void destroy() throws DestroyFailedException {
		if (encKey != null) encKey.destroy();
		if (hmacKey != null) hmacKey.destroy();
		if (nameKey != null) nameKey.destroy();
		destroyBytes(kwKey);
	}

	public byte[] getKwKey() {
		return kwKey;
	}
	
	public byte[] getSalt() {
		return salt;
	}
	
}
