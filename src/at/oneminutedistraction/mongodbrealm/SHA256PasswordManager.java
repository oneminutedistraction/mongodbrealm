/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm;

import at.oneminutedistraction.mongodbrealm.spi.PasswordManager;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cmlee
 */
public class SHA256PasswordManager implements PasswordManager {
	
	private static final Logger logger = Logger.getLogger(SHA256PasswordManager.class.getName());
	
	private static final String SHA256 = "SHA-256";
	private static final String PROP_SHA_ALGO  = Constants.PROP_PASSWORD_MANAGER + ".algorithm";

	private String digestAlgo = null;

	@Override
	public void init(Properties prop) { 
		digestAlgo = prop.getProperty(PROP_SHA_ALGO, SHA256);
		try {
			MessageDigest.getInstance(digestAlgo);
		} catch (NoSuchAlgorithmException ex) {
			logger.log(Level.SEVERE, "No algorithm found: {0}. Falling back to SHA-256", digestAlgo);
			digestAlgo = SHA256;
		}
	}

	@Override
	public String encrypt(String password) {
		//From http://stackoverflow.com/questions/5531455/how-to-encode-some-string-with-sha256-in-java
		try {
			MessageDigest md = MessageDigest.getInstance(digestAlgo);
			md.reset();
			byte[] hash = md.digest(password.getBytes("UTF-8"));
			StringBuilder buffer = new StringBuilder();
			for (byte b: hash) {
				String hex = Integer.toHexString(0xff & b);
				if (1 == hex.length())
					buffer.append('0');
				buffer.append(hex);
			}
			return (buffer.toString());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			logger.log(Level.SEVERE, "Encrypting password", ex);
		}
		return (null);
	}

	@Override
	public boolean isValid(String encrypted, String password) {
		return (encrypted.equals(encrypt(password)));
	}
	
	public static void main(String... args) throws Throwable {
		SHA256PasswordManager mgr = new SHA256PasswordManager();
		mgr.init(null);
		String encrype = mgr.encrypt("hello world");
		System.out.println(">> " + encrype);
		System.out.println("hello world>> equals:" + mgr.isValid(encrype, "hello world"));
		System.out.println("not hello world>> equals:" + mgr.isValid(encrype, "not hello world"));
	}
}
