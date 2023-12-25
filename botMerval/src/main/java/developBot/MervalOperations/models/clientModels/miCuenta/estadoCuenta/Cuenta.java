package developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta;

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
    private Double disponible;
    private Double comprometido;
    private Double saldo;
    private Double titulosValorizados;
    private Double total;
    private Double margenDescubierto;
    private List<Saldo> saldos;
    private String estado;
}
