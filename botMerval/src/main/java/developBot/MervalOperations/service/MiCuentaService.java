package developBot.MervalOperations.service;

import org.springframework.stereotype.Service;

@Service
public interface MiCuentaService {
    boolean estadoCuenta(String token);
    boolean portafolio(String token, String pais);
}
