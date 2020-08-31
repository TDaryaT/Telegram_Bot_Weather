import Commands.HelpCommand;
import Commands.StartCommand;
import Commands.StopCommand;
import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.TgBasePostgresql;
import utils.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        HelpCommand helpCommand = new HelpCommand(this);
        register(helpCommand);
        LOGGER.info("Registering '/stop'...");
        register(new StopCommand());

        // ответ на незарегистрированную команду
        registerDefaultAction((absSender, message) -> {
            LOGGER.info("Registering unknown command from @" + message.getFrom().getUserName()
                    + " : " + message.getText() + "...");

            SendMessage commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(message.getChatId());
            commandUnknownMessage.setText("The command '" +
                    message.getText() + "' is not known by this bot. Here comes some help ");
            try {
                absSender.execute(commandUnknownMessage);
            } catch (TelegramApiException e) {
                LOGGER.error("Error execute in custom unregistered command " + e.getMessage(), e);
            }
            helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[]{});
        });
    }

    /**
     * Ответ на полученный update (сообщение не начинающееся с / или из клавиатуры)
     *
     * @param update - полученное обновление
     */
    @Override
    public void processNonCommandUpdate(Update update) {
        LOGGER.info("processNonCommandUpdate...");
        SendMessage message = new SendMessage();

        //we get test message
        if (update.hasMessage() && update.getMessage().hasText()) {
            LOGGER.info("Executing non-custom update from @" +
                    update.getMessage().getFrom().getUserName() +
                    " : " + update.getMessage().getText() + "...");

            message.setChatId(update.getMessage().getChatId())
                    .setText("You said: " + update.getMessage().getText() +
                            ", I don't know what to do with this" +
                            EmojiParser.parseToUnicode(":cry: \n") +
                            "maybe you need /help");
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                LOGGER.error("Error execute in non-custom command " + e.getMessage(), e);
            }
            //we get location
        } else if (update.getMessage().hasLocation()) {
            Location location = update.getMessage().getLocation();
            LOGGER.info("Get location from @" + update.getMessage().getFrom().getUserName() + ' ' + location);

            message.setChatId(update.getMessage().getChatId());
            String lat = String.format(Locale.US, "%.2f", location.getLatitude());
            String lon = String.format(Locale.US, "%.2f", location.getLongitude());

            Weather weather = new Weather(lat, lon);
            String[] parseWeather = weather.getWeatherNow();

            //set new weather in base
            TgBasePostgresql base = new TgBasePostgresql();
            if (!base.isWeather(parseWeather)) {
                base.addWeather(parseWeather);
            } else if (!base.isUserCity(parseWeather[0])){
                //TODO: лучше после обновления
                base.addUserCity(parseWeather[0]);
            }
            message.setText(weather.toWrap(parseWeather))
                    .setReplyMarkup(getKeyboard());

            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                LOGGER.error("Error execute in non-custom command " + e.getMessage(), e);
            }
            //save city quick
        } else if (update.hasCallbackQuery()) {
                String call_data = update.getCallbackQuery().getData();
                if (call_data.equals("save_city")) {
                    //TODO: мониторинг погоды
                }
            //we get someone else
        } else {
            LOGGER.info("Executing non-custom update from @" +
                    update.getMessage().getFrom().getUserName() + "without text!");

            message.setChatId(update.getMessage().getChatId())
                    .setText("I don't know what to do with this" +
                            EmojiParser.parseToUnicode(":cry: \n") +
                            "maybe you need /help");
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                LOGGER.error("Error execute in non-custom command " + e.getMessage(), e);
            }
        }
    }

    public static InlineKeyboardMarkup getKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("save this city?").setCallbackData("save_city"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
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
