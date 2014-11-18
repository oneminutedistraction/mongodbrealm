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
public class Constants {
	
	public static final String PREFIX = "mongodbRealm";
	
	public static final String PROP_DB_NAME = PREFIX + ".db";
	public static final String PROP_COLLECTION_NAME = PREFIX + ".collection";			
	public static final String PROP_SERVER = PREFIX + ".server";			
	public static final String PROP_PASSWORD_MANAGER = PREFIX + ".passwordManager";		
	public static final String PROP_MONGO_CLIENT = PREFIX + ".clientFactory";
	
	public static final String SHA256  = "SHA-256";
	public static final String SHA512  = "SHA-512";
	
	public static final String ATTR_USERNAME = "username";
	public static final String ATTR_PASSWORD = "password";
	public static final String ATTR_GROUPS = "groups";
	
	public static final String DEFAULT_DB_NAME = "mongodbRealm";
	public static final String DEFAULT_COLLECTION_NAME = "users";
	public static final String DEFAULT_JAAS_CONTEXT = "mongodbRealm";
}
