Feature: Accession deprecation

  Scenario: I want to deprecate an object but the object does not exist.
    When user deprecates unknown-accession reason: because i say so
    Then user should receive 'accession does not exist'

  Scenario: I want to deprecate an object.
    Given already accessioned A
    When user deprecates id-service-A reason: because i say so
    Then operation finished correctly

  Scenario: I want to deprecate an object but the object has been already merged.
    Given already accessioned A
    And already accessioned B
    And user merges id-service-A into id-service-B reason: because i say so
    When user deprecates id-service-A reason: because i say so
    Then user should receive 'accession already merged exception'

  Scenario: I want to deprecate an object but the object has been already deprecated.
    Given already accessioned A
    And user deprecates id-service-A reason: because i say so
    When user deprecates id-service-A reason: because i say so
    Then user should receive 'accession has been deprecated exception'

  Scenario: I want to retrieve an accession but the object has been already deprecated.
    Given already accessioned A
    And user deprecates id-service-A reason: because i say so
    When user retrieves accessions: id-service-A
    Then user should receive 'accession has been deprecated exception'