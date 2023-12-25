package developBot.MervalOperations.feignClients;

import developBot.MervalOperations.models.clientModels.notificacion.Notificacion;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "https://api.invertironline.com")
public interface NotificacionClient {

    @GetMapping("/api/v2/Notificacion")
    Notificacion getNotifications(@RequestHeader("Authorization") String token);
}
