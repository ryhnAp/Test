Feature: Removing Items from Buy list

  Scenario: Successfully Removing an Existing Item from Buy List
    Given the user has added a commodity to the buy list
    When the user removes the commodity from the buy list
    Then the commodity should be removed successfully from the buy list

  Scenario: Reducing Quantity of an Item in Buy List
    Given the user has added a commodity with quantity greater than 1 to the buy list
    When the user removes the commodity from the buy list
    Then the quantity of the commodity in the buy list should be reduced by 1

  Scenario: Attempting to Remove a Nonexistent Item from Buy List
    Given the user does not have a specific commodity in the buy list
    When the user attempts to remove the commodity from the buy list
    Then an exception "CommodityIsNotInBuyList" should be thrown
    And the buy list should remain unchanged

  Scenario: Removing a Single Item from Multiple Items in Buy List
    Given the user has added multiple commodities to the buy list
    When the user removes one of the commodities from the buy list
    Then the removed commodity should no longer be present in the buy list

  Scenario: Attempting to Remove an Item from an Empty Buy List
    Given the user has an empty buy list
    When the user attempts to remove a commodity from the buy list
    Then an exception "CommodityIsNotInBuyList" should be thrown
