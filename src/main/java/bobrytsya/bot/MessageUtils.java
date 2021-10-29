package bobrytsya.bot;

import bobrytsya.bot.entity.User;
import bobrytsya.bot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MessageUtils {
    private static final Logger logger = LoggerFactory.getLogger(MessageUtils.class);
    private final int PAUSE_TO_AVOID_BLOCKING_IN_MS = 100;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BobrytsyaBot bot;

    public void sendMessageWithTimeout(String message) {
        if (!message.isEmpty()) {
            List<User> allUsers = userRepository.findAll();
            for(User user : allUsers) {
                try {
                    logger.info("sending message to user {}", user.getId());
                    SendMessage sm = new SendMessage();
                    sm.setChatId(user.getId().toString());
                    sm.setParseMode(ParseMode.HTML);
                    sm.setText(message);
                    sm.disableWebPagePreview();
                    bot.execute(sm);
                    TimeUnit.MILLISECONDS.sleep(PAUSE_TO_AVOID_BLOCKING_IN_MS);
                } catch (TelegramApiException | InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            logger.info("Message was sent to {} users", allUsers.size());
        }
    }
}
