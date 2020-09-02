import Commands.*;
import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static Commands.ReminderCommand.getKeyboardRemind;
import static Commands.ReminderCommand.getMessageRemind;
import static Commands.WeatherCommand.getMessageWeatherNow;

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
        LOGGER.info("Registering '/weather'...");
        register(new WeatherCommand());
        //LOGGER.info("Registering '/reminder'...");
        //register(new ReminderCommand());

        // ответ на незарегистрированную команду
        registerDefaultAction((absSender, message) -> {
            LOGGER.info("Registering unknown command from " + message.getFrom().getUserName()
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
     * @param update - полученное обновление
     */
    @Override
    public void processNonCommandUpdate(Update update) {
        LOGGER.info("processNonCommandUpdate...");
        SendMessage message = new SendMessage();

        //message generation
        if (update.hasCallbackQuery()) {
            LOGGER.info("Callback Query...");
            String call_data = update.getCallbackQuery().getData();
            if (call_data.equals("remind_weather")) {
                //remind weather
                LOGGER.info("remind weather..");
                message = getMessageRemind(update);
            } else {
                message = getMessageNot(update);
            }
        }else if (update.hasMessage() && update.getMessage().hasText()) {
            //text message
            String text = update.getMessage().getText();
            message = getMessageNot(update, text);

        } else if (update.getMessage().hasLocation()) {
            //we get location
            message = getMessageWeatherNow(update)
                    .setReplyMarkup(getKeyboardRemind());
        } else {
            // we get someone else
            message = getMessageNot(update);
        }
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in non-custom command " + e.getMessage(), e);
        }
    }

    private SendMessage getMessageNot(Update update){
        SendMessage message = new SendMessage();

        LOGGER.info("Executing non-custom update from @" +
                update.getMessage().getFrom().getUserName() + "without text!");

        message.setChatId(update.getMessage().getChatId())
                .setText("I don't know what to do with this" +
                        EmojiParser.parseToUnicode(":cry: \n") +
                        "maybe you need /help");
        return  message;
    }

    private SendMessage getMessageNot(Update update, String text){
        SendMessage message = new SendMessage();

        LOGGER.info("Executing non-custom update from @" +
                update.getMessage().getFrom().getUserName() + "without text!");

        message.setChatId(update.getMessage().getChatId())
                .setText("You said: " + text +
                        ", I don't know what to do with this" +
                        EmojiParser.parseToUnicode(":cry: \n") +
                        "maybe you need /help");
        return  message;
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
