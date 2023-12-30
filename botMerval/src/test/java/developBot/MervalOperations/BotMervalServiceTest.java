package developBot.MervalOperations;


import developBot.MervalOperations.busienss.CallsApiIOL;
import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.Cuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Titulo;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.Punta;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import developBot.MervalOperations.service.BotMervalService;
import developBot.MervalOperations.busienss.BotMervalServiceImpl;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;


public class BotMervalServiceTest {

    private CallsApiIOL callsApiIOLMock;
    private BotMervalServiceImpl botMervalService;

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

        BotMervalServiceImpl botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

        List<String> originalList = new ArrayList<>();
        originalList.add("AAPL");
        originalList.add("GOOGL");
        originalList.add("AMZN");
        originalList.add("META");

        List<String> resultado = botMervalService.removeOperationalTickets("token","argentina",originalList);

        Assertions.assertEquals(Arrays.asList("AMZN","META"),resultado);
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

        BotMervalServiceImpl botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

        List<Posicion> list = botMervalService.operationalTickets(any(),any());

        Assertions.assertEquals(Arrays.asList(p1,p2,p3,p4),list);
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

        BotMervalServiceImpl botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

        List<Operacion> operacions1 = botMervalService.removePendingOrders("token");

        Assertions.assertEquals(Arrays.asList(operacions),operacions1);
    }

    @Test
    public void EMAsSaleOperationTest(){
        BotMervalServiceImpl botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

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

        BotMervalServiceImpl botMervalService = new BotMervalServiceImpl(callsApiIOLMock);
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
    public void saleOperationTest() throws InterruptedException {

        CotizacionDetalleMobile cotizacionDetalleMobile = new CotizacionDetalleMobile();

        Punta punta = new Punta();
        Punta punta2 = new Punta();
        punta2.setPrecioCompra(100.0);
        punta.setPrecioCompra(150.0);

        //Las puntas que vienen de api siempre es de mayor a menor. es decir que en la list.get(0) esta la mas competitiva del momento
        Assertions.assertEquals(Arrays.asList(punta,punta2).get(0).getPrecioCompra(),150.0);

        cotizacionDetalleMobile.setPuntas(Arrays.asList(punta,punta2));
        when(callsApiIOLMock.getDetailCotization("token","GGAL")).thenReturn(cotizacionDetalleMobile);

        Response response = new Response();
        response.setOk(true);
        when(callsApiIOLMock.postSellAsset(any(),any(),any(),any())).thenReturn(response);

        BotMervalServiceImpl botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

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
        Assertions.assertEquals(Arrays.asList(punta,punta2).get(0).getPrecioVenta(),300);

        cotizacionDetalleMobile.setPuntas(Arrays.asList(punta,punta2));
        cotizacionDetalleMobile.setUltimoPrecio(400.0);
        when(callsApiIOLMock.getDetailCotization("token","GGAL")).thenReturn(cotizacionDetalleMobile);

        EstadoCuenta estadoCuenta = new EstadoCuenta();
        Cuenta cuenta = new Cuenta();
        cuenta.setTotal(100000.0);
        cuenta.setDisponible(78500.0);
        //en el codigo siempre pide la cuenta.get(0) por eso solo agrego una.
        estadoCuenta.setCuentas(List.of(cuenta));

        when(callsApiIOLMock.getAccountStatus(any())).thenReturn(estadoCuenta);

        Response response = new Response();
        response.setOk(true);
        when(callsApiIOLMock.postBuyAsset(any(),any(),any(),any())).thenReturn(response);

        BotMervalServiceImpl botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

        List<BigDecimal> lis = new ArrayList<>();
        //siguiendo la logica del metodo:
        lis.add(new BigDecimal("250"));//Este va a ser EMA3
        lis.add(new BigDecimal("200"));//Este va a ser EMA9
        lis.add(new BigDecimal("150"));//Este va a ser EMA21
        lis.add(new BigDecimal("100"));//Este va a ser EMA50
        //si cada ema es mayor a la que le sigue y el brecio es mayor a ema3 deberia ser true

        boolean fin = botMervalService.purchaseOperation("token","ggal",lis);

        Assertions.assertTrue(fin);
    }

    @Test
    public void normalizeCotizationTest(){

        //este test debe traer cotizaciones de cada dia
        //hay que tener en cuenta que la API IOL devuelve muchas cotizaciones de los 2 dias ahbiles anteriores.
        //siempre buscaremos 1 sola cotizacion del dia y en particular estas dos se busca la cotizacion mas
        //cercana al cierre de mercado (17hs)

        Cotizacion cotizacion = new Cotizacion();
        Cotizacion cotizacion1 = new Cotizacion();
        Cotizacion cotizacion2 = new Cotizacion();
        Cotizacion cotizacion3 = new Cotizacion();
        Cotizacion cotizacion4 = new Cotizacion();
        Cotizacion cotizacion5 = new Cotizacion();
        Cotizacion cotizacion6 = new Cotizacion();
        Cotizacion cotizacion7 = new Cotizacion();
        Cotizacion cotizacion8 = new Cotizacion();

        cotizacion.setFechaHora(LocalDateTime.now().minusDays(1).withHour(13).withMinute(10).withSecond(10).toString());
        cotizacion1.setFechaHora(LocalDateTime.now().minusDays(1).withHour(13).withMinute(15).withSecond(10).toString());
        cotizacion2.setFechaHora(LocalDateTime.now().minusDays(1).withHour(13).withMinute(25).withSecond(10).toString());
        cotizacion3.setFechaHora(LocalDateTime.now().minusDays(2).withHour(14).toString());
        cotizacion4.setFechaHora(LocalDateTime.now().minusDays(2).withHour(15).toString());
        cotizacion5.setFechaHora(LocalDateTime.now().minusDays(3).withHour(16).toString());
        cotizacion6.setFechaHora(LocalDateTime.now().minusDays(4).withHour(14).toString());
        cotizacion7.setFechaHora(LocalDateTime.now().minusDays(5).withHour(13).toString());
        cotizacion8.setFechaHora(LocalDateTime.now().minusDays(6).withHour(16).toString());
        List<Cotizacion> cotizacions = Arrays.asList(cotizacion,
                cotizacion1,cotizacion2,cotizacion3,cotizacion4,cotizacion5,cotizacion6,cotizacion7,cotizacion8);

        BotMervalServiceImpl botMervalService = new BotMervalServiceImpl(callsApiIOLMock);
        List<Cotizacion> cotizacionNormalized = botMervalService.normalizeCotization(cotizacions);

        LocalDateTime now = LocalDateTime.now();
        for (Cotizacion c: cotizacionNormalized) {
            if (c.getFechaHora().compareTo(now.toString())<0){
                System.out.println(c.getFechaHora());
                now = LocalDateTime.parse(c.getFechaHora());
            }else {
                Assertions.fail();
            }
        }
        Assertions.assertTrue(true);
    }


}
