package Commands;

import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.TgBasePostgresql;

import java.util.ArrayList;
import java.util.List;

public class StartCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(StartCommand.class);

    public StartCommand() {
        super("start", "start using bot\n");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/start' command from @" + user.getUserName() + "...");

        StringBuilder sb = new StringBuilder();
        SendMessage message = new SendMessage();
        message.setChatId(chat.getId().toString());


        TgBasePostgresql base = new TgBasePostgresql();
        //user not new
        if (base.isUserBd(user)) {
            sb.append("Hi again! ")
                    .append(EmojiParser.parseToUnicode(" :heart_eyes: \n"))
                    .append("I really thought you wouldn't come back... \n")
                    .append("You can send me your location and I'll tel you weather")
                    .append(EmojiParser.parseToUnicode
                            (":sun_with_face: :cloud_rain: :cloud_tornado:"));
        } else {
            base.addUserID(user);

            sb.append("Hi ")
                    .append(user.getUserName())
                    .append("! Welcome to My WeatherBot!")
                    .append(EmojiParser.parseToUnicode(" :heart_eyes: \n"))
                    .append("You can send me your location and I'll tel you weather")
                    .append(EmojiParser.parseToUnicode
                            (":sun_with_face: :cloud_rain: :cloud_tornado:"));
        }
        message.setText(sb.toString())
                .setReplyMarkup(getKeyboard());
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/start' command", e);
        }
    }

    public static ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton("I'm here!").setRequestLocation(true));

        keyboard.add(keyboardFirstRow);
        return replyKeyboardMarkup.setKeyboard(keyboard);
    }
}
