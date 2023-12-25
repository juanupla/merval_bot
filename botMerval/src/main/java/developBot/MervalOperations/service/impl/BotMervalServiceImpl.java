package developBot.MervalOperations.service.impl;

import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.models.PrecioDTO;
import developBot.MervalOperations.service.BotMervalService;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BotMervalServiceImpl implements BotMervalService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ModelMapper modelMapper = new ModelMapper();

    //001 Este método elimina aquellos activos que se encuentran en cartera ya que
    //por cada activo se abriran posiciones del 5% del capital, sin ajuste.
    //así solo se podran aquellos activos que no se encuentren en cartera.
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

    //003 este método será interno y se encargara de calcular las EMAs solicitadas y se devuelven en el objeto
    // EmasDTO que contiene 4 BidDecimal que corresponde a cada EMA

    //aca vamos a devolver una EmaDTO, esto ahora esta solo para probar
    public boolean calculoEMAs(String token, String simbolo) {
        //necesito traer 100 cotizaciones
        /*
        List<Cotizacion> cotizaciones = getCotizaciones(token,simbolo);
        List<PrecioDTO> cotiz = getPriceByIntervals(cotizaciones);
        BigDecimal ema3 = getEma(3,cotiz);
        BigDecimal ema9 = getEma(9,cotiz);
        BigDecimal ema21 = getEma(21,cotiz);
        BigDecimal ema50 = getEma(50,cotiz);

        boolean flag = true;
        if(ema21.compareTo(ema50)<0){
            flag = false;
        } else if (ema9.compareTo(ema21)<0) {
            flag = false;
        } else if (ema3.compareTo(ema9)<0) {
            flag = false;
        }

        System.out.println("Ema3: " + ema3.toString() + "\n"+
                "Ema9: " + ema9.toString() + "\n"+
                "Ema21: " + ema21 + "\n"+
                "Ema50: " + ema50.toString() + "\n");
        return flag;

         */
        return false;
    }

    //SUB-001 Este metodo se utiliza en el metodo 003 para devolverle el listado de cotizaciones segun el simbolo recibido
    //La lista devuelve el historial de mayor a menor
    public List<Cotizacion> getCotizaciones(String token, String simbolo){
        LocalDateTime fechaHasta = LocalDateTime.now();
        LocalDateTime fechaDesde = fechaHasta.minusDays(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaDesdeStr = fechaDesde.format(formatter);
        String fechaHastaStr = fechaHasta.format(formatter);

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
    }


    public List<BigDecimal> getEMAs(List<PrecioDTO> cotizaciones){
        boolean flag = false;

        BigDecimal betaEMA9 = getBeta(9);
        BigDecimal betaEMA21 = getBeta(21);
        BigDecimal betaEMA50 = getBeta(50);

        BigDecimal ema_1ByEMA9 = getEma_1(9,cotizaciones);
        BigDecimal ema_1EMA21 = getEma_1(21,cotizaciones);
        BigDecimal ema_1EMA50 = getEma_1(50,cotizaciones);



        return null;
    }

    //Este metodo deuvelve los ultimos 100 intervalos de 15 min, considerando al acual
    public List<PrecioDTO> getPriceByIntervals(List<Cotizacion> cotizaciones) {


        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioHorario = ahora.withHour(11).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finHorario = ahora.withHour(17).withMinute(0).withSecond(0).withNano(0);


        LocalDateTime tiempoActual = LocalDateTime.now();
        LocalDateTime tiempoMenos1485Minutos = tiempoActual.minusMinutes(1485); // 1485 minutos, es decir, 99 intervalos de 15 minutos

        int contador = 99;

        List<PrecioDTO> precioDTOList = new ArrayList<>();

        while (contador >= 0) {
            LocalDateTime inicioIntervalo = tiempoMenos1485Minutos;
            LocalDateTime finIntervalo = tiempoMenos1485Minutos.plusMinutes(15);


            if (inicioIntervalo.isBefore(inicioHorario)) {
                inicioIntervalo = inicioHorario;
            }
            if (finIntervalo.isAfter(finHorario)) {
                finIntervalo = finHorario;
            }

            List<Cotizacion> cotizacionIntervalo = new ArrayList<>();

            for (Cotizacion cotizacion : cotizaciones) {
                LocalDateTime fechaCotizacion = LocalDateTime.parse(cotizacion.getFechaHora());
                if (fechaCotizacion.isAfter(inicioIntervalo) && fechaCotizacion.isBefore(finIntervalo)
                        && fechaCotizacion.isAfter(inicioHorario) && fechaCotizacion.isBefore(finHorario)) {
                    cotizacionIntervalo.add(cotizacion);
                }
            }

            double ultimoPrecio = 0;
            LocalDateTime horaUltimoPrecio = null;
            for (Cotizacion cotizacion : cotizacionIntervalo) {
                LocalDateTime fechaCotizacion = LocalDateTime.parse(cotizacion.getFechaHora());
                if (fechaCotizacion.isAfter(inicioIntervalo) && fechaCotizacion.isBefore(finIntervalo)) {
                    if (horaUltimoPrecio == null || fechaCotizacion.isAfter(horaUltimoPrecio)) {
                        horaUltimoPrecio = fechaCotizacion;
                        ultimoPrecio = cotizacion.getUltimoPrecio();
                    }
                }
            }

            PrecioDTO priceByIntervals = new PrecioDTO(ultimoPrecio, horaUltimoPrecio, contador);
            precioDTOList.add(priceByIntervals);

            contador--;
            tiempoMenos1485Minutos = tiempoMenos1485Minutos.plusMinutes(15);
            precioDTOList.sort(Comparator.comparingInt(PrecioDTO::getIntervaloNro));
        }
        return precioDTOList;
    }
    //este metodo respalda al getEma() en una fracion del calculo matematio a realizar: Ema-t1

    //SUB-002 este metodo se utiliza para obtener la EMAt-1
    public BigDecimal getEma_1(Integer emaNro, List<PrecioDTO> cotizaciones) {
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
        return numerador.divide(n1);
    }

}
