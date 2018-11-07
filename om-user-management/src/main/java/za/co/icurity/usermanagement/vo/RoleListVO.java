package za.co.icurity.usermanagement.vo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class RoleListVO extends StatusOutVO {

    private List<RoleVO> role;

    public RoleListVO() {
        super();
    }

    public void setRole(List<RoleVO> role) {
        this.role = role;
    }

    public List<RoleVO> getRole() {
        if (role == null) {
            role = new ArrayList<RoleVO>();
        }
        return role;
    }
}
