/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.oneminutedistraction.mongodbrealm;

import at.oneminutedistraction.mongodbrealm.spi.MongoClientFactory;
import java.util.Properties;

import static at.oneminutedistraction.mongodbrealm.Constants.*;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cmlee
 */
public class DefaultMongoClientFactory implements MongoClientFactory {
	
	private static final Logger logger = Logger.getLogger(DefaultMongoClientFactory.class.getName());
	
	private Properties props;

	@Override
	public void init(Properties props) {
		if (null == props)
			props = new Properties();
		this.props = props;
	}

	@Override
	public com.mongodb.MongoClient create() throws Exception {
		
		//Server list
		String servers = props.getProperty(PROP_SERVER, "localhost");
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
			return (new com.mongodb.MongoClient(serverList));
		} else {
			logger.log(Level.INFO, "MongoDB instance at localhost");
			try {
				return (new com.mongodb.MongoClient("localhost"));
			} catch (UnknownHostException ex) { 
				//Seriously?
			}
		}
		return (null);
	}
	
}
