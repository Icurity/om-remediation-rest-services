package za.co.icurity.usermanagement.util;

import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import za.co.icurity.usermanagement.vo.CheckExistingUserInVO;
import za.co.icurity.usermanagement.vo.GetADUsernameInVO;
import za.co.icurity.usermanagement.vo.UserOutVO;
 
/**
 * @author icurity
 *
 */
@JsonInclude(value = Include.NON_NULL)
@Component
public class ValidateADUsernameInVOUtil {
	
	@Autowired
	private UserOutVO userOutVO;
	private static final Logger LOG = LoggerFactory.getLogger(ValidateUserInVOUtil.class);
	
	/*This method is to validate the input fields*/
	
 
	/**
	 * @param checkExistingUserInVO
	 * @return
	 */
	public UserOutVO validateADUsernameInVO(GetADUsernameInVO getADUsernameInVO) {

		   StringBuffer buffer = new StringBuffer();
		
	    	 userOutVO.setStatus("");
	    	 if (getADUsernameInVO.getSurname() == null || getADUsernameInVO.getSurname().trim().isEmpty()) {
	             buffer.append("Mandatory field :  Surname" + System.getProperty("line.separator"));
	         }
	         if (getADUsernameInVO.getIdNumber() == null || getADUsernameInVO.getIdNumber().trim().isEmpty()) {
	             buffer.append("Mandatory field : ID Number" + System.getProperty("line.separator"));
	         }
	         if (getADUsernameInVO.getIdType() == null || getADUsernameInVO.getIdType().trim().isEmpty()) {
	             buffer.append("Mandatory field : ID Type" + System.getProperty("line.separator"));
	         }
	         if (getADUsernameInVO.getEmailAddress() == null || getADUsernameInVO.getEmailAddress().trim().isEmpty()) {
	             buffer.append("Mandatory field : Email Address" + System.getProperty("line.separator"));
	         }	         
	    	
	        if (buffer.length() > 0) {
	        	userOutVO.setStatus("Error");
	        	userOutVO.setErrorMessage(buffer.toString());
	            return userOutVO;
	        }

		return userOutVO;

	}
	
}
