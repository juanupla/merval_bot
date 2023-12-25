package developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Estadistica {
    private String descripcion;
    private int cantidad;
    private Double volumen;
}
