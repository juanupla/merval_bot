package developBot.MervalOperations.feignClients;

import developBot.MervalOperations.models.clientModel.titulos.cotizacionDetalle.CotizacionDetalleMobile;
import developBot.MervalOperations.models.clientModel.titulos.cotizacion.Cotizacion;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "https://api.invertironline.com")
public interface TitulosClient {

    @GetMapping("/api/v2/{mercado}/Titulos/{simbolo}/CotizacionDetalleMobile/{plazo}")
    CotizacionDetalleMobile getQuoteBySymbol(@PathVariable String mercado,//puede ser: bCBA, nYSE, nASDAQ, aMEX, bCS, rOFX
                                             @PathVariable String simbolo,//puede ser cualquier simbolo/tiket
                                             @PathVariable String plazo,//puede ser: t0, t1, t2
                                             @RequestHeader("Authorization") String token
                                             );

    @GetMapping("/api/v2/{Mercado}/Titulos/{Simbolo}/Cotizacion")
    Cotizacion getQuote(
            @PathVariable String mercado,//puede ser: bCBA, nYSE, nASDAQ, aMEX, bCS, rOFX
            @PathVariable String simbolo,//puede ser cualquier simbolo/tiket
            @RequestHeader("Authorization") String token);
}
