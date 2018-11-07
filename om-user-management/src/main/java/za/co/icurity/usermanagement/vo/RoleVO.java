package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class RoleVO {

    private String memberOf;


    public RoleVO() {
        super();
    }


	public String getMemberOf() {
		return memberOf;
	}


	public void setMemberOf(String memberOf) {
		this.memberOf = memberOf;
	}

   
}
