/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm;

import at.oneminutedistraction.mongodbrealm.spi.PasswordManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import javax.security.auth.login.LoginException;

import com.mongodb.BasicDBList;
import java.util.List;
import java.util.stream.Collectors;

import static at.oneminutedistraction.mongodbrealm.Constants.*;
import com.mongodb.MongoException;
import java.util.Arrays;
import java.util.Optional;

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
	
	public BasicDBObject find(String username) {
		return ((BasicDBObject)users.findOne(new BasicDBObject(ATTR_USERNAME, username)));
	}
	
	public String[] authenticate(final String username, final String password) 
			throws LoginException {
				
		BasicDBObject user = find(username);
		if (null == user)
			throw new LoginException(username + " does not exist");				
		
		if (!(user.containsField(ATTR_PASSWORD) && 
				passwordMgr.isValid(user.getString(ATTR_PASSWORD)
						, password)))
			throw new LoginException("Invalid password");
		
		return (groups(user));
		
//		BasicDBList groups = (BasicDBList)user.get(ATTR_GROUPS);
//		if (null == groups)
//			return (new String[]{});
//
//		
//		List<String> g = groups.stream()
//				.map(e -> { return (e.toString()); })
//				.collect(Collectors.toList());
//		return (g.toArray(new String[g.size()]));
	}
	
	public String[] groups(String username) {
		return (groups(find(username)));
	}
	public String[] groups(BasicDBObject user) {
		if (null == user)
			return (null);
		
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
		
		insert(create(username, password, groupList));
		
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
		verify(updateUser, "Cannot find user: " + username);
		
		try {
			users.update(new BasicDBObject(ATTR_USERNAME, username), updateUser);
		} catch (MongoException ex) {
			throw new MongoDBRealmException("Updating user " + username, ex);
		}		
	}

	public void remove(String username)
			throws MongoDBRealmException {
		remove(new BasicDBObject(ATTR_USERNAME, username));
	}
	public void remove(BasicDBObject deleteUser) 
			throws MongoDBRealmException {

		String username = deleteUser.getString(ATTR_USERNAME);
		verify(deleteUser, "Cannot find user: " + username);

		try {
			users.remove(deleteUser);
		} catch (MongoException ex) {
			throw new MongoDBRealmException("Deleting user " + username, ex);
		}
	}

	private void verify(final BasicDBObject obj, final String exceptionMsg) 
			throws MongoDBRealmException {
		final String username = obj.getString(ATTR_USERNAME);
		if (null == find(username))
			throw new MongoDBRealmException(exceptionMsg);
	}
	
}
