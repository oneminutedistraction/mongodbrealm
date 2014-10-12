/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm;

/**
 *
 * @author cmlee
 */
public class MongoDBRealmException extends RuntimeException {

	public MongoDBRealmException() { }

	public MongoDBRealmException(String message) {
		super(message);
	}

	public MongoDBRealmException(Throwable cause) {
		super(cause);
	}

	public MongoDBRealmException(String message, Throwable cause) {
		super(message, cause);
	}		
	
}
