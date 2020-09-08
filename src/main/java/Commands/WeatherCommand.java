package Commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.PostgresqlUser;
import utils.Weather;

import java.util.ArrayList;
import java.util.List;

import static Commands.BadWeatherCommand.getKeyboardWorseWeather;
import static Commands.LocateCommand.getKeyboardLoc;

public class WeatherCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(WeatherCommand.class);

    public WeatherCommand() {
        super("weather",
                "determines the current weather by coordinate now \n");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/weather' command from @" + user.getUserName() + "...");

        SendMessage message;
        int user_id = user.getId();
        message = getMessageWeatherNow(user_id, chat.getId())
                .setChatId(chat.getId().toString());

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/weather' command", e);
        }
    }

    public static SendMessage getMessageWeatherNow(int user_id, long chat_id) {
        SendMessage message = new SendMessage();

        PostgresqlUser base = new PostgresqlUser();
        if (base.isUserId(user_id)) {
            Weather weather = new Weather(base.getLatUser(user_id), base.getLonUser(user_id));
            String[] parseWeather = weather.getWeatherNow();

            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            rowsInline.add(getKeyboardWorseWeather());
            markupInline.setKeyboard(rowsInline);

            return message.setText("The weather now: \n" +
                    weather.toWrap(parseWeather))
                    .setReplyMarkup(markupInline)
                    .setChatId(chat_id);
        } else {
            return message.setText("I can't tell the weather, if I don't know where are you. " +
                    "I need your coordinates").setReplyMarkup(getKeyboardLoc());
        }
    }

    public static List<InlineKeyboardButton> getKeyboardWeatherNow() {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Weather now?")
                .setCallbackData("weather"));
        return rowInline;
    }
}
