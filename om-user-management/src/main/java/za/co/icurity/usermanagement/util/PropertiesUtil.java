package za.co.icurity.usermanagement.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
	
	public static Properties getProperties() throws Exception {
		Properties properties = new Properties();
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
