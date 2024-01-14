Feature: User Credit Management

  Scenario: Add credit to the user's account
    Given a user with a credit of 100
    When the user adds 50 credits
    Then the user's credit is 150


  Scenario: Add credit successfully with zero initial credit
    Given a user with zero credit
    When the user adds 50 credits
    Then the user's credit is 50

  Scenario: Attempt to add zero credit
    Given a user with a credit of 100
    When the user attempts to add 0 credits
    Then the user's credit should remain unchanged

  Scenario: Attempt to add negative credit
    Given a user with a credit of 150
    When the user attempts to add -50 credits
    Then an InvalidCreditRange exception thrown
