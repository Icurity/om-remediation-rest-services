package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

@Component
public class StatusOutVO {
	
	    private String status;
	    private String errorMessage;
	    
	    public StatusOutVO() {
	        super();
	    }
	    
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getErrorMessage() {
			return errorMessage;
		}
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	    

}
