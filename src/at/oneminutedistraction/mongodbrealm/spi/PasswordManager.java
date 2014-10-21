/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm.spi;

import java.util.Properties;

/**
 *
 * @author cmlee
 */
public interface PasswordManager {
	
	public void init(Properties prop);
	
	public String encrypt(String password);
	
	public boolean isValid(String encrypted, String password);
}
