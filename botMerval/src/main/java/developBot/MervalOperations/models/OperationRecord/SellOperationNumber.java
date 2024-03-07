package developBot.MervalOperations.models.OperationRecord;

import developBot.MervalOperations.entities.OperationRecordEntities.OperationRecordEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellOperationNumber {
    private UUID id;
    private String simbol;

    private Long number;

    private Long amount;
    private Long residualQuantity;
    private Boolean status;
    private OperationRecordEntity operationRecordId;


}
