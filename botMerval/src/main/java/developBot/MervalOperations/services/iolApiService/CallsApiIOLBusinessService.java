package developBot.MervalOperations.services.iolApiService;

import developBot.MervalOperations.models.clientModel.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModel.miCuenta.portafolio.Portafolio;
import developBot.MervalOperations.models.clientModel.operar.PurcheaseResponse;
import developBot.MervalOperations.models.clientModel.responseModel.Response;
import developBot.MervalOperations.models.clientModel.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.models.clientModel.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface CallsApiIOLBusinessService {
    Portafolio getPortafolioByPais(String token, String pais);
    Operacion[] getOperaciones(String token);
    Response deletePendingOrders(String token, Integer numeroOperacion);
    CotizacionDetalleMobile getDetailCotization(String token, String simbolo);
    PurcheaseResponse postSellAsset(String token, String simbolo, Double cantidadVenta, Double precioPuntaCompra);
    EstadoCuenta getAccountStatus(String token);
    PurcheaseResponse postBuyAsset(String token, String simbolo, Integer cantidad, Double precioPuntaVenta);
    List<Cotizacion> getCotizaciones(String token, String simbolo);
    Operacion[] getEndOfTheDayTrades(String token);
}
