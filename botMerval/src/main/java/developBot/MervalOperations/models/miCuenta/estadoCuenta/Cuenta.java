package developBot.MervalOperations.models.miCuenta.estadoCuenta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cuenta {
    private String numero;
    private String tipo;
    private String moneda;
    private Long disponible;
    private Long comprometido;
    private Long saldo;
    private Long titulosValorizados;
    private Long total;
    private Long margenDescubierto;
    private List<Saldo> saldos;
    private String estado;
}
