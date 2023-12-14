package developBot.MervalOperations.models.miCuenta.operacioneByNumero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Estado {
    private String detalle;
    private LocalDateTime fecha;
}
