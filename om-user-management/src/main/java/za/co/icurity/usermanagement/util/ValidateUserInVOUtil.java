package za.co.icurity.usermanagement.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import za.co.icurity.usermanagement.vo.UserInVO;
import za.co.icurity.usermanagement.vo.UserOutVO;

/**
 * @author icurity
 *
 */
@Component
public class ValidateUserInVOUtil {
	
	@Autowired
	private UserOutVO userOutVO;
	private static final Logger LOG = LoggerFactory.getLogger(ValidateUserInVOUtil.class);
	
	/*This method is to validate the input fields*/
	
	/** 
	 * validates the user input
	 * @param userInVO
	 * @return
	 */
	public UserOutVO validateUserInVO(UserInVO userInVO) {

		userOutVO.setStatus("");
		StringBuffer buffer = new StringBuffer();
		if (userInVO == null || userInVO.getLastName() == null || userInVO.getLastName().trim().isEmpty() || userInVO.getPassword() == null || userInVO.getPassword().trim().isEmpty() || userInVO.getUsername() == null || userInVO.getUsername().trim().isEmpty() || userInVO.getDateOfBirth() == null || userInVO.getDateOfBirth().trim().isEmpty() || userInVO.getIdNumber() == null || userInVO.getIdNumber().trim().isEmpty()) {
			userOutVO.setStatus("Error");
			if (userInVO.getLastName() == null || userInVO.getLastName().trim().isEmpty()) {
				buffer.append("Mandatory field : Last Name" + System.getProperty("line.separator"));
			}
			if (userInVO.getPassword() == null || userInVO.getPassword().trim().isEmpty()) {
				buffer.append("Mandatory field : Password" + System.getProperty("line.separator"));
			}
			if (userInVO.getUsername() == null || userInVO.getUsername().trim().isEmpty()) {
				buffer.append("Mandatory field : Username" + System.getProperty("line.separator"));
			}
			if (userInVO.getDateOfBirth() == null || userInVO.getDateOfBirth().trim().isEmpty()) {
				buffer.append("Mandatory field : Date of Birth" + System.getProperty("line.separator"));
			}
			if (userInVO.getIdNumber() == null || userInVO.getIdNumber().trim().isEmpty()) {
				buffer.append("Mandatory field : ID Number" + System.getProperty("line.separator"));
			}
			userOutVO.setErrorMessage(buffer.toString());
			/*
			 * if (logger.isLoggable(Level.SEVERE)) { logger.logp(Level.SEVERE, CLASS_NAME,
			 * "createUser", "Validation Errors: " + buffer.toString()); }
			 */
			return userOutVO;
		} else {
			if (userInVO.getDateOfBirth() == null || !userInVO.getDateOfBirth().matches("^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/[0-9]{4}$")) {
				userOutVO.setStatus("Error");
				buffer.append("Please enter date in format dd/MM/yyyy" + System.getProperty("line.separator"));
				userOutVO.setErrorMessage(buffer.toString());
				
				/*
				 * if (logger.isLoggable(Level.SEVERE)) { logger.logp(Level.SEVERE, CLASS_NAME,
				 * "createUser", "Validation errors: " + buffer.toString()); }
				 */
				return userOutVO;
			}
		}

		return userOutVO;

	}
	
}
