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
	@Autowired
	private RoleListVO roleListVO;
	static final String searchBase = "cn=users";
	
	

	/**
	 * Create a user in OIM and provision
	 * 
	 * @param userInVO contains input values
	 * @return UserOutVO or exception message
	 * @throws Exception
	 */

	   @RequestMapping(value = "/createUser", method = RequestMethod.POST, headers = "Accept=application/json")	 
	   public ResponseEntity<UserOutVO> createUser(@RequestBody UserInVO userInVO) throws Exception {
		OIMClient oimClient = null;		
		userOutVO.setUserId(null);
		userOutVO.setErrorMessage(null);
		userOutVO.setUserId(null);
		userOutVO.setErrorCode(null);
		String ssaid = null;
	
		// validate input fields - returns userOutVO with status
		if(validateUserInVOUtil.validateUserInVO(userInVO).getStatus().equalsIgnoreCase("Error")) {
			LOG.error("validateUserInVO " + userOutVO.getErrorMessage());
			userOutVO.setStatus("Error");
			userOutVO.setErrorMessage(userOutVO.getErrorMessage());
			return new ResponseEntity<>(userOutVO, HttpStatus.BAD_REQUEST);			
		}

		// Get the OIMClient object
		
		oimClient = oimLoginProxy.userLogin();
		if (oimClient == null) {
			userOutVO.setStatus("Error");
			userOutVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(userOutVO, HttpStatus.SERVICE_UNAVAILABLE);		
		}

		// generate, check existing ssaid(employee number) and set it userInVO
		while (true) {
			ssaid = generateSSAID.getSSAID();
			if (!userManagerService.checkExistingEmployeeNumber(oimClient, ssaid)) { //confirm with Mocx
				userInVO.setSsaId(ssaid);
				break;
			}
		}
		// create user
		userOutVO = userManagerService.createUser(oimClient, userInVO);
		if(userOutVO.getStatus().equalsIgnoreCase("Error")) {
			userOutVO.setStatus("Error");
			userOutVO.setErrorMessage(userOutVO.getErrorMessage());
			return new ResponseEntity<>(userOutVO, HttpStatus.SERVICE_UNAVAILABLE);		
		}
		try {
			// provision user
			if (userOutVO.getUserId() != null) {
				accountVO.setEmployeeNumber(ssaid);
				accountVO.setUserKey(userOutVO.getUserId());
				accountVO.setUsername(userInVO.getUsername());
				StatusOutVO statusOutVO = userManagerService.provisionUser(oimClient, accountVO);				
				if(statusOutVO.getStatus().equalsIgnoreCase("Error")) {
					userOutVO.setStatus("Error");
					userOutVO.setErrorMessage(userOutVO.getErrorMessage());
					return new ResponseEntity<>(userOutVO, HttpStatus.SERVICE_UNAVAILABLE);
				}else {
					userOutVO.setStatus("Success");
					userOutVO.setErrorMessage("User "+userInVO.getUsername() +" successfully created ");					
					return new ResponseEntity<>(userOutVO, HttpStatus.CREATED);
				}
			}else {
				userOutVO.setStatus("Error");
				userOutVO.setErrorMessage(userOutVO.getErrorMessage());
				return new ResponseEntity<>(userOutVO, HttpStatus.SERVICE_UNAVAILABLE);
			}
		}catch (Exception e) {
			userOutVO.setStatus("Error");
			userOutVO.setErrorMessage(userOutVO.getErrorMessage());
			return new ResponseEntity<>(userOutVO, HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			if (oimClient != null) {
				oimClient.logout();
				oimClient = null;
			}
		}
	}

	/**
	 * Gets the status of the user
	 * @param userInVO
	 * @return password change status and user first login status
	 * @throws Exception
	 */

	@RequestMapping(value = "userstatus", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<UserStatusVO> userstatus(@RequestParam("username") String username) throws Exception {
		userStatusVO.setErrorMessage(null);
		userStatusVO.setStatus(null);
		userStatusVO.setMigratedUserFirstLogin(null);
		userStatusVO.setChangePassword(null);
		if (username == null || username.isEmpty()) {
			userStatusVO.setStatus("Error");
			userStatusVO.setErrorMessage("Mandatory field : Username");
			return new ResponseEntity<>(userStatusVO, HttpStatus.NOT_FOUND);
		}
		DirContext dirContext = ovdLoginProxy.connect();
		try {
			String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username + "))";
			SearchResult searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
			Attributes attributes = null;
			if (searchResult != null) {
				attributes = searchResult.getAttributes();
			} else {
				LOG.info(this + "No user found for username: " + username);
				userStatusVO.setStatus("Error");
				userStatusVO.setErrorMessage("No user found for username: " + username);
				return new ResponseEntity<>(userStatusVO, HttpStatus.NOT_FOUND);
			}
			if (attributes != null) {
				for (NamingEnumeration ae = attributes.getAll(); ae.hasMore();) {
					Attribute attr = (Attribute) ae.next();
					if (attr.getID().equalsIgnoreCase("obfirstlogin")) {
						if (attr.get() != null)
							if (attr.get().equals("true")) {
								userStatusVO.setMigratedUserFirstLogin("true");
							} else if (attr.get().equals("false")) {
								userStatusVO.setMigratedUserFirstLogin("false");
							}
					} else if (attr.getID().equalsIgnoreCase("obpasswordchangeflag")) {
						if (attr.get() != null)
							if (attr.get().equals("0")) {
								userStatusVO.setChangePassword("false");
							} else if (attr.get().equals("1")) {
								userStatusVO.setChangePassword("true");
							}
					}
				}
			}
			userStatusVO.setStatus("Success");
			userStatusVO.setMigratedUserFirstLogin(userStatusVO.getMigratedUserFirstLogin());
			userStatusVO.setChangePassword(userStatusVO.getChangePassword());
			return new ResponseEntity<>(userStatusVO, HttpStatus.OK);
		} catch (Exception e) {
			LOG.error(this + " Error on getUserStatus. ERROR: " + e.getMessage());
			userStatusVO.setStatus("Error");
			userStatusVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(userStatusVO, HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			dirContext = null;
		}
	}

	/**
	 * Fetches the roles for the user
	 * @param username
	 * @return Roles with success status else error status
	 */
	@RequestMapping(value = "fetchUserRoles", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<RoleListVO> fetchUserRoles(@RequestParam("username") String username) {
		DirContext dirContext = null;  
		roleListVO.setErrorMessage(null);
		roleListVO.setStatus(null);
		roleListVO.setRole(null); 

		if (username == null || username.isEmpty()) {
			roleListVO.setStatus("Error");
			roleListVO.setErrorMessage("Mandatory field : Username");
			return new ResponseEntity<>(roleListVO, HttpStatus.NOT_FOUND);
		}

		try {
			dirContext = ovdLoginProxy.connect();
		} catch (InvalidNameException ine) {
			LOG.error(this + " fetchUserRoles: ERROR: " + ine.getMessage());
			ine.printStackTrace();
		} catch (NamingException ne) {
			LOG.error(this + " fetchUserRoles: ERROR: " + ne.getMessage());
			ne.printStackTrace();
		}
		if (dirContext == null) {
			roleListVO.setStatus("Error");
			roleListVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(roleListVO, HttpStatus.SERVICE_UNAVAILABLE);
		}

		try {
			String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username + "))";
			SearchResult searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
			Attributes attributes = null;
			if (searchResult != null) {
				attributes = searchResult.getAttributes();
			} else {
				LOG.info(this + "No user found for username: " + username);
				roleListVO.setStatus("Error");
				roleListVO.setErrorMessage("No user found for username: " + username);
				return new ResponseEntity<>(roleListVO, HttpStatus.NOT_FOUND);
			}

			ArrayList<RoleVO> memberOfList = new ArrayList<RoleVO>();
			
			if (attributes.get("memberOf") != null) {
				for (Enumeration vals = attributes.get("memberOf").getAll(); vals.hasMoreElements();) {
					String[] myData = vals.nextElement().toString().toLowerCase().split("cn=");
					int i = 0;
					for (String str : myData) {
						String group = str.split(",")[0];
						if (!group.contains("admin group")) {
							group = group.replace("r-", "").replace("members group", "").trim();
							RoleVO roleVO = new RoleVO();
							if (group.length() > 0) {
								roleVO.setName(group);
								memberOfList.add(roleVO);
							}
						}
					}

				}
			}
			if (attributes.get("ismemberOf") != null) {
				for (Enumeration vals = attributes.get("ismemberOf").getAll(); vals.hasMoreElements();) {
					String[] myData = vals.nextElement().toString().toLowerCase().split("cn=");
					for (String str : myData) {
						String group = str.split(",")[0];
						if (!group.contains("admin group") && !group.contains("groups")) {
							group = group.replace("r-", "").replace("members group", "").trim();
							RoleVO roleVO = new RoleVO();
							if (group.length() > 0) {
								roleVO.setName(group);
								memberOfList.add(roleVO);
							}
						}
					}
				}
			}
			if (memberOfList.size() > 0) {
				roleListVO.setStatus("Success");
				roleListVO.setRole(memberOfList);
				return new ResponseEntity<>(roleListVO, HttpStatus.OK);
			} else {
				LOG.error(this + " fetchUserRoles: No roles granted for username: " + username);
				roleListVO.setStatus("Error");
				roleListVO.setErrorMessage("No roles granted for username: " + username);
				return new ResponseEntity<>(roleListVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on fetchUserRoles " + e.getMessage());
			roleListVO.setStatus("Error");
			roleListVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(roleListVO, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			dirContext = null;
		}
	}

	/**
	 * This method checks the username existance already. 
	 * @param username: Username is the input value
	 * @return Returns Success (status 200) if found else error status
	 */
	@RequestMapping(value = "checkExistingUserName", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<CheckExistingUsernameOutVO> checkExistingUserName(@RequestParam("username") String username) {
		OIMClient oimClient = null;
		oimClient = oimLoginProxy.userLogin();
		checkExistingUsernameOutVO.setErrorMessage(null);
		checkExistingUsernameOutVO.setStatus(null);
		checkExistingUsernameOutVO.setUsernameExists(null);

		if (username == null || username.isEmpty()) {
			checkExistingUsernameOutVO.setStatus("Error");
			checkExistingUsernameOutVO.setErrorMessage("Mandatory field : Username");
			return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.NOT_FOUND);
		}
		try {
			if (oimClient == null) {
				LOG.error(this + " checkExistingUserName: Failed  to OIMCLient login");
				checkExistingUsernameOutVO.setStatus("Error");
				checkExistingUsernameOutVO.setErrorMessage("System unavailable at the moment, please try again");
				return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.SERVICE_UNAVAILABLE);
			}

			LOG.info(this + " checkExistingUserName Logged into OIMClient  ");
			if (userManagerService.checkExistingUserName(oimClient, username)) {
				checkExistingUsernameOutVO.setStatus("Success");
				checkExistingUsernameOutVO.setUsernameExists("true");
				return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.OK);
			} else {
				LOG.error(this + " checkExistingUserName: No user found for username: " + username);
				checkExistingUsernameOutVO.setStatus("Error");
				checkExistingUsernameOutVO.setErrorMessage("No user found for username: " + username);
				return new ResponseEntity<>(checkExistingUsernameOutVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on checkExistingUserName " + e.getMessage());
			checkExistingUsernameOutVO.setStatus("Error");
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