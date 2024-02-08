package developBot.MervalOperations;


import developBot.MervalOperations.busienss.CallsApiIOL;
import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.Cuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.Saldo;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Titulo;
import developBot.MervalOperations.models.clientModels.operar.PurcheaseResponse;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.Punta;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import developBot.MervalOperations.busienss.BotMervalBusiness;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class BotMervalServiceTest {

    private CallsApiIOL callsApiIOLMock;
    private BotMervalBusiness botMervalService;

    @BeforeEach
    public void beforEach(){
        callsApiIOLMock = mock(CallsApiIOL.class);
    }


    @Test
    public void removeOperationalTicketsTest() throws InterruptedException {

        Titulo t1 = new Titulo();
        Titulo t2 = new Titulo();

        t1.setSimbolo("AAPL");
        t2.setSimbolo("GOOGL");

        Posicion p1 = new Posicion();
        Posicion p2 = new Posicion();
        p1.setTitulo(t1);
        p2.setTitulo(t2);

        Portafolio portafolio = new Portafolio("argentina",Arrays.asList(p1,p2));
        when(callsApiIOLMock.getPortafolioByPais(any(),any())).thenReturn(portafolio);

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);

        List<String> originalList = new ArrayList<>();
        originalList.add("AAPL");
        originalList.add("GOOGL");
        originalList.add("AMZN");
        originalList.add("META");

        List<String> resultado = botMervalService.removeOperationalTickets("token","argentina",originalList);

        assertEquals(Arrays.asList("AMZN","META"),resultado);
    }

    @Test
    public void operationalTicketsTest(){
        Titulo t1 = new Titulo();
        Titulo t2 = new Titulo();
        Titulo t3 = new Titulo();
        Titulo t4 = new Titulo();

        t1.setSimbolo("AAPL");
        t2.setSimbolo("GOOGL");
        t3.setSimbolo("AMZN");
        t4.setSimbolo("META");

        Posicion p1 = new Posicion();
        Posicion p2 = new Posicion();
        Posicion p3 = new Posicion();
        Posicion p4 = new Posicion();

        p1.setTitulo(t1);
        p2.setTitulo(t2);
        p3.setTitulo(t3);
        p4.setTitulo(t4);

        Portafolio portafolio = new Portafolio("argentina",Arrays.asList(p1,p2,p3,p4));
        when(callsApiIOLMock.getPortafolioByPais(any(),any())).thenReturn(portafolio);

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);

        List<Posicion> list = botMervalService.operationalTickets(any(),any());

        assertEquals(Arrays.asList(p1,p2,p3,p4),list);
    }

    @Test
    public void removePendingOrdersTest(){

        Operacion operacion = new Operacion();
        Operacion operacion2 = new Operacion();
        operacion.setNumero(10);
        operacion2.setNumero(20);
        Operacion[] operacions = new Operacion[2];
        operacions[0] = operacion;
        operacions[1] = operacion2;

        when(callsApiIOLMock.getOperaciones(any())).thenReturn(operacions);

        Response response = new Response();
        response.setOk(true);

        when(callsApiIOLMock.deletePendingOrders("token",10)).thenReturn(response);
        when(callsApiIOLMock.deletePendingOrders("token",20)).thenReturn(response);

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);

        List<Operacion> operacions1 = botMervalService.removePendingOrders("token");

        assertEquals(Arrays.asList(operacions),operacions1);
    }

    @Test
    public void EMAsSaleOperationTest(){
        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);

        BigDecimal ema1 = new BigDecimal("100");
        BigDecimal ema2 = new BigDecimal("200");

        Assertions.assertTrue(botMervalService.EMAsSaleOperation(Arrays.asList(ema1,ema2)));
    }
    @Test
    public void EMAsPurchaseOperationTest() throws InterruptedException {

        BigDecimal ema3 = new BigDecimal("1243");
        BigDecimal ema9 = new BigDecimal("1189");
        BigDecimal ema21 = new BigDecimal("1100");
        BigDecimal ema50 = new BigDecimal("900");

        CotizacionDetalleMobile cotizacionDetalleMobile = new CotizacionDetalleMobile();
        cotizacionDetalleMobile.setUltimoPrecio(1450.0);
        when(callsApiIOLMock.getDetailCotization(any(),any())).thenReturn(cotizacionDetalleMobile);

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);
        boolean fin = botMervalService.EMAsPurchaseOperation("token","ggal",Arrays.asList(ema3,ema9,ema21,ema50));

        Assertions.assertTrue(fin);

        //las emas no quedarian cruzadas para dar compra y el resultado deberia ser falso
        ema3 = new BigDecimal("1100");
        fin = botMervalService.EMAsPurchaseOperation("token","ggal",Arrays.asList(ema3,ema9,ema21,ema50));

        Assertions.assertFalse(fin);

        //aca las emas estarian cruzadas correctamente pero el precio no estaria
        // encima de la ema mas corta y deberia dar falso
        ema3 = new BigDecimal("1243");
        cotizacionDetalleMobile.setUltimoPrecio(1000.0);
        when(callsApiIOLMock.getDetailCotization(any(),any())).thenReturn(cotizacionDetalleMobile);
        fin = botMervalService.EMAsPurchaseOperation("token","ggal",Arrays.asList(ema3,ema9,ema21,ema50));

        Assertions.assertFalse(fin);
    }

    @Test
    public void calculoEMAsTest() throws InterruptedException {

        List<Cotizacion> cotizacionesMock = new ArrayList<>();

        //agregamos cotizaciones para los cálculos getEMA_1() y getEma()
        for (int i = 0; i < 200; i++) {
            Cotizacion cotizacionMock = mock(Cotizacion.class);

            //cotizacionMock.getUltimoPrecio() es llamado multiples veces en getEMA_1 y en getEMA(luego de ser normalizado)
            when(cotizacionMock.getUltimoPrecio()).thenReturn(200.0);

            //cotizacionMock.getFechaHora() es llamado multiples veces en getEma, a su vez llama a normalizeCotization();
            LocalDateTime localDateTime = LocalDateTime.now().minusDays(i+1).withHour(16);
            when(cotizacionMock.getFechaHora()).thenReturn(localDateTime.toString());
            cotizacionesMock.add(cotizacionMock);
        }

        when(callsApiIOLMock.getCotizaciones(any(),any())).thenReturn(cotizacionesMock);

        botMervalService = new BotMervalBusiness(callsApiIOLMock);
        List<BigDecimal> fin = botMervalService.calculoEMAs(any(),any());

        assertEquals(4,fin.size());
    }

    @Test
    public void saleOperationTest() throws InterruptedException {

        CotizacionDetalleMobile cotizacionDetalleMobile = new CotizacionDetalleMobile();

        Punta punta = new Punta();
        Punta punta2 = new Punta();
        punta2.setPrecioCompra(100.0);
        punta.setPrecioCompra(150.0);

        //Las puntas que vienen de api siempre es de mayor a menor. es decir que en la list.get(0) esta la mas competitiva del momento
        assertEquals(Arrays.asList(punta,punta2).get(0).getPrecioCompra(),150.0);

        cotizacionDetalleMobile.setPuntas(Arrays.asList(punta,punta2));
        when(callsApiIOLMock.getDetailCotization("token","GGAL")).thenReturn(cotizacionDetalleMobile);

        PurcheaseResponse response = new PurcheaseResponse();
        response.setNumeroOperacion(432234);
        when(callsApiIOLMock.postSellAsset(any(),any(),any(),any())).thenReturn(response);

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);

        List<BigDecimal> lis = new ArrayList<>();
        //siguiendo la logica del metodo:
        lis.add(new BigDecimal("100"));//Este va a ser EMA3
        lis.add(new BigDecimal("150"));//Este va a ser EMA9
        //si EMA3 es menor a EMA9 entonces vende

        Titulo t = new Titulo();
        t.setSimbolo("GGAL");
        Posicion p = new Posicion();
        p.setTitulo(t);
        boolean fin = botMervalService.saleOperation("token",lis,p);

        Assertions.assertTrue(fin);
    }

    @Test
    public void purchaseOperationTest() throws InterruptedException {
        CotizacionDetalleMobile cotizacionDetalleMobile = new CotizacionDetalleMobile();

        Punta punta = new Punta();
        Punta punta2 = new Punta();
        punta2.setPrecioVenta(350.0);
        punta.setPrecioVenta(300.0);
        punta.setCantidadVenta(20.0);
        punta2.setCantidadVenta(20.0);

        //Las puntas que vienen de api siempre menor a mayor. es decir que en la list.get(0) esta la mas competitiva del momento
        assertEquals(Arrays.asList(punta,punta2).get(0).getPrecioVenta(),300);

        cotizacionDetalleMobile.setPuntas(Arrays.asList(punta,punta2));
        cotizacionDetalleMobile.setUltimoPrecio(400.0);
        when(callsApiIOLMock.getDetailCotization("token","GGAL")).thenReturn(cotizacionDetalleMobile);

        EstadoCuenta estadoCuenta = new EstadoCuenta();
        Cuenta cuenta = new Cuenta();
        cuenta.setTotal(100000.0);
        cuenta.setDisponible(78500.0);
        //---
        Saldo saldo0 = new Saldo();
        Saldo saldo1 = new Saldo();
        Saldo saldo2 = new Saldo();
        saldo2.setDisponibleOperar(78500.0);
        cuenta.setSaldos(Arrays.asList(saldo0,saldo1,saldo2));
        //---
        //en el codigo siempre pide la cuenta.get(0) por eso solo agrego una.
        estadoCuenta.setCuentas(List.of(cuenta));

        when(callsApiIOLMock.getAccountStatus(any())).thenReturn(estadoCuenta);

        PurcheaseResponse purcheaseResponse = new PurcheaseResponse();
        purcheaseResponse.setNumeroOperacion(3432);
        when(callsApiIOLMock.postBuyAsset(any(),any(),any(),any())).thenReturn(purcheaseResponse);

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);

        List<BigDecimal> lis = new ArrayList<>();
        //siguiendo la logica del metodo:
        lis.add(new BigDecimal("250"));//Este va a ser EMA3
        lis.add(new BigDecimal("200"));//Este va a ser EMA9
        lis.add(new BigDecimal("150"));//Este va a ser EMA21
        lis.add(new BigDecimal("100"));//Este va a ser EMA50
        //si cada ema es mayor a la que le sigue y el precio es mayor a ema3 deberia ser true

        boolean fin = botMervalService.purchaseOperation("token","ggal",lis);

        Assertions.assertTrue(fin);
    }

    @Test
    public void normalizeCotizationTest(){

        //este test debe traer cotizaciones de cada dia.
        //hay que tener en cuenta que la API IOL devuelve 'muchas' cotizaciones de los "2 dias" ahbiles anteriores.
        //siempre buscaremos 1 sola cotizacion del dia y en particular estas dos se busca la cotizacion mas
        //cercana al cierre de mercado (17hs)


        //Otro tema es que a la hora de procesar test pueden aparecer cotizaciones con fecha de los fines de semana
        //ya que las preubas utilizan LocalDateTime.now(). Esto no supone un problema ya que la API
        //y por logica del funcionamiento del sistema financiero no brindara fechas de cotizaciones tales dias.
        //a fines de probar el metodo alcanza. ya que garantiza un dia habil para diaDeAyer y para diaAntesDeAyer
        //y todas las cotizaciones por detas(que la api solo devolvera 1 cotizacion por cada día, a excepcion de los 2 dias
        // habiles anteriores ya mencionados)
        Cotizacion cotizacion = new Cotizacion();
        Cotizacion cotizacion1 = new Cotizacion();
        Cotizacion cotizacion2 = new Cotizacion();
        Cotizacion cotizacion3 = new Cotizacion();
        Cotizacion cotizacion4 = new Cotizacion();
        Cotizacion cotizacion5 = new Cotizacion();
        Cotizacion cotizacion6 = new Cotizacion();
        Cotizacion cotizacion7 = new Cotizacion();
        Cotizacion cotizacion8 = new Cotizacion();
        Cotizacion cotizacion9 = new Cotizacion();
        Cotizacion cotizacion10 = new Cotizacion();
        Cotizacion cotizacion11 = new Cotizacion();
        Cotizacion cotizacion12 = new Cotizacion();

        cotizacion.setFechaHora(LocalDateTime.now().minusDays(1).withHour(16).withMinute(10).withSecond(10).toString());
        cotizacion1.setFechaHora(LocalDateTime.now().minusDays(1).withHour(13).withMinute(15).withSecond(10).toString());
        cotizacion2.setFechaHora(LocalDateTime.now().minusDays(1).withHour(13).withMinute(25).withSecond(10).toString());
        cotizacion3.setFechaHora(LocalDateTime.now().minusDays(2).withHour(14).toString());
        cotizacion4.setFechaHora(LocalDateTime.now().minusDays(2).withHour(16).withMinute(2).toString());
        cotizacion5.setFechaHora(LocalDateTime.now().minusDays(3).withHour(16).withMinute(1).toString());
        cotizacion6.setFechaHora(LocalDateTime.now().minusDays(4).withHour(16).withMinute(32).toString());
        cotizacion7.setFechaHora(LocalDateTime.now().minusDays(5).withHour(16).withMinute(3).toString());
        cotizacion8.setFechaHora(LocalDateTime.now().minusDays(6).withHour(16).withMinute(3).toString());
        cotizacion9.setFechaHora(LocalDateTime.now().minusDays(7).withHour(16).withMinute(3).toString());
        cotizacion10.setFechaHora(LocalDateTime.now().minusDays(8).withHour(16).withMinute(3).toString());
        cotizacion11.setFechaHora(LocalDateTime.now().minusDays(9).withHour(16).withMinute(3).toString());
        cotizacion12.setFechaHora(LocalDateTime.now().minusDays(10).withHour(16).withMinute(3).toString());
        List<Cotizacion> cotizacions = Arrays.asList(cotizacion,
                cotizacion1,cotizacion2,cotizacion3,cotizacion4,cotizacion5,cotizacion6,cotizacion7,cotizacion8,
                cotizacion9,cotizacion10,cotizacion11,cotizacion12);

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);
        List<Cotizacion> cotizacionNormalized = botMervalService.normalizeCotization(cotizacions);

        LocalDateTime now = LocalDateTime.now();
        for (Cotizacion c: cotizacionNormalized) {
            if (c.getFechaHora().compareTo(now.toString())<0){
                System.out.println(c.getFechaHora());              //Descomenta si queres visualizarel resultado
                now = LocalDateTime.parse(c.getFechaHora());
            }else {
                Assertions.fail();
            }
        }
        Assertions.assertTrue(true);

        LocalDateTime ayer = LocalDateTime.parse(cotizacionNormalized.get(0).getFechaHora());
        LocalDateTime antesAyer = LocalDateTime.parse(cotizacionNormalized.get(1).getFechaHora());

        DayOfWeek ayerDia = ayer.getDayOfWeek();
        DayOfWeek antesAyerDia = antesAyer.getDayOfWeek();

        assertNotEquals("SUNDAY",ayerDia.name());
        assertNotEquals("SATURDAY",ayerDia.name());
        assertNotEquals("SUNDAY",antesAyerDia.name());
        assertNotEquals("SATURDAY",antesAyerDia.name());

    }

    @Test
    public void getEMAsTest() {

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);

        //el metodo getEMAs() utiliza varios metodos cediendo las cotizaciones por parametros
        //es escencial
        List<Cotizacion> cotizacionesMock = new ArrayList<>();

        //agregamos cotizaciones para los cálculos getEMA_1() y getEma()
        for (int i = 0; i < 200; i++) {
            Cotizacion cotizacionMock = mock(Cotizacion.class);

            //cotizacionMock.getUltimoPrecio() es llamado multiples veces en getEMA_1 y en getEMA(luego de ser normalizado)
            when(cotizacionMock.getUltimoPrecio()).thenReturn(200.0);

            //cotizacionMock.getFechaHora() es llamado multiples veces en getEma, a su vez llama a normalizeCotization();
            LocalDateTime localDateTime = LocalDateTime.now().minusDays(i+1).withHour(16);
            when(cotizacionMock.getFechaHora()).thenReturn(localDateTime.toString());
            cotizacionesMock.add(cotizacionMock);
        }

        List<BigDecimal> result = botMervalService.getEMAs(cotizacionesMock);

        assertEquals(4, result.size());
    }

    @Test
    public void getEmaTest(){

        BotMervalBusiness botMervalService = new BotMervalBusiness(callsApiIOLMock);

        //el metodo getEMAs() utiliza varios metodos cediendo las cotizaciones por parametros
        //es escencial
        List<Cotizacion> cotizacionesMock = new ArrayList<>();

        //agregamos cotizaciones para los cálculos getEMA_1() y getEma()
        for (int i = 0; i < 200; i++) {
            Cotizacion cotizacionMock = mock(Cotizacion.class);

            //cotizacionMock.getUltimoPrecio() es llamado multiples veces en getEMA_1 y en getEMA(luego de ser normalizado)
            when(cotizacionMock.getUltimoPrecio()).thenReturn(200.0);

            //cotizacionMock.getFechaHora() es llamado multiples veces en normalizeCotization();
            LocalDateTime localDateTime = LocalDateTime.now().minusDays(i+1).withHour(16);
            when(cotizacionMock.getFechaHora()).thenReturn(localDateTime.toString());
            cotizacionesMock.add(cotizacionMock);
        }
        //la beta nunca es mayor a 1
        BigDecimal result = botMervalService.getEma(3,new BigDecimal("0.5"),new BigDecimal(100),cotizacionesMock);
        assertEquals(new BigDecimal("187.5"),result.setScale(1, RoundingMode.HALF_UP));
    }

    @Test
    public void isItMondaySundayOrSaturdayTest(){
        BotMervalBusiness botMervalBusiness = new BotMervalBusiness(callsApiIOLMock);

        LocalDateTime dateTime = LocalDateTime.of(2023,12,31,10,0,0);
        String result = botMervalBusiness.isItMondaySundayOrSaturday(dateTime);

        assertEquals("SUNDAY",result);

        dateTime = LocalDateTime.of(2023,12,30,10,0,0);
        result = botMervalBusiness.isItMondaySundayOrSaturday(dateTime);
        assertEquals("SATURDAY",result);

        dateTime = LocalDateTime.of(2023,12,25,10,0,0);
        result = botMervalBusiness.isItMondaySundayOrSaturday(dateTime);
        assertEquals("MONDAY",result);
    }

    @Test
    public void getEma_1Test(){
        List<Cotizacion> cotizacionesMock = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            Cotizacion cotizacionMock = mock(Cotizacion.class);

            when(cotizacionMock.getUltimoPrecio()).thenReturn(200.0);

            LocalDateTime localDateTime = LocalDateTime.now().minusDays(i-1);
            when(cotizacionMock.getFechaHora()).thenReturn(localDateTime.toString());
            cotizacionesMock.add(cotizacionMock);
        }

        BotMervalBusiness botMervalBusiness = new BotMervalBusiness(callsApiIOLMock);
        BigDecimal fin = botMervalBusiness.getEma_1(3,cotizacionesMock);
        assertEquals(new BigDecimal("200.0"),fin.setScale(1,RoundingMode.HALF_UP));

         fin = botMervalBusiness.getEma_1(9,cotizacionesMock);
        assertEquals(new BigDecimal("200.0"),fin.setScale(1,RoundingMode.HALF_UP));

         fin = botMervalBusiness.getEma_1(21,cotizacionesMock);
        assertEquals(new BigDecimal("200.0"),fin.setScale(1,RoundingMode.HALF_UP));

         fin = botMervalBusiness.getEma_1(50,cotizacionesMock);
        assertEquals(new BigDecimal("200.0"),fin.setScale(1,RoundingMode.HALF_UP));
    }

    @Test
    public void getBetaTest(){
        botMervalService = new BotMervalBusiness(callsApiIOLMock);

        BigDecimal result =  botMervalService.getBeta(3);
        assertEquals(new BigDecimal("0.5"),result.setScale(1,RoundingMode.HALF_UP));

        result =  botMervalService.getBeta(9);
        assertEquals(new BigDecimal("0.2"),result.setScale(1,RoundingMode.HALF_UP));

        result =  botMervalService.getBeta(21);
        assertEquals(new BigDecimal("0.0909"),result.setScale(4,RoundingMode.HALF_UP));

        result =  botMervalService.getBeta(50);
        assertEquals(new BigDecimal("0.03921569"),result.setScale(8,RoundingMode.HALF_UP));
    }

    @Test
    public void isItFeriadoTest(){
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

        List<LocalDateTime> date = new ArrayList<>(Arrays.asList(anoNuevo.atStartOfDay(), carnaval1.atStartOfDay(),
                carnaval2.atStartOfDay(), diaMemoria.atStartOfDay(), viernesSanto.atStartOfDay(),
                feriadoTuristico1.atStartOfDay(), diaMalvinas.atStartOfDay(), diaTrabajador.atStartOfDay(),
                revolucionMayo.atStartOfDay(), belgrano.atStartOfDay(), feriadoTuristico2.atStartOfDay(),
                diaIndependencia.atStartOfDay(), feriadoTuristico3.atStartOfDay(), inmaculadaConcepcion.atStartOfDay(),
                navidad.atStartOfDay(), gueemes.atStartOfDay(), sanMartin.atStartOfDay(),
                diversidadCultural.atStartOfDay(), soberaniaNacional.atStartOfDay()));
        BotMervalBusiness botMervalBusiness = new BotMervalBusiness(callsApiIOLMock);
        for (LocalDateTime time:date) {
            assertTrue(botMervalBusiness.isItFeriado(time));
        }
    }

}
