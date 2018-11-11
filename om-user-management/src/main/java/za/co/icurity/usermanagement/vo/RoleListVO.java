package za.co.icurity.usermanagement.vo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class RoleListVO extends StatusOutVO {

    private List<RoleVO> roles = null;

    public RoleListVO() {
        super();
    }
/*
    public void setRole(List<RoleVO> role) {
        this.roles = roles;
    }

    public List<RoleVO> getRole() {
        if (roles == null) {
        	roles = new ArrayList<RoleVO>();
        }
        return roles;
    }*/

	public List<RoleVO> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleVO> roles) {
		this.roles = roles;
	}
    
    
    
    
}
