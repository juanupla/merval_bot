package developBot.MervalOperations.service;

import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.List;

@Service
public interface BotMervalService {

    List<String> removeOperationalTickets(String token, String pais ,List<String> ticketsList) throws InterruptedException;

    List<Operacion> removePendingOrders(String token);
    List<BigDecimal> calculoEMAs(String token, String simbolo) throws InterruptedException;

    boolean EMAsSaleOperation(List<BigDecimal> emas) throws InterruptedException;
    boolean EMAsPurchaseOperation(String token, String ticket, List<BigDecimal> emas) throws InterruptedException;
    boolean saleOperation(String token, String tiket);
    boolean purchaseOperation(String token, String ticket, List<BigDecimal> emas) throws InterruptedException;
}
