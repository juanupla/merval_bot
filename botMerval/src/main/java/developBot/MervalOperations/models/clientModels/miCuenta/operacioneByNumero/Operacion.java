package developBot.MervalOperations.models.clientModels.miCuenta.operacioneByNumero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operacion {
    private LocalDateTime fecha;
    private Long cantidad;
    private Double precio;
}
