package developBot.MervalOperations.authentication;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;
@Data
public class JwtUtil {

    private String accesToken;
    private String refreshToken;
    private Long expires_in;
    private String expires;

    public JwtUtil() {
        getToken();
    }

    public String getToken() {
    // configurar los datos
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    // configurar los parametros
    String requestBody = requestBody();
    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    // realizar la solicitud
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.postForEntity("https://api.invertironline.com/token", request, String.class);
    String resp = extractToken(response.getBody());
    return  resp;
}

    private String extractToken(String responseBody) {

        JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);

        expires_in = jsonObject.get("expires_in").getAsLong();
        refreshToken = jsonObject.get("refresh_token").getAsString();
        accesToken = jsonObject.get("access_token").getAsString();
        expires = jsonObject.get(".expires").getAsString();
        return accesToken;
    }

    private String requestBody(){
        return "username=";
    }

    private String requestBodyRefresh(){
        return "refresh_token="+refreshToken+"&grant_type=refresh_token";
    }

    public String refToken() {
        // configurar los datos
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // configurar los parametros
        String requestBody = requestBodyRefresh();
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        // realizar la solicitud
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity("https://api.invertironline.com/token", request, String.class);
        String resp = extractToken(response.getBody());
        return resp;
    }

}
