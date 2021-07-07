package bobrytsya.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class BobrytsyaBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(BobrytsyaBot.class);

    @Autowired
    UserRepository userRepository;

    @Value("${botToken}")
    private String botToken;

    @Value("${botUsername}")
    private String botUsername;

    @Value("${newUserMessage}")
    private String newUserMessage;

    @Value("${deletedUserMessage}")
    private String deletedUserMessage;

    @Value("${wrongCommandResponseMessage}")
    private String wrongCommandResponseMessage;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage()) {
            Message message = update.getMessage();
            logger.info("User {} sent message {}", message.getChatId(), message.getText());
            String response;

            if("/start".equals(message.getText())) {
                saveUser(message);
                logger.info("User {} was added to DB", message.getChatId());
                response = newUserMessage;
            } else if("/delete".equals(message.getText())) {
                deleteUser(message);
                logger.info("User {} was removed from DB", message.getChatId());
                response = deletedUserMessage;
            } else {
                response = wrongCommandResponseMessage;
            }
            sendMessage(message, response);
        }
    }

    private void sendMessage(Message message, String response) {
        try {
            this.execute(new SendMessage(String.valueOf(message.getChatId()), response));
        } catch (TelegramApiException exception) {
            logger.error(exception.getMessage(), exception);
        }
    }

    private void saveUser(Message message) {
        User user = new User();
        user.setId(message.getChatId());
        user.setUserName(message.getFrom().getUserName());
        user.setFirstName(message.getFrom().getFirstName());
        user.setLastName(message.getFrom().getLastName());
        userRepository.save(user);
    }

    private void deleteUser(Message message) {
        User user = new User();
        user.setId(message.getChatId());
        userRepository.delete(user);
    }
}
