package runner;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/cucumber/removeItemFromBuyList.feature" ,
        glue = "behavior.remove"
)
public class removeItemFromBuyListTestRunner {

}
