package developBot.MervalOperations.models.clientModels.miCuenta.operacioneByNumero;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Arancel {
    private String tipo;
    private Double neto;
    private Double iva;
    private String moneda;
}
