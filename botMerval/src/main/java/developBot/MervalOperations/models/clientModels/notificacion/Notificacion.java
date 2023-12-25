package developBot.MervalOperations.models.clientModels.notificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notificacion {
    private String titulo;
    private String mensaje;
    private String link;
}
