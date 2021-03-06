package za.co.icurity.usermanagement.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.naming.directory.DirContext;

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
import za.co.icurity.usermanagement.vo.CheckExistingUserInVO;
import za.co.icurity.usermanagement.vo.GetADUsernameInVO;
import za.co.icurity.usermanagement.vo.GetADUsernameOutVO;
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
	@Autowired
	private GetADUsernameOutVO getADUsernameOutVO; 

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
			userOutVO.setStatus("Success");
			LOG.info(this + " User object created : " + userInVO.getUsername());
		} catch (Exception ex) {
			userOutVO.setStatus("Error");
			userOutVO.setErrorMessage("Error creating user on OIM for username "+userInVO.getUsername());
			LOG.error(this+" Error creating user on OIM for username "+userInVO.getUsername());
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
		}catch (Exception ex) {
			LOG.error(this+" checkExistingEmployeeNumber Search error ");
		}
		return false;
	}
	public boolean checkExistingCellphoneAttribute(OIMClient oimClient, String cellnumber) {

		List<User> users = null;
		HashMap<String, Object> parameters = null;
		Set<String> attrNames = null;
	
		SearchCriteria criteria = new SearchCriteria("Telephone Number", cellnumber, SearchCriteria.Operator.EQUAL);
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
		}catch (Exception ex) {
			LOG.error(this+" checkExistingCellphoneAttribute Search error ");
		}
		return false;
	}
	public boolean checkExistingUserName(OIMClient oimClient, String username) {

		List<User> users = null;
		HashMap<String, Object> parameters = null;
		Set<String> attrNames = null;
		SearchCriteria criteria = new SearchCriteria("User Login", username, SearchCriteria.Operator.EQUAL);
		attrNames = new HashSet<String>();
		attrNames.add("Last Name");
		attrNames.add("User Login");
		try {
			UserManager userManager_local = oimClient.getService(UserManager.class);
			users = userManager_local.search(criteria, attrNames, parameters);
			if (users != null && !users.isEmpty() && users.get(0) != null && users.get(0).getLastName() != null) {
				return true;
			}else {
				criteria = new SearchCriteria("Employee Number", username, SearchCriteria.Operator.EQUAL);
				users = userManager_local.search(criteria, attrNames, parameters);
				if (users != null && !users.isEmpty() && users.get(0) != null && users.get(0).getLastName() != null) {
					return true;
				}else {
				return false;
			}
			}
		} catch (Exception ex) {
			LOG.error(this+" Search error ");
		}
		return false;
	}

	public boolean checkExistingUser(OIMClient oimClient, CheckExistingUserInVO checkExistingUserInVO) {

		   Set<String> attrNames = null;
		   List<User> users = null;
		   HashMap<String, Object> parameters = null;
	    	SearchCriteria criteriaFirstName = new SearchCriteria("First Name", checkExistingUserInVO.getFirstName(), SearchCriteria.Operator.EQUAL);
	        SearchCriteria criteriaLastName = new SearchCriteria("Last Name", checkExistingUserInVO.getLastName(), SearchCriteria.Operator.EQUAL);
	        SearchCriteria criteriaDateOfBirth = new SearchCriteria("birth_date", checkExistingUserInVO.getDateOfBirth(), SearchCriteria.Operator.EQUAL);
	        SearchCriteria criteriaIDNumber = new SearchCriteria("id_number", checkExistingUserInVO.getIdNumber(), SearchCriteria.Operator.EQUAL);
	        SearchCriteria criteriaFirstAndLast = new SearchCriteria(criteriaFirstName, criteriaLastName, SearchCriteria.Operator.AND);
	        SearchCriteria criteriaDOBAndID = new SearchCriteria(criteriaDateOfBirth, criteriaIDNumber, SearchCriteria.Operator.AND);
	        SearchCriteria criteria = new SearchCriteria(criteriaFirstAndLast, criteriaDOBAndID, SearchCriteria.Operator.AND);
	        attrNames = new HashSet<String>();
	        attrNames.add("First Name");
	        attrNames.add("Last Name");
	        attrNames.add("birth_date");
	        attrNames.add("id_number");	        
	        SearchCriteria searchCriteria = new SearchCriteria(criteria, attrNames, SearchCriteria.Operator.EQUAL);
	        try {
				UserManager userManager_local = oimClient.getService(UserManager.class);
				users = userManager_local.search(criteria, attrNames, parameters);
				if (users != null && !users.isEmpty() && users.get(0) != null && users.get(0).getLastName() != null) {
					return true;
				}

		} catch (Exception ex) {
			LOG.error(this+" Search error ");
		}
		return false;
	}
	
	public GetADUsernameOutVO getADUsernameAttributes(OIMClient oimClient, GetADUsernameInVO getADUsernameInVO) {

		   Set<String> attrNames = null;
		   List<User> users = null;
		   HashMap<String, Object> parameters = null;
		   SearchCriteria criteriaSurname = new SearchCriteria("Last Name", getADUsernameInVO.getSurname(), SearchCriteria.Operator.EQUAL);
	        SearchCriteria criteriaIDNumber = new SearchCriteria("id_number", getADUsernameInVO.getIdNumber(), SearchCriteria.Operator.EQUAL);
	        SearchCriteria criteriaIDType = new SearchCriteria("id_type", getADUsernameInVO.getIdType(), SearchCriteria.Operator.EQUAL);
	        SearchCriteria criteriaEmail = new SearchCriteria("Email", getADUsernameInVO.getEmailAddress(), SearchCriteria.Operator.EQUAL);

	        SearchCriteria criteriaSurnameIDNo = new SearchCriteria(criteriaSurname, criteriaIDNumber, SearchCriteria.Operator.AND);
	        SearchCriteria criteriaIDTypeEmail = new SearchCriteria(criteriaIDType, criteriaEmail, SearchCriteria.Operator.AND);
	        SearchCriteria criteria = new SearchCriteria(criteriaSurnameIDNo, criteriaIDTypeEmail, SearchCriteria.Operator.AND);

	        attrNames = new HashSet<String>();
	        attrNames.add("Employee Number");
	        attrNames.add("User Login");
	        attrNames.add("First Name");
	        attrNames.add("Email");
	        try {
	            UserManager userManager_local = oimClient.getService(UserManager.class);
	            users = userManager_local.search(criteria, attrNames, parameters);
	            if (users != null && !users.isEmpty() && users.size() == 1 && users.get(0) != null) {
	                getADUsernameOutVO.setStatus("Success");
	                if (users.get(0).getAttribute("Employee Number") != null) {
	                    getADUsernameOutVO.setUsernumber(users.get(0).getAttribute("Employee Number").toString());
	                }
	                if (users.get(0).getAttribute("User Login") != null) {
	                    getADUsernameOutVO.setOmUserAlias(users.get(0).getAttribute("User Login").toString());
	                }
	                if (users.get(0).getAttribute("First Name") != null) {
	                    getADUsernameOutVO.setFirstName(users.get(0).getAttribute("First Name").toString());
	                }
	                if (users.get(0).getAttribute("Email") != null) {
	                    getADUsernameOutVO.setMail(users.get(0).getAttribute("Email").toString());
	                }
	            } else {
	                getADUsernameOutVO.setStatus("Error");
	                getADUsernameOutVO.setErrorMessage("No username/alias could be retrieved");
	            }
	        } catch (Exception ex) {
	          /*  if (logger.isLoggable(Level.SEVERE)) {
	                logger.logp(Level.SEVERE, CLASS_NAME, "getADUsername", "Error: " + ex.getMessage());
	            }*/
	            getADUsernameOutVO.setStatus("Error");
	            getADUsernameOutVO.setErrorMessage("No username/alias could be retrieved");
	            return getADUsernameOutVO;
	        } 
	        return getADUsernameOutVO;
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
			statusOutVO.setErrorMessage("Exception on Provisioning the user");
			LOG.error(this +" Exception while setting the properties "+e.getMessage());
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
				statusOutVO.setErrorMessage("Exception on Provisioning the user");
				LOG.error(this +" Error on provisionUser ");
			}
		} catch (Exception ex) {
			statusOutVO.setStatus("Error");
			statusOutVO.setErrorMessage("Exception on Provisioning the user");
			LOG.error(this +" Error on provisionUser "+ex.getMessage());
		}
		return statusOutVO;
	}
	
	}
