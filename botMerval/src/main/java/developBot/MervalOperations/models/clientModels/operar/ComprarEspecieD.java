package developBot.MervalOperations.models.clientModels.operar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComprarEspecieD {
    private String mercado;
    private String simbolo;
    private Long cantidad;
    private Double precio;
    private String plazo;
    private LocalDateTime validez;
    private String tipoOrden;
    private Double monto;
    private Long idFuente;
}
