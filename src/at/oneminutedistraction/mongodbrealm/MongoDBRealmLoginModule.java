/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm;

import com.sun.appserv.security.AppservPasswordLoginModule;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;

/**
 *
 * @author cmlee
 */
public class MongoDBRealmLoginModule extends AppservPasswordLoginModule {
	
	private static final Logger logger = Logger.getLogger(MongoDBRealmLoginModule.class.getName());
		

	@Override
	protected void authenticateUser() throws LoginException {
		
		logger.log(Level.INFO, "Authenticating {0} on {1}"
				, new Object[]{_username, _currentRealm.getClass().getName()});
		
		MongoDBRealm mongoRealm = null;
						
		if (!(_currentRealm instanceof MongoDBRealm)) {
			String error = MessageFormat.format("{0} is not MongoDBRealm. Check login.conf"
					, _currentRealm.getClass().getName());
			logger.log(Level.SEVERE, error);
			throw new LoginException(error);
		}
		
		mongoRealm = (MongoDBRealm)_currentRealm;
		
			
		//Hard code this
		commitUserAuthentication(new String[]{ "mongo" });
	}
	
}
