Feature: Accession History

  Scenario: User wants to find the history of accession which is not created yet
    When user search history of unknown-accession
    Then user receives accession does not exist exception

  Scenario: User wants to find the history of accession just been created
    Given already accessioned A
    When user search history of id-service-A
    Then search returns total number of events as 1
    And user should receive history events in order :CREATED

  Scenario: User wants to find the history of accession just been updated
    Given already accessioned A
    And user updates id-service-A patch 1 with AA
    When user search history of id-service-A
    Then search returns total number of events as 2
    And user should receive history events in order :CREATED,UPDATED

  Scenario: User wants to find the history of accession just been merged
    Given already accessioned A
    And already accessioned B
    And user merges id-service-A into id-service-B reason: because user says so
    When user search history of id-service-A
    Then search returns total number of events as 2
    And user should receive history events in order :CREATED,MERGED
    When user search history of id-service-B
    Then search returns total number of events as 1
    And user should receive history events in order :CREATED

  Scenario: User wants to find the history of accession which undergone series of events
    Given already accessioned A
    And user updates id-service-A patch 1 with AA
    And user sends patch AAB for accession id-service-A
    And user deprecates id-service-A reason: because user says so
    When user search history of id-service-A
    Then search returns total number of events as 4
    And user should receive history events in order :CREATED,UPDATED,PATCHED,DEPRECATED