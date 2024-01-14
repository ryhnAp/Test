package behavior.withdraw;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import model.User;
import org.junit.Assert;
import exceptions.InsufficientCredit;
import exceptions.InvalidCreditRange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class withdrawCreditTest {
    private User user;
    private Exception exception;

    @Given("a user with credit of {int}")
    public void given_user_with_credit_of(Integer initialCredit) {
        user = new User("username", "password", "email", "birthDate", "address");
        user.setCredit(initialCredit.floatValue());
    }

    @When("the user withdraws {int} credits")
    public void when_user_withdraws_credits(Integer withdrawalAmount) {
        handleWithdrawal(withdrawalAmount.floatValue());
    }
    @When("the user wants to withdraw {int} credits")
    public void when_user_wants_to_withdraw_credits(Integer withdrawalAmount) {
        handleWithdrawal(withdrawalAmount.floatValue());
    }
    private void handleWithdrawal(float withdrawalAmount) {
        try {
            user.withdrawCredit(withdrawalAmount);
        } catch (Exception e) {
            exception = e;
        }
    }
    @Then("the user's credit should be {float}")
    public void then_users_credit_should_be(float expectedCredit) {
        assertEquals(expectedCredit, user.getCredit(), 0.001);
    }

    @Then("an InsufficientCredit exception should be thrown")
    public void then_InsufficientCreditException_should_be_thrown() {
        assertTrue(exception instanceof InsufficientCredit, "Expected InsufficientCredit exception");
    }


    @Then("the withdrawal amount is negative")
    public void then_withdrawal_amount_is_negative() {
        Assert.assertTrue("Expected withdrawal amount to be negative", exception instanceof InvalidCreditRange);
    }

    @Then("an InvalidCreditRange exception should be thrown")
    public void then_Invalid_Credit_Range_Exception_should_be_thrown() {
        assertTrue(exception instanceof InvalidCreditRange, "Expected InvalidCreditRange exception");

    }

    @Then("an unexpected result occurs")
    public void then_unexpected_result_occurs() {
        Assert.assertNull("Expected an unexpected result, but the operation did not throw an exception", exception);
    }

    @Then("an InvalidCreditRange exception should not be thrown")
    public void then_InvalidCreditRangeException_should_not_be_thrown() {
        Assert.assertNull("Unexpected InvalidCreditRange exception thrown", exception);
    }

}
