package bobrytsya.bot;

import bobrytsya.bot.entity.PowerAccidentInfo;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DtekSiteParser {
    private static final Logger logger = LoggerFactory.getLogger(DtekSiteParser.class);
    private final String INFO_MESSAGE = "%1$s %2$s\nна вул. %3$s \nможе бути відсутня електроенергія.\nПричина: %4$s\n\n";

    public WebDriver getWebDriver() {
        logger.info("Downloading driver");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("--no-sandbox");
        return new ChromeDriver(options);
    }

    public String getPlannedOutagesInfo(String date, WebDriver webDriver) {
        String requestURL = "https://www.dtek-krem.com.ua/ua/outages-archive?shutdown-date=" + date + "&query=Бобриця&rem=К.-Святошинський";
        webDriver.get(requestURL);

        List<WebElement> rowsWithData = webDriver.findElements(By.xpath("//tr[@data-id]"));
        StringBuilder stringBuilder = new StringBuilder();
        for (WebElement webElement : rowsWithData) {
            List<WebElement> listOfColumns = webElement.findElements(By.className("search-masks-text"));
            if(!listOfColumns.isEmpty()) {
                String townsPlusStreets = listOfColumns.get(1).getText();

                String lineSeparator = determineLineSeparator(townsPlusStreets);
                List<String> items = Arrays.asList(townsPlusStreets.split(lineSeparator));

                String streets = items.stream().filter(name -> name.startsWith("Бобриця")).findAny().get().replace("Бобриця: ", "");
                String typeOfWork = listOfColumns.get(2).getText();
                String timeOfWork = listOfColumns.get(3).getText();
                stringBuilder.append(String.format(INFO_MESSAGE, date, timeOfWork, streets, typeOfWork));
            }
        }
        return stringBuilder.toString();
    }

    private String determineLineSeparator(String townsPlusStreets) {
        String lineSeparator;
        if(townsPlusStreets.contains("нас.пункт:")) {
            lineSeparator = "нас.пункт:";
        } else {
            lineSeparator = System.lineSeparator();
        }
        return lineSeparator;
    }

    public List<PowerAccidentInfo> parsePowerAccidentsPage(WebDriver webDriver) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate = LocalDate.now();
        String date = localDate.format(dtf);
        String requestURL = "https://www.dtek-krem.com.ua/ua/shutdowns?disconnect-from=" + date + "&subdivision=K14&query=Бобриця";
        webDriver.get(requestURL);
        List<WebElement> listOfPowerOutages = webDriver.findElements(By.xpath("//tr[@data-id]"));
        List<PowerAccidentInfo> listOfAccidents = new ArrayList<>();
        for (WebElement webElement : listOfPowerOutages) {
            List<WebElement> listOfColumns = webElement.findElements(By.tagName("td"));
            if(!listOfColumns.isEmpty()) {
                List<String> townsPlusStreets = Arrays.asList(listOfColumns.get(1).getText().split("\\r?\\n"));
                String streets = townsPlusStreets.stream().filter(name -> name.startsWith("Бобриця")).findAny().get().replace("Бобриця: ", "");
                String accidentTime = listOfColumns.get(2).getText().substring(11);
                String resumptionTime = listOfColumns.get(3).getText();

                PowerAccidentInfo powerAccidentInfo = new PowerAccidentInfo();
                powerAccidentInfo.setStreet(streets);
                powerAccidentInfo.setAccidentDate(LocalDate.now());
                powerAccidentInfo.setAccidentTime(accidentTime);
                powerAccidentInfo.setResumptionTime(resumptionTime);

                listOfAccidents.add(powerAccidentInfo);
            }
        }
        return listOfAccidents;
    }
}
