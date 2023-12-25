package developBot.MervalOperations;

import developBot.MervalOperations.authentication.JwtUtil;
import developBot.MervalOperations.bot.BotMerval;
import developBot.MervalOperations.service.MiCuentaService;
import developBot.MervalOperations.service.impl.MiCuentaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BotMervalApplication {
	@Autowired
	private MiCuentaService miCuentaService;
	public static void main(String[] args){



		JwtUtil prueba = new JwtUtil();

//		System.out.println(prueba.getAccesToken()+"\n"+prueba.getExpires_in()+"\n "+ prueba.getRefreshToken());
//		prueba.refToken();
//		System.out.println(prueba.getAccesToken()+"\n"+prueba.getExpires_in()+"\n"+ prueba.getRefreshToken());

//		BotMerval botMerval = new BotMerval();
//		String tok = prueba.getToken();
//		botMerval.pruebaWallwtStatus("Bearer "+tok);

		BotMerval bot = new BotMerval();
		bot.ejecution();



	}
}
