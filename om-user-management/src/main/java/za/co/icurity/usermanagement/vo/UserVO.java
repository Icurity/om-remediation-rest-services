package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

 
public class UserVO {
	
	private String firstName;
    private String lastName;
    private String country;
    private String cellPhoneNumber;
    private String dateOfBirth;
    private String idNumber;
    private boolean cellPhoneValidated;
    
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCellPhoneNumber() {
		return cellPhoneNumber;
	}
	public void setCellPhoneNumber(String cellPhoneNumber) {
		this.cellPhoneNumber = cellPhoneNumber;
	}
	public String getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public String getIdNumber() {
		return idNumber;
	}
	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}
	public boolean isCellPhoneValidated() {
		return cellPhoneValidated;
	}
	public void setCellPhoneValidated(boolean cellPhoneValidated) {
		this.cellPhoneValidated = cellPhoneValidated;
	}
    
}
