package developBot.MervalOperations.models.miCuenta.estadoCuenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoCuenta {
    private List<Cuenta> cuentas;
    private List<Estadistica> estadisticas;
    private Long totalEnPesos;
}
