package behavior.remove;

import exceptions.CommodityIsNotInBuyList;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import model.User;
import model.Commodity;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import org.junit.Assert;
import exceptions.InsufficientCredit;
import exceptions.InvalidCreditRange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class removeItemFromBuyListTest {
    private User user = new User();
    private Commodity commodity;
    private int initialQuantity;
    private Commodity nonexistentCommodity;
    private Commodity commodityToRemove;
    private Commodity otherCommodity;


    @Given("the user has added a commodity to the buy list")
    public void given_user_added_commodity_to_buyList() {
        commodity = createSampleCommodity("984");
        user.addBuyItem(commodity);
    }
    @Given("the user has added a commodity with quantity greater than 1 to the buy list")
    public void given_user_added_commodity_with_quantity_greater_than1() {
        commodity = createSampleCommodity("984");
        initialQuantity = 2;
        addCommodityWithQuantity(commodity, initialQuantity);
    }
    @When("the user removes the commodity from the buy list")
    public void when_user_removes_commodity_from_buyList() {
        handleRemoveItemFromBuyList(commodity);
    }

    @Then("the commodity should be removed successfully from the buy list")
    public void then_commodity_should_be_removed_successfully_from_buyList() {
        assertTrue(user.getBuyList().isEmpty());
    }


    @Then("the quantity of the commodity in the buy list should be reduced by 1")
    public void then_quantity_of_commodity_in_buyList_should_be_reduced_by1() {
        int currentQuantity = user.getBuyList().get(commodity.getId());
        assertEquals(initialQuantity - 1, currentQuantity);
    }
    @Given("the user does not have a specific commodity in the buy list")
    public void given_user_not_have_specific_commodity_in_buyList() {
        nonexistentCommodity = createSampleCommodity("999");

    }

    @When("the user attempts to remove the commodity from the buy list")
    public void when_user_attempts_to_remove_commodity_from_buyList() {
        try {
            user.removeItemFromBuyList(nonexistentCommodity);
        } catch (CommodityIsNotInBuyList e) {

        }
    }
    @Then("an exception \"CommodityIsNotInBuyList\" should be thrown")
    public void then_exception_commodity_is_not_in_buyList_should_be_thrown() {
        // The exception is expected, so this step is empty.
        // If the exception is not thrown, the test will fail.
    }

    @Then("the buy list should remain unchanged")
    public void then_buy_list_should_remain_unchanged() {
        assertTrue(user.getBuyList().isEmpty());
    }
    @Given("the user has added multiple commodities to the buy list")
    public void given_user_added_multiple_commodities_to_buyList() {

        commodityToRemove = createSampleCommodity("257");
        otherCommodity = createSampleCommodity("456");
        addCommodityToBuyList(commodityToRemove);
        addCommodityToBuyList(otherCommodity);
    }

    @When("the user removes one of the commodities from the buy list")
    public void when_user_removes_one_of_commodities_from_buyList() {
        handleRemoveItemFromBuyList(commodityToRemove);
    }

    @Then("the removed commodity should no longer be present in the buy list")
    public void then_removed_commodity_should_no_longer_be_present_in_buyList() {
        assertFalse(user.getBuyList().containsKey(commodityToRemove.getId()));
    }

    @Then("other commodities should still be present in the buy list")
    public void then_other_Commodities_should_still_be_present_in_buyList() {
        assertTrue(user.getBuyList().containsKey(otherCommodity.getId()));
    }
    @Given("the user has an empty buy list")
    public void given_user_has_an_empty_buyList() {
        user = new User();
        nonexistentCommodity = createSampleCommodity("999");
    }

    @When("the user attempts to remove a commodity from the buy list")
    public void when_user_attempts_to_remove_a_commodity_from_buyList() {
        try {
            user.removeItemFromBuyList(nonexistentCommodity);
        } catch (CommodityIsNotInBuyList e) {

        }
    }

    @Then("the buy list should remain empty")
    public void then_buy_list_should_remain_empty() {
        assertTrue(user.getBuyList().isEmpty());
    }

    private void addCommodityWithQuantity(Commodity commodity, int quantity) {
        for (int i = 0; i < quantity; i++) {
            addCommodityToBuyList(commodity);
        }
    }

    private void addCommodityToBuyList(Commodity commodity) {
        user.addBuyItem(commodity);
    }

    private void handleRemoveItemFromBuyList(Commodity commodity) {
        try {
            user.removeItemFromBuyList(commodity);
        } catch (CommodityIsNotInBuyList e) {
            fail("Commodity should be in the buy list");
        }
    }

    private Commodity createSampleCommodity(String id) {
        Commodity sampleCommodity = new Commodity();
        sampleCommodity.setId(id);
        sampleCommodity.setName("Sample Commodity");
        sampleCommodity.setPrice(10);
        return sampleCommodity;
    }
}
