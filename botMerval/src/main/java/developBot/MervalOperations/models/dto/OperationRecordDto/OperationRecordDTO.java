package developBot.MervalOperations.models.dto.OperationRecordDto;

import jakarta.persistence.Column;
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
public class OperationRecordDTO {

    @NotNull
    private UUID id;
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

    private Double salePrice;
    private Long salesAmount;

    private Double yield;

    private Double annualizedYield;

    private String result;

}
