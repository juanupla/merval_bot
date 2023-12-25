package developBot.MervalOperations.models.clientModels.titulos.cotizacion;

import developBot.MervalOperations.models.clientModels.titulos.PuntasModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cotizacion {
    private Double ultimoPrecio;
    private Double variacion;
    private Double apertura;
    private Double maximo;
    private Double minimo;
    private String fechaHora;
    private String tendencia;
    private Double cierreAnterior;
    private Double montoOperado;
    private Double volumenNominal;
    private Double precioPromedio;
    private String moneda;
    private Double precioAjuste;
    private Double interesesAbiertos;
    private List<PuntasModel> puntas;
    private Integer cantidadOperaciones;
    private String descripcionTitulo;
    private String plazo;
    private Integer laminaMinima;
    private Integer lote;

}
