package developBot.MervalOperations.models.clientModels.miCuenta.portafolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Posicion {
    private Integer cantidad;
    private Double comprometido;
    private Double puntosVariacion;
    private Double variacionDiaria;
    private Double ultimoPrecio;
    private Double ppc;
    private Double gananciaPorcentaje;
    private Double gananciaDinero;
    private Double valorizado;
    private Titulo titulo;
    private Parking parking;
}
