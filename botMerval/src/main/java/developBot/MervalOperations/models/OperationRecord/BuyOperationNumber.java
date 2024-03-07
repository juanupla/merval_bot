package developBot.MervalOperations.models.OperationRecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyOperationNumber {
    private UUID id;
    private String simbol;
    private Long number;
    private Long amount;
}
