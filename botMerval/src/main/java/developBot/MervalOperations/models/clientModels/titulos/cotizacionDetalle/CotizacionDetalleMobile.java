package developBot.MervalOperations.models.clientModels.titulos.cotizacionDetalle;

import developBot.MervalOperations.models.clientModels.titulos.PuntasModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CotizacionDetalleMobile {
    private boolean operableCompra;
    private boolean operableVenta;
    private boolean visible;
    private Double ultimoPrecio;
    private Double variacion;
    private Double apertura;
    private Double maximo;
    private Double minimo;
    private LocalDateTime fechaHora;
    private String tendencia;
    private Double cierreAnterior;
    private Double montoOperado;
    private Long volumenNominal;
    private Double precioPromedio;
    private String moneda;
    private Double precioAjuste;
    private Long interesesAbiertos;
    private List<PuntasModel> puntas;
    private Long cantidadOperaciones;
    private String simbolo;
    private String pais;
    private String mercado;
    private String tipo;
    private String descripcionTitulo;
    private String plazo;
    private int laminaMinima;
    private int lote;
    private int cantidadMinima;
    private Double puntosVariacion;
}
