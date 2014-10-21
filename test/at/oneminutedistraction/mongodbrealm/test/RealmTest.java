/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm.test;

import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static at.oneminutedistraction.mongodbrealm.Constants.*;
import at.oneminutedistraction.mongodbrealm.MongoDBRealm;
import at.oneminutedistraction.mongodbrealm.MongoDBRealmException;
import at.oneminutedistraction.mongodbrealm.SHA256PasswordManager;
import at.oneminutedistraction.mongodbrealm.UserCollection;
import at.oneminutedistraction.mongodbrealm.spi.PasswordManager;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.security.auth.login.LoginException;

/**
 *
 * @author cmlee
 */
public class RealmTest {
	
	private static Properties props;
	
	private static MongoDBRealm realm;
	
	private static PasswordManager mgr = null;
	private static MongoClient mongoClient = null;
	private static DB db = null;	
	private static DBCollection collection = null;

	public UserCollection users = null;
	
	public RealmTest() {
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		props = new Properties();
		props.setProperty(PROP_SERVER, "localhost");
		props.setProperty(PROP_DB_NAME, "myusers");
		props.setProperty(PROP_COLLECTION_NAME, "mongousers");
		
		realm = new MongoDBRealm();			
		
		mgr = new SHA256PasswordManager();
		mgr.init(props);		
		mongoClient = new MongoClient("localhost");
		db = mongoClient.getDB(props.getProperty(PROP_DB_NAME));
		collection = db.getCollection(props.getProperty(PROP_COLLECTION_NAME));	
	}
	
	@AfterClass
	public static void tearDownClass() {
		db.dropDatabase();
		mongoClient.close();
	}
	
	@Before
	public void setUp() {
		users = new UserCollection(mgr, collection);		
	}
	
	@After
	public void tearDown() {
		try {
			users.remove(UserCollectionTest.USER);
		} catch (MongoDBRealmException ex) { }
	}
	
	 @Test(expected = LoginException.class)
	 public void mongoRealm() throws LoginException {
		 
		 String[] groups = null;
		 System.out.println(">>> mongo realm: " + props);
		 try {
			realm.init(props);
		 } catch (BadRealmException | NoSuchRealmException ex) {
			 assertFalse("Realm initialization: " + ex.getMessage(), true);
			 return;
		 }
		 
		 users.insert(UserCollectionTest.USER, UserCollectionTest.PASSWORD
				 , UserCollectionTest.GROUPS);
		 
		 try {
			groups = realm.authenticate(UserCollectionTest.USER
					, UserCollectionTest.PASSWORD);
		 } catch (LoginException ex) {
			 fail("realm.authenticate exception: " + ex.getMessage());
			 return;
		 }
		 
		 assertArrayEquals("Group fail", UserCollectionTest.GROUPS
				 , groups);
		 
		 Enumeration<String> grpEnum = null;
		 try {
			grpEnum = realm.getGroupNames(UserCollectionTest.USER);
		 } catch (InvalidOperationException | NoSuchUserException ex) {
			 fail("get group names: " + ex.getMessage());
			 return;
		 }
		 List<String> grpList = new LinkedList<>();
		 while (grpEnum.hasMoreElements())
			 grpList.add(grpEnum.nextElement());
		 assertArrayEquals(groups, grpList.toArray(new String[]{}));
		 
		 try {
			realm.authenticate(UserCollectionTest.USER, "incorrect password");		 
		 } finally {
			 users.remove(UserCollectionTest.USER);
		 }
	 }
}
