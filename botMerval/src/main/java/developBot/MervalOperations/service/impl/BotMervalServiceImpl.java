package developBot.MervalOperations.service.impl;

import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.operar.Comprar;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import developBot.MervalOperations.service.BotMervalService;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
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
    //si el activo en cartera da positivo en EMAsPurchaseOperation no hace nada, si da positivo en
    //EMAsSaleOperation ejecutara entonces saleOperation para vender el activo en cuestion.
    @Override
    public List<String> removeOperationalTickets(String token, String pais ,List<String> ticketsList) throws InterruptedException {

        int intentos = 3;
        while (intentos > 0) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                Map<String, String> urlParams = new HashMap<>();
                urlParams.put("pais", pais);

                ResponseEntity<Portafolio> portafolioResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/portafolio/{pais}",
                        HttpMethod.GET,entity, Portafolio.class,urlParams);

                Portafolio portafolio = modelMapper.map(portafolioResponseEntity.getBody(), Portafolio.class);

                if(portafolio == null){
                    break;
                }

                if(portafolio.getActivos().size() > 0){
                    for (Posicion activo: portafolio.getActivos()) {
                        for(int i = 0; i<ticketsList.size();i++){
                            if(ticketsList.get(i).equalsIgnoreCase(activo.getTitulo().getSimbolo())){
                                ticketsList.remove(i);
                            }
                        }
                    }
                }
                return ticketsList;
            } catch (HttpServerErrorException e) {
                // Manejo de errores específicos de servidor
                // Por ejemplo, registrar el error y reducir el contador de intentos
                intentos--;
                // Esperar antes de volver a intentar (puedes ajustar el tiempo según tus necesidades)
                Thread.sleep(5000); // Esperar 5 segundos antes de reintentar
            }
        }
        return null;

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
        if (operaciones == null) {
            return Collections.emptyList();
        }
        if (operaciones.length > 0) {
            int intentos = 3;
            while (intentos > 0) {
                try {
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

                        if (Objects.requireNonNull(operacionResponse.getBody()).isOk()) {
                            flag1++;
                        }
                    }
                    return operacionesList;

                } catch (Exception e) {
                    intentos--;
                }
            }


        } else {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    //Metodo para verificar si las disposicion de emas dan compra
    @Override
    public boolean EMAsPurchaseOperation(String token, String ticket, List<BigDecimal> emas) throws InterruptedException {
        int intentos = 3;
        while (intentos > 0) {
            try {
                BigDecimal ema3 = emas.get(0);
                BigDecimal ema9 = emas.get(1);
                BigDecimal ema21 = emas.get(2);
                BigDecimal ema50 = emas.get(3);

                String path = "https://api.invertironline.com/api/v2/bCBA/Titulos/"+ticket.toUpperCase()+"/CotizacionDetalleMobile/t2";

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);

                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<CotizacionDetalleMobile> response = restTemplate.exchange(
                        path,
                        HttpMethod.GET,
                        entity,
                        CotizacionDetalleMobile.class
                );
                CotizacionDetalleMobile cotizacion = modelMapper.map(response.getBody(),CotizacionDetalleMobile.class);

                if (cotizacion == null){
                    return false;
                }

                BigDecimal bigDecimalValue = BigDecimal.valueOf(cotizacion.getUltimoPrecio());
                if(bigDecimalValue.compareTo(ema3)>0 && ema3.compareTo(ema9)>0 && ema9.compareTo(ema21)>0 && ema21.compareTo(ema50)>0){
                    return true;
                }
                return false;
            } catch (HttpServerErrorException e) {
                intentos--;
                Thread.sleep(5000); // Esperar 5 segundos antes de reintentar
            }
        }
        return false;
    }
    //Metodo para verificar si las disposicion de emas dan venta
    @Override
    public boolean EMAsSaleOperation(List<BigDecimal> emas) throws InterruptedException {
        BigDecimal ema3 = emas.get(0);
        BigDecimal ema9 = emas.get(1);

        if(ema3.compareTo(ema9)<0){
            return true;
        }
        return false;
    }

    //Metodo para ejecutar la venta de un activo
    @Override
    public boolean saleOperation(String token, String tiket){
        return false;
    }

    //Metodo para ejecutar la compra de un activo
    //podra no operar incluso si las condiciones de EMAsPurchaseOperation estan dadas por cuestiones de capital
    @Override
    public boolean purchaseOperation(String token, String ticket, List<BigDecimal> emas) throws InterruptedException {
        if (EMAsPurchaseOperation(token,ticket,emas)){
            int intentos = 3;
            while (intentos > 0) {
                try {
                    String path = "https://api.invertironline.com/api/v2/bCBA/Titulos/"+ticket.toUpperCase()+"/CotizacionDetalleMobile/t2";

                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + token);

                    HttpEntity<?> entity = new HttpEntity<>(headers);

                    ResponseEntity<CotizacionDetalleMobile> response = restTemplate.exchange(
                            path,
                            HttpMethod.GET,
                            entity,
                            CotizacionDetalleMobile.class
                    );
                    CotizacionDetalleMobile cotizacion = response.getBody(); //traigo la ultima cotizacion del instrumento


                    ResponseEntity<Portafolio> portafolioResponseEntity = restTemplate.exchange("https://api.invertironline.com/api/v2/estadocuenta",
                            HttpMethod.GET,entity, Portafolio.class);

                    EstadoCuenta estadoCuenta = modelMapper.map(portafolioResponseEntity.getBody(), EstadoCuenta.class); //traigo el estado de cuenta

                    double valor5PorcientoCartera = estadoCuenta.getCuentas().get(0).getDisponible()*0.05;
                    if(cotizacion == null || valor5PorcientoCartera < cotizacion.getPuntas().get(0).getPrecioVenta()){ //que al menos se púeda comprar 1
                        return false;
                    }
                    double operacion = valor5PorcientoCartera/cotizacion.getPuntas().get(0).getPrecioVenta();
                    int operacionFinal = (int) Math.floor(operacion);

                    if (operacionFinal>= 1){
                        if(cotizacion.getPuntas().get(0).getCantidadVenta() >= operacionFinal){//que la cantidad en la punta de venta sea igual o mayor a mi intencion de compra
                            Comprar compra = new Comprar();
                            compra.setMercado("bCBA");
                            compra.setSimbolo(ticket);
                            compra.setCantidad(Long.parseLong(Integer.toString(operacionFinal)));
                            compra.setPrecio(cotizacion.getPuntas().get(0).getPrecioVenta());
                            compra.setPlazo("t2");

                            LocalDateTime time = LocalDateTime.now().withHour(17).withMinute(0).withSecond(0);
                            compra.setValidez(time);
                            compra.setTipoOrden("precioLimite");


                            String url1 = "https://api.invertironline.com/api/v2/operar/Comprar";
                            HttpHeaders headers1 = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);


                            HttpEntity<Comprar> requestEntity1 = new HttpEntity<>(compra, headers);

                            // Enviar la solicitud POST y obtener la respuesta
                            ResponseEntity<Response> responseEntity = restTemplate.exchange(
                                    url1,
                                    HttpMethod.POST,
                                    requestEntity1,
                                    Response.class);

                            Response response1 = responseEntity.getBody();

                            if (response1 != null && response1.isOk()){
                                System.out.println("El tiket: " +ticket+" se ha procesado adecuadamente. Se realizo la compra de este instrumento");
                                return true;
                            }
                            else {
                                System.out.println("El tiket: " +ticket+" no se ha procesado adecuadamente y no se realizo la compra de este instrumento");
                                if(response1 != null){
                                    System.out.println("mensaje: " +response1.getDetalleMensajes());
                                }

                                return false;
                            }

                        }else {
                            System.out.println("Compra del ticket: "+ticket+" no fue procesado porque la cantidad en la punta de ventas no eran suficientes");
                            return false;
                        }

                    }
                    System.out.println("El tiket: " +ticket+" se ha procesado adecuadamente y no se realizo la compra por no alcanzar la unidad minima de compra");
                    return false;
                } catch (HttpServerErrorException e) {
                    intentos--;
                    Thread.sleep(3000); // Esperar 5 segundos antes de reintentar
                }
            }

        }
        System.out.println("el tiket "+ticket+" se ha procesado adecuadamente y no se realizo la compra por estrategia fuera de rango");
        return false;
    }

    //SUB-001 Este metodo se utiliza en el metodo 003 para devolverle el listado de cotizaciones segun el simbolo recibido
    //La lista devuelve el historial de mayor a menor
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
                Thread.sleep(3000); // Esperar 3 segundos antes de reintentar
            }
        }

        return null;
    }

    //Este metodo se utiliza para normalizar las cotizaciones recibidas y
    //objtener desde el dia anterior(habil) al actual las cotizaciones al cierre de cada dia
    //este metodo se utiliza en el getEma
    public List<Cotizacion> normalizeCotization(List<Cotizacion> cotizaciones){
        List<Cotizacion> cotizacionNormalized = new ArrayList<>();


        LocalDateTime fecha = LocalDateTime.now().minusDays(1);
        if (isItMondaySundayOrSaturday(fecha).equals("MONDAY")){
            fecha = fecha.minusDays(2);//queda parado en sabado, pero en la logica que sigue se va a restar 1
        }
        if (isItMondaySundayOrSaturday(fecha).equals("SUNDAY")){
            fecha = fecha.minusDays(1);//queda parado en sabado, pero en la logica que sigue se va a restar 1
        }

        LocalDateTime fechaAyer = fecha.minusDays(1);
        LocalDateTime fechaAntesDeAyer = LocalDateTime.now();
        if(isItMondaySundayOrSaturday(fechaAyer).equals("MONDAY")){
            fechaAntesDeAyer = fecha.minusDays(4);
        }else {
            fechaAntesDeAyer = fechaAntesDeAyer.minusDays(2);
        }
        fechaAyer = fechaAyer.withHour(16);  //los horarios de mercado son de las 11 a 17hs, necesito obtener la ultima cotizacion del dia
        fechaAntesDeAyer = fechaAyer.withHour(16);

        LocalDateTime fechaReferente = fechaAyer.withHour(18);

        Cotizacion cotizacionFechaAyer = new Cotizacion();
        Cotizacion cotizacionFechaAntesDeAyer = new Cotizacion();

        for (int j = 0; j < cotizaciones.size(); j++) {
            if (cotizaciones.get(j).getFechaHora().compareTo(fechaAyer.toString())>0){

                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime fechaConvertida = LocalDateTime.parse(cotizaciones.get(j).getFechaHora(), formatter);
                fechaAyer = fechaConvertida;
                cotizacionFechaAyer = cotizaciones.get(j);
            }
            if(cotizaciones.get(j).getFechaHora().compareTo(fechaAntesDeAyer.toString())>0 &&
                    cotizaciones.get(j).getFechaHora().compareTo(fechaReferente.toString())<0){

                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime fechaConvertida = LocalDateTime.parse(cotizaciones.get(j).getFechaHora(), formatter);
                fechaAntesDeAyer = fechaConvertida;
                cotizacionFechaAntesDeAyer = cotizaciones.get(j);
            }
        }
        //hasta acá encuentro los dos dias anteriores de cotizacion, tienen muchas cotizaciones(intradiarias) y solo quiero las del cierre



        //ahora, elimino todas esas fechas, solo me voy a quedar con las que seleccione en el paso anterior
        LocalDateTime eliminarRango = fechaAntesDeAyer.withHour(1);
        for (int k = 0; k < cotizaciones.size(); k++) {
            if (cotizaciones.get(k).getFechaHora().compareTo(eliminarRango.toString())>0
                    && !cotizaciones.get(k).equals(cotizacionFechaAyer)
                    && !cotizaciones.get(k).equals(cotizacionFechaAntesDeAyer))
            {
                continue;
            }
            else {
                cotizacionNormalized.add(cotizaciones.get(k));
            }
        }

        return cotizacionNormalized;
    }

    //003 este método será interno y se encargara de calcular las EMAs solicitadas y se devuelven en el objeto
    // EmasDTO que contiene 4 BidDecimal que corresponde a cada EMA
    //aca vamos a devolver una EmaDTO, esto ahora esta solo para probar
    public List<BigDecimal> calculoEMAs(String token, String simbolo) throws InterruptedException {

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

    public BigDecimal getEma(Integer emaNro, BigDecimal beta, BigDecimal ema_1,List<Cotizacion> cotizacions){

        List<Cotizacion> cotizaciones = normalizeCotization(cotizacions);

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

    public String isItMondaySundayOrSaturday(LocalDateTime localDate){

        DayOfWeek diaSemana = localDate.getDayOfWeek();
        return diaSemana.name().toUpperCase();
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
