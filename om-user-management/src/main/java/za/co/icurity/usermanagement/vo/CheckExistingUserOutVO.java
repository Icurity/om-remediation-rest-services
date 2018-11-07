package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class CheckExistingUserOutVO extends StatusOutVO {
    private String userExists;

    public CheckExistingUserOutVO() {
        super();
    }

    public void setUserExists(String userExists) {
        this.userExists = userExists;
    }

    public String getUserExists() {
        return userExists;
    }
}
