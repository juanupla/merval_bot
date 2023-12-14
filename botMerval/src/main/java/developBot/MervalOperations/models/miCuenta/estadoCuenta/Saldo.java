package developBot.MervalOperations.models.miCuenta.estadoCuenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Saldo {
    private String liquidacion;
    private Long saldo;
    private Long comprometido;
    private Long disponible;
    private Long disponibleOperar;
}