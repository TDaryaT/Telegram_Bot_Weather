import Commands.HelpCommand;
import Commands.StartCommand;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyWeatherTgBot extends TelegramLongPollingCommandBot {
    private static final String BOT_TOKEN = System.getenv("TOKEN");
    private static final String BOT_NAME = "LetMeKnowAboutWeatherBot";

    private static final Logger LOGGER = LogManager.getLogger(MyWeatherTgBot.class);

    /**
     * Конструктор и регистрация кастомных команд (вида /start)
     */
    public MyWeatherTgBot(DefaultBotOptions botOptions) {
        super(botOptions);
        LOGGER.info("Initializing My Weather bot...");

        // регистрация всех кастомных команд
        LOGGER.info("Registering commands...");
        LOGGER.info("Registering '/start'...");
        register(new StartCommand());
        LOGGER.info("Registering '/help'...");
        register(new HelpCommand(this));
    }

    /**
     * Ответ на полученный update (сообщение не начинающееся с /)
     *
     * @param update - полученное обновление
     */
    @Override
    public void processNonCommandUpdate(Update update) {
        LOGGER.info("Executing non-custom update...");
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("You said: " + update.getMessage().getText());
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                LOGGER.error("Error execute in non-custom command", e);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
