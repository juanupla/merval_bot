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
    private List<String> pais;
    private List<String> mercado;
    private List<String> tipo;
    private List<String> plazo;
    private List<String> moneda;
}
