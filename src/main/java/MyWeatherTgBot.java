import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MyWeatherTgBot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "";
    private static final String BOT_NAME = "LetMeKnowAboutWeatherBot";

    public MyWeatherTgBot(){}

    public MyWeatherTgBot(DefaultBotOptions options){
        super(options);
    }

    public void onUpdateReceived(Update update) {

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
