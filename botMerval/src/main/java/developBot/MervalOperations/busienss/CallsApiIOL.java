package developBot.MervalOperations.busienss;

import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.operar.Comprar;
import developBot.MervalOperations.models.clientModels.operar.PurcheaseResponse;
import developBot.MervalOperations.models.clientModels.operar.Vender;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
public class CallsApiIOL {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ModelMapper modelMapper = new ModelMapper();
    //---------------------------------------------------
    //-------- llamadas a la API
    public Portafolio getPortafolioByPais(String token, String pais){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put("pais", pais);

        ResponseEntity<Portafolio> portafolioResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/portafolio/"+pais,
                HttpMethod.GET,entity, Portafolio.class);

        Portafolio portafolio = portafolioResponseEntity.getBody();
        return portafolio;
    }
    public Operacion[] getOperaciones(String token){
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

        Operacion[] operaciones = response.getBody();

        return operaciones;
    }

    public Response deletePendingOrders(String token, Integer numeroOperacion){
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
        Response resp = operacionResponse.getBody();
        return resp;
    }
    public CotizacionDetalleMobile getDetailCotization(String token, String simbolo){
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
        CotizacionDetalleMobile cotizacion = response.getBody(); //traigo la ultima cotizacion del instrumento
        return cotizacion;
    }

    public PurcheaseResponse postSellAsset(String token, String simbolo, Double cantidadVenta, Double precioPuntaCompra){
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

        PurcheaseResponse respon = resp.getBody();
        return respon;
    }

    public EstadoCuenta getAccountStatus(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<EstadoCuenta> portafolioResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/estadocuenta",
                HttpMethod.GET, entity, EstadoCuenta.class);

        EstadoCuenta estadoCuenta = portafolioResponseEntity.getBody();

        return estadoCuenta;
    }

    public PurcheaseResponse postBuyAsset(String token, String simbolo, Integer cantidad, Double precioPuntaVenta){
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

        PurcheaseResponse response = responseEntity.getBody();

        return response;
    }

    public List<Cotizacion> getCotizaciones(String token, String simbolo) throws InterruptedException {
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
                Thread.sleep(1500); // Esperar 1.3 segundos antes de reintentar
            }
        }

        return null;
    }
}
