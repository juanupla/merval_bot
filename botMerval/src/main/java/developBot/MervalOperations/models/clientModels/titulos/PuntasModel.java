package developBot.MervalOperations.models.clientModels.titulos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuntasModel {
    private Double cantidadCompra;
    private Double precioCompra;
    private Double precioVenta;
    private Double cantidadVenta;
}
