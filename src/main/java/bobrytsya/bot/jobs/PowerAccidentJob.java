package bobrytsya.bot.jobs;

import bobrytsya.bot.*;
import bobrytsya.bot.entity.PowerAccidentInfo;
import bobrytsya.bot.repository.PowerAccidentInfoRepository;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PowerAccidentJob {
    private static final Logger logger = LoggerFactory.getLogger(PowerAccidentJob.class);
    private final String INFO_MESSAGE = "\n<code>%1$s</code> аварійне відключення електроенергії по вул. <b>%2$s</b>\n" +
            "Орієнтовний час відновлення %3$s\n";

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    PowerAccidentInfoRepository powerAccidentInfoRepository;

    @Scheduled(cron = "${accidentsJobTime}")
    public void buildAndSendMessage() {
        logger.info("PowerAccidentJob is starting");
        String message = makeMessage(getNewAccidents());
        messageUtils.sendMessageWithTimeout(message);
        logger.info("PowerAccidentJob job is finished");
    }

    private List<PowerAccidentInfo> getNewAccidents() {
        DtekSiteParser dtekSiteParser = new DtekSiteParser();
        WebDriver webDriver = dtekSiteParser.getWebDriver();
        List<PowerAccidentInfo> parsedAccidents = dtekSiteParser.parsePowerAccidentsPage(webDriver);
        webDriver.quit();

        List<PowerAccidentInfo> newAccidents = new ArrayList();
        if (!parsedAccidents.isEmpty()) {
            List<PowerAccidentInfo> oldAccidents = powerAccidentInfoRepository.findAllByAccidentDateIs(LocalDate.now());

            newAccidents = parsedAccidents.stream()
                    .filter(accident -> !oldAccidents.contains(accident))
                    .peek(accident -> logger.info(accident.getAccidentTime() + " " + accident.getStreet()))
                    .collect(Collectors.toList());
            powerAccidentInfoRepository.saveAll(newAccidents);
        }
        return newAccidents;
    }

    private String makeMessage(List<PowerAccidentInfo> newAccidents) {
        StringBuilder sb = new StringBuilder();
        for (PowerAccidentInfo pai : newAccidents) {
            sb.append(String.format(INFO_MESSAGE, pai.getAccidentTime(), pai.getStreet(), pai.getResumptionTime()));
        }
        sb.append("<a href=\"https://www.dtek-krem.com.ua/ua/shutdowns?query=" +
                "Бобриця&subdivision=K14&disconnect-from=" + getCurrentDate() + "\">Детальніше</a>");
        return sb.toString();
    }

    private String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}
