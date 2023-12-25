package developBot.MervalOperations.models.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrecioDTO {
    private Double ultimoPrecio;
    private LocalDateTime fechaHora;

    private Integer intervaloNro;
}
