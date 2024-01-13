package KatalonRecorder;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class Credit {
  private WebDriver driver;
  private String baseUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();

  @Before
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    baseUrl = "https://www.google.com/";
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testCredit() throws Exception {
    driver.get("http://localhost:8080/swagger-ui/index.html#/");
    driver.get("http://localhost:8080/swagger-ui/index.html#/");
    driver.findElement(By.cssSelector("button.opblock-summary-control > svg.arrow > use")).click();
    driver.findElement(By.xpath("//div[@id='operations-user-controller-addCredit']/div[2]/div/div/div/div[2]/button")).click();
    driver.findElement(By.xpath("//input[@value='']")).click();
    driver.findElement(By.xpath("//input[@value='ryhnap']")).clear();
    driver.findElement(By.xpath("//input[@value='ryhnap']")).sendKeys("ryhnap");
    driver.findElement(By.xpath("//div[@id='operations-user-controller-addCredit']/div[2]/div/div/div[3]/div[2]/div/div/div/textarea")).click();
    driver.findElement(By.xpath("//div[@id='operations-user-controller-addCredit']/div[2]/div/div/div[3]/div[2]/div/div/div/textarea")).clear();
    driver.findElement(By.xpath("//div[@id='operations-user-controller-addCredit']/div[2]/div/div/div[3]/div[2]/div/div/div/textarea")).sendKeys("{\n  \"credit\": 100\n}");
    driver.findElement(By.xpath("//div[@id='operations-user-controller-addCredit']/div[2]/div/div[2]/button")).click();
    driver.findElement(By.xpath("//div[@id='operations-user-controller-addCredit']/div[2]/div/div[2]/button[2]")).click();
    driver.findElement(By.xpath("//div[@id='operations-user-controller-addCredit']/div[2]/div/div/div/div[2]/button[2]")).click();
    driver.findElement(By.xpath("//div[@id='operations-user-controller-addCredit']/div[2]/div/div/div/div[2]/button")).click();
    driver.findElement(By.cssSelector("button.opblock-summary-control > svg.arrow")).click();
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  private boolean isAlertPresent() {
    try {
      driver.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }

  private String closeAlertAndGetItsText() {
    try {
      Alert alert = driver.switchTo().alert();
      String alertText = alert.getText();
      if (acceptNextAlert) {
        alert.accept();
      } else {
        alert.dismiss();
      }
      return alertText;
    } finally {
      acceptNextAlert = true;
    }
  }
}
