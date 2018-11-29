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
import za.co.icurity.usermanagement.util.AttributesUtil;
import za.co.icurity.usermanagement.util.GenerateSSAID;
import za.co.icurity.usermanagement.util.StringUtil;
import za.co.icurity.usermanagement.util.ValidateADUsernameInVOUtil;
import za.co.icurity.usermanagement.util.ValidateCheckExistingUserInVOUtil;
import za.co.icurity.usermanagement.util.ValidateUserInVOUtil;
import za.co.icurity.usermanagement.vo.CheckExistingCellphoneOutVO;
import za.co.icurity.usermanagement.vo.CheckExistingEmployeeNoOutVO;
import za.co.icurity.usermanagement.vo.CheckExistingSSAIDOutVO;
import za.co.icurity.usermanagement.vo.CheckExistingUserInVO;
import za.co.icurity.usermanagement.vo.CheckExistingUserOutVO;
import za.co.icurity.usermanagement.vo.CheckExistingUsernameOutVO;
import za.co.icurity.usermanagement.vo.FetchUserRolesInVO;
import za.co.icurity.usermanagement.vo.GetADUsernameInVO;
import za.co.icurity.usermanagement.vo.GetADUsernameOutVO;
import za.co.icurity.usermanagement.vo.GetUserAccountLockedOutVO;
import za.co.icurity.usermanagement.vo.ProvisionUserAccountVO;
import za.co.icurity.usermanagement.vo.RoleListVO;
import za.co.icurity.usermanagement.vo.RoleVO;
import za.co.icurity.usermanagement.vo.StatusOutVO;
import za.co.icurity.usermanagement.vo.UserDetailsVO;
import za.co.icurity.usermanagement.vo.UserInVO;
import za.co.icurity.usermanagement.vo.UserOutVO;
import za.co.icurity.usermanagement.vo.UserStatusVO;
import za.co.icurity.usermanagement.vo.UsernameVO;

/**
 * @author
 *
 */
/**
 * @author cofasys
 *
 */
/**
 * @author cofasys
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
	private CheckExistingUserOutVO checkExistingUserOutVO;
	@Autowired
	private UserStatusVO userStatusVO;
	@Autowired
	private RoleListVO roleListVO;
	@Autowired
	private CheckExistingUserInVO checkExistingUserInVO;
	@Autowired
	private ValidateCheckExistingUserInVOUtil validateCheckExistingUserInVOUtil;
	@Autowired
	private CheckExistingSSAIDOutVO checkExistingSSAIDOutVO;
	@Autowired
	private UserDetailsVO userDetailsVO;
	@Autowired
	private AttributesUtil attributesUtil;
	@Autowired
	private GetADUsernameInVO getADUsernameInVO;
	@Autowired
	private GetADUsernameOutVO getADUsernameOutVO;
	@Autowired
	private ValidateADUsernameInVOUtil validateADUsernameInVOUtil;
	@Autowired
	private GetUserAccountLockedOutVO getUserAccountLockedOutVO;
	@Autowired
	private CheckExistingEmployeeNoOutVO checkExistingEmployeeNoOutVO;
	@Autowired
	private CheckExistingCellphoneOutVO checkExistingCellphoneOutVO;
	@Autowired
	private StringUtil stringUtil;

	static final String searchBase = "cn=users";

	/**
	 * Create a user in OIM and provision
	 * 
	 * @param userInVO contains input values
	 * @return UserOutVO or exception message
	 * @throws Exception
	 */

	// @RequestMapping(value = "/createUser", method = RequestMethod.POST, headers =
	// "Accept=application/json")
	public ResponseEntity<UserOutVO> createUser(@RequestBody UserInVO userInVO) throws Exception {
		OIMClient oimClient = null;
		userOutVO.setUserId(null);
		userOutVO.setErrorMessage(null);
		userOutVO.setUserId(null);
		userOutVO.setErrorCode(null);
		String ssaid = null;

		// validate input fields - returns userOutVO with status
		if (validateUserInVOUtil.validateUserInVO(userInVO).getStatus().equalsIgnoreCase("Error")) {
			LOG.error(this + " createUser " + userOutVO.getErrorMessage());
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
			if (!userManagerService.checkExistingEmployeeNumber(oimClient, ssaid)) { // confirm with Mocx
				userInVO.setSsaId(ssaid);
				break;
			}
		}
		// create user
		userOutVO = userManagerService.createUser(oimClient, userInVO);
		if (userOutVO.getStatus().equalsIgnoreCase("Error")) {
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
				if (statusOutVO.getStatus().equalsIgnoreCase("Error")) {
					userOutVO.setStatus("Error");
					userOutVO.setErrorMessage(userOutVO.getErrorMessage());
					return new ResponseEntity<>(userOutVO, HttpStatus.SERVICE_UNAVAILABLE);
				} else {
					userOutVO.setStatus("Success");
					userOutVO.setErrorMessage("User " + userInVO.getUsername() + " successfully created ");
					return new ResponseEntity<>(userOutVO, HttpStatus.CREATED);
				}
			} else {
				userOutVO.setStatus("Error");
				userOutVO.setErrorMessage(userOutVO.getErrorMessage());
				return new ResponseEntity<>(userOutVO, HttpStatus.SERVICE_UNAVAILABLE);
			}
		} catch (Exception e) {
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
	 * 
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
			LOG.error(this + " userstatus: Mandatory field : Username " + username);
			return new ResponseEntity<>(userStatusVO, HttpStatus.BAD_REQUEST);
		}

		DirContext dirContext = ovdLoginProxy.connect();

		if (dirContext == null) {
			for (int i = 0; i < 3; i++) {
				Thread.sleep(1000);
				dirContext = ovdLoginProxy.connect();
				if (dirContext != null) {
					break;
				}
			}
		}

		if (dirContext == null) {
			userStatusVO.setStatus("Error");
			userStatusVO.setErrorMessage("System unavailable at the moment, please try again");
			LOG.error(this + " userstatus: Failed login to OVD (dirContext is null)  for the user" + username);
			return new ResponseEntity<>(userStatusVO, HttpStatus.SERVICE_UNAVAILABLE);
		}

		try {
			// String searchFilter = "(|(uid=" + usernameVO.getUsername() + ")(omUserAlias="
			// + usernameVO.getUsername()+ "))";
			// String searchFilter = "(|(uid=" + username + ")(omUserAlias=" + username +
			// ")(cn=" + username + "))";
			String usernametmp = stringUtil.escapeMetaCharacters(username);

			//String searchFilter = "(|(uid=" + usernametmp + ")(omUserAlias=" + usernametmp + "))";
			String searchFilter = "(|(uid=" + usernametmp + ")(cn=" + usernametmp + "))";

			SearchResult searchResult = null;

			try {
				searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
			} catch (Exception e) {
				userStatusVO.setStatus("Error");
				userStatusVO.setErrorMessage("Failed to search the user " + username);
				LOG.error(this + " userstatus: Failed to search with the searchFilter " + searchFilter + " for the user " + username);
				e.printStackTrace();
				return new ResponseEntity<>(userStatusVO, HttpStatus.SERVICE_UNAVAILABLE);
			}

			Attributes attributes = null;
			if (searchResult != null) {
				attributes = searchResult.getAttributes();
				LOG.info(this + " userstatus: User attributes from ovd found for the user " + username);
			} else {
				LOG.info(this + " userstatus: No user found for username: " + username);
				userStatusVO.setStatus("Error");
				userStatusVO.setErrorMessage("No user found for username: " + username);
				return new ResponseEntity<>(userStatusVO, HttpStatus.OK);
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
			LOG.info(this + " User status Succeess for the user: " + username);
			return new ResponseEntity<>(userStatusVO, HttpStatus.OK);
		} catch (Exception e) {
			userStatusVO.setStatus("Error");
			userStatusVO.setErrorMessage("System unavailable at the moment, please try again");
			LOG.error(this + " Error on getUserStatus. ERROR: " + e.getMessage());
			return new ResponseEntity<>(userStatusVO, HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			if (dirContext != null) {
				dirContext.close();
			}
		}
	}

	/**
	 * Fetches the roles for the user
	 * 
	 * @param username
	 * @return Roles with success status else error status
	 */
	@RequestMapping(value = "fetchUserRoles", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<RoleListVO> fetchUserRoles(@RequestParam("username") String username) throws Exception {
		DirContext dirContext = null;
		roleListVO.setErrorMessage(null);
		roleListVO.setStatus(null);
		roleListVO.setRoles(null);
		LOG.info(this + " Entering into fetchUserRoles " + username);
		if (username == null || username.isEmpty()) {
			roleListVO.setStatus("Error");
			roleListVO.setErrorMessage("Mandatory field : Username");
			LOG.error(this + " fetchUserRoles: Mandatory field : Username " + username);
			return new ResponseEntity<>(roleListVO, HttpStatus.BAD_REQUEST);
		}

		/*
		 * try { dirContext = ovdLoginProxy.connect(); LOG.info(this +
		 * " fetchUserRolesUser: OVD logged in for the user " + username); } catch
		 * (InvalidNameException ine) { LOG.error(this + " fetchUserRoles: ERROR: " +
		 * ine.getMessage()); ine.printStackTrace(); } catch (NamingException ne) {
		 * LOG.error(this + " fetchUserRoles: ERROR: " + ne.getMessage());
		 * ne.printStackTrace(); } if (dirContext == null) { LOG.error(this +
		 * " fetchUserRoles: ERROR on login to ovd for the user  "+username);
		 * roleListVO.setStatus("Error"); roleListVO.
		 * setErrorMessage("System unavailable at the moment, please try again"); return
		 * new ResponseEntity<>(roleListVO, HttpStatus.SERVICE_UNAVAILABLE); }
		 */

		dirContext = ovdLoginProxy.connect();

		if (dirContext == null) {
			for (int i = 0; i < 3; i++) {
				Thread.sleep(1000);
				dirContext = ovdLoginProxy.connect();
				if (dirContext != null) {
					break;
				}
			}
		}

		if (dirContext == null) {
			roleListVO.setStatus("Error");
			roleListVO.setErrorMessage("System unavailable at the moment, please try again");
			LOG.error(this + " fetchUserRoles: Failed login to OVD (dirContext is null)  for the user" + username);
			return new ResponseEntity<>(roleListVO, HttpStatus.SERVICE_UNAVAILABLE);
		}

		try {
			String usernametmp = stringUtil.escapeMetaCharacters(username);
			//String searchFilter = "(|(uid=" + usernametmp + ")(omUserAlias=" + usernametmp + "))";
			String searchFilter = "(|(uid=" + usernametmp + ")(cn=" + usernametmp + "))";
			LOG.info(this + " fetchUserRoles search filter ");
			SearchResult searchResult = null;
			try {
				searchResult = ovdLoginProxy.findUserAttributes(dirContext, searchBase, searchFilter);
			} catch (Exception e) {
				roleListVO.setStatus("Error");
				roleListVO.setErrorMessage("Failed to search the user " + username);
				LOG.error(this + " fetchUserRoles: Failed to search with the searchFilter " + searchFilter + " for the user " + username);
				return new ResponseEntity<>(roleListVO, HttpStatus.SERVICE_UNAVAILABLE);
			}

			Attributes attributes = null;
			if (searchResult != null) {
				attributes = searchResult.getAttributes();
			} else {
				roleListVO.setStatus("Error");
				roleListVO.setErrorMessage("No user found for username: " + username);
				LOG.info(this + " fetchUserRoles: No user found for username: " + username);
				return new ResponseEntity<>(roleListVO, HttpStatus.OK);
			}

			ArrayList<RoleVO> memberOfList = new ArrayList<RoleVO>();

			if (attributes.get("memberOf") != null) {
				for (Enumeration vals = attributes.get("memberOf").getAll(); vals.hasMoreElements();) {
					String[] myData = vals.nextElement().toString().split("CN=");
					// int i = 0;
					for (String str : myData) {
						String group = str.split(",")[0];
						if (!group.contains("Admin Group")) {
							group = group.replace("R-", "").replace("Members Group", "").trim();
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
					String[] myData = vals.nextElement().toString().split("cn=");
					for (String str : myData) {
						String group = str.split(",")[0];
						if (!group.contains("Admin Group") && !group.contains("groups")) {
							group = group.replace("R-", "").replace("Members Group", "").trim();
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
				roleListVO.setRoles(memberOfList);
				LOG.info(this + " fetchUserRoles: Fetch user roles success for: " + username);
				return new ResponseEntity<>(roleListVO, HttpStatus.OK);
			} else {

				roleListVO.setStatus("Error");
				roleListVO.setErrorMessage("No roles granted for username: " + username);
				LOG.error(this + " fetchUserRoles: No roles granted for username: " + username);
				return new ResponseEntity<>(roleListVO, HttpStatus.OK);
			}
		} catch (Exception e) {
			roleListVO.setStatus("Error");
			roleListVO.setErrorMessage("System unavailable at the moment, please try again");
			LOG.error(this + " Error on fetchUserRoles " + e.getMessage());
			return new ResponseEntity<>(roleListVO, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (dirContext != null) {
				dirContext.close();
			}
		}
	}

	/**
	 * This method checks the username existance already.
	 * 
	 * @param username: Username is the input value
	 * @return Returns Success (status 200) if found else error status
	 */
	// @RequestMapping(value = "checkExistingUserName", method = RequestMethod.GET,
	// headers = "Accept=application/json")
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

	/**
	 * @param employeeNumber
	 * @return
	 */
	// @RequestMapping(value = "checkExistingEmployeeNumber", method =
	// RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<CheckExistingEmployeeNoOutVO> checkExistingEmployeeNumber(@RequestParam("employeeNumber") String employeeNumber) {
		OIMClient oimClient = null;
		oimClient = oimLoginProxy.userLogin();
		checkExistingEmployeeNoOutVO.setErrorMessage(null);
		checkExistingEmployeeNoOutVO.setStatus(null);
		checkExistingEmployeeNoOutVO.setEmployeeNoExists(null);

		if (employeeNumber == null || employeeNumber.isEmpty()) {
			checkExistingEmployeeNoOutVO.setStatus("Error");
			checkExistingEmployeeNoOutVO.setErrorMessage("Mandatory field : employeeNumber");
			return new ResponseEntity<>(checkExistingEmployeeNoOutVO, HttpStatus.NOT_FOUND);
		}
		try {
			if (oimClient == null) {
				LOG.error(this + " checkExistingEmployeeNumber: Failed  to OIMCLient login");
				checkExistingEmployeeNoOutVO.setStatus("Error");
				checkExistingEmployeeNoOutVO.setErrorMessage("System unavailable at the moment, please try again");
				return new ResponseEntity<>(checkExistingEmployeeNoOutVO, HttpStatus.SERVICE_UNAVAILABLE);
			}
			LOG.info(this + " checkExistingEmployeeNumber Logged into OIMClient  ");
			if (userManagerService.checkExistingEmployeeNumber(oimClient, employeeNumber)) {
				checkExistingEmployeeNoOutVO.setStatus("Success");
				checkExistingEmployeeNoOutVO.setEmployeeNoExists("true");
				return new ResponseEntity<>(checkExistingEmployeeNoOutVO, HttpStatus.OK);
			} else {
				LOG.error(this + " checkExistingEmployeeNumber: No user found for username: " + employeeNumber);
				checkExistingEmployeeNoOutVO.setStatus("Error");
				checkExistingEmployeeNoOutVO.setEmployeeNoExists("false");
				return new ResponseEntity<>(checkExistingEmployeeNoOutVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on checkExistingEmployeeNumber " + e.getMessage());
			checkExistingEmployeeNoOutVO.setStatus("Error");
			checkExistingEmployeeNoOutVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(checkExistingEmployeeNoOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (oimClient != null) {
				oimClient.logout();
				oimClient = null;
			}
		}
	}

	// @RequestMapping(value = "checkExistingCellphone", method = RequestMethod.GET,
	// headers = "Accept=application/json")
	public ResponseEntity<CheckExistingCellphoneOutVO> checkExistingCellphone(@RequestParam("cellNumber") String cellNumber) {
		OIMClient oimClient = null;
		oimClient = oimLoginProxy.userLogin();
		checkExistingCellphoneOutVO.setErrorMessage(null);
		checkExistingCellphoneOutVO.setStatus(null);
		checkExistingCellphoneOutVO.setCellphoneExists(null);

		if (cellNumber == null || cellNumber.isEmpty()) {
			checkExistingCellphoneOutVO.setStatus("Error");
			checkExistingCellphoneOutVO.setErrorMessage("Mandatory field : Cellphone Number");
			return new ResponseEntity<>(checkExistingCellphoneOutVO, HttpStatus.NOT_FOUND);
		}
		try {
			if (oimClient == null) {
				LOG.error(this + " checkExistingCellphone: Failed  to OIMCLient login");
				checkExistingCellphoneOutVO.setStatus("Error");
				checkExistingCellphoneOutVO.setErrorMessage("System unavailable at the moment, please try again");
				return new ResponseEntity<>(checkExistingCellphoneOutVO, HttpStatus.SERVICE_UNAVAILABLE);
			}
			LOG.info(this + " checkExistingCellphone Logged into OIMClient  ");
			if (userManagerService.checkExistingCellphoneAttribute(oimClient, cellNumber)) {
				checkExistingCellphoneOutVO.setStatus("Success");
				checkExistingCellphoneOutVO.setCellphoneExists("true");
				return new ResponseEntity<>(checkExistingCellphoneOutVO, HttpStatus.OK);
			} else {
				LOG.error(this + " checkExistingCellphone: No cell number found : " + cellNumber);
				checkExistingCellphoneOutVO.setStatus("Error");
				checkExistingCellphoneOutVO.setCellphoneExists("false");
				return new ResponseEntity<>(checkExistingCellphoneOutVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on checkExistingCellphone " + e.getMessage());
			checkExistingCellphoneOutVO.setStatus("Error");
			checkExistingCellphoneOutVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(checkExistingCellphoneOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (oimClient != null) {
				oimClient.logout();
				oimClient = null;
			}
		}
	}

	/**
	 * @param checkExistingUserInVO
	 * @return true if the user found
	 */
	// @RequestMapping(value = "checkExistingUser", method = RequestMethod.GET,
	// headers = "Accept=application/json")
	public ResponseEntity<CheckExistingUserOutVO> checkExistingUser(CheckExistingUserInVO checkExistingUserInVO) {
		OIMClient oimClient = null;
		oimClient = oimLoginProxy.userLogin();
		checkExistingUserOutVO.setErrorMessage(null);
		checkExistingUserOutVO.setStatus(null);
		checkExistingUserOutVO.setUserExists(null);

		if (validateCheckExistingUserInVOUtil.validateCheckExistingUserInVO(checkExistingUserInVO).getStatus().equalsIgnoreCase("Error")) {
			LOG.error("checkExistingUser " + userOutVO.getErrorMessage());
			checkExistingUserOutVO.setStatus("Error");
			checkExistingUserOutVO.setErrorMessage(userOutVO.getErrorMessage());
			return new ResponseEntity<>(checkExistingUserOutVO, HttpStatus.BAD_REQUEST);
		}

		try {
			if (oimClient == null) {
				checkExistingUserOutVO.setStatus("Error");
				checkExistingUserOutVO.setErrorMessage("System unavailable at the moment, please try again");
				return new ResponseEntity<>(checkExistingUserOutVO, HttpStatus.SERVICE_UNAVAILABLE);
			}
			LOG.info(this + " checkExistingUser Logged into OIMClient  ");
			if (userManagerService.checkExistingUser(oimClient, checkExistingUserInVO)) {
				checkExistingUserOutVO.setStatus("Success");
				checkExistingUserOutVO.setUserExists("true");
				return new ResponseEntity<>(checkExistingUserOutVO, HttpStatus.OK);
			} else {
				LOG.error(this + " checkExistingUser: No user found with specified details. Please provide valid input. " + checkExistingUserInVO.getLastName());
				checkExistingUserOutVO.setStatus("Error");
				checkExistingUserOutVO.setErrorMessage("No user found with specified details. Please provide valid input.");
				return new ResponseEntity<>(checkExistingUserOutVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on checkExistingUser " + e.getMessage());
			checkExistingUserOutVO.setStatus("Error");
			checkExistingUserOutVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(checkExistingUserOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (oimClient != null) {
				oimClient.logout();
				oimClient = null;
			}
		}
	}

	/**
	 * @param ssaid
	 * @return
	 */
	// @RequestMapping(value = "checkExistingSSAID", method = RequestMethod.GET,
	// headers = "Accept=application/json")
	public ResponseEntity<CheckExistingSSAIDOutVO> checkExistingSSAID(@RequestParam("ssaid") String ssaid) {
		OIMClient oimClient = null;
		oimClient = oimLoginProxy.userLogin();
		checkExistingSSAIDOutVO.setErrorMessage(null);
		checkExistingSSAIDOutVO.setStatus(null);
		checkExistingSSAIDOutVO.setSsaid(null);

		if (ssaid == null || ssaid.isEmpty()) {
			checkExistingSSAIDOutVO.setStatus("Error");
			checkExistingSSAIDOutVO.setErrorMessage("Mandatory field : Username");
			return new ResponseEntity<>(checkExistingSSAIDOutVO, HttpStatus.NOT_FOUND);
		}

		try {
			if (oimClient == null) {
				checkExistingUserOutVO.setStatus("Error");
				checkExistingUserOutVO.setErrorMessage("System unavailable at the moment, please try again");
				return new ResponseEntity<>(checkExistingSSAIDOutVO, HttpStatus.SERVICE_UNAVAILABLE);
			}
			if (userManagerService.checkExistingEmployeeNumber(oimClient, ssaid)) { // confirm with Mocx and RK about checking with employee number
				checkExistingSSAIDOutVO.setStatus("Success");
				checkExistingSSAIDOutVO.setSsaid("true");
				return new ResponseEntity<>(checkExistingSSAIDOutVO, HttpStatus.OK);
			} else {
				checkExistingSSAIDOutVO.setStatus("Error");
				checkExistingSSAIDOutVO.setErrorMessage("No user found for ssaid " + ssaid);
				return new ResponseEntity<>(checkExistingSSAIDOutVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on checkExistingSSAID " + e.getMessage());
			checkExistingUsernameOutVO.setStatus("Error");
			checkExistingUsernameOutVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(checkExistingSSAIDOutVO, HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (oimClient != null) {
				oimClient.logout();
				oimClient = null;
			}
		}
	}

	// @RequestMapping(value = "userdetails", method = RequestMethod.GET, headers =
	// "Accept=application/json")
	public ResponseEntity<UserDetailsVO> getUserDetails(@RequestParam("username") String username) throws Exception {
		userDetailsVO.setErrorMessage(null);
		userDetailsVO.setStatus(null);
		userDetailsVO.setStatus(null);
		userDetailsVO.setFirstName(null);
		userDetailsVO.setLastName(null);
		userDetailsVO.setCellPhoneNumber(null);
		userDetailsVO.setDateOfBirth(null);
		userDetailsVO.setIdNumber(null);
		userDetailsVO.setMigratedUserFirstLogin(null);
		userDetailsVO.setUserAccountLocked(null);
		userDetailsVO.setIdType(null);
		userDetailsVO.setEmployeeNumber(null);

		if (username == null || username.isEmpty()) {
			userDetailsVO.setStatus("Error");
			userDetailsVO.setErrorMessage("Mandatory field : Username");
			return new ResponseEntity<>(userDetailsVO, HttpStatus.NOT_FOUND);
		}
		DirContext dirContext = ovdLoginProxy.connect();
		try {
			userDetailsVO = attributesUtil.getUserDetailsAttributes(dirContext, username);

			if (userDetailsVO.getStatus().equalsIgnoreCase("Success")) {
				return new ResponseEntity<>(userDetailsVO, HttpStatus.OK);
			} else {
				userDetailsVO.setStatus("Error");
				userDetailsVO.setErrorMessage("No user found for username: " + username);
				return new ResponseEntity<>(userDetailsVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on getUserStatus. ERROR: " + e.getMessage());
			userStatusVO.setStatus("Error");
			userStatusVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(userDetailsVO, HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			dirContext = null;
		}
	}

	/*
	 * @RequestMapping(value = "getADUsername", method = RequestMethod.GET, headers
	 * = "Accept=application/json") public ResponseEntity<GetADUsernameOutVO>
	 * getADUsername(GetADUsernameInVO getADUsernameInVO) throws Exception {
	 * 
	 * OIMClient oimClient = null; getADUsernameOutVO.setErrorMessage(null);
	 * getADUsernameOutVO.setStatus(null);
	 * 
	 * if(validateADUsernameInVOUtil.validateADUsernameInVO(getADUsernameInVO).
	 * getStatus().equalsIgnoreCase("Error")) { LOG.error("validateUserInVO " +
	 * userOutVO.getErrorMessage()); getADUsernameOutVO.setStatus("Error");
	 * getADUsernameOutVO.setErrorMessage(userOutVO.getErrorMessage()); return new
	 * ResponseEntity<>(getADUsernameOutVO, HttpStatus.BAD_REQUEST); } oimClient =
	 * oimLoginProxy.userLogin(); try { if (oimClient == null) {
	 * checkExistingUserOutVO.setStatus("Error"); checkExistingUserOutVO.
	 * setErrorMessage("System unavailable at the moment, please try again"); return
	 * new ResponseEntity<>(getADUsernameOutVO, HttpStatus.SERVICE_UNAVAILABLE); }
	 * 
	 * if (userManagerService.getADUsernameAttributes(oimClient,
	 * getADUsernameInVO).getStatus().equalsIgnoreCase("Error")) {
	 * getADUsernameOutVO.setStatus("Error");
	 * getADUsernameOutVO.setErrorMessage("No username/alias could be retrieved");
	 * return new ResponseEntity<>(getADUsernameOutVO, HttpStatus.NOT_FOUND); }else
	 * { return new ResponseEntity<>(getADUsernameOutVO, HttpStatus.OK); } } catch
	 * (Exception e) { LOG.error(this + " Error on getUserStatus. ERROR: " +
	 * e.getMessage()); userStatusVO.setStatus("Error"); userStatusVO.
	 * setErrorMessage("System unavailable at the moment, please try again"); return
	 * new ResponseEntity<>(getADUsernameOutVO, HttpStatus.SERVICE_UNAVAILABLE);
	 * }finally { if (oimClient != null) { oimClient.logout(); oimClient = null; } }
	 * }
	 */

	// @RequestMapping(value = "getUserAccountLocked", method = RequestMethod.GET,
	// headers = "Accept=application/json")
	public ResponseEntity<GetUserAccountLockedOutVO> getUserAccountLocked(@RequestParam("username") String username) throws Exception {
		getUserAccountLockedOutVO.setUserAccountStatus(null);
		if (username == null || username.isEmpty()) {
			getUserAccountLockedOutVO.setStatus("Error");
			getUserAccountLockedOutVO.setErrorMessage("Mandatory field : Username");
			return new ResponseEntity<>(getUserAccountLockedOutVO, HttpStatus.NOT_FOUND);
		}
		DirContext dirContext = ovdLoginProxy.connect();
		try {
			getUserAccountLockedOutVO = attributesUtil.getUserAccountLockedAttributes(dirContext, username);

			if (getUserAccountLockedOutVO.getStatus().equalsIgnoreCase("Success")) {
				return new ResponseEntity<>(getUserAccountLockedOutVO, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(getUserAccountLockedOutVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error(this + " Error on getUserAccountLocked. ERROR: " + e.getMessage());
			userStatusVO.setStatus("Error");
			userStatusVO.setErrorMessage("System unavailable at the moment, please try again");
			return new ResponseEntity<>(getUserAccountLockedOutVO, HttpStatus.SERVICE_UNAVAILABLE);
		} finally {
			dirContext = null;
		}
	}

}