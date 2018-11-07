package za.co.icurity.usermanagement.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
			} else {
				return false;
			}
		} catch (Exception ex) {
			LOG.error(this+" Search error ");
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
	
	  public UserStatusVO ovdUserStatus(DirContext dirContext,UsernameVO usernameVO) {
		  
		 /* dirContext.search(name, matchingAttributes, attributesToReturn)*/
	      
	        return null;
	    }


}
