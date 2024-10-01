package utils.javautils;

import com.google.common.base.Function;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;


public class BaseUtils {
    public static WebDriver driver ;
    public static ThreadLocal<WebDriver> threadDriver = new ThreadLocal<WebDriver>();
    public static final int DEFAULT_WAIT_IN_SECS = Integer.parseInt(System.getProperty("default.wait.secs", "150"));
    public static final float MAX_RETRIES = 5.0F;
    static boolean shouldMaximizeWindows;
    public static final Dimension SYS_DEFAULT_WINDOW_SIZE = new Dimension(1280, 1024);  // 1280x1024 is the minimum supported resolution (as of Aug2014)
    static String userAgentString;
    static Dimension defaultWindowSize;
    private static final Logger LOGGER = LoggerUtil.getLogger();

    public BaseUtils() {
    }

    /*Logger methods*/
    /*------------------------------------------------------*/
    public static void logStep(String log)
    {
        LOGGER.info(log);
        Reporter.logStep(log);
    }

    static void logDebug(String message) {
        //Reporter.log('DEBUG: ' + message)
        LOGGER.info(message);
    }

    static void warning(String message) {
        LOGGER.warning(message);
    }

    protected static void fail(String message) {
        LOGGER.info(message);
        Reporter.logFail(message,driver);
        Reporter.logStep(message);

    }

    static void pass(String message)
    {
        Reporter.logPass(message);
        LOGGER.info(message);
    }

    public static void logException(String exception) {
        Reporter.logStep("EXCEPTION: " + exception);
        LOGGER.severe( exception);
    }

    public static void logAssertion(String message) {
        logStep("ASSERTION: " + message);
        Reporter.logStep(message);
    }


    /*Assertions*/

    public static void assertEquals(String assertMessage, Object param1, Object param2, String failureMessage) {
        logAssertion(assertMessage + " -- assertEquals(" + param1.toString() + " <---> " + param2.toString() + ")");
        try {
            Assert.assertEquals(param1.toString(), param2.toString(), failureMessage);
        } catch (AssertionError e) {
            logStep("Assertion Failed: " + e.getMessage());
            throw e;
        }
    }

    public static boolean assertNotEquals(String assertMessage, Object param1, Object param2, String failureMessage) {
        logAssertion(assertMessage + " -- assertNotEquals(" + param1.toString() + " <---> " + param2.toString() + ")");

        try {
            if (!param1.equals(param2)) {
                logAssertion("Assertion Passed: Arguments are not equal");
                return true;
            } else {
                logAssertion("Assertion Failed: Arguments are equal");
                return false;
            }
        } catch (Exception e) {
            logAssertion("Exception occurred: " + e.getMessage());
            throw e;
        }
    }


    public static boolean assertTrue(String assertMessage, boolean condition, String failureMessage){
        logStep(assertMessage + " -- assertTrue(" + condition + ")");
        if (!condition) {
            fail(failureMessage);
            Assert.assertFalse(condition, failureMessage);
        }
        return condition;
    }

    /*Selenium Reusable methods*/
    /*------------------------------------------------------*/
    public static boolean click(WebElement element)
    {
        try
        {
            element.click();
            return true;
        }
        catch (Exception e)
        {
            logStep("Exception while clicking on the element" +e.getMessage());
            return false;
        }
    }

    public static void sendTextOnUI(WebElement element, String text)
    {
        try
        {
            element.sendKeys(text);
        }
        catch (Exception e)
        {
            logStep("Exception while sending text to the element" + e.getMessage());
        }
    }

    public static void clickElementByText(String visibleText) {
        String elementText = "Element with text '";
        try {
            String xpathExpression = "//li[text()='" + visibleText + "']";
            WebElement element = driver.findElement(By.xpath(xpathExpression));
            if (element.isDisplayed()) {
                logStep( elementText + visibleText + "' is displayed.");
                element.click();
            } else {
                logStep(elementText + visibleText + "' is not displayed.");
            }
        } catch (NoSuchElementException e) {
            logStep(elementText + visibleText + "' not found.");
            e.printStackTrace();
        }
    }

    public static boolean isChecked(WebElement element) {
        try {
            if (!element.isSelected()) {
                element.click();
            }
            String checked = element.getAttribute("checked");
            return checked != null && checked.equals("true");
        } catch (Exception e) {
            logStep("Exception in isChecked: " + e.getMessage());
            // Handle the error more gracefully, depending on your use case
            return false;
        }
    }

    public boolean clickOnceClickable(WebElement element, int maxRetries, int waitTimeInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(waitTimeInSeconds));

        for (int i = 0; i < maxRetries; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(element));
                element.click();
                return true;
            } catch (Exception e) {
                logStep("Retry " + (i + 1) + " - Element is not clickable yet: " + e.getMessage());
            }
        }
        logStep("Element is not clickable after " + maxRetries + " retries.");
        return false;
    }

    /*Re-usable window handle methods*/
    public void openNewWindow(String url) {
        ((JavascriptExecutor) driver).executeScript("window.open()");

        // Switch to the new tab
        String originalHandle = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }
        driver.get(url);
    }

    public void switchToNewWindow() {
        try {
            Object[] windowHandles = driver.getWindowHandles().toArray();
            String newWindowHandle = (String) windowHandles[1];
            driver.switchTo().window(newWindowHandle);
        } catch (Exception e) {
            logStep("Exception occurred while switching to a new window: "+ e.getMessage());
        }
    }

    public void closeNewWindowAndSwitchBack() {
        try {
            String originalWindowHandle = driver.getWindowHandle();
            driver.close();
            driver.switchTo().window(originalWindowHandle);
        } catch (Exception e) {
            logStep("Exception occurred while closing a new window and switching back: " + e.getMessage());
        }
    }

    static void switchToDefaultContent()
    {
        try
        {
            driver.switchTo().defaultContent();
        }
        catch (Exception e)
        {
            logStep("Exception occurred while switching to default content: " + e.getMessage());
        }
    }

    /*Webdriver Wait for element synchronisation*/

    static void impliciteWait(int waitTimeinSeconds) {
        try
        {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTimeinSeconds));
        }
        catch (Exception e)
        {
            logStep("Exception in implicitWait: " +e.getMessage());
        }
    }


    public static void retryUntilInteractable(WebDriver driver, By locator, Consumer<WebElement> methodToRetry) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                WebElement element = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_IN_SECS))
                        .until(ExpectedConditions.elementToBeClickable(locator));
                if (element != null) {
                    methodToRetry.accept(element);
                    break;
                }
            } catch (Exception e) {
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logStep("Interrupted while waiting to retry: " + ie.getMessage());
                        return;
                    }
                    driver.navigate().refresh();
                } else {
                    logStep("Max retries reached. Element located by " + locator + " is not clickable.");
                }
            }
        }
    }


    public static void waitForElementToBeVisible( WebElement locator, int timeoutInSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(ExpectedConditions.visibilityOfElementLocated((By) locator));
        } catch (Exception e) {
            logStep("Exception in waitForVisible: " + e.getMessage());
        }
    }


    public static void waitForElementToLoad(WebElement element) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_IN_SECS));
            wait.until(ExpectedConditions.presenceOfElementLocated((By) element));
        } catch (Exception e) {
            logStep("Exception in waitForElementToLoad: " + e.getMessage());
            // Handle the error more gracefully, depending on your use case
        }
    }

    public static void waitForTextInElement(WebDriver driver, By locator, String text, int timeoutInSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
        } catch (Exception e) {
            logStep("Exception in waitForTextInElement: " + e.getMessage());
        }
    }

    public static void waitForElementToBePresent(WebDriver driver, By locator, int timeoutInSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            logStep("Exception in waitForElementPresent: " + e.getMessage());
        }
    }

    public static boolean scrollToElement(WebDriver driver, By locator) {
        try {
            WebElement element = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            return true;
        } catch (Exception e) {
            logStep("Exception in scrollToElement: " + e.getMessage());
            return false;
        }
    }


    /*Popup Handling methods*/
    /*--------------------------------------------------------------------------------*/

    public static String acceptPopupMessage(WebDriver driver) {
        String message = null;
        try {
            Alert alert = driver.switchTo().alert();
            message = alert.getText();
            alert.accept();
        } catch (Exception e) {
            logStep("Exception in acceptPopupMessage: " + e.getMessage());
            // You might want to handle the error more gracefully, depending on your use case
        }
        return message;
    }

    public static String dismissPopupMessage(WebDriver driver) {
        String message = null;
        try {
            Alert alert = driver.switchTo().alert();
            message = alert.getText();
            alert.dismiss();
        } catch (Exception e) {
            // Handle exception gracefully, optionally log it
            message = null;
        }
        return message;
    }

    /*Window Handling methods*/
    /*--------------------------------------------------------------------------------*/

    protected static void resizeWindowToDefault(WebDriver driver) {
        try {
            if (shouldMaximizeWindows) {
                driver.manage().window().maximize();
            } else {
                Dimension defaultWindowSize = new Dimension(1280, 800);
                driver.manage().window().setSize(defaultWindowSize);
            }
        } catch (WebDriverException e) {
            // Handle WebDriverException specifically for window management issues
            logStep("WebDriverException in resizeWindowToDefault: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other exceptions that might occur
            logStep("Exception in resizeWindowToDefault: " + e.getMessage());
        }
    }

    protected static Map<String, Integer> getHeightInfo(WebDriver driver) {
        try {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            String script = "return window.screen.height + ':' + window.innerHeight + ':' + (window.outerHeight - window.innerHeight);";
            String[] info = ((String) jsExecutor.executeScript(script)).split(":");

            int screenPx = Integer.parseInt(info[0]);
            int viewportPx = Integer.parseInt(info[1]);
            int windowBorderPx = Integer.parseInt(info[2]);

            Map<String, Integer> heightInfo = new HashMap<>();
            heightInfo.put("screenHeight", screenPx);
            heightInfo.put("viewportHeight", viewportPx);
            heightInfo.put("windowBorderHeight", windowBorderPx);

            return heightInfo;
        } catch (Exception e) {
            logStep("Exception in getHeightInfo: " + e.getMessage());
            Map<String, Integer> errorInfo = new HashMap<>();
            errorInfo.put("screenHeight", -1);
            return errorInfo;
        }
    }

    public static boolean closeWindow(WebDriver driver) {
        logStep("Closing window: " + driver.getTitle());
        try {
            driver.close();
            return true;
        } catch (Exception e) {
            logStep("Exception while closing the window: " + e.getMessage());
            return false;
        }
    }

    public static void resetWindowSizeToSystemDefault(WebDriver driver) {
        try {
            setWindowDefaultSize(driver, SYS_DEFAULT_WINDOW_SIZE);
        } catch (Exception e) {
            logStep("Exception in resetWindowSizeToSystemDefault: " + e.getMessage());
            // Handle the error more gracefully, depending on your use case
        }
    }

    private static void setWindowDefaultSize(WebDriver driver, Dimension size) {
        try {
            Dimension widthInfo = (Dimension) getWidthInfo(driver);

            if (widthInfo.getWidth() > 0) {
                if (widthInfo.getWidth() == size.getWidth() && !userAgentString.contains("Mac")) {
                    shouldMaximizeWindows = true;
                } else if (widthInfo.getWidth() > 0) {
                    // Account for the window borders when sizing
                    size = new Dimension(size.getWidth() + widthInfo.getWidth(), size.getHeight());
                }
            }
            defaultWindowSize = size;
        } catch (Exception e) {
            logStep("Exception in setWindowDefaultSize: " + e.getMessage());
        }
    }

    protected static Map<String, Integer> getWidthInfo(WebDriver driver) {
        try {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            String script = "return window.screen.width + ':' + window.innerWidth + ':' + (window.outerWidth - window.innerWidth);";
            String[] info = ((String) jsExecutor.executeScript(script)).split(":");

            int screenPx = Integer.parseInt(info[0]);
            int viewportPx = Integer.parseInt(info[1]);
            int windowBorderPx = Integer.parseInt(info[2]);

            Map<String, Integer> widthInfo = new HashMap<>();
            widthInfo.put("screenWidth", screenPx);
            widthInfo.put("viewportWidth", viewportPx);
            widthInfo.put("windowBorderWidth", windowBorderPx);

            return widthInfo;
        } catch (Exception e) {
            logStep("Exception in getWidthInfo: " + e.getMessage());
            Map<String, Integer> errorInfo = new HashMap<>();
            errorInfo.put("screenWidth", -1);
            return errorInfo;
        }
    }


    public static boolean closeAllOtherWindows() {
        String originalHandle = driver.getWindowHandle();
        try {
            Set<String> handles = driver.getWindowHandles();
            for (String handle : handles) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    closeWindow(driver); // Assuming you have a method to close the current window
                }
            }
            driver.switchTo().window(originalHandle);
            return true;
        } catch (Exception e) {
            logStep("Failed closing windows in closeAllOtherWindows: " + e.getMessage());
            return false;
        } finally {
            try {
                driver.switchTo().window(originalHandle);
            } catch (Exception e) {
            }
        }
    }


    public static void retryUntilInteractable(WebDriver driver, By locator, Runnable methodToRetry) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                WebElement element = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_IN_SECS))
                        .until(ExpectedConditions.elementToBeClickable(locator));
                if (element != null) {
                    methodToRetry.run();
                    break;
                }
            } catch (Exception e) {
                if (i < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt(); // Reset the interrupted status
                        logStep("Thread was interrupted: " + interruptedException.getMessage());
                    }
                    driver.navigate().refresh();
                } else {
                    logStep("Max retries reached. Element located by " + locator + " is not clickable.");
                }
            }
        }
    }

    public static boolean waitForUi(int timeoutSecs) {
        String simpleLoader = "SimpleLoader";
        try {
            if (driver.findElements(By.className(simpleLoader)).isEmpty()) {
                return true;
            } else {
                int count = driver.findElements(By.className(simpleLoader)).size();
                if (count > 0) {
                    return betterWait(() -> driver.findElements(By.className(simpleLoader)).size() < count, timeoutSecs);
                }
            }
        } catch (Exception e) {
            logStep("Exception in waitForUi: " + e.getMessage());
            // Handle the error more gracefully, depending on your use case
            return false;
        }
        return false; // Default return if the above conditions are not met
    }


    public static boolean betterWait(Callable<Boolean> conditionCheck, int timeoutSecs) {
        final int MAX_STALE_ELEMENT_RETRIES = 10;
        int retries = 1;
        while (true) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSecs));
                wait.until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver input) {
                        try {
                            return conditionCheck.call();
                        } catch (Exception e) {
                            logStep("Exception in conditionCheck: " + e.getMessage());
                            return false;
                        }
                    }
                });
                return true;
            } catch (TimeoutException te) {
                logDebug("TimeoutException in betterWait");
                return false;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                if (retries < MAX_STALE_ELEMENT_RETRIES) {
                    logStep("betterWait failed with Stale or NoSuchElementException. Tried '" + retries + "' number(s) to recover");
                    retries++;
                } else {
                    logStep("betterWait failed with Stale or NoSuchElementException. Tried '" + retries + "' number(s) to recover: \n" + e.getMessage());
                    return false;
                }
            } catch (Exception e) {
                logStep("betterWait failed with exception: " + e.getMessage());
                return false;
            }
        }
    }


    public static WebElement waitForElement(WebDriver driver, WebElement locator, Duration timeout, ExpectedCondition<WebElement> expectedCondition) {
        WebElement element = null;
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            element = wait.until(expectedCondition);
        } catch (TimeoutException e) {
            logStep("Timeout occurred while waiting for the element: " + e.getMessage());
        } catch (NoSuchElementException e) {
            logStep("Element not found: " + e.getMessage());
        } catch (Exception e) {
            logStep("An unexpected error occurred: " + e.getMessage());
        }
        return element;
    }

    // Specific methods for different conditions (presence, visibility, clickability)

    public static void waitForPresence(WebElement locator, int timeout) {
        waitForElement(driver, locator, Duration.ofSeconds(timeout), ExpectedConditions.presenceOfElementLocated((By)locator));
    }


    public static void waitForVisibility(WebElement locator, int timeout) {
        waitForElement(driver, (WebElement) locator, Duration.ofSeconds(timeout), ExpectedConditions.visibilityOfElementLocated((By) locator));
    }



    public static void waitForClickability(WebElement locator, int timeout) {
        waitForElement(driver, locator, Duration.ofSeconds(timeout), ExpectedConditions.elementToBeClickable(locator));
    }


    public static void waitForUIToLoad(WebDriver driver, int timeoutInSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            // Wait for the page to be fully loaded (document.readyState = 'complete')
            wait.until(webDriver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
            logStep("UI has fully loaded.");
        } catch (TimeoutException e) {
            logStep("Timeout occurred while waiting for the UI to load: " + e.getMessage());
        } catch (Exception e) {
            logStep("An unexpected error occurred while waiting for the UI to load: " + e.getMessage());
        }
    }


    public static void waitForUIToload(WebDriver driver, int timeoutInSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));

            // Wait for document.readyState to be 'complete'
            wait.until(webDriver -> ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").equals("complete"));

            // Check for any active jQuery AJAX requests
            Boolean jQueryDefined = (Boolean) ((JavascriptExecutor) driver)
                    .executeScript("return typeof jQuery != 'undefined'");

            if (jQueryDefined) {
                wait.until(webDriver -> ((JavascriptExecutor) driver)
                        .executeScript("return jQuery.active == 0").equals(true));
            }

            logStep("UI has fully loaded, including any jQuery AJAX requests.");
        } catch (TimeoutException e) {
            logStep("Timeout occurred while waiting for the UI to load: " + e.getMessage());
        } catch (Exception e) {
            logStep("An unexpected error occurred while waiting for the UI to load: " + e.getMessage());
        }
    }

    public static void enterPassword(WebDriver driver, String password) {
        waitForElementToBeVisible(driver.findElement(By.xpath("(//*[@type='password'])[1]")),5);
        if (password.length() != 4) {
            throw new IllegalArgumentException("Password must be exactly 4 characters long.");
        }

        for (int i = 0; i < password.length();) {
            String charToEnter = String.valueOf(password.charAt(i++)); // Get the character to enter
            WebElement inputField = driver.findElement(By.xpath("(//*[@type='password'])["+ i +"]"));

            inputField.clear(); // Clear the field in case there's any prefilled data
            inputField.sendKeys(charToEnter); // Enter the character into the field
        }

        logStep("Password entered successfully.");
    }



    public static void waitForUI(WebDriver driver, int timeoutInSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));

            // 1. Wait for document.readyState to be 'complete'
            wait.until(webDriver -> ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").equals("complete"));
            logStep("Document is in ready state.");

            // 2. Check for any active jQuery AJAX requests (if jQuery is present)
            Boolean isJQueryDefined = (Boolean) ((JavascriptExecutor) driver)
                    .executeScript("return typeof jQuery != 'undefined'");

            if (isJQueryDefined) {
                wait.until(webDriver -> ((JavascriptExecutor) driver)
                        .executeScript("return jQuery.active == 0").equals(true));
                logStep("All jQuery AJAX requests have completed.");
            }

            // 3. Check for Angular (if Angular is present)
            Boolean isAngularDefined = (Boolean) ((JavascriptExecutor) driver)
                    .executeScript("return window.getAllAngularTestabilities ? true : false");

            if (isAngularDefined) {
                wait.until(webDriver -> ((JavascriptExecutor) driver)
                        .executeScript("return window.getAllAngularTestabilities().findIndex(x=>!x.isStable()) === -1"));
                logStep("All Angular tasks are stable and completed.");
            }

            // 4. Add any custom JavaScript waiting logic for React or other async tasks (if needed)
            wait.until(webDriver -> ((JavascriptExecutor) driver)
                    .executeScript("return window.performance.timing.loadEventEnd > 0"));
            logStep("All React or other asynchronous tasks are complete.");

            // 5. Optional forced sleep as a fallback
            Thread.sleep(2000);  // Adjust the sleep time as necessary

            logStep("UI has fully loaded.");
        } catch (TimeoutException e) {
            logStep("Timeout occurred while waiting for the UI to load: " + e.getMessage());
        } catch (InterruptedException e) {
            logStep("Interrupted during forced wait: " + e.getMessage());
        } catch (Exception e) {
            logStep("An unexpected error occurred while waiting for the UI to load: " + e.getMessage());
        }
    }


    public static boolean waitForInterface(int timeoutSecs) {
        try {
            List<WebElement> loaders = driver.findElements(By.className("SimpleLoader"));
            if (loaders.isEmpty()) {
                return true;
            } else {
                int count = loaders.size();
                if (count > 0) {
                    // Pass a closure-like lambda to betterWait
                    return betterWaits(() -> driver.findElements(By.className("SimpleLoader")).size() < count, timeoutSecs);
                }
            }
        } catch (Exception e) {
            logException("Exception in waitForUi: " + e.getMessage());
            return false;
        }
        return false;
    }


    private static final int MAX_STALE_ELEMENT_RETRIES = 10;

    // Updated betterWait method using closure-like Supplier (lambda function)
    public static boolean betterWaits(Supplier<Boolean> conditionCheck, int timeoutSecs) {
        int retries = 1;
        while (true) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSecs));

                wait.until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver webDriver) {
                        return conditionCheck.get(); // Execute the condition as a lambda
                    }
                });
                return true;
            } catch (TimeoutException te) {
                logDebug("TimeoutException in betterWait");
                return false;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                if (retries < MAX_STALE_ELEMENT_RETRIES) {
                    logException("betterWait failed with Stale or NoSuchElement Exception. Tried " + retries + " time(s) to recover");
                    retries++;
                } else {
                    logException("betterWait failed with Stale or NoSuchElement Exception. Max retries reached: " + e.getMessage());
                    return false;
                }
            } catch (Exception e) {
                logException("betterWait failed with exception: " + e);
                return false;
            }
        }
    }

    private WebDriverWait getWait(int timeOutInSeconds,int pollingEveryInMiliSec) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeOutInSeconds));
        wait.pollingEvery( Duration.ofSeconds(timeOutInSeconds));
        wait.ignoring(NoSuchElementException.class);
        wait.ignoring(ElementNotInteractableException.class);
        wait.ignoring(StaleElementReferenceException.class);
        wait.ignoring(NoSuchFrameException.class);
        return wait;
    }


    public static void setImplicitWait(long timeout, TimeUnit unit) {
        driver
                .manage()
                .timeouts()
                .implicitlyWait(timeout, unit == null ? TimeUnit.SECONDS : unit);
    }

    public void waitForElementVisible(By locator,int timeOutInSeconds,int pollingEveryInMiliSec) {
        setImplicitWait(1, TimeUnit.SECONDS);
        WebDriverWait wait = getWait(timeOutInSeconds, pollingEveryInMiliSec);
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(locator)));
        setImplicitWait(10, TimeUnit.SECONDS);
    }

    public void hardWait(int timeOutInMiliSec) throws InterruptedException {
        Thread.sleep(timeOutInMiliSec);
    }

    public WebElement handleStaleElement(By locator,int retryCount,int delayInSeconds) throws InterruptedException {

        WebElement element = null;

        while (retryCount >= 0) {
            try {
                element = driver.findElement(locator);
                return element;
            } catch (StaleElementReferenceException e) {
                hardWait(delayInSeconds);
                retryCount--;
            }
        }
        throw new StaleElementReferenceException("Element cannot be recovered");
    }

    public void elementExits(By locator,int timeOutInSeconds,int pollingEveryInMiliSec) {
        setImplicitWait(1, TimeUnit.SECONDS);
        WebDriverWait wait = getWait(timeOutInSeconds, pollingEveryInMiliSec);
        wait.until(elementLocatedBy(locator));
        setImplicitWait(10, TimeUnit.SECONDS);
    }

    public void elementExistAndVisible(WebElement locator,int timeOutInSeconds,int pollingEveryInMiliSec) {
        setImplicitWait(1, TimeUnit.SECONDS);
        WebDriverWait wait = getWait(timeOutInSeconds, pollingEveryInMiliSec);
        wait.until(elementLocatedBy((By) locator));
        wait.until(ExpectedConditions.visibilityOfElementLocated((By) locator));
        setImplicitWait(10, TimeUnit.SECONDS);

    }

    public void waitForIframe(By locator,int timeOutInSeconds,int pollingEveryInMiliSec) {
        setImplicitWait(1, TimeUnit.SECONDS);
        WebDriverWait wait = getWait(timeOutInSeconds, pollingEveryInMiliSec);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
        driver.switchTo().defaultContent();
        setImplicitWait(10, TimeUnit.SECONDS);
    }

    private Function<WebDriver, Boolean> elementLocatedBy(final By locator){
        return new Function<WebDriver, Boolean>() {

            @Override
            public Boolean apply(WebDriver driver) {
                return driver.findElements(locator).size() >= 1;
            }
        };
    }


    /*Element Condition checck methods*/
    /*----------------------------------------------------------------*/
    public static boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
            return false;
        }
    }

    public static boolean isElementEnabled(WebElement locator) {
        try {
            return locator.isEnabled();
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            logStep("Exception in isElementEnabled: " + e.getMessage());
            // Exception is caught to prevent it from being propagated. Return false if the element is not enabled or not present.
            return false;
        }
    }

    public static boolean isElementSelected(By locator) {
        try {
            WebElement element = driver.findElement(locator);
            return element.isSelected();
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            logStep("Exception in isElementSelected: " + e.getMessage());
            return false;
        }
    }

    public static boolean isElementNotSelected(By locator) {
        try {
            return !driver.findElement(locator).isSelected();
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            logStep("Exception in isElementNotSelected: " + e.getMessage());
            return true;
        }
    }

    public static boolean isTextPresent(String text) {
        try {
            return driver.getPageSource().contains(text);
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            logStep("Exception in isTextPresent: " + e.getMessage());
            return false;
        }
    }

    public static boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            logStep("Exception in isElementPresent: " + e.getMessage());
            return false;
        }
    }

    public static boolean isDropdownPresent(By dropdownLocator) {
        try {
            return driver.findElement(dropdownLocator).isDisplayed();
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            logStep("Exception in isDropdownPresent: " + e.getMessage());
            return false;
        }
    }

    public static boolean isOptionPresentInDropdown(By dropdownLocator, String optionText) {
        try {
            WebElement dropdown = driver.findElement(dropdownLocator);
            Select select = new Select(dropdown);
            List<WebElement> options = select.getOptions();

            for (WebElement option : options) {
                if (option.getText().equals(optionText)) {
                    return true;
                }
            }
            return false;
        } catch (NoSuchElementException | ElementNotInteractableException e) {
            logStep("Exception in isOptionPresentInDropdown: " + e.getMessage());
            return false;
        }
    }

    /*iFrame reusable methods*/
    /*---------------------------------------------------------------------------------------------*/

    public static boolean switchToFrame(WebElement frame) {
        try {
            driver.switchTo().frame(frame);
            return true;
        } catch (Exception e) {
            logStep("Exception in switchToFrame: " + e.getMessage());
            // Exception is caught to prevent it from being propagated. Return false if there is an issue switching to the frame.
            return false;
        }
    }

    public static void switchToFrameWithWait(WebDriver driver, By frameLocator) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_IN_SECS));
            WebElement frameElement = wait.until(ExpectedConditions.presenceOfElementLocated(frameLocator));
            driver.switchTo().frame(frameElement);
        } catch (NoSuchFrameException e) {
            logStep("Frame not found: " + e.getMessage());
        } catch (InvalidSelectorException e) {
            logStep("Invalid frame selector: " + e.getMessage());
        }
    }

    public static boolean switchFrameByClass(String className) {
        try {
            WebElement frameElement = null;
            for (WebElement iframe : driver.findElements(By.tagName("iframe"))) {
                if (iframe.getAttribute("class").equalsIgnoreCase(className)) {
                    frameElement = iframe;
                    break;
                }
            }

            if (frameElement != null) {
                driver.switchTo().frame(frameElement);
                return true;
            } else {
                logStep("Frame with class '" + className + "' not found.");
                return false;
            }
        } catch (Exception e) {
            logStep("Exception in switchFrameByClass: " + e.getMessage());
            return false;
        }
    }

    public static void quitBrowser() {
        waitForUi(DEFAULT_WAIT_IN_SECS);
        try {
                driver.quit();
        } catch (Exception e) {
            logStep("An exception occurred while quitting the browser: " + e.getMessage());
        }
    }


    /*Drag and drop method*/
    /*--------------------------------------------------------------*/

    public static void performDragAndDrop(WebElement sourceLocator, WebElement targetLocator) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_IN_SECS));

        // Wait for source element to be clickable
        wait.until(ExpectedConditions.elementToBeClickable(sourceLocator));

        Actions actions = new Actions(driver);
        actions.clickAndHold(sourceLocator).perform();

        wait.until(ExpectedConditions.attributeContains(sourceLocator, "draggable", "true"));
        wait.until(ExpectedConditions.presenceOfElementLocated((By) targetLocator));

        actions.moveToElement(targetLocator).release().build().perform();
    }







    public static boolean searchElementByText(int columnIndex, String searchedText) {
        boolean searchResultFound = false;
        int count = 0;

        waitForUi(1); // Custom wait function
        // Find all rows in the table
        List<WebElement> rows = driver.findElements(By.xpath("//div[contains(@class,'rdt_TableRow')]"));

        try {
            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);

                // Locate the column (td/div) within the row by column index
                WebElement tdElement = row.findElement(By.xpath(".//div[@data-column-id='" + columnIndex + "']"));
                String text = tdElement.getText();

                // Check if the text matches the searched text
                if (text.toLowerCase().contains(searchedText.toLowerCase())) {
                    count++;
                    searchResultFound = true;
                }
            }
        } catch (StaleElementReferenceException e) {
            // If StaleElementReferenceException occurs, refresh the rows list and retry
            rows = driver.findElements(By.xpath("//div[contains(@class,'rdt_TableRow')]"));
            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);

                // Reattempt the same search in case of stale element
                WebElement tdElement = row.findElement(By.xpath(".//div[@data-column-id='" + columnIndex + "']"));
                String text = tdElement.getText();
                if (text.toLowerCase().contains(searchedText.toLowerCase())) {
                    count++;
                    searchResultFound = true;
                }
            }
        }

        logStep("Number of matching elements: " + count);
        return searchResultFound;
    }


    public static String getLearnerNameByIndex(int rowIndex, int columnIndex) {
        String learnerName = "";

        waitForUi(1); // Custom wait function

        List<WebElement> rows = driver.findElements(By.xpath("//div[contains(@class,'rdt_TableRow')]"));

        try {
            if (rowIndex < rows.size()) {
                WebElement row = rows.get(rowIndex);
                // Locate the column (td/div) within the row by column index
                WebElement tdElement = row.findElement(By.xpath(".//div[@data-column-id='" + columnIndex + "']"));
                learnerName = tdElement.getText(); // Get the text in the cell

            } else {
                logStep("Row index out of bounds");
            }
        } catch (StaleElementReferenceException e) {
            // If StaleElementReferenceException occurs, refresh the rows list and retry
            rows = driver.findElements(By.xpath("//div[contains(@class,'rdt_TableRow')]"));

            if (rowIndex < rows.size()) {
                WebElement row = rows.get(rowIndex);
                logStep("Row " + rowIndex + " found after retry.");

                // Reattempt to find the element by column index
                WebElement tdElement = row.findElement(By.xpath(".//div[@data-column-id='" + columnIndex + "']"));
                learnerName = tdElement.getText();

                logStep("Column " + columnIndex + " text after retry: " + learnerName);
            } else {
                logStep("Row index out of bounds after retry");
            }
        }

        return learnerName;
    }

}
