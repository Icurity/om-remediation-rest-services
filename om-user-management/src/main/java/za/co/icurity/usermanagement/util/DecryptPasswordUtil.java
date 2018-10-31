package za.co.icurity.usermanagement.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author cofasys
 *
 */
@Component
public class DecryptPasswordUtil {
	
	private final String characterEncoding = "UTF-8";
	private static final Logger LOG = LoggerFactory.getLogger(DecryptPasswordUtil.class);
	/**
	 * 
	 * @param encryptedText
	 * @return
	 */
	 public String decrypt(String encryptedText) {
	        String cipherText = "";
	        try {
	            byte[] cipheredBytes = Base64.decodeBase64(encryptedText);


	            Properties properties = getProperties();
	            String key = properties.getProperty("key_value");
	            int iterations =
	                Integer.parseInt(properties.getProperty("password_iteration"));
	            int keySize = Integer.parseInt(properties.getProperty("keysize"));
	            String initVector = properties.getProperty("initvector");
	            String cipherTransformation =
	                properties.getProperty("cipherTransformation");
	            String aesEncryptionAlgorithm =
	                properties.getProperty("aesEncryptionAlgorithm");
	            byte[] keyBytes = getKeyBytes(key, keySize, iterations);
	            byte[] keyBytesInitVector = initVector.getBytes(characterEncoding);
	            Cipher cipher = Cipher.getInstance(cipherTransformation);
	            SecretKeySpec secretKeySpecy =
	                new SecretKeySpec(keyBytes, aesEncryptionAlgorithm);
	            IvParameterSpec ivParameterSpec =
	                new IvParameterSpec(keyBytesInitVector);
	            cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec);
	            cipheredBytes = cipher.doFinal(cipheredBytes);
	            cipherText = new String(cipheredBytes);

	        } catch (NoSuchAlgorithmException ns) {
	        	LOG.error(this+" decrypt Error"+ns.getMessage());
	          /*  if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "decrypt",
	                            "Error: " + ns.getMessage());
	            }*/
	        } catch (NoSuchPaddingException nsp) {
	        	LOG.error(this+" decrypt Error"+nsp.getMessage());
	           /* if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "decrypt",
	                            "Error: " + nsp.getMessage());
	            }*/
	        } catch (InvalidKeyException ik) {
	        	LOG.error(this+" decrypt Error"+ik.getMessage());
	           /* if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "decrypt",
	                            "Error: " + ik.getMessage());
	            }*/
	        } catch (InvalidAlgorithmParameterException ia) {
	        	LOG.error(this+" decrypt Error"+ia.getMessage());
	           /* if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "decrypt",
	                            "Error: " + ia.getMessage());
	            }*/
	        } catch (IllegalBlockSizeException ib) {
	        	LOG.error(this+" decrypt Error"+ib.getMessage());
	           /* if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "decrypt",
	                            "Error: " + ib.getMessage());
	            }*/
	        } catch (BadPaddingException bp) {
	        	LOG.error(this+" decrypt Error"+bp.getMessage());
	           /* if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "decrypt",
	                            "Error: " + bp.getMessage());
	            }*/
	        } catch (Exception e) {
	        	LOG.error(this+" decrypt Error"+e.getMessage());
	           /* if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "decrypt",
	                            "Error: " + e.getMessage());
	            }*/
	        }
	        return cipherText;

	    }
	 
		/**
		 * @return properties file 
		 * @throws Exception
		 */
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
	  
	    private byte[] getKeyBytes(String key,int iteration,int keySize) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException{
	        byte[] keyBytes= new byte[16];
	        SecretKey secretKey;
	        
	        byte[] parameterKeyBytes= key.getBytes(characterEncoding);
	        
	       // byte[] saltBytes = salt.getBytes(characterEncoding);        
	        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	       PBEKeySpec spec = new PBEKeySpec(key.toCharArray(), parameterKeyBytes, iteration,keySize);
	      
	        secretKey = skf.generateSecret(spec);
	     

	        System.arraycopy(parameterKeyBytes, 0, keyBytes, 0, Math.min(parameterKeyBytes.length, keyBytes.length));
	        return keyBytes;
	    }
}
