package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class ProvisionUserAccountVO {
    
    private String employeeNumber ;
    private String username;
    private String userKey;
    
    public ProvisionUserAccountVO() {
        super();
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserKey() {
        return userKey;
    }
}
