package developBot.MervalOperations.models.clientModel.operar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurcheaseResponse {
    private Integer numeroOperacion;
    private Integer statusCode;
    private String title;
    private String description;
}
