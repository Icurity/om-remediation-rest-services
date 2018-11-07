package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class UserStatusVO extends StatusOutVO {

	private String obPasswordChangeFlag;
	private String obFirstLogin;

	public UserStatusVO() {
		super();
	}

	public String getObPasswordChangeFlag() {
		return obPasswordChangeFlag;
	}

	public void setObPasswordChangeFlag(String obPasswordChangeFlag) {
		this.obPasswordChangeFlag = obPasswordChangeFlag;
	}

	public String getObFirstLogin() {
		return obFirstLogin;
	}

	public void setObFirstLogin(String obFirstLogin) {
		this.obFirstLogin = obFirstLogin;
	}

}