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
		
	}
	
	public String[] authenticate(final String username, final String password) 
			throws LoginException {
				
		BasicDBObject user = (BasicDBObject)users.findOne(
				new BasicDBObject(ATTR_USERNAME, username));
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
	
	public boolean insert(String username, String password, String... groups) {
		return (true);
	}
	
	public boolean update(String username, String password, String... groups) {
		return (true);
	}
	public boolean update(BasicDBObject user) {
		return (true);
	}
	
}
