package developBot.MervalOperations.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientJwtUtilDTO {
    private String accesToken;
    private String refreshToken;
    private Long expires_in;
    private String expires;
}
