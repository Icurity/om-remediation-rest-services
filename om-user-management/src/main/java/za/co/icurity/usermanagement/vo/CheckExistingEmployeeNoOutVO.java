package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class CheckExistingEmployeeNoOutVO extends StatusOutVO  {
    
    private String employeeNoExists;
    
    public CheckExistingEmployeeNoOutVO() {
        super();
    }

    public void setEmployeeNoExists(String employeeNoExists) {
        this.employeeNoExists = employeeNoExists;
    }

    public String getEmployeeNoExists() {
        return employeeNoExists;
    }
}
