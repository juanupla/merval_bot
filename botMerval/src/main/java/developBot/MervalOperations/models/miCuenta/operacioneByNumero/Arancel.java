package developBot.MervalOperations.models.miCuenta.operacioneByNumero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Arancel {
    private String tipo;
    private Long neto;
    private Long iva;
    private String moneda;
}
