package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

@Component
public class UserOutVO extends StatusOutVO{

	 private String userId;
	 private String errorCode;
	 
	 public UserOutVO() {
	        super();
	    }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	 
}
