package developBot.MervalOperations;

import developBot.MervalOperations.bot.BotMerval;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BotMervalApplication {
	public static void main(String[] args){

		BotMerval bot = new BotMerval();
		bot.ejecution();

	}
}
