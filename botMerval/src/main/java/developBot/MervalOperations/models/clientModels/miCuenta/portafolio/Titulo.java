package developBot.MervalOperations.models.clientModels.miCuenta.portafolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Titulo {
    private String simbolo;
    private String descripcion;
    private String pais;
    private String mercado;
    private String tipo;
    private String plazo;
    private String moneda;
}
