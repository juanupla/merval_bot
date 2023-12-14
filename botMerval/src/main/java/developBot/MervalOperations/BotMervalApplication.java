package developBot.MervalOperations;

import developBot.MervalOperations.authentication.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class BotMervalApplication {
	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(BotMervalApplication.class, args);
		JwtUtil prueba = new JwtUtil();

		System.out.println(prueba.getAccesToken()+"\n"+prueba.getExpires_in()+"\n "+ prueba.getRefreshToken());
		prueba.refToken();
		System.out.println(prueba.getAccesToken()+"\n"+prueba.getExpires_in()+"\n"+ prueba.getRefreshToken());
	}
}
