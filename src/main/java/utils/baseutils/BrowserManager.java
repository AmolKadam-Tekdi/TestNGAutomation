package utils.baseutils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import utils.javautils.BaseUtils;
import utils.javautils.PropertiesFileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;


public class BrowserManager extends BaseUtils {
    static String locatorsPath = System.getProperty("user.dir") + "/src/test/java/resources/locators/";

    static {
        PropertiesFileManager.getInstance().setPath(locatorsPath);
    }

    private static String environment;
    static String envFilePath = "src/test/ENV.properties";



    public static void readEnvironment(String envFilePath) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(envFilePath));
            environment = new String(encoded).trim();
        } catch (IOException e) {
            System.err.println("Error reading the environment file: " + e.getMessage());
            throw new RuntimeException("Failed to read environment file", e);
        }
    }

    public static void browserRun() throws Exception {
        String browser = "chrome";
        switch (browser) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--remote-allow-origins=*");
                chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-logging"));
                chromeOptions.setExperimentalOption("useAutomationExtension", false);
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--disable-infobars");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-gpu");
                driver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
//                FirefoxOptions firefoxOptions = new FirefoxOptions();
//                driver = new FirefoxDriver(firefoxOptions);
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--remote-allow-origins=*");
                driver = new EdgeDriver(edgeOptions);
                break;
            default:
                System.out.println("Invalid Browser Selection");
                return;
        }

        driver.manage().window().maximize();
        readEnvironment(envFilePath);
        switch (environment) {
            case "QA":
//                driver.get("https://uat-web-sid.betalaunch.in/home");
                    driver.get("https://eg-uat.tekdinext.com/");
                break;
            case "DEV":
//                driver.get("https://uat-web-sid.betalaunch.in/home");
                driver.get("https://eg-uat.tekdinext.com/");
                break;
            default:
                System.out.println("Invalid Environment Selection");
        }
    }


    public static void main(String[] args) throws Exception {
        browserRun();
    }

}