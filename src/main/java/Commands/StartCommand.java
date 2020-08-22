package Commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.vdurmont.emoji.EmojiParser;

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

        //TODO: Проверка что первый раз
        sb.append("Hi ")
                .append(user.getUserName())
                .append(EmojiParser.parseToUnicode
                        ("! Welcome to My WeatherBot! :heart_eyes: \t\n"))
                .append(EmojiParser.parseToUnicode
                        ("You can send me your location and I'll tel you weather"))
                .append(EmojiParser.parseToUnicode(":sun_with_face: :cloud_rain: :cloud_tornado:"));
        message.setText(sb.toString());

        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/start' command", e);
        }
    }
}
