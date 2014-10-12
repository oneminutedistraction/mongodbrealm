/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm;

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
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author cmlee
 */

@Service(name = Constants.SERVICE_NAME)
public class MongoDBRealm extends AppservRealm {	
	
	private static final Logger logger = Logger.getLogger(MongoDBRealm.class.getName());	
	
	private MongoClient mongoClient = null;	
	private String collectionName = null;
	private PasswordManager passwordMgr = null;

	@Override
	protected void init(Properties props) 
			throws BadRealmException, NoSuchRealmException {
		
		super.init(props);
		
		logger.log(Level.INFO, "MongoDBRealm init()");
				
		props.keySet().stream().forEach(key -> {
			logger.log(Level.INFO, "property: {0}: {1}",
					new Object[]{ key, props.getProperty((String)key)});
		});	
		
		//Collection name
		collectionName = props.getProperty(Constants.PROP_COLLECTION_NAME, "users");
		logger.log(Level.INFO, "Collection name: {0}", collectionName);
		
		//Server list
		String servers = props.getProperty(Constants.PROP_SERVER, "localhost");
		List<ServerAddress> serverList = new LinkedList<>();
		for (String s: servers.trim().replaceAll(" +", " ").split(" ")) {			
			String[] hostPort = s.split(":");
			ServerAddress serverAddress = null;		
			try {
				switch (hostPort.length) {					
					case 1:					
						serverAddress = new ServerAddress(hostPort[0]);
						break;
					case 2:
						serverAddress = new ServerAddress(hostPort[0]
								, Integer.parseInt(hostPort[1]));
						break;
					default:
						continue;
					
				}
			} catch (UnknownHostException ex) {
				logger.log(Level.WARNING, "Ignoring unknown host: {0}", hostPort[0]);
				continue;
			} catch (NumberFormatException ex) {
				logger.log(Level.WARNING, "Ignoring port number error: {0}", s);
				continue;
			}			
			serverList.add(serverAddress);
		}
				
		if (serverList.size() > 0) {
			logger.log(Level.INFO, "MongoDB replicas: {0}", serverList);
			mongoClient = new MongoClient(serverList);
		} else {
			logger.log(Level.INFO, "MongoDB instance at localhost");
			try {
				mongoClient = new MongoClient("localhost");
			} catch (UnknownHostException ex) { 
				//Seriously?
			}
		}
		
		String passwd = props.getProperty(Constants.PROP_PASSWORD_MANAGER
				, SHA256PasswordManager.class.getName());
		logger.log(Level.INFO, "PasswordManager class: {0}", passwd);
		try {
			Object obj = Class.forName(passwd);
			if (!(obj instanceof PasswordManager))
				throw new BadRealmException(passwd + " is not an instance of PasswordManager");
			passwordMgr = (PasswordManager)obj;
		} catch (ClassNotFoundException ex) {
			String msg = MessageFormat.format(
					"Cannot instantiate PasswordManager class: {0}", passwd);
			logger.log(Level.SEVERE, msg);
			throw new BadRealmException(msg, ex);
		}
	}		
	
	public String[] authenticate(final String username, final String password) 
			throws LoginException {
		return (null);
	}

	@Override
	public String getAuthType() {
		return ("MongoDBRealm");
	}
	
	public String[] getGroupNamesAsArray(String username) {
		logger.log(Level.INFO, "Groups for {0}", username);
		return (null);
	}

	@Override
	public Enumeration getGroupNames(String username) 
			throws InvalidOperationException, NoSuchUserException {
		return (null);
	}
	
}
