package developBot.MervalOperations.services.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import developBot.MervalOperations.models.dto.ClientJwtUtilDTO;
import developBot.MervalOperations.services.ClientJwtUtilService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClientJwtServiceImpl implements ClientJwtUtilService {

    private String accesToken;
    private String refreshToken;
    private Long expires_in;
    private String expires;

    @Override
    public ClientJwtUtilDTO getToken() {
        int intentos = 3;
        while (intentos>0){
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                String requestBody = requestBody();
                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.postForEntity("https://api.invertironline.com/token", request, String.class);
                ClientJwtUtilDTO resp = extractToken(response.getBody());

                return  resp;
            }catch (Error e){
                intentos--;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ClientJwtUtilDTO extractToken(String responseBody) {
        JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);

        expires_in = jsonObject.get("expires_in").getAsLong();
        refreshToken = jsonObject.get("refresh_token").getAsString();
        accesToken = jsonObject.get("access_token").getAsString();
        expires = jsonObject.get(".expires").getAsString();
        if(accesToken != null){
            ClientJwtUtilDTO clientJwtUtilDTO = new ClientJwtUtilDTO(accesToken,refreshToken,expires_in,expires);
            return clientJwtUtilDTO;
        }
        else {
            throw new ErrorResponseException(HttpStatusCode.valueOf(500));
        }

    }

    private String requestBody() {
        return "username=juan_ce@live.com.ar&password=xK2Mr#CJWYFxzZ.&grant_type=password";
    }

    @Override
    public ClientJwtUtilDTO refToken(String refreshToken) {
        // configurar los datos
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>(refreshToken, headers);
        // realizar la solicitud
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity("https://api.invertironline.com/token", request, String.class);
        ClientJwtUtilDTO resp = extractToken(response.getBody());
        return resp;
    }
}
