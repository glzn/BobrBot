package bobrytsya.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class SendMessageJob {
    private static final Logger logger = LoggerFactory.getLogger(SendMessageJob.class);

    @Autowired
    DTEKParser dtekParser;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BobrytsyaBot bot;

    private int PAUSE_TO_AVOID_BLOCKING_IN_MS = 100;

    @Scheduled(cron = "${cronTime}")
    public void sendResultInfo() {
        logger.info("Daily job is starting");
        String message = dtekParser.createMessage();
        if (!message.isEmpty()) {
            List<User> allUsers = userRepository.findAll();
            for(User user : allUsers) {
                sendMessageWithTimeout(user, message);
            }
            logger.info("Message was sent to {} users", allUsers.size());
        }
        logger.info("Daily job is finished");
    }

    private void sendMessageWithTimeout(User user, String message) {
        try {
            logger.info("sending message to user {}", user.getId());
            bot.execute(new SendMessage(user.getId().toString(), message));
            logger.info("message to user {} was sent", user.getId());
            TimeUnit.MILLISECONDS.sleep(PAUSE_TO_AVOID_BLOCKING_IN_MS);
        } catch (TelegramApiException | InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
