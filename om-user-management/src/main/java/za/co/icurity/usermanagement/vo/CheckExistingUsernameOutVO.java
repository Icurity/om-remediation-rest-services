package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class CheckExistingUsernameOutVO extends StatusOutVO {
    private String usernameExists;

    public CheckExistingUsernameOutVO() {
        super();
    }

    public void setUsernameExists(String usernameExists) {
        this.usernameExists = usernameExists;
    }

    public String getUsernameExists() {
        return usernameExists;
    }
}
