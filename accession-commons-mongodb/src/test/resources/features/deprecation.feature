Feature: Accession deprecation

  Scenario: User wants to deprecate an object but the object does not exist.
    When user deprecates unknown-accession reason: because user says so
    Then user should receive 'accession does not exist'

  Scenario: User wants to deprecate an object.
    Given already accessioned A
    When user deprecates id-service-A reason: because user says so
    Then operation finished correctly
    When user retrieves accessions: id-service-A
    Then user should receive 'accession has been deprecated exception'

  Scenario: User wants to deprecate an object but the object has been already merged.
    Given already accessioned A
    And already accessioned B
    And user merges id-service-A into id-service-B reason: because user says so
    When user deprecates id-service-A reason: because user says so
    Then user should receive 'accession already merged exception'

  Scenario: User wants to deprecate an object that has been updated.
    Given already accessioned A
    And user updates id-service-A patch 1 with AA
    When user deprecates id-service-A reason: because user says so
    Then operation finished correctly
    When user retrieves accessions: id-service-A
    Then user should receive 'accession has been deprecated exception'

  Scenario: User wants to deprecate an object that has been patched.
    Given already accessioned A
    And user sends patch AAB for accession id-service-A
    When user deprecates id-service-A reason: because user says so
    Then operation finished correctly
    When user retrieves accessions: id-service-A
    Then user should receive 'accession has been deprecated exception'

  Scenario: User wants to deprecate an object but the object has been already deprecated.
    Given already accessioned A
    And user deprecates id-service-A reason: because user says so
    When user deprecates id-service-A reason: because user says so
    Then user should receive 'accession has been deprecated exception'

  Scenario: User wants to retrieve an accession but the object has been already deprecated.
    Given already accessioned A
    And user deprecates id-service-A reason: because user says so
    When user retrieves accessions: id-service-A
    Then user should receive 'accession has been deprecated exception'