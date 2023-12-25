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
    private Long cantidad;
    private Double precio;
    private LocalDateTime validez;
    private String tipoOrden;///precioLimite
    private String plazo;//t0-t2
    private Long idFuente;
}
