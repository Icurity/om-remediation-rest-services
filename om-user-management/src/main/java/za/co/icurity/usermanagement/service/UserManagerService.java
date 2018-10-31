package za.co.icurity.usermanagement.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import oracle.iam.identity.rolemgmt.api.RoleManager;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.identity.usermgmt.vo.UserManagerResult;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;
import oracle.iam.provisioning.api.ApplicationInstanceService;
import oracle.iam.provisioning.api.ProvisioningService;
import oracle.iam.provisioning.vo.Account;
import oracle.iam.provisioning.vo.AccountData;
import oracle.iam.provisioning.vo.ApplicationInstance;
import za.co.icurity.usermanagement.util.DecryptPasswordUtil;
import za.co.icurity.usermanagement.util.PropertiesUtil;
import za.co.icurity.usermanagement.vo.ProvisionUserAccountVO;
import za.co.icurity.usermanagement.vo.StatusOutVO;
import za.co.icurity.usermanagement.vo.UserInVO;
import za.co.icurity.usermanagement.vo.UserOutVO;
import za.co.icurity.usermanagement.vo.UserStatusVO;
import za.co.icurity.usermanagement.vo.UsernameVO;


@Component
public class UserManagerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserManagerService.class);
	@Autowired
	private UserOutVO userOutVO;
	@Autowired
	private UserInVO userInVO;
	@Autowired
	private DecryptPasswordUtil descriptPassword;

	/**
	 * @param oimClient
	 * @param userInVO
	 * @return UserOutVO 
	 */
	public UserOutVO createUser(OIMClient oimClient, UserInVO userInVO) {

		HashMap<String, Object> mapAttrs = null;
		UserManagerResult result = null;
		User user = null;

		mapAttrs = new HashMap<String, Object>();
		if (userInVO.getFirstName() != null) {
			mapAttrs.put("First Name", userInVO.getFirstName());
		}
		if (userInVO.getLastName() != null) {
			mapAttrs.put("Last Name", userInVO.getLastName());
		}
		if (userInVO.getPassword() != null) {
			try {
				userInVO.setPassword(descriptPassword.decrypt(userInVO.getPassword()));
			} catch (Exception e) {
				LOG.error(this + " createUser\", \"Error: " + e.getMessage());
				/*
				 * if (logger.isLoggable(Level.SEVERE)) { logger.logp(Level.SEVERE, CLASS_NAME,
				 * "createUser", "Error: " + e.getMessage()); }
				 */
				userOutVO.setStatus("Error");
				userOutVO.setErrorMessage("Exception while decrypting the password");
				return userOutVO;
			}
			mapAttrs.put("usr_password", userInVO.getPassword());
		}
		if (userInVO.getCountry() != null) {
			mapAttrs.put("Country", userInVO.getCountry());
		}
		if (userInVO.getCellPhoneNumber() != null) {
			mapAttrs.put("Telephone Number", userInVO.getCellPhoneNumber());
		}
		if (userInVO.getUsername() != null) {
			mapAttrs.put("User Login", userInVO.getUsername());
		}
		if (userInVO.getDateOfBirth() != null) {
			mapAttrs.put("birth_date", userInVO.getDateOfBirth());
		}
		if (userInVO.getIdNumber() != null) {
			mapAttrs.put("id_number", userInVO.getIdNumber());
		}
		if (userInVO.getSsaId() != null) {
			mapAttrs.put("Employee Number", userInVO.getSsaId());
			mapAttrs.put("Common Name", userInVO.getSsaId());
		}
		if (userInVO.getGender() != null) {
			mapAttrs.put("gender", userInVO.getGender());
		}
		if (userInVO.getIdType() != null) {
			if ("RSA-ID".equalsIgnoreCase(userInVO.getIdType()))
				mapAttrs.put("id_type", "RSA ID");
			else
				mapAttrs.put("id_type", userInVO.getIdType());
		}
		if (userInVO.getCountryCode() != null) {
			mapAttrs.put("country_code", userInVO.getCountryCode());
		}
		if (userInVO.getCountryName() != null) {
			mapAttrs.put("cellphone_country", userInVO.getCountryName());
		}

		mapAttrs.put("cell_validated", String.valueOf(userInVO.isCellPhoneValidated()));
		mapAttrs.put("act_key", 1L);
		mapAttrs.put("Role", "Full-Time");
		mapAttrs.put("usr_change_pwd_at_next_logon", "0");
		mapAttrs.put("pwd_flag", "false");
		mapAttrs.put("migr_firstLogin", "false");

		user = new User(userInVO.getUsername(), mapAttrs);

		/*
		 * if (logger.isLoggable(Level.FINE)) { logger.logp(Level.FINE, CLASS_NAME,
		 * "createUser", "User object created : " + userInVO.getUsername()); }
		 */

		try {
			UserManager userManager = oimClient.getService(UserManager.class);
			result = userManager.create(user);
			userOutVO.setUserId(result.getEntityId());
			LOG.info(this + " User object created : " + userInVO.getUsername());
			System.out.println("User created successfully");
		} catch (Exception ex) {
			System.out.println("User creation failed");
			ex.printStackTrace();
		}
		return userOutVO;
	}

	public boolean checkExistingEmployeeNumber(OIMClient oimClient, String employeeNumber) {

		List<User> users = null;
		HashMap<String, Object> parameters = null;
		Set<String> attrNames = null;
		SearchCriteria criteria = new SearchCriteria("Employee Number", employeeNumber, SearchCriteria.Operator.EQUAL);
		attrNames = new HashSet<String>();
		attrNames.add("Last Name");
		attrNames.add("User Login");
		try {
			UserManager userManager_local = oimClient.getService(UserManager.class);
			users = userManager_local.search(criteria, attrNames, parameters);
			if (users != null && !users.isEmpty() && users.get(0) != null && users.get(0).getLastName() != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			LOG.error(this+" Search error ");
			ex.printStackTrace();
		}
		return false;
	}

	public StatusOutVO provisionUser(OIMClient oimClient, ProvisionUserAccountVO accountVO) {

		StatusOutVO statusOutVO = new StatusOutVO();
		StringBuffer buffer = new StringBuffer();
		String appInstanceName = null;
		String adServerKey = null;
		String adOrganizationName = null;
		Set<String> returnAttrRole = null;
		HashMap configParam = null;
		String roleName = null;
		String proviosioningStatus = "";

		try {
			Properties properties = PropertiesUtil.getProperties();
			appInstanceName = properties.getProperty("AD_application_instance_name");
			adServerKey = properties.getProperty("AD_server_key");
			adOrganizationName = properties.getProperty("AD_organization_name");
			roleName = properties.getProperty("role_name");
		} catch (Exception e) {
			statusOutVO.setStatus("Error");
			statusOutVO.setErrorMessage("Exception while logging in. Please try again.");
			LOG.error(this + statusOutVO.getErrorMessage());
			return statusOutVO;
		}

		try {
			ApplicationInstanceService appInstService_local = oimClient.getService(ApplicationInstanceService.class);
			ProvisioningService provisioningService_local = oimClient.getService(ProvisioningService.class);
			RoleManager rolemanager = oimClient.getService(RoleManager.class);

			ApplicationInstance appInstance = appInstService_local.findApplicationInstanceByName(appInstanceName);

			HashMap<String, Object> parentData = new HashMap<String, Object>();
			parentData.put("UD_ADUSER_SERVER", adServerKey);
			parentData.put("UD_ADUSER_ORGNAME", adOrganizationName);

			AccountData accountData = new AccountData(String.valueOf(appInstance.getAccountForm().getFormKey()), null, parentData);

			Account account = new Account(appInstance, accountData);
			account.setAccountType(Account.ACCOUNT_TYPE.Primary);

			provisioningService_local.provision(accountVO.getUserKey(), account);

			List accounts = provisioningService_local.getAccountsProvisionedToUser(accountVO.getUserKey(), true);

			proviosioningStatus = ((Account) accounts.get(accounts.size() - 1)).getAccountStatus().toString();

			if ("Provisioned".equalsIgnoreCase(proviosioningStatus)) {
				statusOutVO.setStatus("Success");
			} else {
				statusOutVO.setStatus("Error");
				statusOutVO.setErrorMessage("Error while creating user account in AD");
				LOG.error(this + statusOutVO.getErrorMessage());
			}
		} catch (Exception ex) {
			statusOutVO.setStatus("Error");
			statusOutVO.setErrorMessage("Error while provisioning resource to AD");
			LOG.error(this + statusOutVO.getErrorMessage());
		}
		return statusOutVO;
	}
	
	  public UserStatusVO oimUserStatus(OIMClient oimClient,UsernameVO usernameVO) {
	       /* if (logger.isLoggable(Level.FINE)) {
	            logger.entering(CLASS_NAME, "getUserStatus");
	        }
	        if (logger.isLoggable(Level.FINE)) {
	            logger.logp(Level.FINE, CLASS_NAME, "getUserStatus", "__getUserStatus Begin__");
	        }*/
	        UserStatusVO userStatusVO = new UserStatusVO();
	        Set<String> attrNames = null;

	        attrNames = new HashSet<String>();
	        attrNames.add("usr_change_pwd_at_next_logon");
	        attrNames.add("usr_disabled");
	        attrNames.add("migr_firstLogin");
	        attrNames.add("usr_locked");
	        attrNames.add("usr_login_attempts_ctr");

	        // Login to OIM with proper credentials
	       /* if (logger.isLoggable(Level.FINE)) {
	            logger.logp(Level.FINE, CLASS_NAME, "getUserStatus", "__Login Begin__");
	        }*/
	      //  OIMClient oimClient = null;
	      /*  try {
	            Properties properties = getProperties();
	            Hashtable<String, String> env = new Hashtable<String, String>();
	            System.setProperty("java.security.auth.login.config", properties.getProperty("auth_conf"));
	            System.setProperty("APPSERVER_TYPE", properties.getProperty("appserver_type"));
	            System.setProperty("weblogic.Name", properties.getProperty("weblogic_name"));
	            env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, "weblogic.jndi.WLInitialContextFactory");
	            env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, properties.getProperty("oim_url"));
	            oimClient = new OIMClient(env);
	            oimClient.login(properties.getProperty("oim_username"), properties.getProperty("oim_password").toCharArray());
	        } catch (LoginException ex) {
	            if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "getUserStatus", "Error: " + ex.getMessage());
	            }
	            userStatusVO.setStatus("Error");
	            userStatusVO.setErrorMessage("Exception while logging in. Please try again.");
	            return userStatusVO;
	        } catch (Exception e) {
	            if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "getUserStatus", "Error: " + e.getMessage());
	            }
	            userStatusVO.setStatus("Error");
	            userStatusVO.setErrorMessage("Exception while logging in. Please try again.");
	            return userStatusVO;
	        }
	        if (logger.isLoggable(Level.FINE)) {
	            logger.logp(Level.FINE, CLASS_NAME, "getUserStatus", "__Login End__");
	        }
*/
	        // BEGIN ---- get Username from ssa id
	        Set<String> attrName = null;
	        String username = null;
	        User users = null;
	        attrName = new HashSet<String>();
	        attrName.add("First Name");
	        attrName.add("Last Name");
	        attrName.add("User Login");
	        UserManager userManager_local = oimClient.getService(UserManager.class);
	        try {
	            users = userManager_local.getDetails("User Login", usernameVO.getUsername(), attrName);
	            if (users != null && users.getAttributes() != null && !users.getAttributes().isEmpty() && users.getAttributes().get("User Login") != null) {
	                username = users.getAttributes().get("User Login").toString();
	            }
	        } catch (Exception ex) {
	            try {
	                users = userManager_local.getDetails("Employee Number", usernameVO.getUsername(), attrName);
	                if (users != null && users.getAttributes() != null && !users.getAttributes().isEmpty() && users.getAttributes().get("User Login") != null) {
	                    username = users.getAttributes().get("User Login").toString();
	                }
	            } catch (Exception exObj) {
	               /* if (logger.isLoggable(Level.SEVERE)) {
	                    logger.logp(Level.SEVERE, CLASS_NAME, "getUserStatus", "Error: " + exObj.getMessage());
	                }*/
	                userStatusVO.setStatus("Error");
	                userStatusVO.setErrorMessage("No user found for username : " + usernameVO.getUsername());
	                return userStatusVO;
	            }
	        }
	        if (username == null || username.equals("Error")) {
	            userStatusVO.setStatus("Error");
	            userStatusVO.setErrorMessage("No user found for username : " + usernameVO.getUsername());
	            return userStatusVO;
	        }

	        try {
	            User user = userManager_local.getDetails("User Login", username, attrNames);
	            if (user != null) {
	                if (user.getAttribute("usr_change_pwd_at_next_logon") != null && user.getAttribute("usr_change_pwd_at_next_logon").toString() != null) {
	                    if (user.getAttribute("usr_change_pwd_at_next_logon").toString().equals("0")) {
	                        userStatusVO.setChangePasswordAtNextLogon("false");
	                    } else if (user.getAttribute("usr_change_pwd_at_next_logon").toString().equals("1")) {
	                        userStatusVO.setChangePasswordAtNextLogon("true");
	                    }
	                }
	                if (user.getAttribute("usr_disabled") != null && user.getAttribute("usr_disabled").toString() != null) {
	                    if (user.getAttribute("usr_disabled").toString().equals("0")) {
	                        userStatusVO.setUserAccountDisabled("false");
	                    } else if (user.getAttribute("usr_disabled").toString().equals("1")) {
	                        userStatusVO.setUserAccountDisabled("true");
	                    }
	                }
	                if (user.getAttribute("usr_locked") != null && user.getAttribute("usr_locked").toString() != null) {
	                    if (user.getAttribute("usr_locked").toString().equals("0")) {
	                        userStatusVO.setUserAccountLocked("false");
	                    } else if (user.getAttribute("usr_locked").toString().equals("1")) {
	                        userStatusVO.setUserAccountLocked("true");
	                    }
	                }
	                if (user.getAttribute("migr_firstLogin") != null) {
	                    userStatusVO.setMigratedUserFirstLogin(user.getAttribute("migr_firstLogin").toString());
	                } else {
	                    userStatusVO.setMigratedUserFirstLogin("false");
	                }
	                if (user.getAttribute("usr_login_attempts_ctr") != null) {
	                    userStatusVO.setIncorrectLoginAttemptsMade(user.getAttribute("usr_login_attempts_ctr").toString());
	                }
	                userStatusVO.setStatus("Success");
	            }
	        } catch (Exception ex) {
	          /*  if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "getUserStatus", "Error: " + ex.getMessage());
	            }*/
	            userStatusVO.setStatus("Error");
	            userStatusVO.setErrorMessage("No user found for username : " + username);
	        } finally {
	            //Logout from OIMClient
	            if (oimClient != null) {
	               /* if (logger.isLoggable(Level.FINE)) {
	                    logger.logp(Level.FINE, CLASS_NAME, "getUserStatus", "logging out");
	                }*/
	                oimClient.logout();
	                oimClient = null;
	            }
	        }
	       /* if (logger.isLoggable(Level.FINE)) {
	            logger.logp(Level.FINE, CLASS_NAME, "getUserStatus", "__getUserStatus End__");
	        }
	        if (logger.isLoggable(Level.FINE)) {
	            logger.exiting(CLASS_NAME, "getUserStatus");
	        }*/
	        return userStatusVO;
	    }


}
