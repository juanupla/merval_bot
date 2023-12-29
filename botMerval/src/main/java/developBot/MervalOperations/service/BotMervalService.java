package developBot.MervalOperations.service;

import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public interface BotMervalService {

    List<String> removeOperationalTickets(String token, String pais ,List<String> ticketsList) throws InterruptedException;
    List<Posicion> operationalTickets(String token, String pais);

    List<Operacion> removePendingOrders(String token);
    List<BigDecimal> calculoEMAs(String token, String simbolo) throws InterruptedException;


    //se debe eliminar el metodo de abajo, solo lo puse para probarlo. forma parte de otro metodo que se muestra acá
    boolean EMAsSaleOperation(List<BigDecimal> emas) throws InterruptedException;
    //se debe eliminar el metodo de abajo, solo lo puse para probarlo. forma parte de otro metodo que se muestra acá
    boolean EMAsPurchaseOperation(String token, String ticket, List<BigDecimal> emas) throws InterruptedException;



    boolean saleOperation(String token, List<BigDecimal> emas,Posicion activo) throws InterruptedException;
    boolean purchaseOperation(String token, String ticket, List<BigDecimal> emas) throws InterruptedException;
}
