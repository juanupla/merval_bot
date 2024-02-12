package developBot.MervalOperations.services.iolApiService;

import developBot.MervalOperations.models.clientModel.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.dto.ClientJwtUtilDTO;
import org.springframework.stereotype.Service;

@Service
public interface ClientJwtUtilService {
    ClientJwtUtilDTO getToken();
    ClientJwtUtilDTO extractToken(String responseBody);
    ClientJwtUtilDTO refToken(String refreshToken);

}
