package za.co.icurity.usermanagement.vo;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
@Component
public class CheckExistingCellphoneOutVO extends StatusOutVO {
    private String cellphoneExists;
    
    public CheckExistingCellphoneOutVO() {
        super();
    }

    public void setCellphoneExists(String cellphoneExists) {
        this.cellphoneExists = cellphoneExists;
    }

    public String getCellphoneExists() {
        return cellphoneExists;
    }
}
