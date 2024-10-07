package utils.javautils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.observer.ExtentObserver;
import io.cucumber.java.Before;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.baseutils.BrowserManager;
import org.testng.ITestContext;


import java.lang.reflect.Method;

public class BaseTest extends BrowserManager
{


    private static boolean isSuiteInitialized = false;
    private static ExtentReports extent;
    private static ExtentTest suiteTest;

    @BeforeSuite
    public void setUpSuite(ITestContext context) throws Exception {
        if (!isSuiteInitialized) {
            // Initialize the ExtentReports object
            ExtentReports htmlReporter = new ExtentReports();
            extent = new ExtentReports();
            Reporter.setupReport();

            suiteTest = extent.createTest("TestSuite"); // Create a suite test instance

            String suiteName = context.getSuite().getName();  // Get the suite name
            logStep("Running the Test Suite: " + suiteName);
            isSuiteInitialized = true;
        }
    }





//    private static boolean isSuiteInitialized = false;
//
//    @BeforeSuite
//    public void setUpSuite(ITestContext context) throws Exception {
//        if (!isSuiteInitialized) {
//            String suiteName = context.getSuite().getName();  // Get the suite name
//            Reporter.setupReport(suiteName);
//            isSuiteInitialized = true;
//        }
//    }

    @BeforeMethod(alwaysRun = true)
    @Parameters("localOrRemote")
    public void setUpLog(Method method, @Optional("local") String localOrRemote) throws Exception {
        LoggerUtil.setLogFileName(method.getName());
//        Reporter.setupReport(method.getName());
        Reporter.createTest(method.getName());
        browserRun();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) throws Exception {
        logResultStatus(result);
        waitForUi(2);
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
