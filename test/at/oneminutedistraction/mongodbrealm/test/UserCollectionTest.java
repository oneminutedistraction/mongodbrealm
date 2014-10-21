/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm.test;

import at.oneminutedistraction.mongodbrealm.Constants;
import at.oneminutedistraction.mongodbrealm.spi.PasswordManager;
import at.oneminutedistraction.mongodbrealm.SHA256PasswordManager;
import at.oneminutedistraction.mongodbrealm.UserCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.util.Properties;
import javax.security.auth.login.LoginException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author cmlee
 */
public class UserCollectionTest {

	public static final String USER = "fred";
	public static final String PASSWORD = "yabadabadoo";
	public static final String[] GROUPS = {"bedrock", "slate", "rockhead"};

	//From http://www.xorbin.com/tools/sha256-hash-calculator
	public static final String SHA256_PASSWORD = "999f36dd648c74f52972745be2ee94c4b53c48639debbf310bfd5d5fc84ee4f6";

	private static PasswordManager mgr = null;
	private static MongoClient mongoClient = null;
	private static DB db = null;
	private static DBCollection collection = null;

	public UserCollection users = null;

	@BeforeClass
	public static void setUpClass() throws Exception {
		System.out.println(">>> setup class");
		mgr = new SHA256PasswordManager();
		mgr.init(new Properties());

		mongoClient = new MongoClient("localhost");
		db = mongoClient.getDB("testusers");
		collection = db.getCollection("users");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		System.out.println(">>> teardown class");
		db.dropDatabase();
		mongoClient.close();
	}

	@Before
	public void setup() {
		if (null == users)
			users = new UserCollection(mgr, collection);		
	}

	@Test
	public void passwordDigest() {
		assertTrue("SHA-256", mgr.isValid(SHA256_PASSWORD, PASSWORD));
	}

	@Test
	public void addUser() {
		users.insert(USER, PASSWORD, GROUPS);
		BasicDBObject fromUsers = users.find(USER);
		assertNotNull("Newly inserted user", fromUsers);
		
		BasicDBObject fromDB = (BasicDBObject)collection.findOne(
				new BasicDBObject(Constants.ATTR_USERNAME, USER));

		assertNotNull("Selecting from collection", fromDB);

		assertEquals("Finding users", fromDB, fromUsers);				

		users.remove(fromUsers);
	}
	
	@Test(expected = LoginException.class)
	public void validateUser() throws LoginException {
		users.insert(USER, PASSWORD, GROUPS);
		
		String[] groups = null;
		try {
			groups = users.authenticate(USER, PASSWORD);
		} catch (LoginException ex) {
			ex.printStackTrace();
			fail("Authentication failed: " + ex.getMessage());
		}
		
		assertArrayEquals("Validating groups", GROUPS, groups);
		
		try {
			users.authenticate(USER, "wrong password");
		} finally {		
			users.remove(USER);
		}
	}
}
