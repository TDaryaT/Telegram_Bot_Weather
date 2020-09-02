package Commands;

import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;

public class HelpCommand extends BotCommand {
    private static final Logger LOGGER = LogManager.getLogger(HelpCommand.class);
    private final ICommandRegistry commandRegistry;

    /**
     * @param iCommandRegistry - содержат все кастомные команды
     */
    public HelpCommand(ICommandRegistry iCommandRegistry) {
        super("help", "list all known commands\n");
        commandRegistry = iCommandRegistry;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        LOGGER.info(getCommandIdentifier() + " Executing '/help' command from @" + user.getUserName() + "...");

        SendMessage helpMessage = getHelpMessage()
                .setChatId(chat.getId().toString());
        try {
            absSender.execute(helpMessage);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/help' command", e);
        }
    }

    public SendMessage getHelpMessage(){
        StringBuilder helpMessageBuilder = new StringBuilder();
        helpMessageBuilder.append("<b>What's commands I can do </b>")
                .append(EmojiParser.parseToUnicode(":question: "));

        for (IBotCommand command : commandRegistry.getRegisteredCommands()) {
            if (!command.getCommandIdentifier().equals("start")) {
                helpMessageBuilder
                        .append(EmojiParser.parseToUnicode("\n:red_circle: "))
                        .append(command.toString());
            }
        }
        return new SendMessage()
                .enableHtml(true)
                .setText(helpMessageBuilder.toString());
    }
}
