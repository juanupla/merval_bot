package developBot.MervalOperations.feignClients;

import developBot.MervalOperations.models.clientModel.responseModel.Response;
import developBot.MervalOperations.models.clientModel.miCuenta.estadoCuenta.EstadoCuenta;
import developBot.MervalOperations.models.clientModel.miCuenta.operacioneByNumero.OperacionDetalleModel;
import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(url = "https://api.invertironline.com")
public interface MiCuentaClient {
    @GetMapping("/api/v2/operaciones")
    List<Operacion> getOperations(@RequestParam Integer numero,
                                  @RequestParam String estado,//pueden ser: pendientes,terminadas,canceladas
                                  @RequestParam LocalDateTime fechaDesde,
                                  @RequestParam LocalDateTime fechaHasta,
                                  @RequestParam String pais,//puede ser: argentina, estados_Unidos
                                  @RequestHeader("Authorization") String token//AL ENVIAR EL TOKEN DEBE IR EL PREFIJO Bearer + un espacio y el token
                                  );

    @GetMapping("/api/v2/operaciones/{numero}")
    OperacionDetalleModel getOperationsByNumber(@PathVariable Integer number, @RequestHeader("Authorization") String token);

    @GetMapping("/api/v2/estadocuenta")
    EstadoCuenta getWalletStatus(@RequestHeader("Authorization") String token);

    @DeleteMapping("/api/v2/operaciones/{numero}")
    Response deleteOpera(@PathVariable Integer numero, @RequestHeader("Authorization") String token);

}
