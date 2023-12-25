package developBot.MervalOperations.service.impl;

import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.service.MiCuentaService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MiCuentaServiceImpl implements MiCuentaService {

    @Override
    public boolean estadoCuenta(String token) {
        RestTemplate restTemplate = new RestTemplate();
        ModelMapper modelMapper = new ModelMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EstadoCuenta> estadoCuentaResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/estadocuenta",
                HttpMethod.GET,entity, EstadoCuenta.class);

        EstadoCuenta estadoCuenta = modelMapper.map(estadoCuentaResponseEntity.getBody(), EstadoCuenta.class);

        for (int i = 0; i< estadoCuenta.getCuentas().size(); i++){
            System.out.println(estadoCuenta.getCuentas().get(i));
        }
        if(estadoCuenta.getEstadisticaModels() != null){
            for (int i = 0; i< estadoCuenta.getEstadisticaModels().size(); i++){
                System.out.println(estadoCuenta.getEstadisticaModels().get(i));
            }
        }
        System.out.println("totalEnPesos: " + estadoCuenta.getTotalEnPesos());
        return true;
    }
    @Override
    public boolean portafolio(String token, String pais) {//pais = argentina ; estados_Unidos
        RestTemplate restTemplate = new RestTemplate();
        ModelMapper modelMapper = new ModelMapper();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("pais", pais);

        ResponseEntity<Portafolio> portafolioResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/portafolio/{pais}",
                HttpMethod.GET,entity, Portafolio.class,urlParams);

        Portafolio portafolio = modelMapper.map(portafolioResponseEntity.getBody(), Portafolio.class);

        System.out.println("pais:" + portafolio.getPais());
        if(portafolio.getActivos() != null){
            for (int i = 0; i< portafolio.getActivos().size(); i++){
                System.out.println("Activo: " + portafolio.getActivos().get(i));
            }
        }
        return true;
    }
}
