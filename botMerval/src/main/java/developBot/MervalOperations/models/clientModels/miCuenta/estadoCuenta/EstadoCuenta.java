package developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoCuenta {
    private List<Cuenta> cuentas;
    private List<Estadistica> estadisticaModels;
    private Double totalEnPesos;
}
