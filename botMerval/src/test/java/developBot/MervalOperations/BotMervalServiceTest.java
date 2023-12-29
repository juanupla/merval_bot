package developBot.MervalOperations;


import developBot.MervalOperations.busienss.BotMerval;
import developBot.MervalOperations.busienss.CallsApiIOL;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Titulo;
import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.titulos.Punta;
import developBot.MervalOperations.models.clientModels.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import developBot.MervalOperations.service.BotMervalService;
import developBot.MervalOperations.service.impl.BotMervalServiceImpl;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.sql.Array;
import java.util.*;


import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;


public class BotMervalServiceTest {

    private CallsApiIOL callsApiIOLMock;

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

        BotMervalService botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

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

        BotMervalService botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

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

        BotMervalService botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

        List<Operacion> operacions1 = botMervalService.removePendingOrders("token");

        Assertions.assertEquals(Arrays.asList(operacions),operacions1);
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

        BotMervalService botMervalService = new BotMervalServiceImpl(callsApiIOLMock);

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

}
