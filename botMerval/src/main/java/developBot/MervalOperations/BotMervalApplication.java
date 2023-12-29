package developBot.MervalOperations;

import developBot.MervalOperations.busienss.BotMerval;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotMervalApplication {
	public static void main(String[] args) throws InterruptedException {

		BotMerval bot = new BotMerval();
		bot.ejecution();

	}
}
