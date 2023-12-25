package developBot.MervalOperations.feignClients;

import developBot.MervalOperations.models.clientModels.responseModel.Response;
import developBot.MervalOperations.models.clientModels.operar.Comprar;
import developBot.MervalOperations.models.clientModels.operar.ComprarEspecieD;
import developBot.MervalOperations.models.clientModels.operar.Vender;
import developBot.MervalOperations.models.clientModels.operar.VenderEspecieD;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "https://api.invertironline.com")
public interface OperarClient {

    @PostMapping("/api/v2/operar/Comprar")
    Response postCompra(@RequestBody Comprar comprar, @RequestHeader("Authorization") String token);
    @PostMapping("/api/v2/operar/ComprarEspecieD")
    Response postCompraEspecieD(@RequestBody ComprarEspecieD comprarEspecieD, @RequestHeader("Authorization") String token);
    @PostMapping("/api/v2/operar/ComprarEspecieD")
    Response postVenta(@RequestBody Vender vender, @RequestHeader("Authorization") String token);
    @PostMapping("/api/v2/operar/ComprarEspecieD")
    Response postVentaEspecieD(@RequestBody VenderEspecieD venderEspecieD, @RequestHeader("Authorization") String token);
}
