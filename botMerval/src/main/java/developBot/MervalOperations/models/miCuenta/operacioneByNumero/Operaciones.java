package developBot.MervalOperations.models.miCuenta.operacioneByNumero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operaciones {
    private Long numero;
    private String mercado;
    private String simbolo;
    private String moneda;
    private String tipo;
    private LocalDateTime fechaAlta;
    private LocalDateTime validez;
    private LocalDateTime fechaOperado;
    private String estadoActual;
    private List<Estado> estados;
    private List<Arancel> aranceles;
    private List<Operacion> operaciones;
    private Long precio;
    private int cantidad;
    private Long monto;
    private Long fondosParaOperacion;
    private Long montoOperacion;
    private String modalidad;
    private Long arancelesARS;
    private Long arancelesUSD;
    private String plazo;
}
