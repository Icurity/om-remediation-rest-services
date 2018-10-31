package za.co.icurity.usermanagement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import oracle.iam.platform.OIMClient;
import za.co.icurity.usermanagement.proxy.OIMLoginProxy;
import za.co.icurity.usermanagement.service.UserManagerService;
import za.co.icurity.usermanagement.util.GenerateSSAID;
import za.co.icurity.usermanagement.util.ValidateUserInVOUtil;
import za.co.icurity.usermanagement.vo.ProvisionUserAccountVO;
import za.co.icurity.usermanagement.vo.StatusOutVO;
import za.co.icurity.usermanagement.vo.UserInVO;
import za.co.icurity.usermanagement.vo.UserOutVO;
import za.co.icurity.usermanagement.vo.UserStatusVO;
import za.co.icurity.usermanagement.vo.UsernameVO;

/**
 * @author 
 *
 */
@RestController
@RequestMapping("/omuser")
public class ResourceController {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceController.class);
	@Autowired
	private ValidateUserInVOUtil validateUserInVOUtil;
	@Autowired
	private GenerateSSAID generateSSAID;
	@Autowired
	private OIMLoginProxy oimLoginProxy;
	@Autowired
	private UserManagerService userManagerService;
	@Autowired
	private ProvisionUserAccountVO accountVO;

	@RequestMapping(value = "read", method = RequestMethod.GET)
	String readResource() {
		return "Welcome to my World of Heaven!";
	}

	/**
	 * Create a user in OIM and provision
	 * 
	 * @param userInVO contains input values
	 * @return UserOutVO or exception message
	 * @throws Exception
	 */
	@RequestMapping(value = "/createUser", method = RequestMethod.POST,headers = "Accept=application/json")
   	public UserOutVO createUser(@RequestBody UserInVO userInVO) throws Exception {

		OIMClient oimClient = null;
		String ssaid = null;

		// validate input fields - returns userOutVO with status
		UserOutVO userOutVO = validateUserInVOUtil.validateUserInVO(userInVO);
		if (userOutVO.getStatus().equals("Error")) {
			LOG.error("validateUserInVO " + userOutVO.getErrorMessage());
			throw new Exception("Validation Errors:" + userOutVO.getErrorMessage());
		}

		// Get the OIMClient object
		oimClient = oimLoginProxy.userLogin();
		if (oimClient == null) {
			throw new Exception("Login to OIMClient failed");
		}

		// generate, check existing ssaid(employee number) and set it userInVO
		while (true) {
			ssaid = generateSSAID.getSSAID();
			if (!userManagerService.checkExistingEmployeeNumber(oimClient, ssaid)) {
				userInVO.setSsaId(ssaid);
				break;
			}
		}
		// create user
		userOutVO = userManagerService.createUser(oimClient, userInVO);

		// provision user
		if (userOutVO.getUserId() != null) {
			accountVO.setEmployeeNumber(ssaid);
			accountVO.setUserKey(userOutVO.getUserId());
			accountVO.setUsername(userInVO.getUsername());
			StatusOutVO statusOutVO = userManagerService.provisionUser(oimClient, accountVO);
			if (statusOutVO.getStatus() != "Success") {
				userOutVO.setStatus("Error");
				userOutVO.setErrorMessage("Error while creating user account in AD");
				LOG.error(this + userOutVO.getErrorMessage());
				throw new Exception("User provisioning failed");
			}
		}else {
			LOG.error(this + " User provisioning failed " + userOutVO.getErrorMessage());
			throw new Exception("UserId is null for provisioning");
		}
		return userOutVO;
	}

	 /**
	 * @param userInVO
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/userstatus", method = RequestMethod.GET,headers = "Accept=application/json")
	   	public UserOutVO getUserStatus(@RequestBody UserInVO userInVO) throws Exception {		 

		    OIMClient oimClient = null;
		    UsernameVO usernameVO = null;
		    
			oimClient = oimLoginProxy.userLogin();
			if (oimClient == null) {
				throw new Exception("Login to OIMClient failed");
			}
		 
			userManagerService.oimUserStatus(oimClient,usernameVO);
		 
		 return null;
		 
	 }

}