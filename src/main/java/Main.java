import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Initializing API context...");
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        LOGGER.info("Configuring bot options...");
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        try {
            telegramBotsApi.registerBot(new MyWeatherTgBot(botOptions));
            LOGGER.info("My weather bot is ready for work!");
        } catch (TelegramApiException e) {
            LOGGER.error("Error while initializing bot!");
        }
    }
}
