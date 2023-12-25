package developBot.MervalOperations.service;

import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BotMervalService {

    List<String> removeOperationalTickets(String token, String pais ,List<String> ticketsList);

    List<Operacion> removePendingOrders(String token);
    boolean calculoEMAs(String token, String simbolo);
}
