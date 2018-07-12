Feature: Accession merge

  Scenario: I want to merge an object but the destination object does not exist.
    Given already accessioned A
    When user merges id-service-A with id-service-B reason: because i say so
    Then user receives a hash already exists exception

  Scenario: I want to merge an object but the origin object does not exist.
    Given already accessioned B
    When user merges id-service-A with id-service-B reason: because i say so
    Then user receives a hash already exists exception

  Scenario: I want to merge an object but none of the two exist.
    When user merges id-service-A with id-service-B reason: because i say so
    Then user receives a hash already exists exception

  Scenario: I want to merge an object
    Given already accessioned A
    And already accessioned B
    When user merges id-service-A with id-service-B reason: because i say so
    Then user operation finished correctly

  Scenario: I want to merge an object but the object has been already merged
    Given already accessioned A
    And already accessioned B
    And already accessioned C
    And user merges id-service-B with id-service-C reason: because i say so
    When user merges id-service-A with id-service-B reason: because i say so
    Then user receives an accession already merged error

  Scenario: I want to merge an object to another and then to a third one
    Given already accessioned A
    And already accessioned B
    And already accessioned C
    When user merges id-service-A with id-service-B reason: because i say so
    When user merges id-service-B with id-service-C reason: because i say so
    Then user operation finished correctly