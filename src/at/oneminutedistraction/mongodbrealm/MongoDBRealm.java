/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm;

import at.oneminutedistraction.mongodbrealm.spi.PasswordManager;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.sun.appserv.security.AppservRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;

import static at.oneminutedistraction.mongodbrealm.Constants.*;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author cmlee
 */

//@Service(name = Constants.SERVICE_NAME)
public class MongoDBRealm extends AppservRealm {	
	
	private static final Logger logger = Logger.getLogger(MongoDBRealm.class.getName());
	
	private MongoClient mongoClient = null;	
	private String collectionName = null;
	private PasswordManager passwordMgr = null;
	private String dbName = null;

	@Override
	public void init(Properties props) 
			throws BadRealmException, NoSuchRealmException {
		
		super.init(props);
		
		logger.log(Level.INFO, "MongoDBRealm init()");
				
		props.keySet().stream().forEach(key -> {
			logger.log(Level.INFO, "property: {0}: {1}",
					new Object[]{ key, props.getProperty((String)key)});
		});	

		//DB name
		dbName = props.getProperty(PROP_DB_NAME, DEFAULT_DB_NAME);
		
		//Collection name
		collectionName = props.getProperty(PROP_COLLECTION_NAME, DEFAULT_COLLECTION_NAME);
		logger.log(Level.INFO, "Collection name: {0}", collectionName);
		
		//Create MongoClientFactory
		String clientClass = props.getProperty(PROP_MONGO_CLIENT
				, DefaultMongoClient.class.getName());
		logger.log(Level.INFO, "MongoClient class: {0}", clientClass);
		Object obj = null;
		try {
			obj = Class.forName(clientClass).newInstance();
			if (!(obj instanceof at.oneminutedistraction.mongodbrealm.spi.MongoClientFactory))
				throw new BadRealmException(clientClass + " is not an instance of MongoClient interface");
		} catch (ClassNotFoundException | InstantiationException 
				| IllegalAccessException ex) {
			String msg = MessageFormat.format(
					"Cannot instantiate MongoClient class: {0}", clientClass);
			logger.log(Level.SEVERE, msg);
			throw new BadRealmException(msg, ex);
		}
		at.oneminutedistraction.mongodbrealm.spi.MongoClientFactory mc = 
				(at.oneminutedistraction.mongodbrealm.spi.MongoClientFactory)obj;
		mc.init(props);
		try {
			mongoClient = mc.create();
		} catch (Exception ex) {
			String msg = MessageFormat.format("Creating MongoClient with {0}", clientClass);
			logger.log(Level.SEVERE, msg, ex);
			throw new BadRealmException(msg, ex);
		}
		
		//Create PasswordManager
		String passwd = props.getProperty(PROP_PASSWORD_MANAGER
				, SHA256PasswordManager.class.getName());
		logger.log(Level.INFO, "PasswordManager class: {0}", passwd);
		try {
			obj = Class.forName(passwd).newInstance();				
			if (!(obj instanceof PasswordManager))
				throw new BadRealmException(passwd + " is not an instance of PasswordManager");
			passwordMgr = (PasswordManager)obj;
		} catch (ClassNotFoundException | InstantiationException 
				| IllegalAccessException ex) {
			String msg = MessageFormat.format(
					"Cannot instantiate PasswordManager class: {0}", passwd);
			logger.log(Level.SEVERE, msg);
			throw new BadRealmException(msg, ex);
		}
		passwordMgr.init(props);
	}		

	public String[] authenticate(final String username, final String password) 
			throws LoginException {
		
		UserCollection collection = createUserCollection();

		return (collection.authenticate(username, password));
	}		

	@Override
	public String getAuthType() {
		return ("MongoDBRealm");
	}

	@Override
	public Enumeration getGroupNames(String username) 
			throws InvalidOperationException, NoSuchUserException {
		
		UserCollection collection = createUserCollection();
		String[] groups = collection.groups(username);
		if (null == groups)
			throw new NoSuchUserException(username + " does not exist");				
		return (Collections.enumeration(Arrays.asList(groups)));
	}
	
	private UserCollection createUserCollection() {
		DB db = mongoClient.getDB(dbName);
		DBCollection userCollection = db.getCollection(collectionName);
		return (new UserCollection(passwordMgr, userCollection));
	}
	
}
