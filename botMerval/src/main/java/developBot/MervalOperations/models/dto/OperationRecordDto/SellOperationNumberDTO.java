package developBot.MervalOperations.models.dto.OperationRecordDto;

import developBot.MervalOperations.entities.OperationRecordEntities.OperationRecordEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellOperationNumberDTO {
    private String simbol;

    private Long number;

    private Long amount;
    private Long residualQuantity;
    private Boolean status;

}
