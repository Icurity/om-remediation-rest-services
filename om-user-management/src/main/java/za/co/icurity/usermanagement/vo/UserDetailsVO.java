package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class UserDetailsVO extends StatusOutVO{
    private String firstName;
    private String lastName;
    private String cellPhoneNumber;
    private String dateOfBirth;
    private String idNumber;
    private String migratedUserFirstLogin;
    private String userAccountLocked;
    private String idType;
    private String employeeNumber;

    public UserDetailsVO() {
        super();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setCellPhoneNumber(String cellPhoneNumber) {
        this.cellPhoneNumber = cellPhoneNumber;
    }

    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setMigratedUserFirstLogin(String migratedUserFirstLogin) {
        this.migratedUserFirstLogin = migratedUserFirstLogin;
    }

    public String getMigratedUserFirstLogin() {
        return migratedUserFirstLogin;
    }

    public void setUserAccountLocked(String userAccountLocked) {
        this.userAccountLocked = userAccountLocked;
    }

    public String getUserAccountLocked() {
        return userAccountLocked;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdType() {
        return idType;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }
}
