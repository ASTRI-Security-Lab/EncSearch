package org.astri.snds.encsearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

public class Main {
	
	private static String USER_HOME = System.getProperty("user.home");
	private static Path CONFIG_FILE = Paths.get(USER_HOME, ".org.astri.snds.encsearch", "config.json");
	
	// TODO: a String ideally should not be used for a password since it can't be cleared from memory
	private static String readPassword() throws IOException {
		System.out.println("Please type your password");

		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		// not sure if this needs closing ... it would close System.in as well
		String pwd = scanner.nextLine();
		if (pwd.isEmpty()) throw new IllegalArgumentException("Password cannot be empty.");
		return pwd;

		/*
		ArrayList<Byte> pw = new ArrayList<>();
		while (true) {
			int b = System.in.
			if ((b == 13) || (b == 10)) break;
		}
		
		byte[] res = new byte[pw.size()];
		for (int i = 0; i < pw.size(); i++) {
			res[i] = pw.get(i);
			pw.set(0,  (byte) 0);
			System.out.print(res[i]);
		}
		return res;
		*/
	}
	
	private static void addFiles(String[] args) throws Exception {
		ConfigParams cfg = ConfigParams.read(CONFIG_FILE.toString());

		// just add everything
		String pwd = readPassword();
		FileCrypto crypt = new FileCrypto(pwd, cfg.salt, Paths.get(cfg.encrypted_path));
		try {
			KeywordExtractor kwExtractor = new KeywordExtractor(crypt.getKwKey());
			// Modified DocManager to take additional host parameter for encryption headers storing in DB
			DocManager mgr = new DocManager(Paths.get(cfg.docs_path), kwExtractor, crypt, cfg.search_server);
			mgr.search();

			IndexUploader up = new IndexUploader(cfg.search_server, cfg.username, cfg.salt);
			up.upload(kwExtractor.index);
		} finally {
			//FileCrypto.destroyBytes(pwd);
			crypt.destroy();
		}
	}
	
	private static void query(String[] args) throws Exception {
		ConfigParams cfg = ConfigParams.read(CONFIG_FILE.toString());
		String pwd = readPassword();
		FileCrypto crypt = new FileCrypto(pwd, cfg.salt, Paths.get(cfg.decrypt_path));

		try {
			String q = args[1];
			
			KeywordExtractor queryEx = new KeywordExtractor(crypt.getKwKey());
			queryEx.extractFromDocument("query", q);
			
			RemoteQuery query = new RemoteQuery(cfg.search_server);
			List<String> docs = query.searchKeywords(queryEx.index.keySet());
			
			System.out.println("Found documents:");
			docs.forEach(i -> System.out.println(i));
			
			// also decrypt each
			docs.forEach(i -> {
				try {
					crypt.decryptFile(Paths.get(cfg.encrypted_path, i + FileCrypto.EXT_DATA));
				} catch (Exception e) {
					System.err.println("Error decrypting file" + i);
				}}
			);
		} finally {
			//FileCrypto.destroyBytes(pwd);
			crypt.destroy();
		}
	}
	
	private static void init() throws MalformedURLException, FileNotFoundException, JAXBException {
		ConfigParams c = new ConfigParams();
		c.docs_path = Paths.get(USER_HOME, "Documents").toString();
		c.salt = new byte[16];
		SecureRandom rnd = new SecureRandom();
		rnd.nextBytes(c.salt);
		c.done = new ArrayList<String>();
		c.encrypted_path = Paths.get(USER_HOME, ".org.astri.snds.encsearch", "encrypted").toString();
		c.decrypt_path = Paths.get(USER_HOME, ".org.astri.snds.encsearch", "decrypted").toString();
		c.search_server = new URL("http://localhost:8080/EncSearchServer/rest");
		c.username = genRandomWord(rnd);
		
		CONFIG_FILE.toFile().getParentFile().mkdirs();
		c.write(CONFIG_FILE.toString());
		
		// Andrew Hon (Feb 16, 2016)
		// Display username for mobile app setup
		System.out.println("Username created: " + c.username);
	}
	
	private static String genRandomWord(Random rnd) {
		final String[] groups = new String[] { "bflmpsvz", "aeiouy" };
		
		StringBuilder res = new StringBuilder(8);
		for (int i = 0; i < res.capacity(); i++) {
			String g = groups[i % 2];
			res.append(g.charAt(rnd.nextInt(g.length())));
		}
		return res.toString();
	}

	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				System.out.println("init | add | q");
				return;
			}
			String op = args[0];
			
			if (op.equals("add")) {
				addFiles(args);
				
			} else if (op.equals("q")) {
				query(args);

			} else if (op.equals("init")) {
				init();
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
