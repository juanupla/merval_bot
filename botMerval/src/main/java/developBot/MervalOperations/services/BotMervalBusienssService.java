package developBot.MervalOperations.services;

import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModel.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.clientModel.titulos.cotizacion.Cotizacion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface BotMervalBusienssService{
    List<String> removeOperationalTickets(String token, String pais , List<String> ticketsList);
    List<Posicion> operationalTickets(String token, String pais);
    List<Operacion> removePendingOrders(String token);
    boolean saleOperation(String token, List<BigDecimal> emas, Posicion activo);
    boolean purchaseOperation(String token, String ticket, List<BigDecimal> emas);
    boolean EMAsPurchaseOperation(String token, String ticket, List<BigDecimal> emas);
    boolean EMAsSaleOperation(List<BigDecimal> emas);
    List<BigDecimal> calculoEMAs(String token, String simbolo);
    List<Cotizacion> normalizeCotization(List<Cotizacion> cotizaciones);
    boolean isItFeriado (LocalDateTime fecha);
    List<BigDecimal> getEMAs(List<Cotizacion> cotizaciones);
    BigDecimal getEma(Integer emaNro, BigDecimal beta, BigDecimal ema_1,List<Cotizacion> cotizacions);
    String isItMondaySundayOrSaturday(LocalDateTime localDate);
    BigDecimal getEma_1(Integer emaNro, List<Cotizacion> cotizaciones);
    BigDecimal getBeta(Integer emaNro);

}
