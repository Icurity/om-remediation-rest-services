package za.co.icurity.usermanagement.vo;

public class UserStatusVO extends StatusOutVO{
    
    private String migratedUserFirstLogin;
    private String userAccountLocked;
    private String userAccountDisabled;
    private String changePasswordAtNextLogon;
    private String incorrectLoginAttemptsMade;
    
    public UserStatusVO() {
        super();
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

    public void setUserAccountDisabled(String userAccountDisabled) {
        this.userAccountDisabled = userAccountDisabled;
    }

    public String getUserAccountDisabled() {
        return userAccountDisabled;
    }

    public void setChangePasswordAtNextLogon(String changePasswordAtNextLogon) {
        this.changePasswordAtNextLogon = changePasswordAtNextLogon;
    }

    public String getChangePasswordAtNextLogon() {
        return changePasswordAtNextLogon;
    }

    public void setIncorrectLoginAttemptsMade(String incorrectLoginAttemptsMade) {
        this.incorrectLoginAttemptsMade = incorrectLoginAttemptsMade;
    }

    public String getIncorrectLoginAttemptsMade() {
        return incorrectLoginAttemptsMade;
    }
}
