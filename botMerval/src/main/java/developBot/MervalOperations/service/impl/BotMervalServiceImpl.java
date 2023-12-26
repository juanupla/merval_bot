package developBot.MervalOperations.service.impl;

import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.service.BotMervalService;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BotMervalServiceImpl implements BotMervalService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ModelMapper modelMapper = new ModelMapper();

    //001 Este método elimina aquellos activos presente en la cartera de la lista inicial a recorrer ya que
    //por cada activo se abriran posiciones del 5% del capital, sin ajuste.
    //así solo se podran procesar aquellos activos que no se encuentren en cartera.

    //este metodo tambien podra realizar una comprovacion del estado de los activos ya en carter, es decir:
    //si el activo en cartera da positico en EMAsPurchaseOperation no hace nada, si da positivo en
    //EMAsSaleOperation ejecutara entonces saleOperation para vender el activo en cuestion.
    @Override
    public List<String> removeOperationalTickets(String token, String pais ,List<String> ticketsList) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("pais", pais);

        ResponseEntity<Portafolio> portafolioResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/portafolio/{pais}",
                HttpMethod.GET,entity, Portafolio.class,urlParams);

        Portafolio portafolio = modelMapper.map(portafolioResponseEntity.getBody(), Portafolio.class);



        if(portafolio.getActivos().size() > 0){
            for (Posicion activo: portafolio.getActivos()) {
                for(int i = 0; i<ticketsList.size();i++){
                    if(ticketsList.get(i).equals(activo.getTitulo().getSimbolo().toUpperCase())){
                        ticketsList.remove(i);
                    }
                }
            }
        }
        return ticketsList;
    }

    //002 Este método eliminará aquellas ordenes que queden en estado pendiente,
    //se haya hecho una ejecucion parcial o no, se eliminaran con esta función.
    @Override
    public List<Operacion> removePendingOrders(String token) {
        String url = "https://api.invertironline.com/api/v2/operaciones";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("estado", "pendientes");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Operacion[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Operacion[].class
        );

        Operacion[] operaciones = response.getBody();
        if (operaciones.length > 0) {
            List<Operacion> operacionesList = Arrays.asList(operaciones);
            int flag1 = 0;

            for (Operacion operacion : operacionesList) {
                String operacionUrl = "https://api.invertironline.com/api/v2/operaciones/" + operacion.getNumero();
                ResponseEntity<Response> operacionResponse = restTemplate.exchange(
                        operacionUrl,
                        HttpMethod.GET,
                        entity,
                        Response.class
                );

                if (operacionResponse.getBody().isOk()) {
                    flag1++;
                }
            }

            System.out.println("Se han eliminado: " + flag1 + " operaciones pendientes" + "\n" +
                    "y se han encontrado: " + operacionesList.size() + " operaciones pendientes");

            return operacionesList;
        } else {
            // Manejo de respuesta nula
            return Collections.emptyList();
        }
    }

    //Metodo para verificar si las disposicion de emas dan compra
    @Override
    public boolean EMAsPurchaseOperation(List<BigDecimal> emas){
        return false;
    }
    //Metodo para verificar si las disposicion de emas dan venta
    @Override
    public boolean EMAsSaleOperation(List<BigDecimal> emas){
        return false;
    }
    //Metodo para ejecutar la venta de un activo
    @Override
    public boolean saleOperation(String token, String tiket){
        return false;
    }
    //Metodo para ejecutar la compra de un activo
    @Override
    public boolean purchaseOperation(String token, String tiket){
        return false;
    }


    //SUB-001 Este metodo se utiliza en el metodo 003 para devolverle el listado de cotizaciones segun el simbolo recibido
    //La lista devuelve el historial de mayor a menor
    public List<Cotizacion> getCotizaciones(String token, String simbolo){


        LocalDateTime horaHasta = LocalDateTime.now().withHour(17).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime horaDesde = LocalDateTime.now().minusDays(200).withHour(0).withMinute(0).withSecond(0);

//        while (!esHorarioLaboral(horaDesde, horaHasta)) {
//            // Si la hora actual está fuera del horario laboral, ajustar las fechas al día siguiente
//            horaDesde = horaDesde.plusDays(1).withHour(11);
//            horaHasta = horaHasta.plusDays(1).withHour(17);
//        }
        DayOfWeek diaSem = horaHasta.getDayOfWeek();
        if(diaSem == DayOfWeek.MONDAY) {
            horaDesde = horaDesde.minusDays(2);
        }


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaDesdeStr = horaDesde.format(formatter);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaHastaStr = horaHasta.format(formatter2);

        String path = "https://api.invertironline.com/api/v2/bCBA/Titulos/"+simbolo.toString()+"/Cotizacion/seriehistorica/"+fechaDesdeStr+"/"+fechaHastaStr+"/sinAjustar";

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
    }

    //003 este método será interno y se encargara de calcular las EMAs solicitadas y se devuelven en el objeto
    // EmasDTO que contiene 4 BidDecimal que corresponde a cada EMA
    //aca vamos a devolver una EmaDTO, esto ahora esta solo para probar
    public List<BigDecimal> calculoEMAs(String token, String simbolo) {

        List<Cotizacion> cotizaciones = getCotizaciones(token, simbolo);

        List<BigDecimal> emas = getEMAs(cotizaciones);

        return emas;
    }

    //Este metodo se usara en el 003 para obtener un listados de las EMAs
    public List<BigDecimal> getEMAs(List<Cotizacion> cotizaciones){

        BigDecimal betaEMA3 = getBeta(3);
        BigDecimal betaEMA9 = getBeta(9);
        BigDecimal betaEMA21 = getBeta(21);
        BigDecimal betaEMA50 = getBeta(50);

        BigDecimal ema_1EMA3 = getEma_1(3,cotizaciones);
        BigDecimal ema_1EMA9 = getEma_1(9,cotizaciones);
        BigDecimal ema_1EMA21 = getEma_1(21,cotizaciones);
        BigDecimal ema_1EMA50 = getEma_1(50,cotizaciones);

        BigDecimal getEma3 = getEma(3,betaEMA3,ema_1EMA3,cotizaciones);
        BigDecimal getEma9 = getEma(9,betaEMA9,ema_1EMA9,cotizaciones);
        BigDecimal getEma21 = getEma(21,betaEMA21,ema_1EMA21,cotizaciones);
        BigDecimal getEma50 = getEma(50,betaEMA50,ema_1EMA50,cotizaciones);

        List<BigDecimal> emas = new ArrayList<>();
        emas.add(getEma3);
        emas.add(getEma9);
        emas.add(getEma21);
        emas.add(getEma50);

        return emas;
    }

    public BigDecimal getEma(Integer emaNro, BigDecimal beta, BigDecimal ema_1,List<Cotizacion> cotizaciones){
        BigDecimal ultimaEma = new BigDecimal("0");
        for (int i = emaNro-1; i>=0;i--){

            Double ultim = cotizaciones.get(i).getUltimoPrecio();
            BigDecimal ultimMultip = beta.multiply(BigDecimal.valueOf(ultim));

            BigDecimal betaResta = BigDecimal.valueOf(1).subtract(beta);
            BigDecimal multipl;
            if(i==emaNro-1){
                multipl = betaResta.multiply(ema_1);
            }
            else {
                multipl = betaResta.multiply(ultimaEma);
            }
            ultimaEma = ultimMultip.add(multipl);
        }
        return ultimaEma;
    }

    public BigDecimal getEma_1(Integer emaNro, List<Cotizacion> cotizaciones) {
        if(emaNro == 3){
            BigDecimal acumulador = new BigDecimal("0");
            for(int i = 3; i<6; i++){
                Double ultimpPrecio = cotizaciones.get(i).getUltimoPrecio();
                BigDecimal ultimpPrecioBigDecimal = BigDecimal.valueOf(ultimpPrecio);
                acumulador = acumulador.add(ultimpPrecioBigDecimal);
            }
            return acumulador.divide(new BigDecimal("3"),20, RoundingMode.HALF_UP);
        } else if (emaNro == 9) {

            BigDecimal acumulador = new BigDecimal("0");
            for(int i = 9; i<18; i++){
                Double ultimpPrecio = cotizaciones.get(i).getUltimoPrecio();
                BigDecimal ultimpPrecioBigDecimal = BigDecimal.valueOf(ultimpPrecio);
                acumulador = acumulador.add(ultimpPrecioBigDecimal);
            }
            return acumulador.divide(new BigDecimal("9"),20, RoundingMode.HALF_UP);
        } else if (emaNro == 21) {


            BigDecimal acumulador = new BigDecimal("0");
            for(int i = 20; i<42; i++){
                Double ultimpPrecio = cotizaciones.get(i).getUltimoPrecio();
                BigDecimal ultimpPrecioBigDecimal = BigDecimal.valueOf(ultimpPrecio);
                acumulador = acumulador.add(ultimpPrecioBigDecimal);
            }
            return acumulador.divide(new BigDecimal("21"),20, RoundingMode.HALF_UP);

        } else if (emaNro == 50) {
            BigDecimal acumulador = new BigDecimal("0");
            for(int i = 49; i<100; i++){
                Double ultimpPrecio = cotizaciones.get(i).getUltimoPrecio();
                BigDecimal ultimpPrecioBigDecimal = BigDecimal.valueOf(ultimpPrecio);
                acumulador = acumulador.add(ultimpPrecioBigDecimal);
            }
            return acumulador.divide(new BigDecimal("50"),20, RoundingMode.HALF_UP);
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public BigDecimal getBeta(Integer emaNro){

        BigDecimal numerador = new BigDecimal("2");
        BigDecimal n = BigDecimal.valueOf(emaNro);
        BigDecimal n1= n.add(new BigDecimal("1"));
        return numerador.divide(n1, 20, RoundingMode.HALF_UP); // Ajusta la escala y el modo de redondeo según necesites

    }

}
