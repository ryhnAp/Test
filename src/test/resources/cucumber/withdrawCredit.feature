Feature: user withdraw credit

  Scenario: Successful credit withdraw
    Given a user with credit of 200
    When the user withdraws 50 credits
    Then the user's credit should be 150

  Scenario: Insufficient Credit from withdraw
    Given a user with credit of 150
    When the user withdraws 250 credits
    Then an InsufficientCredit exception should be thrown

  Scenario: Negative Credit Withdraw
    Given a user with credit of 60
    When the user wants to withdraw -34 credits
    Then an unexpected result occurs

  Scenario: Invalid range for credit withdraw
    Given a user with credit of 60
    When the user withdraws -34 credits
    Then an unexpected result occurs
