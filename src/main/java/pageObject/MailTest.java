package pageObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pageObject.pages.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MailTest {
    private WebDriver driver;
    private DraftsFolderPage drafts;
    private SentFolderPage sentPage;

    @BeforeClass(description = "Start browser")
    private void initBrowser() {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(25, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        drafts = new DraftsFolderPage(driver);
        sentPage = new SentFolderPage(driver);
    }

    @AfterClass
    private void tearDown() {
        sentPage.logout();
        driver.close();
    }

    @Test(description = "Login test")
    public void loginTest() {
        InboxPage inbox = new HomePage(driver).open().inputUsername("new_account_2018").inputPassword("password2018").chooseDomain().signIn();
        //TODO: wait for presence
        Assert.assertTrue(inbox.isElementPresent(InboxPage.USER_EMAIL_LOCATOR));
        inbox.openWriteNewMail();
    }

    @Test(dependsOnMethods = "loginTest")
    public void saveNewMailTest() {
        CreateNewMailPage newMail = new CreateNewMailPage(driver);
        newMail.fillAddressee("ayzhan7797@mail.ru");
        newMail.fillSubject("test(module 5)");
        newMail.fillBody("Hello!");
        newMail.saveDraft();
        Assert.assertTrue(newMail.isElementPresent(CreateNewMailPage.SAVED_LOCATOR));
        newMail.openDraftsFolder();
    }

    @Test(dependsOnMethods = "saveNewMailTest")
    public void testAddressee() {
        Assert.assertTrue(drafts.isElementPresent(DraftsFolderPage.FILLED_ADDRESSEE_LOCATOR));
    }

    @Test(dependsOnMethods = "testAddressee")
    public void testSubject() {
        drafts.openMail();
        Assert.assertTrue(drafts.isElementPresent(DraftsFolderPage.FILLED_SUBJECT_LOCATOR));
    }

    @Test(dependsOnMethods = "testSubject")
    public void testContent() {
        driver.switchTo().frame(0);
        Assert.assertTrue(drafts.isElementPresent(DraftsFolderPage.FILLED_BODY_LOCATOR));
    }

    @Test(dependsOnMethods = "testContent")
    public void sendMailTest() {
        driver.switchTo().defaultContent();
        drafts.sendMail();
        drafts.openDraftsFolder();
        driver.navigate().refresh();
        List<WebElement> selects = driver.findElements(DraftsFolderPage.DATALIST_LOCATOR);
        boolean subj = false;
        try {
            for (WebElement select : selects) {
                subj = (select.getText().contains("test(module 5)"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertFalse(subj);
    }

    @Test(dependsOnMethods = "sendMailTest")
    public void sentFolderTest() {
        drafts.openSentFolder();
        List<WebElement> selects = driver.findElements(SentFolderPage.DATALIST_LOCATOR);
        boolean subj = true;
        try {
            for (WebElement select : selects) {
                subj = (select.getText().contains("test(module 5)"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(subj);
    }
}
