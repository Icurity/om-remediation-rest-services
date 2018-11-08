package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class UserStatusVO extends StatusOutVO {

	private String changePassword;
	private String migratedUserFirstLogin;

	public UserStatusVO() {
		super();
	}

	public String getChangePassword() {
		return changePassword;
	}

	public void setChangePassword(String changePassword) {
		this.changePassword = changePassword;
	}

	public String getMigratedUserFirstLogin() {
		return migratedUserFirstLogin;
	}

	public void setMigratedUserFirstLogin(String migratedUserFirstLogin) {
		this.migratedUserFirstLogin = migratedUserFirstLogin;
	}

	
}