package Pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import utils.javautils.BaseUtils;

public class EGLoginPage extends BaseUtils {

    public EGLoginPage() {
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//*[@placeholder='Enter Username']")
    public WebElement usernameField;

    @FindBy(xpath = "//*[@placeholder='Enter Password']")
    public WebElement passwordField;

    @FindBy(xpath = "(//*[text()='Login'])[4]")
    public WebElement loginButton;

    @FindBy(xpath = "//*[text()='Academic Year']")
    public WebElement AcademicYear;

    @FindBy(xpath = "//*[text()='Continue']")
    public WebElement Continue;

    @FindBy(xpath = "(//*[@aria-label='Select'])[1]")
    public WebElement SelectAcademicYear;

    @FindBy(xpath = "(//*[@aria-label='Select'])[2]")
    public WebElement SelectState;


    public void enterUsername(String username) throws Exception {
        sendTextOnUI( usernameField,username);
    }

    public void enterPassword(String password) {
        logStep("the user enters the password {string}");
        sendTextOnUI( passwordField,password);
    }

    public void clickonLoginButton() {
        click(loginButton);
    }


    public void selectAcademicYear(String string) {
        Select sel = new Select(SelectAcademicYear);
        sel.selectByVisibleText(string);
    }

    public void selectState(String string) {
        Select sel = new Select(SelectState);
        sel.selectByVisibleText(string);
    }

    public void clickOnContinueButton() {
        click(Continue);
    }

    public boolean isEnabled(String element)
    {
        switch (element)
        {
            case "usernameField" : return isElementEnabled(usernameField);
            case "passwordField" : return isElementEnabled(passwordField);
            case "loginButton" : return isElementEnabled(loginButton);
            default: logStep("Invalid Locator" + element);
        }
        return false;
    }

    public String getTextfromUI(String element)
    {
        switch (element)
        {
            case "AcademicYear" : return AcademicYear.getText();
        }
        return null;
    }

    public boolean isDisplayed(String element)
    {
        switch (element)
        {
            case "AcademicYeartext" : return isElementDisplayed(AcademicYear);
            default: logStep("Invalid Locator" + element);
        }
        return false;
    }


}
