package Commands;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.TgBasePostgresql;

public class StopCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(StartCommand.class);

    public StopCommand() {
        super("stop", "With this command you can stop the Bot\n");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        String userName = user.getFirstName() + " " + user.getLastName();

        SendMessage answer = new SendMessage();
        answer.setChatId(chat.getId().toString());
        answer.setText("Good bye " + userName + "\n" + "Hope to see you soon!");

        TgBasePostgresql base = new TgBasePostgresql();
        base.deleteUser(user.getId());
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/stop' command", e);
        }
    }
}
