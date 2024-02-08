package developBot.MervalOperations.busienss;

import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.operar.PurcheaseResponse;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BotMervalBusiness {

    private final CallsApiIOL callsApiIOL; // No inicializar aquí, sino a través del constructor

    public BotMervalBusiness(CallsApiIOL callsApiIOL) {

        this.callsApiIOL = callsApiIOL;
    }


    //001 Este método elimina aquellos activos presente en la cartera de la lista inicial a recorrer ya que
    //por cada activo se abriran posiciones del 6% del capital, sin ajuste.
    //así solo se podran procesar aquellos activos que no se encuentren en cartera.
    public List<String> removeOperationalTickets(String token, String pais ,List<String> ticketsList) throws InterruptedException {
        int intentos = 3;
        while (intentos > 0) {
            try {

                Portafolio portafolio = callsApiIOL.getPortafolioByPais(token,pais);

                if(portafolio == null){
                    break;
                }
                List<String> fin = new ArrayList<>();
                if(portafolio.getActivos().size() > 0){
                    for (String ticket:ticketsList) {
                        boolean flag = false;
                        for(int i = 0; i<portafolio.getActivos().size();i++) {
                            if(ticket.equalsIgnoreCase(portafolio.getActivos().get(i).getTitulo().getSimbolo())){
                               flag = true;
                               break;
                            }
                        }
                        if (!flag){
                            fin.add(ticket);
                        }
                    }
                    return fin;
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
                        System.out.println("El activo: " +activo.getTitulo().getSimbolo().toUpperCase()+" devolvio null al consultar getDetailCotization()");
                        return false;
                    }


                    //------------------------
                    //Si las EMAs estan dando venta pero el precio esta por encima de las emas mas cortas, no las vende. => considerando un posible rebote del mercado
                    if(emas.get(0).compareTo(new BigDecimal(cotizacion.getUltimoPrecio()))<0
                    && emas.get(1).compareTo(new BigDecimal(cotizacion.getUltimoPrecio()))<0){
                        System.out.println("El tiket: " +activo.getTitulo().getSimbolo()+" se ha procesado adecuadamente. Indica venta pero el precio esta por encima de las EMAs 3 y 9 -> posible rebote");
                        return false;
                    }
                    //-----------------------


                    //-------------Venta---------------

                    Double cantidad = activo.getCantidad();
                    Double precioPuntaCompra = cotizacion.getPuntas().get(0).getPrecioCompra(); //precio obtenido en operaciones anteriores(segun precio obtenido de las puntas - CotizacionDetalleMobile- )

                    PurcheaseResponse respon = callsApiIOL.postSellAsset(token,activo.getTitulo().getSimbolo().toUpperCase(),cantidad,precioPuntaCompra);

                    if (respon != null && respon.getNumeroOperacion() != null && respon.getNumeroOperacion()>0){
                        System.out.println("El tiket: " +activo.getTitulo().getSimbolo()+" se ha procesado adecuadamente y se realizo la VENTA de este instrumento"+"\n"+
                                "El numero de operacion es: " + respon.getNumeroOperacion() + "\n"+
                                "La cantidad operada fue de: "+activo.getCantidad()+" unidades");
                        return true;
                    }
                    else {
                        System.out.println("El tiket: " +activo.getTitulo().getSimbolo()+" NO se ha procesado adecuadamente y NO realizo la VENTA de este instrumento");
                        if (respon!= null && respon.getTitle() != null && respon.getDescription()!= null){
                            System.out.println("Title: " + respon.getTitle()+"\n"+
                            "Description: " + respon.getDescription());
                        }
                        return false;
                    }
                }
                catch (Exception e){
                    intentos --;
                }
            }
            System.out.println("El tiket: " +activo.getTitulo().getSimbolo()+" NO se ha procesado adecuadamente y NO se realizo la VENTA. *Problemas con el servidor");
            return false;
        }
        else {
            System.out.println("El tiket: " +activo.getTitulo().getSimbolo()+" se ha procesado adecuadamente y NO se realizo la VENTA de este instrumento");
            return false;
        }
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

                    double valor6PorcientoCartera = estadoCuenta.getCuentas().get(0).getTotal();
                    valor6PorcientoCartera = valor6PorcientoCartera*0.06;

                    if(cotizacion == null){
                        System.out.println("Intento nro: "+(4-intentos)+ "con contizacion nula");
                        break;
                    }

                    //que al menos se púeda comprar 1
                    if(valor6PorcientoCartera < cotizacion.getPuntas().get(0).getPrecioVenta()){
                        System.out.println("el 6% de la cartera no es suficiente para operar al menos 1 unidad del tiket: " +ticket);
                        return false;
                    }

                    //los fondos "disponibles" no poseen el 6% sobre el total de la cartera para operar este activo
                    //----------------------------
                    //if(valor6PorcientoCartera > estadoCuenta.getCuentas().get(0).getDisponible()){
                    if(valor6PorcientoCartera > estadoCuenta.getCuentas().get(0).getSaldos().get(2).getDisponibleOperar()){
                        //if(estadoCuenta.getCuentas().get(0).getDisponible()>cotizacion.getPuntas().get(0).getPrecioVenta()){
                        if(estadoCuenta.getCuentas().get(0).getSaldos().get(2).getDisponibleOperar()>cotizacion.getPuntas().get(0).getPrecioVenta()){//si los fondos disponibles son menor al 6% pero puedo comprar al menos una unidad del prodcuto (y evitar el cash. lo cual es necesario en escenarios de volatilidad en el par usd/ars)
                            //valor6PorcientoCartera = estadoCuenta.getCuentas().get(0).getDisponible();
                            valor6PorcientoCartera = estadoCuenta.getCuentas().get(0).getSaldos().get(2).getDisponibleOperar();//valor6PorcientoCartera deja a un lado el valor real que el mismo nombre indica para tener un numero menor.. sera el restante de la cartera "no operable"
                        }
                        else {
                            System.out.println("Los fondos 'Disponibles' no cubren el 6% del capital total para operar este activo: " +ticket+" ni la compra minima de 1 unidad");
                            return false;
                        }
                    }
                    //----------------------------
                    double operacion = valor6PorcientoCartera/cotizacion.getPuntas().get(0).getPrecioVenta();

                    //si puedo comprar 2,653 siempre redondeo hacia abajo
                    int operacionFinal = (int) Math.floor(operacion);

                    if (operacionFinal>= 1){
                        //que la cantidad en la punta de venta sea igual o mayor a la cantidad de mi intencion de compra
                        if(cotizacion.getPuntas().get(0).getCantidadVenta() >= operacionFinal){

                            PurcheaseResponse response1 = callsApiIOL.postBuyAsset(token,ticket,operacionFinal,cotizacion.getPuntas().get(0).getPrecioVenta());


                            if (response1 != null && response1.getNumeroOperacion() != null && response1.getNumeroOperacion()>0){
                                System.out.println("El tiket: " +ticket+" se ha procesado adecuadamente y se realizo la COMPRA de "+operacionFinal+" unidades de este instrumento"+"\n"+
                                        "El numero de operacion es: " + response1.getNumeroOperacion());
                                return true;
                            }
                            else {
                                System.out.println("El tiket: " +ticket+" NO se ha procesado adecuadamente y NO se realizo la COMPRA de este instrumento");
                                if (response1!= null && response1.getTitle() != null && response1.getDescription()!= null){
                                    System.out.println("Title: " + response1.getTitle()+"\n"+
                                            "Description: " + response1.getDescription());
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
                    //Thread.sleep(1000); // Esperar 1 segundos antes de reintentar
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


        LocalDateTime fechaAyer = LocalDateTime.now().minusDays(1);
        LocalDateTime fechaAntesDeAyer = LocalDateTime.now();

        //si ayer es feriado restar 1 dia, luego corroborar que no quede en fin de semana, si queda finde restar otro dia
        while (isItFeriado(fechaAyer) || isItMondaySundayOrSaturday(fechaAyer).equals("SUNDAY")){
            fechaAyer = fechaAyer.minusDays(1);
            if (isItMondaySundayOrSaturday(fechaAyer).equals("SUNDAY")){
                fechaAyer = fechaAyer.minusDays(2);
            } else if (isItMondaySundayOrSaturday(fechaAyer).equals("SATURDAY")) {
                fechaAyer = fechaAyer.minusDays(1);
            }
        }
        //si la fecha de ayer cae lunes, la fecha a buscar para antesDeAyer debe ser viernes
        if (isItMondaySundayOrSaturday(fechaAyer).equals("MONDAY")){
            fechaAntesDeAyer = fechaAyer.minusDays(3);
            //si viernes es feriado restar 1 dia, luego corroborar que no quede en fin de semana, si queda finde restar otro dia
            while (isItFeriado(fechaAntesDeAyer)){
                fechaAntesDeAyer = fechaAntesDeAyer.minusDays(1);
                if (isItMondaySundayOrSaturday(fechaAntesDeAyer).equals("SUNDAY")){
                    fechaAntesDeAyer = fechaAntesDeAyer.minusDays(2);
                } else if (isItMondaySundayOrSaturday(fechaAntesDeAyer).equals("SATURDAY")) {
                    fechaAntesDeAyer = fechaAntesDeAyer.minusDays(1);
                }
            }
        }
        else {
            //si fecha de ayer no cae lunes, entonces fecha antesDeAyer es: fechaAyer -1. salvo
            //que al hacer esta cuenta caiga en un feriado
            fechaAntesDeAyer = fechaAyer.minusDays(1);
            while (isItFeriado(fechaAntesDeAyer)){
                fechaAntesDeAyer = fechaAntesDeAyer.minusDays(1);
                if (isItMondaySundayOrSaturday(fechaAntesDeAyer).equals("SUNDAY")){
                    fechaAntesDeAyer = fechaAntesDeAyer.minusDays(2);
                } else if (isItMondaySundayOrSaturday(fechaAntesDeAyer).equals("SATURDAY")) {
                    fechaAntesDeAyer = fechaAntesDeAyer.minusDays(1);
                }
            }
        }

        fechaAyer = fechaAyer.withHour(16).withMinute(0);  //los horarios de mercado son de las 11 a 17hs, necesito obtener la ultima cotizacion del dia
        fechaAntesDeAyer = fechaAntesDeAyer.withHour(16).withMinute(0);


        Cotizacion cotizacionFechaAyer = new Cotizacion();
        Cotizacion cotizacionFechaAntesDeAyer = new Cotizacion();

        for (int j = 0; j < cotizaciones.size(); j++) {
            if (cotizaciones.get(j).getFechaHora().compareTo(fechaAyer.toString())>0){

                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime fechaConvertida = LocalDateTime.parse(cotizaciones.get(j).getFechaHora(), formatter);
                fechaAyer = fechaConvertida;
                cotizacionFechaAyer = cotizaciones.get(j);
            } else if (cotizaciones.get(j).getFechaHora().compareTo(fechaAntesDeAyer.withHour(10).toString())>0 &&
                    cotizaciones.get(j).getFechaHora().compareTo(fechaAntesDeAyer.withHour(17).withMinute(50).toString())<0){

                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime fechaConvertida = LocalDateTime.parse(cotizaciones.get(j).getFechaHora(), formatter);
                fechaAntesDeAyer = fechaConvertida;
                cotizacionFechaAntesDeAyer = cotizaciones.get(j);
            }
        }
        //hasta acá encuentro los dos dias anteriores de cotizacion, tienen muchas cotizaciones(intradiarias) y solo quiero las del cierre

        cotizacionNormalized.add(cotizacionFechaAyer);
        cotizacionNormalized.add(cotizacionFechaAntesDeAyer);

        //ahora, elimino todas esas fechas, solo me voy a quedar con las que seleccione en el paso anterior
        LocalDateTime eliminarRango = fechaAntesDeAyer.withHour(0);
        for (int k = 0; k < cotizaciones.size(); k++) {
            if (cotizaciones.get(k).getFechaHora().compareTo(eliminarRango.toString())>0)
            {
                continue;
            }
            else {
                cotizacionNormalized.add(cotizaciones.get(k));
            }
        }

        return cotizacionNormalized;
    }

    //Fecha de feriados en Argentina
    //esta lista debe actualizarse cada año
    public boolean isItFeriado (LocalDateTime fecha){
        List<LocalDateTime> localDateTimes = new ArrayList<>();

        LocalDate anoNuevo = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate carnaval1 = LocalDate.of(2024, Month.FEBRUARY, 12);
        LocalDate carnaval2 = LocalDate.of(2024, Month.FEBRUARY, 13);
        LocalDate diaMemoria = LocalDate.of(2024, Month.MARCH, 24);
        LocalDate viernesSanto = LocalDate.of(2024, Month.MARCH, 29);
        LocalDate feriadoTuristico1 = LocalDate.of(2024, Month.APRIL, 1);
        LocalDate diaMalvinas = LocalDate.of(2024, Month.APRIL, 2);
        LocalDate diaTrabajador = LocalDate.of(2024, Month.MAY, 1);
        LocalDate revolucionMayo = LocalDate.of(2024, Month.MAY, 25);
        LocalDate belgrano = LocalDate.of(2024, Month.JUNE, 20);
        LocalDate feriadoTuristico2 = LocalDate.of(2024, Month.JUNE, 21);
        LocalDate diaIndependencia = LocalDate.of(2024, Month.JULY, 9);
        LocalDate feriadoTuristico3 = LocalDate.of(2024, Month.OCTOBER, 11);
        LocalDate inmaculadaConcepcion = LocalDate.of(2024, Month.DECEMBER, 8);
        LocalDate navidad = LocalDate.of(2024, Month.DECEMBER, 25);
        LocalDate gueemes = LocalDate.of(2024, Month.JUNE, 17);
        LocalDate sanMartin = LocalDate.of(2024, Month.AUGUST, 17);
        LocalDate diversidadCultural = LocalDate.of(2024, Month.OCTOBER, 12);
        LocalDate soberaniaNacional = LocalDate.of(2024, Month.NOVEMBER, 18);

        localDateTimes.add(anoNuevo.atStartOfDay());
        localDateTimes.add(carnaval1.atStartOfDay());
        localDateTimes.add(carnaval2.atStartOfDay());
        localDateTimes.add(diaMemoria.atStartOfDay());
        localDateTimes.add(viernesSanto.atStartOfDay());
        localDateTimes.add(feriadoTuristico1.atStartOfDay());
        localDateTimes.add(diaMalvinas.atStartOfDay());
        localDateTimes.add(diaTrabajador.atStartOfDay());
        localDateTimes.add(revolucionMayo.atStartOfDay());
        localDateTimes.add(belgrano.atStartOfDay());
        localDateTimes.add(feriadoTuristico2.atStartOfDay());
        localDateTimes.add(diaIndependencia.atStartOfDay());
        localDateTimes.add(feriadoTuristico3.atStartOfDay());
        localDateTimes.add(inmaculadaConcepcion.atStartOfDay());
        localDateTimes.add(navidad.atStartOfDay());
        localDateTimes.add(gueemes.atStartOfDay());
        localDateTimes.add(sanMartin.atStartOfDay());
        localDateTimes.add(diversidadCultural.atStartOfDay());
        localDateTimes.add(soberaniaNacional.atStartOfDay());

        fecha = fecha.withHour(1).withMinute(0).withSecond(0).withNano(0);
        for (LocalDateTime localDateTime: localDateTimes) {
            localDateTime = localDateTime.withHour(1).withMinute(0).withSecond(0).withNano(0);
            if (localDateTime.equals(fecha)){
                return true;
            }
        }
        return false;
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
