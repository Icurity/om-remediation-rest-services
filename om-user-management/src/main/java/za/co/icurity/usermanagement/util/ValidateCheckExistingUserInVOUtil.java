package za.co.icurity.usermanagement.util;

import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import za.co.icurity.usermanagement.vo.CheckExistingUserInVO;
import za.co.icurity.usermanagement.vo.UserOutVO;
 
/**
 * @author icurity
 *
 */
@JsonInclude(value = Include.NON_NULL)
@Component
public class ValidateCheckExistingUserInVOUtil {
	
	@Autowired
	private UserOutVO userOutVO;
	private static final Logger LOG = LoggerFactory.getLogger(ValidateUserInVOUtil.class);
	
	/*This method is to validate the input fields*/
	
 
	/**
	 * @param checkExistingUserInVO
	 * @return
	 */
	public UserOutVO validateCheckExistingUserInVO(CheckExistingUserInVO checkExistingUserInVO) {

	    	userOutVO.setStatus("");
	    	StringBuffer buffer = new StringBuffer();
		 if (checkExistingUserInVO.getFirstName() == null || checkExistingUserInVO.getFirstName().trim().isEmpty()) {
	            buffer.append("Mandatory field : First Name" + System.getProperty("line.separator"));
	        }
	        if (checkExistingUserInVO.getLastName() == null || checkExistingUserInVO.getLastName().trim().isEmpty()) {
	            buffer.append("Mandatory field : Last Name" + System.getProperty("line.separator"));
	        }
	        if (checkExistingUserInVO.getIdNumber() == null || checkExistingUserInVO.getIdNumber().trim().isEmpty()) {
	            buffer.append("Mandatory field : ID Number" + System.getProperty("line.separator"));
	        }
	        if (checkExistingUserInVO.getDateOfBirth() == null || checkExistingUserInVO.getDateOfBirth().trim().isEmpty()) {
	            buffer.append("Mandatory field : Date of Birth" + System.getProperty("line.separator"));
	        }

	        if (buffer.length() > 0) {
	        	userOutVO.setStatus("Error");
	        	userOutVO.setErrorMessage(buffer.toString());
	    /*        if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "checkExistingUser", "Validation errors: " + buffer.toString());
	            }*/
	            return userOutVO;
	        }

		return userOutVO;

	}
	
}
