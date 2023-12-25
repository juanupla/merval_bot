package developBot.MervalOperations.models.clientModels.operar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comprar {
    private String mercado;
    private String simbolo;
    private Long cantidad;
    private Double precio;
    private String plazo;//t0-t2
    private LocalDateTime validez;
    private String tipoOrden;//precioLimite
    private Double monto;
    private Long idFuente;
}
