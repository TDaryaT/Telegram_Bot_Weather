package Commands;

import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.TgBasePostgresql;
import utils.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WeatherCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(WeatherCommand.class);

    public WeatherCommand() {
        super("weather",
                "determines the current weather by coordinate now \n");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/weather' command from @" + user.getUserName() + "...");

        StringBuilder sb = new StringBuilder();
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        sb.append("Send me your location and I'll tel you weather")
                .append(EmojiParser.parseToUnicode(":point_down: \n"));

        message.setReplyMarkup(getKeyboardLoc())
                .setText(sb.toString());
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/weather' command", e);
        }
    }

    public static SendMessage getMessageWeatherNow(Update update) {
        SendMessage message = new SendMessage();

        Location location = update.getMessage().getLocation();
        LOGGER.info("Get location from @" + update.getMessage().getFrom().getUserName() + ' ' + location);

        message.setChatId(update.getMessage().getChatId());
        String lat = String.format(Locale.US, "%.2f", location.getLatitude());
        String lon = String.format(Locale.US, "%.2f", location.getLongitude());

        Weather weather = new Weather(lat, lon);
        String[] parseWeather = weather.getWeatherNow();

        //set new weather in base
        User user = update.getMessage().getFrom();
        TgBasePostgresql base = new TgBasePostgresql();
        if (!base.isWeather(parseWeather)) {
            base.setWeather(parseWeather);
        }
        if (!base.isUserCity(user, parseWeather[0])) {
            base.updateUserCity(user, parseWeather[0]);
            base.updateLatUser(location.getLatitude(), user);
            base.updateLonUser(location.getLongitude(), user);
        }

        return message.setText(weather.toWrap(parseWeather));
    }

    public static ReplyKeyboardMarkup getKeyboardLoc() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("I'm here!").setRequestLocation(true));
        keyboard.add(keyboardFirstRow);

        //KeyboardRow keyboardSecondRow = new KeyboardRow();
        //keyboardSecondRow.add(new KeyboardButton("city"));
        //keyboard.add(keyboardSecondRow);

        return replyKeyboardMarkup.setKeyboard(keyboard);
    }
}
