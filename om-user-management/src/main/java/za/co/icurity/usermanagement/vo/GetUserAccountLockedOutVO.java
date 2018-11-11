package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class GetUserAccountLockedOutVO extends StatusOutVO{
    private String userAccountStatus;
    
    public GetUserAccountLockedOutVO() {
        super();
    }

	public String getUserAccountStatus() {
		return userAccountStatus;
	}

	public void setUserAccountStatus(String userAccountStatus) {
		this.userAccountStatus = userAccountStatus;
	}


}
