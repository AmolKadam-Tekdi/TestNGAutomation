package utils.javautils;

import com.aventstack.extentreports.ExtentReports;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.BrowserManager.BrowserManager;
import org.testng.ITestContext;


import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class BaseTest extends BrowserManager
{

    private static boolean isSuiteInitialized = false;

    @BeforeSuite
    public void setUpSuite(ITestContext context) throws Exception {
        logStep("Current working directory: " + System.getProperty("user.dir"));

        if (!isSuiteInitialized) {
            String suiteName = context.getSuite().getName();  // Get the suite name
            Reporter.setupReport(suiteName);
            isSuiteInitialized = true;
        }
    }



    @BeforeMethod(alwaysRun = true)
    @Parameters("localOrRemote")
    public void setUpLog(Method method, @Optional("local") String localOrRemote) throws Exception {
        LoggerUtil.setLogFileName(method.getName());
        Reporter.createTest(method.getName());
        browserRun();
        long timeout = 10;
        TimeUnit unit = null;
        try {
            setImplicitWait(10, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            driver
                    .manage()
                    .timeouts()
                    .implicitlyWait(timeout, unit == null ? TimeUnit.SECONDS : unit);
        }
    }




    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) throws Exception {
        logResultStatus(result);
        waitForUi(2);
        driver.quit();
    }

    private void logResultStatus(ITestResult result) throws Exception {
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                Reporter.logPass("**** " + result.getName() + " has PASSED ****");
                break;
            case ITestResult.FAILURE:
                fail("**** " + result.getName() + " has FAILED ****");
                Reporter.logFail("**** " + result.getName() + " has FAILED ****", driver);
                break;
            case ITestResult.SKIP:
                logStep("**** " + result.getName() + " has been SKIPPED ****");
                Reporter.logStep("**** " + result.getName() + " has been SKIPPED ****");
                break;
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        Reporter.flushReport();
    }


}
