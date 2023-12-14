package developBot.MervalOperations.models.operar;

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
    private int cantidad;
    private Long precio;
    private String plazo;
    private LocalDateTime validez;
    private String tipoOrden;
    private Long monto;
    private Long idFuente;
}
