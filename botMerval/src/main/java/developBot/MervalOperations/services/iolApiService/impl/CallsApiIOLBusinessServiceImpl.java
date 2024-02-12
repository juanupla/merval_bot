package developBot.MervalOperations.services.iolApiService.impl;

import developBot.MervalOperations.models.clientModel.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModel.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModel.operar.Comprar;
import developBot.MervalOperations.models.clientModel.operar.PurcheaseResponse;
import developBot.MervalOperations.models.clientModel.operar.Vender;
import developBot.MervalOperations.models.clientModel.responseModel.Response;
import developBot.MervalOperations.models.clientModel.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.clientModel.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import developBot.MervalOperations.services.iolApiService.CallsApiIOLBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class CallsApiIOLBusinessServiceImpl implements CallsApiIOLBusinessService {
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public Portafolio getPortafolioByPais(String token, String pais) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Portafolio> portafolioResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/portafolio/"+pais,
                HttpMethod.GET,entity, Portafolio.class);

        try {
            Portafolio portafolio = portafolioResponseEntity.getBody();
            if(portafolio!= null){
                return portafolio;
            }
            else {
                throw new ErrorResponseException(HttpStatusCode.valueOf(500));
            }
        }catch (ErrorResponseException e){
            throw e;
        }


    }

    @Override
    public Operacion[] getOperaciones(String token) {//operaciones pendientes
        String url = "https://api.invertironline.com/api/v2/operaciones?estado=pendientes";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Operacion[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Operacion[].class
        );


        try {
            Operacion[] operaciones = response.getBody();
            if(operaciones!= null){
                return operaciones;
            }
            else {
                throw new ErrorResponseException(HttpStatusCode.valueOf(500));
            }
        }catch (ErrorResponseException e){
            throw e;
        }
    }

    @Override
    public Response deletePendingOrders(String token, Integer numeroOperacion) {
        String operacionUrl = "https://api.invertironline.com/api/v2/operaciones/" + numeroOperacion.toString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Response> operacionResponse = restTemplate.exchange(
                operacionUrl,
                HttpMethod.DELETE,
                entity,
                Response.class
        );

        try {
            Response resp = operacionResponse.getBody();
            if(resp!= null){
                return resp;
            }
            else {
                throw new ErrorResponseException(HttpStatusCode.valueOf(500));
            }
        }catch (ErrorResponseException e){
            throw e;
        }
    }

    @Override
    public CotizacionDetalleMobile getDetailCotization(String token, String simbolo) {
        String path1 = "https://api.invertironline.com/api/v2/bCBA/Titulos/"+simbolo.toUpperCase()+"/CotizacionDetalleMobile/t2";

        HttpHeaders headers1 = new HttpHeaders();
        headers1.set("Authorization", "Bearer " + token);

        HttpEntity<?> entity1 = new HttpEntity<>(headers1);

        ResponseEntity<CotizacionDetalleMobile> response = restTemplate.exchange(
                path1,
                HttpMethod.GET,
                entity1,
                CotizacionDetalleMobile.class
        );

        try {
            CotizacionDetalleMobile cotizacion = response.getBody(); //traigo la ultima cotizacion del instrumento
            if(cotizacion!= null){
                return cotizacion;
            }
            else {
                throw new ErrorResponseException(HttpStatusCode.valueOf(500));
            }
        }catch (ErrorResponseException e){
            throw e;
        }
    }

    @Override
    public PurcheaseResponse postSellAsset(String token, String simbolo, Double cantidadVenta, Double precioPuntaCompra) {
        String path = "https://api.invertironline.com/api/v2/operar/Vender";

        LocalDateTime validez = LocalDateTime.now().withHour(17);
        String tipoOrden = "precioLimite";
        String plazo = "t2";
        Vender venta = new Vender("bCBA",simbolo.toUpperCase(),cantidadVenta,precioPuntaCompra,validez.toString(),tipoOrden,plazo,0);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Vender> entity = new HttpEntity<>(venta,headers);

        ResponseEntity<PurcheaseResponse> resp = restTemplate.exchange(path,HttpMethod.POST,entity,PurcheaseResponse.class);


        try {
            PurcheaseResponse respon = resp.getBody();
            if(respon!= null){
                return respon;
            }
            else {
                throw new ErrorResponseException(HttpStatusCode.valueOf(500));
            }
        }catch (ErrorResponseException e){
            throw e;
        }
    }

    @Override
    public EstadoCuenta getAccountStatus(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EstadoCuenta> portafolioResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/estadocuenta",
                HttpMethod.GET, entity, EstadoCuenta.class);

        try {
            EstadoCuenta estadoCuenta = portafolioResponseEntity.getBody();
            if(estadoCuenta!= null){
                return estadoCuenta;
            }
            else {
                throw new ErrorResponseException(HttpStatusCode.valueOf(500));
            }
        }catch (ErrorResponseException e){
            throw e;
        }
    }

    @Override
    public PurcheaseResponse postBuyAsset(String token, String simbolo, Integer cantidad, Double precioPuntaVenta) {
        Comprar compra = new Comprar();
        compra.setMercado("bCBA");
        compra.setSimbolo(simbolo);
        compra.setCantidad(Long.parseLong(Integer.toString(cantidad)));
        compra.setPrecio(precioPuntaVenta);
        compra.setPlazo("t2");

        LocalDateTime time = LocalDateTime.now().withHour(16).withMinute(59).withSecond(0);
        compra.setValidez(time.toString());
        compra.setTipoOrden("precioLimite");
        compra.setMonto(null);
        compra.setIdFuente(0);


        String url = "https://api.invertironline.com/api/v2/operar/Comprar";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<Comprar> requestEntity = new HttpEntity<>(compra, headers);

        // Enviar la solicitud POST y obtener la respuesta
        ResponseEntity<PurcheaseResponse> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                PurcheaseResponse.class);

        try {
            PurcheaseResponse response = responseEntity.getBody();
            if(response!= null){
                return response;
            }
            else {
                throw new ErrorResponseException(HttpStatusCode.valueOf(500));
            }
        }catch (ErrorResponseException e){
            throw e;
        }
    }

    @Override
    public List<Cotizacion> getCotizaciones(String token, String simbolo){
        int intentos = 3;
        while (intentos > 0) {
            try {
                LocalDateTime horaHasta = LocalDateTime.now().withHour(17).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime horaDesde = LocalDateTime.now().minusDays(200).withHour(11).withMinute(0).withSecond(0);



                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String fechaDesdeStr = horaDesde.format(formatter);
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String fechaHastaStr = horaHasta.format(formatter2);

                String path = "https://api.invertironline.com/api/v2/bCBA/Titulos/"+simbolo+"/Cotizacion/seriehistorica/"+fechaDesdeStr+"/"+fechaHastaStr+"/sinAjustar";

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);

                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<Cotizacion[]> response = restTemplate.exchange(
                        path,
                        HttpMethod.GET,
                        entity,
                        Cotizacion[].class
                );

                Cotizacion[] cotizaciones = response.getBody();

                if(cotizaciones == null){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                }

                return Arrays.asList(cotizaciones);
            } catch (HttpServerErrorException e) {
                intentos--;
            }
        }

        throw new ErrorResponseException(HttpStatusCode.valueOf(500));
    }
    @Override
    public Operacion[] getEndOfTheDayTrades(String token) {//operaciones terminadas del dia

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://api.invertironline.com/api/v2/operaciones")
                .queryParam("estado", "terminadas")
                .queryParam("fechaDesde",LocalDate.now().toString())
                .queryParam("fechaHasta",LocalDate.now().toString());

        ResponseEntity<Operacion[]> response = restTemplate.exchange(
                builder.toString(),
                HttpMethod.GET,
                entity,
                Operacion[].class
        );


        try {
            Operacion[] operaciones = response.getBody();
            if(operaciones!= null){
                return operaciones;
            }
            else {
                throw new ErrorResponseException(HttpStatusCode.valueOf(500));
            }
        }catch (ErrorResponseException e){
            throw e;
        }
    }
}
