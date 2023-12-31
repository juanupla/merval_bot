package developBot.MervalOperations.busienss;

import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import lombok.Data;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BotMervalBusiness {

    private final CallsApiIOL callsApiIOL; // No inicializar aquí, sino a través del constructor

    public BotMervalBusiness(CallsApiIOL callsApiIOL) {

        this.callsApiIOL = callsApiIOL;
    }


    //001 Este método elimina aquellos activos presente en la cartera de la lista inicial a recorrer ya que
    //por cada activo se abriran posiciones del 5% del capital, sin ajuste.
    //así solo se podran procesar aquellos activos que no se encuentren en cartera.
    public List<String> removeOperationalTickets(String token, String pais ,List<String> ticketsList) throws InterruptedException {
        int intentos = 3;
        while (intentos > 0) {
            try {

                Portafolio portafolio = callsApiIOL.getPortafolioByPais(token,pais);

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
                Thread.sleep(1500); // Esperar 1.3 segundos antes de reintentar
            }
        }
        return null;

    }


    //Este metodo obtiene aquellos posiciones que ESTAN en cartera, se arma y envia una lista de los mismos.
    public List<Posicion> operationalTickets(String token,String pais) {
        int intentos = 3;
        while (intentos > 0) {
            try {

                Portafolio portafolio = callsApiIOL.getPortafolioByPais(token,pais);

                if (portafolio != null){
                    return new ArrayList<>(portafolio.getActivos());
                }
            }catch (Exception e){
                intentos--;
            }
        }
        return null;
    }


    //002 Este método eliminará aquellas ordenes que queden en estado pendiente,
    //se haya hecho una ejecucion parcial o no, se eliminaran con esta función.
    public List<Operacion> removePendingOrders(String token) {

        Operacion[] operaciones = callsApiIOL.getOperaciones(token);

        if (operaciones.length > 0) {
            int intentos = 3;
            while (intentos > 0) {
                try {
                    List<Operacion> operacionesList = Arrays.asList(operaciones);

                    List<Operacion> operacionesProcesadas = new ArrayList<>();

                    for (Operacion operacion : operacionesList) {

                        Response resp = callsApiIOL.deletePendingOrders(token,operacion.getNumero());

                        if (resp != null && resp.isOk()) {
                            operacionesProcesadas.add(operacion);
                        }
                    }
                    return operacionesProcesadas;

                } catch (Exception e) {
                    intentos--;
                }
            }


        } else {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }


    //Metodo para ejecutar la venta de un activo
    public boolean saleOperation(String token, List<BigDecimal> emas,Posicion activo){
        if (EMAsSaleOperation(emas)){
            int intentos = 3;
            while (intentos>0){
                try {
                    //voy a necesitar consultar el activo para saber
                    // cuales son las puntas para realizar la venta

                    CotizacionDetalleMobile cotizacion = callsApiIOL.getDetailCotization(token,activo.getTitulo().getSimbolo().toUpperCase());

                    if(cotizacion == null){
                        break;
                    }

                    //-------------Venta---------------

                    Integer cantidad = activo.getCantidad();
                    Double precioPuntaCompra = cotizacion.getPuntas().get(0).getPrecioCompra(); //precio obtenido en operaciones anteriores(segun precio obtenido de las puntas - CotizacionDetalleMobile- )

                    Response respon = callsApiIOL.postSellAsset(token,activo.getTitulo().getSimbolo().toUpperCase(),cantidad,precioPuntaCompra);

                    if (respon != null && respon.isOk()){
                        System.out.println("El tiket: " +activo.getTitulo().getSimbolo()+" se ha procesado adecuadamente y se realizo la VENTA de este instrumento");
                        return true;
                    }
                    System.out.println("El tiket: " +activo.getTitulo().getSimbolo()+" no se ha procesado adecuadamente y NO realizo la VENTA de este instrumento");
                    return false;
                }
                catch (Exception e){
                    intentos --;
                }
            }
        }
        System.out.println("El tiket: " +activo.getTitulo().getSimbolo()+" se ha procesado adecuadamente y NO se realizo la VENTA de este instrumento");
        return false;
    }


    //Metodo para ejecutar la compra de un activo
    //podra no operar incluso si las condiciones de EMAsPurchaseOperation estan dadas por cuestiones de capital
    public boolean purchaseOperation(String token, String ticket, List<BigDecimal> emas) throws InterruptedException {
        if (EMAsPurchaseOperation(token,ticket,emas)){
            int intentos = 3;
            while (intentos > 0) {
                try {
                    //traigo la ultima cotizacion del instrumento
                    CotizacionDetalleMobile cotizacion = callsApiIOL.getDetailCotization(token,ticket.toUpperCase());

                    //traigo el estado de cuenta
                    EstadoCuenta estadoCuenta = callsApiIOL.getAccountStatus(token);

                    double valor5PorcientoCartera = estadoCuenta.getCuentas().get(0).getTotal()*0.05;

                    //que al menos se púeda comprar 1
                    if(cotizacion == null || valor5PorcientoCartera < cotizacion.getPuntas().get(0).getPrecioVenta()){
                        return false;
                    }

                    //los findos "disponibles" no poseen el 5% sobre el total de la cartera para operar este activo
                    if(valor5PorcientoCartera > estadoCuenta.getCuentas().get(0).getDisponible()){
                        return false;
                    }

                    double operacion = valor5PorcientoCartera/cotizacion.getPuntas().get(0).getPrecioVenta();

                    //si puedo comprar 2,653 siempre redondeo hacia abajo
                    int operacionFinal = (int) Math.floor(operacion);

                    if (operacionFinal>= 1){
                        //que la cantidad en la punta de venta sea igual o mayor a la cantidad de mi intencion de compra
                        if(cotizacion.getPuntas().get(0).getCantidadVenta() >= operacionFinal){

                            Response response1 = callsApiIOL.postBuyAsset(token,ticket,operacionFinal,cotizacion.getPuntas().get(0).getPrecioVenta());

                            if (response1 != null && response1.isOk()){
                                System.out.println("El tiket: " +ticket+" se ha procesado adecuadamente y se realizo la COMPRA de este instrumento");
                                return true;
                            }
                            else {
                                System.out.println("El tiket: " +ticket+" no se ha procesado adecuadamente y NO se realizo la COMPRA de este instrumento");
                                if(response1 != null){
                                    System.out.println("mensaje: " +response1.getDetalleMensajes());
                                }

                                return false;
                            }

                        }else {
                            System.out.println("Compra del ticket: "+ticket+" NO fue procesado porque la cantidad en la punta de ventas no eran suficientes");
                            return false;
                        }

                    }
                    System.out.println("El tiket: " +ticket+" se ha procesado adecuadamente y NO se realizo la COMPRA por no alcanzar la unidad minima de compra");
                    return false;
                } catch (HttpServerErrorException e) {
                    intentos--;
                    Thread.sleep(1500); // Esperar 1.3 segundos antes de reintentar
                }
            }

        }
        System.out.println("el tiket "+ticket+" se ha procesado adecuadamente y no se realizo la compra por estrategia fuera de rango");
        return false;
    }


    //Metodo para verificar si las disposicion de emas dan compra - Se utiliza en purchaseOperation()
    public boolean EMAsPurchaseOperation(String token, String ticket, List<BigDecimal> emas) throws InterruptedException {
        int intentos = 3;
        while (intentos > 0) {
            try {
                BigDecimal ema3 = emas.get(0);
                BigDecimal ema9 = emas.get(1);
                BigDecimal ema21 = emas.get(2);
                BigDecimal ema50 = emas.get(3);

                CotizacionDetalleMobile cotizacion = callsApiIOL.getDetailCotization(token,ticket.toUpperCase());

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
                Thread.sleep(1500); // Esperar 1.3 segundos antes de reintentar
            }
        }
        return false;
    }


    //Metodo para verificar si las disposicion de emas dan venta - Se utiliza en saleOperation()
    public boolean EMAsSaleOperation(List<BigDecimal> emas){
        BigDecimal ema3 = emas.get(0);
        BigDecimal ema9 = emas.get(1);

        if(ema3.compareTo(ema9)<0){
            return true;
        }
        return false;
    }


    //003 este método será interno y se encargara de calcular las EMAs solicitadas y se devuelven en
    // una lista que contiene 4 BidDecimal que corresponde a cada EMA
    public List<BigDecimal> calculoEMAs(String token, String simbolo) throws InterruptedException {

        List<Cotizacion> cotizaciones = callsApiIOL.getCotizaciones(token, simbolo);

        List<BigDecimal> emas = getEMAs(cotizaciones);

        return emas;
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
                multipl = betaResta.multiply(ema_1);//en el primer calculor de la longitud de la ema, EMAt-1 es el resultado de una SMA(sample mobile averange)
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
            for(int i = 21; i<42; i++){
                Double ultimpPrecio = cotizaciones.get(i).getUltimoPrecio();
                BigDecimal ultimpPrecioBigDecimal = BigDecimal.valueOf(ultimpPrecio);
                acumulador = acumulador.add(ultimpPrecioBigDecimal);
            }
            return acumulador.divide(new BigDecimal("21"),20, RoundingMode.HALF_UP);

        } else if (emaNro == 50) {
            BigDecimal acumulador = new BigDecimal("0");
            for(int i = 50; i<100; i++){
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