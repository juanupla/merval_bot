package developBot.MervalOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Titulo;
import developBot.MervalOperations.service.BotMervalService;
import developBot.MervalOperations.service.impl.BotMervalServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.hamcrest.Matchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;





import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;




public class BotServiceTest {

    @Test
    public void removeOperationalTicketsTest() throws InterruptedException {
        ModelMapper modelMapper = new ModelMapper();

        String cadena = "AAPL,GOOGL,AMZN,PAMP";
        List<String> originalList = Arrays.asList(cadena.split(","));

        // Configuración de la respuesta simulada
        Posicion p1 = new Posicion();
        Posicion p2 = new Posicion();

        Titulo t1 = new Titulo();
        Titulo t2 = new Titulo();

        t1.setSimbolo("AAPL");
        t2.setSimbolo("GOOGL");

        p1.setTitulo(t1);
        p2.setTitulo(t2);

        List<Posicion> posicions = new ArrayList<>();
        posicions.add(p1);
        posicions.add(p2);

        Portafolio portafolio = new Portafolio();
        portafolio.setActivos(posicions);

        // Simulación de RestTemplate
        RestTemplate restTemplateMock = Mockito.mock(RestTemplate.class);
        ResponseEntity<Portafolio> responseEntity = new ResponseEntity<>(portafolio,HttpStatus.OK);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer ");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("pais", "argentina");

        Mockito.when(restTemplateMock.exchange(
                        Mockito.eq("https://api.invertironline.com/api/v2/portafolio/{pais}"),
                        Mockito.eq(HttpMethod.GET),
                        Mockito.eq(entity),
                        Mockito.eq(Portafolio.class),
                        Mockito.anyMap()
                ))
                .thenReturn(responseEntity);

        responseEntity.getBody();
        BotMervalService botMervalService = new BotMervalServiceImpl(restTemplateMock,modelMapper);


        List<String> ultimo = botMervalService.removeOperationalTickets("azdsa", "argentina", originalList);

        // Verificación del resultado esperado
        Assertions.assertEquals(Arrays.asList("AMZN", "PAMP"), ultimo);
    }

}
