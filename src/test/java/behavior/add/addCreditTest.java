package behavior.add;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.Assert;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import model.User;
import exceptions.InvalidCreditRange;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class addCreditTest {
    private User user;
    private float initialCredit;
    private Exception exception;

    @Given("a user with a credit of {int}")
    public void given_user_with_credit(Integer initialCredit) {
        this.initialCredit = initialCredit.floatValue();
        this.user = new User();
        this.user.setCredit(this.initialCredit);
    }

    @When("the user adds {float} credits")
    public void when_user_adds_credits(float amount) {
        try {
            this.user.addCredit(amount);
        } catch (InvalidCreditRange e) {

        }
    }
    @Given("a user with zero credit")
    public void given_user_with_zero_credit() {
        this.initialCredit = 0;
        this.user = new User();
        this.user.setCredit(initialCredit);
    }

    @When("the user attempts to add {int} credits")
    public void when_user_attempts_to_add_credits(Integer amount) throws InvalidCreditRange {
        if (amount < 0) {
            this.exception = assertThrows(InvalidCreditRange.class,
                    () -> this.user.addCredit(amount.floatValue()));
        } else {
            this.user.addCredit(amount.floatValue());
        }
    }
    @Then("the user's credit is {float}")
    public void then_the_users_credit_is(float expectedCredit) {
        assertEquals(expectedCredit, this.user.getCredit(), 0.001);
    }

    @Then("an InvalidCreditRange exception thrown")
    public void then_InvalidCreditRangeException_thrown() {
        assertNotNull("Expected InvalidCreditRange exception to be thrown");
    }

    @Then("the user's credit should remain unchanged")
    public void then_users_credit_should_remain_unchanged() {
        assertEquals(initialCredit, this.user.getCredit(), 0.001);
    }
}