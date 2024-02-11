package developBot.MervalOperations.models.clientModel.operar;

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
    private String validez;
    private String tipoOrden;//precioLimite
    private Double monto;
    private Integer idFuente;
}
