package za.co.icurity.usermanagement.util;

import java.util.logging.Level;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import za.co.icurity.usermanagement.proxy.OVDLoginProxy;
import za.co.icurity.usermanagement.vo.CheckExistingUserInVO;
import za.co.icurity.usermanagement.vo.GetADUsernameInVO;
import za.co.icurity.usermanagement.vo.GetUserAccountLockedOutVO;
import za.co.icurity.usermanagement.vo.UserDetailsVO;
import za.co.icurity.usermanagement.vo.UserOutVO;
 
/**
 * @author icurity
 *
 */
@JsonInclude(value = Include.NON_NULL)
@Component
public class AttributesUtil {
	
	@Autowired
	private UserDetailsVO userDetailsVO;
	@Autowired
	private GetUserAccountLockedOutVO getUserAccountLockedOutVO;
	@Autowired
	private OVDLoginProxy ovdLoginProxy;
	static final String searchBase = "cn=users";	
	
	private static final Logger LOG = LoggerFactory.getLogger(ValidateUserInVOUtil.class);
	
	public UserDetailsVO getUserDetailsAttributes(DirContext dirContext,String username) {
		
		try {
			String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username + ")(cn=" + username + "))"; 
			SearchResult searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
			Attributes attributes = null;
			if (searchResult != null) {
				attributes = searchResult.getAttributes();
			}else{
				LOG.info(this + "No user found for username: " + username);
				userDetailsVO.setStatus("Error");
				userDetailsVO.setErrorMessage("No user found for username: " + username);
				return userDetailsVO;
			}	
			
			if (attributes != null) {
				for (NamingEnumeration ae = attributes.getAll(); ae.hasMore();) {
					Attribute attr = (Attribute) ae.next();
				 if(attr.getID().equalsIgnoreCase("givenName")) {
					 userDetailsVO.setFirstName(attr.get().toString());
				 }
				 if(attr.getID().equalsIgnoreCase("sn")) {
					 userDetailsVO.setLastName(attr.get().toString());
				 }
				 if (attr.getID().equalsIgnoreCase("mobile")) {
					 userDetailsVO.setCellPhoneNumber(attr.get().toString());
				 }
				 if (attr.getID().equalsIgnoreCase("omDateOfBirth")) {
					 userDetailsVO.setDateOfBirth(attr.get().toString());
				 }
				 if (attr.getID().equalsIgnoreCase("employeeID")) {
					 userDetailsVO.setIdNumber(attr.get().toString());
				 }
				 if (attr.getID().equalsIgnoreCase("obfirstlogin")) {
					 userDetailsVO.setMigratedUserFirstLogin(attr.get().toString());
				 }
				 if (attr.getID().equalsIgnoreCase("omUserAccountStatus")) {
					 userDetailsVO.setUserAccountLocked(attr.get().toString());
				 }
				 if (attr.getID().equalsIgnoreCase("employeeType")) {
					 userDetailsVO.setIdType(attr.get().toString());
				 }
				 if (attr.getID().equalsIgnoreCase("employeeNumber")) {
					 userDetailsVO.setEmployeeNumber(attr.get().toString());
				 }
				}	
				userDetailsVO.setStatus("Success");
				return userDetailsVO; 
	        }else {
	        	userDetailsVO.setStatus("Error");
				userDetailsVO.setErrorMessage("No user found for username: " + username);
				return userDetailsVO;
	        }
			}catch (Exception e) {
				LOG.error(this + " Error on getUserDetailsAttributes: " + e.getMessage());
			}
		return userDetailsVO; 
	}
	
    
	public GetUserAccountLockedOutVO getUserAccountLockedAttributes(DirContext dirContext,String username) {
		
		try {
			String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username + ")(cn=" + username + "))"; 
			SearchResult searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
			Attributes attributes = null;
			if (searchResult != null) {
				attributes = searchResult.getAttributes();
            }else{
				LOG.info(this + "No user found for username: " + username);
				getUserAccountLockedOutVO.setStatus("Error");
				getUserAccountLockedOutVO.setErrorMessage("No user found for username: " + username);
				return getUserAccountLockedOutVO;
			}				
			 if (attributes != null) {
				for (NamingEnumeration ae = attributes.getAll(); ae.hasMore();) {
					Attribute attr = (Attribute) ae.next();					
					 if (attr.getID().equalsIgnoreCase("omUserAccountStatus")) {
						 getUserAccountLockedOutVO.setUserAccountStatus(attr.get().toString());
					 }
				}
				 getUserAccountLockedOutVO.setStatus("Success");
					return getUserAccountLockedOutVO; 
			 }else {
		        	getUserAccountLockedOutVO.setStatus("Error");
		        	getUserAccountLockedOutVO.setErrorMessage("No user found for username: " + username);
					return getUserAccountLockedOutVO;
		         }
		}catch (Exception e) {
					LOG.error(this + " Error on getUserAccountLockedAttributes: " + e.getMessage());
			    	getUserAccountLockedOutVO.setStatus("Error");
					getUserAccountLockedOutVO.setErrorMessage("No user found for username: " + username);
			    	return getUserAccountLockedOutVO;
				}
		}
 

	 /* public UserDetailsVO getADUsernameAttributes(DirContext dirContext, GetADUsernameInVO getADUsernameInVO) {
			
			try {
				String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username + ")(cn=" + username + "))"; 
				SearchResult searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
				Attributes attributes = null;
				if (searchResult != null) {
					attributes = searchResult.getAttributes();
				}else{
					LOG.info(this + "No user found for username: " + username);
					userDetailsVO.setStatus("Error");
					userDetailsVO.setErrorMessage("No user found for username: " + username);
					return userDetailsVO;
				}	
				
				
				}catch (Exception e) {
					LOG.error(this + " Error on getUserDetailsAttributes: " + e.getMessage());
				}
			return userDetailsVO; 
		}*/
}
