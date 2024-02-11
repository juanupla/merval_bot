package developBot.MervalOperations.models.clientModel.miCuenta.portafolio;

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
