package za.co.icurity.usermanagement.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import za.co.icurity.usermanagement.proxy.OVDLoginProxy;

public class PropertiesUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);

	
	public static Properties getProperties() throws Exception {
		Properties properties = new Properties();
		LOG.info("PropertiesUtil:  getProperties  Getting the properties values from domain home  ");
		// uncomment below when deploying onto weblogic server
		 String filePath = System.getenv("DOMAIN_HOME") + File.separator + "oimClient_environment.properties";
		// comment below when deploying onto weblogic server
		// String filePath = "O:\\eclipse\\mywork\\OIMClientService\\" + "oimClient_environment.properties";
				
		if (new File(filePath).exists()) {
			try {
				properties.load(new FileInputStream(filePath));
			} catch (IOException e) {
				throw new Exception("oimClient_environment.properties load error", e);
			}
		} else {
			throw new Exception("oimClient_environment.properties file not found in server or domain directories");
		}
		return properties;
	}
	
}
