package developBot.MervalOperations.models.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmasDTO {
    private String activo;
    private BigDecimal EMA3;
    private BigDecimal EMA9;
    private BigDecimal EMA21;
    private BigDecimal EMA50;
}
