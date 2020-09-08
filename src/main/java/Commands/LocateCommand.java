package Commands;

import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.PostgresqlUser;

import java.util.ArrayList;
import java.util.List;

import static Commands.BadWeatherCommand.getKeyboardWorseWeather;
import static Commands.WeatherCommand.getKeyboardWeatherNow;

public class LocateCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(LocateCommand.class);

    public LocateCommand() {
        super("location",
                "I set/change your location for future work \n");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/weather' command from @" + user.getUserName() + "...");

        StringBuilder sb = new StringBuilder();
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());

        sb.append("Send me your location")
                .append(EmojiParser.parseToUnicode(":point_down: \n"));

        message.setReplyMarkup(getKeyboardLoc())
                .setText(sb.toString());
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/location' command", e);
        }
    }
    public static void setLocation(int user_id, double lat, double lon){
        PostgresqlUser base = new PostgresqlUser();
        if (!base.isUserId(user_id)){
            base.setUserID(user_id);
        }
        base.updateLatUser(lat, user_id);
        base.updateLonUser(lon, user_id);
    }

    public static double getLat(Update update){
        Location location = update.getMessage().getLocation();
        LOGGER.info("Get lat from @" + update.getMessage().getFrom().getUserName() + ' ' + location);

        return location.getLatitude();
    }

    public static double getLon(Update update){
        Location location = update.getMessage().getLocation();
        LOGGER.info("Get lon from @" + update.getMessage().getFrom().getUserName() + ' ' + location);

        return location.getLongitude();
    }

    public static SendMessage location(Update update){
        double lat = getLat(update);
        double lon = getLon(update);
        int user_id = update.getMessage().getFrom().getId();
        setLocation(user_id, lat, lon);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(getKeyboardWorseWeather());
        rowsInline.add(getKeyboardWeatherNow());
        markupInline.setKeyboard(rowsInline);

        return new SendMessage()
                .setText("I save your location! You can more activity, see /help")
                .setReplyMarkup(markupInline)
                .setChatId(update.getMessage().getChatId());
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
