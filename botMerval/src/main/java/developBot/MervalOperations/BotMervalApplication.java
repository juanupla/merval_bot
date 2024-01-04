package developBot.MervalOperations;

import developBot.MervalOperations.busienss.BotMerval;
import developBot.MervalOperations.busienss.BotMervalBusiness;
import developBot.MervalOperations.busienss.BotMerval_testVisual;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotMervalApplication {
	public static void main(String[] args) throws InterruptedException {

		//alterno entre el bot real y en el que visualizo el comportamiento de algunos metodos, segun que necesite

		BotMerval bot = new BotMerval();
		//BotMerval_testVisual bot = new BotMerval_testVisual();
		bot.ejecution();
	}
}
