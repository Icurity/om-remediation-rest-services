package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class GetADUsernameInVO{
    
    private String surname;
    private String idType;
    private String idNumber;
    private String emailAddress;
    
    public GetADUsernameInVO() {
        super();
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSurname() {
        return surname;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
