package bobrytsya.bot.jobs;

import bobrytsya.bot.DtekSiteParser;
import bobrytsya.bot.MessageUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PlannedOutagesJob {
    private static final Logger logger = LoggerFactory.getLogger(PlannedOutagesJob.class);

    @Autowired
    MessageUtils messageUtils;

    @Scheduled(cron = "${outagesJobTime}")
    public void buildAndSendMessage() {
        logger.info("PlannedOutagesJob is starting");
        String message = makeMessage();
        messageUtils.sendMessageWithTimeout(message);
        logger.info("PlannedOutagesJob is finished");
    }

    private String makeMessage() {
        StringBuilder builder = new StringBuilder();
        List<String> dates = getNextDaysDates();
        DtekSiteParser dtekSiteParser = new DtekSiteParser();
        WebDriver webDriver = dtekSiteParser.getWebDriver();
        for (String date: dates) {
            builder.append(dtekSiteParser.getPlannedOutagesInfo(date, webDriver));
            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        webDriver.quit();
        return builder.toString();
    }

    private List<String> getNextDaysDates() {
        String pattern = "dd.MM.yyyy";
        DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
        String tomorrow1 = LocalDate.now().plusDays(1).toString(dtf);
        String tomorrow2 = LocalDate.now().plusDays(2).toString(dtf);
        String tomorrow3 = LocalDate.now().plusDays(3).toString(dtf);
        return Arrays.asList(tomorrow1, tomorrow2, tomorrow3);
    }
}
