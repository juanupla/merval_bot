package developBot.MervalOperations.models.titulos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Puntas {
    private int cantidadCompra;
    private Long precioCompra;
    private Long precioVenta;
    private int cantidadVenta;
}
