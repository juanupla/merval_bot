package developBot.MervalOperations.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private LocalDateTime dateOfPurchease;
    @NotNull
    private Double purcheasePrice;
    @NotNull
    private Long purcheaseAmount;
    @NotNull
    private String status; //abierta/cerrada

    private LocalDateTime saleDate;

    private Double salePrice;
    private Long salesAmount;

    private Double yield;

    private Double annualizedYield;

    private String result;

}
