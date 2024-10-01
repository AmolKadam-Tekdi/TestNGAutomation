package utils.javautils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class ExtentReportManager {

    public static ExtentReports extent;
    private static ExtentTest test;
    static String reportName;

    public static void initializeReport(String testCaseName) {
        createDirectory("Reports"); // Ensure the Reports directory exists

        reportName = testCaseName + "_" + getCurrentTimestamp();
        String reportPath = "Reports" + File.separator + reportName + ".html"; // Set the default report path

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Extent Report");
        sparkReporter.config().setReportName("Test Automation Report");
        sparkReporter.config().setTheme(Theme.STANDARD);

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
    }

    private static void createDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Directory created: " + directoryPath);
            } else {
                System.out.println("Failed to create directory: " + directoryPath);
            }
        }
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        return sdf.format(new Date());
    }

    public static void setTestCaseName(String testCaseName) {
        test = extent.createTest(testCaseName);
    }

    public static void logStep(String stepDescription) {
        if (test != null) {
            test.log(Status.INFO, stepDescription);
        } else {
            System.out.println("Extent test object is null. Logging step to console: " + stepDescription);
        }
    }

    public static void logPass(String stepDescription) {
        test.log(Status.PASS, MarkupHelper.createLabel(stepDescription, ExtentColor.GREEN));
    }

    public static void logFail(String stepDescription, WebDriver driver) {
        test.log(Status.FAIL, MarkupHelper.createLabel(stepDescription, ExtentColor.RED));
        captureAndAttachScreenshot(driver);
    }

    public static void flushReport() {
        if (extent != null) {
            extent.flush();
        } else {
            System.out.println("ExtentReports object is null. Report cannot be flushed.");
        }
    }

    private static void captureAndAttachScreenshot(WebDriver driver) {
        if (driver instanceof TakesScreenshot) {
            TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
            byte[] screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            String base64Screenshot = "data:image/png;base64," + Base64.getEncoder().encodeToString(screenshotBytes);
            test.fail("Screenshot", MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
        }
    }
}