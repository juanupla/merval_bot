package developBot.MervalOperations.models.miCuenta.operacioneByNumero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operacion {
    private LocalDateTime fecha;
    private int cantidad;
    private Long precio;
}
