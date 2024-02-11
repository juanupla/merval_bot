package developBot.MervalOperations.models.clientModel.titulos.cotizacionDetalle;

import com.fasterxml.jackson.annotation.JsonProperty;
import developBot.MervalOperations.models.clientModel.titulos.Punta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class    CotizacionDetalleMobile {
    private boolean operableCompra;
    private boolean operableVenta;
    private boolean visible;
    private Double ultimoPrecio;
    private Double variacion;
    private Double apertura;
    private Double maximo;
    private Double minimo;
    @JsonProperty("yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;
    private String tendencia;
    private Double cierreAnterior;
    private Double montoOperado;
    private Integer volumenNominal;
    private Double precioPromedio;
    private String moneda;
    private Double precioAjuste;
    private Double interesesAbiertos;
    private List<Punta> puntas;
    private Integer cantidadOperaciones;
    private String simbolo;
    private String pais;
    private String mercado;
    private String tipo;
    private String descripcionTitulo;
    private String plazo;
    private Integer laminaMinima;
    private Integer lote;
    private Integer cantidadMinima;
    private Double puntosVariacion;
}
