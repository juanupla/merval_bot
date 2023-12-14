package developBot.MervalOperations.models.miCuenta.estadoCuenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Estadistica {
    private String descripcion;
    private int cantidad;
    private Long volumen;
}
