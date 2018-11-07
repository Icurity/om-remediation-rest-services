package za.co.icurity.usermanagement.controller;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;

import oracle.iam.platform.OIMClient;
import za.co.icurity.usermanagement.proxy.OIMLoginProxy;
import za.co.icurity.usermanagement.proxy.OVDLoginProxy;
import za.co.icurity.usermanagement.service.UserManagerService;
import za.co.icurity.usermanagement.util.GenerateSSAID;
import za.co.icurity.usermanagement.util.ValidateUserInVOUtil;
import za.co.icurity.usermanagement.vo.CheckExistingUsernameOutVO;
import za.co.icurity.usermanagement.vo.FetchUserRolesInVO;
import za.co.icurity.usermanagement.vo.ProvisionUserAccountVO;
import za.co.icurity.usermanagement.vo.RoleListVO;
import za.co.icurity.usermanagement.vo.RoleVO;
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
	private OIMLoginProxy oimLoginProxy;
	@Autowired
	private UserManagerService userManagerService;
	@Autowired
	private OVDLoginProxy ovdLoginProxy;
	@Autowired
	private UserOutVO userOutVO;
	@Autowired
	private GenerateSSAID generateSSAID;
	@Autowired
	private ValidateUserInVOUtil validateUserInVOUtil;
	@Autowired
	private ProvisionUserAccountVO accountVO;
	@Autowired
	private CheckExistingUsernameOutVO checkExistingUsernameOutVO;
	@Autowired
	private UserStatusVO userStatusVO;
	static final String searchBase = "cn=users";

	/**
	 * Create a user in OIM and provision
	 * 
	 * @param userInVO contains input values
	 * @return UserOutVO or exception message
	 * @throws Exception
	 */

	@RequestMapping(value = "/createUser", method = RequestMethod.POST, headers = "Accept=application/json")
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

		try {
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
			} else {
				LOG.error(this + " User provisioning failed " + userOutVO.getErrorMessage());
				throw new Exception("UserId is null for provisioning");
			}
		} catch (Exception e) {
			LOG.error(this + " Error on provision user " + userOutVO.getStatus());
		} finally {
			if (oimClient != null) {
				oimClient.logout();
				oimClient = null;
			}
		}
		return userOutVO;
	}

	/**
	 * @param userInVO
	 * @return
	 * @throws Exception
	 */

	@RequestMapping(value = "userstatus", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<UserStatusVO> userstatus(@RequestParam("username") String username) throws Exception {
		userStatusVO.setErrorMessage(null);
		userStatusVO.setStatus(null);
		userStatusVO.setObFirstLogin(null);
		userStatusVO.setObPasswordChangeFlag(null);
		if (username == null || username.isEmpty()) {
			userStatusVO.setStatus("500");
			userStatusVO.setErrorMessage("Please enter valid user name");
			return new ResponseEntity<>(userStatusVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}		
		DirContext dirContext = ovdLoginProxy.connect();
		try { 
			String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username + "))";
			SearchResult searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
			Attributes attributes = null;
			if (searchResult != null) {
				attributes = searchResult.getAttributes();
			}else {
				LOG.info(this + " User " + username + " Not found");
				userStatusVO.setStatus("500");
				userStatusVO.setErrorMessage(" User " + username + " Not found");
				return new ResponseEntity<>(userStatusVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			if (attributes != null) {
				for (NamingEnumeration ae = attributes.getAll(); ae.hasMore();) {
					Attribute attr = (Attribute) ae.next();
					if (attr.getID().equalsIgnoreCase("obfirstlogin")) {
						if (attr.get() != null)
							if (attr.get().equals("true")) {
								userStatusVO.setObFirstLogin("true");
							} else if (attr.get().equals("false")) {
								userStatusVO.setObFirstLogin("false");
							}
					} else if (attr.getID().equalsIgnoreCase("obpasswordchangeflag")) {
						if (attr.get() != null)
							if (attr.get().equals("0")) {
								userStatusVO.setObPasswordChangeFlag("false");
							} else if (attr.get().equals("1")) {
								userStatusVO.setObPasswordChangeFlag("true");
							}
					}
				}	

			}
			userStatusVO.setStatus("Success");
			userStatusVO.setObFirstLogin(userStatusVO.getObFirstLogin());
			userStatusVO.setObPasswordChangeFlag(userStatusVO.getObPasswordChangeFlag());
			return new ResponseEntity<>(userStatusVO, HttpStatus.OK);						
		} catch (Exception e) {
			LOG.error(this + " Error on getUserStatus " + e);
			userStatusVO.setStatus("500");
			userStatusVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(userStatusVO, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {			
			dirContext = null;			
		}
	}

	@RequestMapping(value = "fetchUserRoles", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<UserOutVO> fetchUserRoles(@RequestParam("username") String username) {
		DirContext dirContext = null;
		userOutVO.setErrorCode(null);
		userOutVO.setErrorMessage(null);
		userOutVO.setMemberOf(null);
		userOutVO.setStatus(null);
		userOutVO.setUserId(null);
		
		if (username == null || username.isEmpty()) {
			userOutVO.setStatus("500");
			userOutVO.setErrorMessage("Please enter valid user name");
			return new ResponseEntity<>(userOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		try {
			dirContext = ovdLoginProxy.connect();
		} catch (InvalidNameException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (dirContext == null) {
			userOutVO.setStatus("500");
			userOutVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(userOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		try {

			String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username + "))";
			SearchResult searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
			Attributes attributes = null;
			if (searchResult != null) {
				attributes = searchResult.getAttributes();
			} else {
				LOG.info(this + " User " + username + " Not found");
				userOutVO.setStatus("500");
				userOutVO.setErrorMessage(" User " + username + " Not found");
				return new ResponseEntity<>(userOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			// List<String> memberOf = new ArrayList<String>();
			StringBuffer tmpmemberOf = new StringBuffer();

			if (attributes.get("memberOf") != null) {
				for (Enumeration vals = attributes.get("memberOf").getAll(); vals.hasMoreElements();) {
					String[] myData = vals.nextElement().toString().toLowerCase().split("cn=");
					int i = 0;
					for (String str : myData) {
						String group = str.split(",")[0];
						if (!group.contains("admin group")) {
							group = group.replace("r-", "").replace("members group", "").trim();
							if (i == 0) {
								tmpmemberOf.append(group);
							} else {
								tmpmemberOf.append(group + ",");
							}

							i++;
						}
					}

				}
			}
			StringBuffer tmpIsmemberOf = new StringBuffer();
			if (tmpmemberOf.length() > 0) {
				String tmp = "[" + tmpmemberOf.toString() + "]";
				tmpIsmemberOf.append(tmp.replace(",]", ","));
			}

			if (attributes.get("ismemberOf") != null) {
				for (Enumeration vals = attributes.get("ismemberOf").getAll(); vals.hasMoreElements();) {
					String[] myData = vals.nextElement().toString().toLowerCase().split("cn=");
					int i = 0;
					for (String str : myData) {
						String group = str.split(",")[0];
						if (!group.contains("admin group") && !group.contains("groups")) {
							group = group.replace("r-", "").replace("members group", "").trim();
							if (i == 0) {
								tmpIsmemberOf.append(group);
							} else {
								tmpIsmemberOf.append(group + ",");
							}
							i++;
						}

					}

				}
			}
			if (tmpIsmemberOf.length() > 0) {
				String tmp1 = tmpIsmemberOf.toString() + "]";
				userOutVO.setStatus("200");
				userOutVO.setMemberOf(tmp1.replace(",]", "]"));
			}
			return new ResponseEntity<>(userOutVO, HttpStatus.OK);
		} catch (Exception e) {
			LOG.error(this + " Error on fetchUserRoles " + e);
			// userOutVO.setErrorMessage("Error on getUserStatus");
			userOutVO.setStatus("500");
			userOutVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(userOutVO, HttpStatus.INTERNAL_SERVER_ERROR);

		} finally {
			dirContext = null;
		}

	}
	
	
	@RequestMapping(value = "checkExistingUserName", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<CheckExistingUsernameOutVO> checkExistingUserName(@RequestParam("username") String username) {
		OIMClient oimClient = null;
		// Get the OIMClient object
		oimClient = oimLoginProxy.userLogin();
		checkExistingUsernameOutVO.setErrorMessage(null);
		checkExistingUsernameOutVO.setStatus(null);
		checkExistingUsernameOutVO.setUsernameExists(null);

		if (username == null || username.isEmpty()) {
			checkExistingUsernameOutVO.setStatus("500");
			checkExistingUsernameOutVO.setErrorMessage("Please enter valid user name");
			return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		try {
			if (oimClient == null) {
				LOG.error(this + " checkExistingUserName: Failed  OIMCLient login");
				checkExistingUsernameOutVO.setStatus("500");
				checkExistingUsernameOutVO.setErrorMessage("System unavailable at the moment, please try again");
				return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
				// throw new RuntimeException("checkExistingUserName: Failed OIMCLient login");
			}

			LOG.info(this + " checkExistingUserName Logged into OIMClient  ");
			if (userManagerService.checkExistingUserName(oimClient, username)) {
				checkExistingUsernameOutVO.setStatus("Success");
				checkExistingUsernameOutVO.setUsernameExists("true");
				return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.OK);
			} else {
				/*
				 * checkExistingUsernameOutVO.setUsernameExists("false");
				 * checkExistingUsernameOutVO.setErrorMessage("No user found for username : " +
				 * usernameVO.getUsername());
				 */
				LOG.error(this + " checkExistingUserName: Username not exists");
				// throw new RuntimeException("Username not exists");
				checkExistingUsernameOutVO.setStatus("500");
				checkExistingUsernameOutVO.setErrorMessage("User " + username + " not found");
				return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on checkExistingUserName ");
			// checkExistingUsernameOutVO.setErrorMessage("Error on checkExistingUserName");
			checkExistingUsernameOutVO.setStatus("500");
			checkExistingUsernameOutVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (oimClient != null) {
				oimClient.logout();
				oimClient = null;
			}			
		}
	}

}