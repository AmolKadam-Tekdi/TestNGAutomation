package Pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.javautils.BaseUtils;
import utils.javautils.PropertiesFileManager;

public class LoginPage extends BaseUtils {

    public LoginPage(WebDriver driver) {
        BaseUtils.driver = driver;
    }

    static String invalidLocator = "Invalid locator: ";
    PropertiesFileManager loader = PropertiesFileManager.getInstance();
    static String locatorsPath=System.getProperty("user.dir")+"/src/test/java/resources/locators/";

    static {
        PropertiesFileManager.getInstance().setPath(locatorsPath);
    }


    @FindBy(xpath = "(//*[@title='Login'])[1]")
    public WebElement Login;

    @FindBy(xpath = "(//*[text()=' LOGIN '])[3]")
    public WebElement passwordLoginButton;

    @FindBy(xpath = "(//*[@class='role-card-content'])[1]")
    public WebElement Learner;

    @FindBy(xpath = "//*[@formcontrolname='PhoneNumber']")
    public WebElement PhoneNumber;

    @FindBy(xpath = "//*[@class='mat-checkbox-inner-container']")
    public WebElement TermsAndConditionCheckbox;

    @FindBy(xpath = "//*[text()=' Continue ']")
    public WebElement ContinueButton;

    @FindBy(id = "password")
    public WebElement Password;

    @FindBy(id = "login")
    public WebElement LoginButton;

    @FindBy(xpath = "//*[text()='National Urban Digital Mission']")
    public WebElement FooterText;

    @FindBy(xpath = "//*[@class='cursor-pointer']")
    public WebElement HomeLogo;


    public void clickonLoginButton() { click(Login); }

    public void clickOnLearnerCard() { click(Learner); }

    public void enterPhoneNumber(String phoneNumber)
    {
        sendTextOnUI(PhoneNumber,phoneNumber);
    }

    public void clickOnAccepptCheckbox() { click(TermsAndConditionCheckbox); }

    public void clickOnContinueButton() { click(ContinueButton); }

    public void enterPassword(String password)
    {
        sendTextOnUI(Password,password);
    }

    public void clickOnLoginButton() { click(passwordLoginButton); }



    public String getTextfromUI(String locator)
    {
        switch(locator)
        {
            case "Footer_Text" : return FooterText.getText();
            default: logStep("Invalid locator");
        }
        return locator;
    }


    public WebElement getElement(String locator) {
        switch (locator) {
            case "Footer_Text" : waitForVisibility(FooterText,5);
            default : logStep("Invalid locator: " + locator);
        }
        return null;
    }

    public boolean IsDisplayed(String Locator)
    {
        switch(Locator)
        {
            case "HomeBUtton" : return isElementDisplayed(FooterText);
            case "HomeLogo" : return isElementDisplayed(HomeLogo);
            default: logStep(invalidLocator + Locator);
        }
        return false;
    }

    public boolean isEnabled(String locator)
    {
        switch (locator)
        {
            case "loginButton" : return isElementEnabled(Login);
            case "Learner" : return isElementEnabled(Learner);
            case "PhoneNumber" : return isElementEnabled(PhoneNumber);
            case "TermsAndConditionCheckbox" : return isElementEnabled(TermsAndConditionCheckbox);
            case "ContinueButton" : return isElementEnabled(ContinueButton);
            case "passwordLoginButton" : return isElementEnabled(passwordLoginButton);
            case "Password" : return isElementEnabled(Password);
            case "LoginButton" : return isElementEnabled(LoginButton);
        default : logStep(invalidLocator + locator);
        }
        return false;
    }

}
