/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import javax.security.auth.login.LoginException;

import static at.oneminutedistraction.mongodbrealm.Constants.*;
import com.mongodb.BasicDBList;
import java.util.List;
import java.util.stream.Collectors;

import static at.oneminutedistraction.mongodbrealm.Constants.*;
import com.mongodb.MongoException;
import java.util.Arrays;

/**
 *
 * @author cmlee
 */
public class UserCollection {
	
	private final PasswordManager passwordMgr;
	private final DBCollection users;
	
	public UserCollection(PasswordManager pm, DBCollection c) {
		passwordMgr = pm;
		users = c;		
	}			
	
	private BasicDBObject create(String username, String password) {
		BasicDBObject user = new BasicDBObject();
		
		user.append(ATTR_USERNAME, username)
				.append(ATTR_PASSWORD, passwordMgr.encrypt(password));
		
		return (user);
	}
	
	private BasicDBObject create(String username, String password, String... groupList) {
		BasicDBObject user = create(username, password);
		
		BasicDBList groups = new BasicDBList();
		groups.addAll(Arrays.asList(groupList));
		user.append(ATTR_GROUPS, groups);
		
		return (user);
	}
	
	private BasicDBObject find(String username) {
		return ((BasicDBObject)users.findOne(new BasicDBObject(ATTR_USERNAME, username)));
	}
	
	public String[] authenticate(final String username, final String password) 
			throws LoginException {
				
		BasicDBObject user = find(username);
		if (null == user)
			throw new LoginException(username + " does not exist");
		
		if (!(user.containsField(ATTR_PASSWORD) && 
				passwordMgr.isValid(user.getString(ATTR_PASSWORD), password)))
			throw new LoginException("Invalid password");
		
		BasicDBList groups = (BasicDBList)user.get(ATTR_GROUPS);
		if (null == groups)
			return (new String[]{});

		
		List<String> g = groups.stream()
				.map(e -> { return (e.toString()); })
				.collect(Collectors.toList());
		return (g.toArray(new String[g.size()]));
	}
	
	public void insert(String username, String password, String... groupList) 
			throws MongoDBRealmException{
		
		insert(create(username, passwordMgr.encrypt(password), groupList));
		
	}
	public void insert(BasicDBObject insertUser) 
			throws MongoDBRealmException {
		
		String username = insertUser.getString(ATTR_USERNAME);
		
		if (null != find(username))
			throw new MongoDBRealmException("User exists: " + username);
		
		try {
			users.insert(insertUser);
		} catch (MongoException ex) {
			throw new MongoDBRealmException("Inserting user " + username, ex);			
		}		
		
	}	
	
	public void update(String username, String password, String... groupList) 
			throws MongoDBRealmException {
		
		update(create(username, passwordMgr.encrypt(password), groupList));
				
	}
	public void update(BasicDBObject updateUser) 
			throws MongoDBRealmException{
		
		String username = updateUser.getString(ATTR_USERNAME);
		BasicDBObject toUpdate = find(username);
		if (null == toUpdate)
			throw new MongoDBRealmException("Cannot find user: " + username);
		
		try {
			users.update(new BasicDBObject(ATTR_USERNAME, username), updateUser);
		} catch (MongoException ex) {
			throw new MongoDBRealmException("Updating user " + username, ex);
		}		
	}
	
}
