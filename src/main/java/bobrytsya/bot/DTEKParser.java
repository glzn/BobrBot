package bobrytsya.bot;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DTEKParser {
    private static final Logger logger = LoggerFactory.getLogger(DTEKParser.class);
    private String INFO_MESSAGE = "%1$s %2$s\nна вул. %3$s \nможе бути відсутня електроенергія.\nПричина: %4$s\n\n";
    private static WebDriver webDriver = getWebDriver();

    private static WebDriver getWebDriver() {
        logger.info("Downloading driver");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("--no-sandbox");
        return new ChromeDriver(options);
    }

    public  String createMessage() {
        List<String> dates = getNextDaysDates();
        StringBuilder builder = new StringBuilder();

        for (String date: dates) {
            builder.append(getDtekInfo(webDriver, date));
            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
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

    private String getDtekInfo(WebDriver driver, String date) {
        String requestURL = "https://www.dtek-krem.com.ua/ua/outages-archive?shutdown-date=" + date + "&query=Бобриця&rem=К.-Святошинський";
//        String requestURL = "https://www.dtek-krem.com.ua/ua/outages-archive?shutdown-date=03.06.2021&query=Бобриця+&rem=К.-Святошинський"";
        driver.get(requestURL);
        return parseDtekSite(driver, date);
    }

    private String parseDtekSite(WebDriver driver, String date) {
        List<WebElement> rowsWithData = driver.findElements(By.xpath("//tr[@data-id]"));
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
}
