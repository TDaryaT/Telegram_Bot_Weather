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
import utils.TgBasePostgresql;
import utils.Weather;

import java.util.ArrayList;
import java.util.List;

import static Commands.LocateCommand.getKeyboardLoc;
import static Commands.WeatherCommand.getKeyboardWeatherNow;

public class BadWeatherCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(BadWeatherCommand.class);

    public BadWeatherCommand() {
        super("bad_weather",
                "I'll tell you when and how the weather gets worse \n");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/reminder' command from @" + user.getUserName() + "...");

        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());
        message = getMessageRemind(user.getId(), chat.getId());

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/weather' command", e);
        }
    }

    public static SendMessage getMessageRemind(int user_id, long chat_id){
        SendMessage message = new SendMessage();
        LOGGER.info("Bad weather ...");

        TgBasePostgresql base = new TgBasePostgresql();

        message.setChatId(chat_id);
        if (base.isUserId(user_id)) {
            double lat = base.getLatUser(user_id);
            double lon = base.getLonUser(user_id);

            Weather weather = new Weather(lat, lon);
            String[] parseWeather = weather.getWeather3H();
            String date = parseWeather[5];

            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            rowsInline.add(getKeyboardWeatherNow());
            markupInline.setKeyboard(rowsInline);

            return message.setText("Get ready! At " + date + " expected: \n" +
                    weather.toWrap(parseWeather))
                    .setReplyMarkup(markupInline);
        } else {
            return message.setText("I can't tell the weather, if I don't know where are you. " +
                    "I need your coordinates").setReplyMarkup(getKeyboardLoc());
        }
    }

    public static List<InlineKeyboardButton> getKeyboardWorseWeather() {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("When get worse?")
                .setCallbackData("bad_weather"));
        return rowInline;
    }
}
