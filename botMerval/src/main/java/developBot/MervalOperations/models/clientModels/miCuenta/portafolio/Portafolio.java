package developBot.MervalOperations.models.clientModels.miCuenta.portafolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Portafolio {
    private String pais;
    private List<Posicion> activos;
}
