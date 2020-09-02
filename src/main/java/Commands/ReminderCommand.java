package Commands;

import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.TgBasePostgresql;
import utils.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static Commands.WeatherCommand.getKeyboardLoc;

public class ReminderCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(ReminderCommand.class);

    public ReminderCommand() {
        super("reminder",
                "I will remember your coordinates " +
                        "and tell you if the weather is bad 3 hours later \n");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/reminder' command from @" + user.getUserName() + "...");

        StringBuilder sb = new StringBuilder();
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        TgBasePostgresql base = new TgBasePostgresql();
        String city = base.getCityUser(user.getId());

        sb.append("I'll tel you if the weather is bad 3 hours later \n")
                .append("You in ").append(city).append("?")
                .append(EmojiParser.parseToUnicode(":point_down:"));

        message.setReplyMarkup(getKeyboardLoc())
                .setText(sb.toString());
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/weather' command", e);
        }
    }

    public static SendMessage getMessageRemind(Update update){
        SendMessage message = new SendMessage();
        LOGGER.info("Remind ...");
        int user = update.getCallbackQuery().getFrom().getId();

        TgBasePostgresql base = new TgBasePostgresql();

        message.setChatId(update.getCallbackQuery().getMessage().getChatId());
        String lat = String.format(Locale.US, "%.2f", base.getLatUser(user));
        String lon = String.format(Locale.US, "%.2f", base.getLonUser(user));

        Weather weather = new Weather(lat, lon);

        String[] parseWeather = weather.getWeather3H(user);
        String date = parseWeather[5].substring(10);
        //отправка за три часа до
        return message.setText("Приготовься! в " + date + " будет: \n" +
                weather.toWrap(parseWeather));
    }

    public static InlineKeyboardMarkup getKeyboardRemind() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("When get worse?")
                .setCallbackData("remind_weather"));
        // Set the keyboard to the markup
        rowsInline.add(rowInline);
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    //public static
}
