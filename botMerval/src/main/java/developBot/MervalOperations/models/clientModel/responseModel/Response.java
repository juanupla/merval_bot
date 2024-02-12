package developBot.MervalOperations.models.clientModel.responseModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private boolean ok;
    private List<DetalleMensaje> messages;
}