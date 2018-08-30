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

  Scenario: I want to get accessions of a mixture of objects that may or not be on the system.
    Given already accessioned A
    Given already accessioned C
    Given already accessioned E
    When user retrieves objects: A,B,C,D,E
    Then user receives 3 elements
    And user received a response with values: A,C,E
    And user received accessions: id-service-A,id-service-C,id-service-E

  Scenario: I want to get a specific accession that doesn't exist in the system.
    When user retrieves accessions: id-service-A
    Then user should receive 'accession does not exist'

  Scenario: I want to retrieve an accession that exists in the system
    Given already accessioned A
    Given already accessioned B
    When user retrieves accessions: id-service-A,id-service-B
    Then user receives 2 elements
    And user received a response with values: A,B
    And user received accessions: id-service-A,id-service-B