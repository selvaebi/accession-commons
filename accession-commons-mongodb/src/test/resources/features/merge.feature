Feature: Accession merge

  Scenario: I want to merge an object but the destination object does not exist.
    Given already accessioned A
    When user merges id-service-A into id-service-B reason: because i say so
    Then user should receive 'accession does not exist'

  Scenario: I want to merge an object but the origin object does not exist.
    Given already accessioned B
    When user merges id-service-A into id-service-B reason: because i say so
    Then user should receive 'accession does not exist'

  Scenario: I want to merge an object but none of the two exist.
    When user merges id-service-A into id-service-B reason: because i say so
    Then user should receive 'accession does not exist'

  Scenario: I want to merge an object
    Given already accessioned A
    And already accessioned B
    When user merges id-service-A into id-service-B reason: because i say so
    Then operation finished correctly

  Scenario: I want to merge an object but the destination object has been already merged
    Given already accessioned A
    And already accessioned B
    And already accessioned C
    And user merges id-service-B into id-service-C reason: because i say so
    When user merges id-service-A into id-service-B reason: because i say so
    Then user should receive 'accession already merged exception'

  Scenario: I want to merge an object but the origin object has been already merged
    Given already accessioned A
    And already accessioned B
    And already accessioned C
    And user merges id-service-B into id-service-C reason: because i say so
    When user merges id-service-B into id-service-A reason: because i say so
    Then user should receive 'accession already merged exception'

  Scenario: I want to merge an object to another and then to a third one
    Given already accessioned A
    And already accessioned B
    And already accessioned C
    When user merges id-service-A into id-service-B reason: because i say so
    When user merges id-service-B into id-service-C reason: because i say so
    Then operation finished correctly

  Scenario: I want to merge an object but the origin object has been deprecated
    Given already accessioned A
    And already accessioned B
    And user deprecates id-service-A reason: because i say so
    When user merges id-service-A into id-service-B reason: because i say so
    Then user should receive 'accession has been deprecated exception'

  Scenario: I want to merge an object but the destination object has been deprecated
    Given already accessioned A
    And already accessioned B
    And user deprecates id-service-A reason: because i say so
    When user merges id-service-B into id-service-A reason: because i say so
    Then user should receive 'accession has been deprecated exception'

  Scenario: I want to merge an object but both origin and destination object has been deprecated
    Given already accessioned A
    And already accessioned B
    And user deprecates id-service-A reason: because i say so
    And user deprecates id-service-B reason: because i say so
    When user merges id-service-A into id-service-B reason: because i say so
    Then user should receive 'accession has been deprecated exception'