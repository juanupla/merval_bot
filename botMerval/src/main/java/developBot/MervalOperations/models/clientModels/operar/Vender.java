package developBot.MervalOperations.models.clientModels.operar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vender {
    private String mercado;
    private String simbolo;
    private Double cantidad;
    private Double precio;
    private String validez;
    private String tipoOrden;///precioLimite
    private String plazo;//t0-t2
    private Integer idFuente;
}
