package developBot.MervalOperations.models.clientModel.miCuenta.estadoCuenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Saldo {
    private String liquidacion;
    private Double saldo;
    private Double comprometido;
    private Double disponible;
    private Double disponibleOperar;
}