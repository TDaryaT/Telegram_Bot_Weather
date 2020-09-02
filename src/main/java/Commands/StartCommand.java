package Commands;

import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.TgBasePostgresql;

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
        if (base.isUserId(user.getId())) {
            sb.append("Hi again! ")
                    .append(EmojiParser.parseToUnicode(" :heart_eyes: \n"))
                    .append("I really thought you wouldn't come back... \n")
                    .append("If you forgot that i can look '/help'");
        } else {
            sb.append("Hi ")
                    .append(user.getUserName())
                    .append("! Welcome to My WeatherBot!")
                    .append(EmojiParser.parseToUnicode(" :heart_eyes: \n"))
                    .append("To view my capabilities click '/help");
        }
        message.setText(sb.toString());
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/start' command", e);
        }
    }
}
