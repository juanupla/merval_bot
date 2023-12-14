package developBot.MervalOperations.models.titulos.cotizacionDetalle;

import developBot.MervalOperations.models.titulos.Puntas;
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
    private Long ultimoPrecio;
    private Long variacion;
    private Long apertura;
    private Long maximo;
    private Long minimo;
    private LocalDateTime fechaHora;
    private String tendencia;
    private Long cierreAnterior;
    private Long montoOperado;
    private Long volumenNominal;
    private Long precioPromedio;
    private String moneda;
    private Long precioAjuste;
    private Long interesesAbiertos;
    private List<Puntas> puntas;
    private int cantidadOperaciones;
    private String simbolo;
    private String pais;
    private String mercado;
    private String tipo;
    private String descripcionTitulo;
    private String plazo;
    private int laminaMinima;
    private int lote;
    private int cantidadMinima;
    private Long puntosVariacion;
}
