package developBot.MervalOperations.models.titulos.cotizacionHistorica;

import developBot.MervalOperations.models.titulos.Puntas;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CotizacionHistorica {
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
    private Long cantidadOperaciones;
    private String descripcionTitulo;
    private String plazo;
    private int laminaMinima;
    private int lote;
}
