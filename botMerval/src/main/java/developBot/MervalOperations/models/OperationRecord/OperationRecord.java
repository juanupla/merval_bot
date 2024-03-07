package developBot.MervalOperations.models.OperationRecord;

import developBot.MervalOperations.entities.OperationRecordEntities.SellOperationNumberEntity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationRecord {

    @NotNull
    private UUID id;
    private UUID buyOperationNumberId;
    private List<SellOperationNumber> sellOperationNumbers;
    @NotNull
    private String simbol;
    @NotNull
    private LocalDateTime dateOfPurchase;
    @NotNull
    private Double purchasePrice;
    @NotNull
    private Long purchaseAmount;
    @NotNull
    private Boolean status; //abierta/cerrada
    private LocalDateTime saleDate;
    private Long salesAmount;
    private Double averageSellingPrice;
    private Double yield;
    private Double annualizedYield;
    private String result;

}
