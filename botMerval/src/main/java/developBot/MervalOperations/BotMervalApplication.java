package developBot.MervalOperations;

import developBot.MervalOperations.bot.BotMerval;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@SpringBootApplication
public class BotMervalApplication {
	public static void main(String[] args) throws InterruptedException {

		BotMerval bot = new BotMerval();
		bot.ejecution();

	}
}
