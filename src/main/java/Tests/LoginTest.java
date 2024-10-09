package Tests;//package Tests;

import Pages.EGLoginPage;
import Pages.LoginPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.annotations.Test;
import utils.javautils.BaseTest;

import java.util.concurrent.TimeUnit;

public class LoginTest extends BaseTest {

    static String username = "tekdi_admin";
    static String password = "4he!KY3#cIPE";
    @Test
    public static void loginTest() throws Exception {
        LoginPage lp = PageFactory.initElements(driver, LoginPage.class);
        EGLoginPage egl = PageFactory.initElements(driver,EGLoginPage.class);

        logStep("Enter Username");
        egl.enterUsername(username);
        assertTrue("Username field is enabled", egl.isEnabled("usernameField"),"Username field is not enablled");


        logStep("Enter password");
        assertTrue("password field is enabled", egl.isEnabled("passwordField"),"password field is not enablled");
        egl.enterPassword(password);


        logStep("Click on the Login Button");
        assertTrue("Login Button is enabled", egl.isEnabled("loginButton"),"Login Button is not enablled");
        egl.clickonLoginButton();


        waitForUI(driver,5);

        assertTrue("User Logged in successfully",egl.isDisplayed("AcademicYeartext"),"User not able to log into the system");


    }
}
