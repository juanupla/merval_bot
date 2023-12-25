package developBot.MervalOperations.models.clientModels.miCuenta.operacioneByNumero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperacionDetalleModel {
    private Integer numero;
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
    private Double precio;
    private Long cantidad;
    private Double monto;
    private Double fondosParaOperacion;
    private Double montoOperacion;
    private String modalidad;
    private Double arancelesARS;
    private Double arancelesUSD;
    private String plazo;
}
