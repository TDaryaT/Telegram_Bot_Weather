package Commands;

import com.vdurmont.emoji.EmojiParser;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

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

        StringBuilder helpMessageBuilder = new StringBuilder();
        helpMessageBuilder.append("<b>What's commands I can do </b>")
                .append(EmojiParser.parseToUnicode(":question: "));

        commandRegistry.getRegisteredCommands()
                .forEach(cmd -> helpMessageBuilder
                        .append(EmojiParser.parseToUnicode("\n:red_circle: "))
                        .append(cmd.toString()));

        helpMessageBuilder.append("\n You can send me your location and I'll tel you weather")
                        .append(EmojiParser.parseToUnicode(":point_down:"));

        SendMessage helpMessage = new SendMessage()
                .setChatId(chat.getId().toString())
                .enableHtml(true)
                .setText(helpMessageBuilder.toString())
                .setReplyMarkup(getKeyboard());
        try {
            absSender.execute(helpMessage);
        } catch (TelegramApiException e) {
            LOGGER.error("Error execute in '/help' command", e);
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
