Feature: Accession retrieval

  Scenario: I want to get the accession of an object that is not in the system.
    When user retrieves objects: A
    Then user receives 0 elements

  Scenario: I want to get the accession of an object that it is on the system.
    Given already accessioned A
    When user retrieves objects: A
    Then user receives 1 elements
    And user received a response with values: A
    And user received accessions: id-service-A

  Scenario: I want to get a specific accession that doesn't exist in the system.
    When user retrieves accessions: id-service-A
    Then user should receive 'accession does not exist'

  Scenario: I want to retrieve an accession that exists in the system
    Given already accessioned A
    When user retrieves accessions: id-service-A
    Then user receives 1 elements
    And user received a response with values: A
    And user received accessions: id-service-A