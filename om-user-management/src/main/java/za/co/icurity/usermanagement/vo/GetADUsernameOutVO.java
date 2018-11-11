package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class GetADUsernameOutVO extends StatusOutVO  {
    
    private String usernumber;
    private String omUserAlias;
    private String mail;
    private String firstName;
    
    public GetADUsernameOutVO() {
        super();
    }

    public void setUsernumber(String usernumber) {
        this.usernumber = usernumber;
    }

    public String getUsernumber() {
        return usernumber;
    }

    public void setOmUserAlias(String omUserAlias) {
        this.omUserAlias = omUserAlias;
    }

    public String getOmUserAlias() {
        return omUserAlias;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMail() {
        return mail;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }
}
