Feature: Accession modification

  Scenario Outline: I want to create a new patch of an existing object, in order to register improvements made to it
  without losing access to the old information.
    Given already accessioned <objects>
    When user sends patch <input> for accession <accession>
    Then user should receive version list for <accession> with 2 version
    And patch must have versions increased
    Examples:
      | objects | input | accession    |
      | A       | AA    | id-service-A |
      | B       | BB    | id-service-B |
      | A,B,C,E | AA    | id-service-A |

  Scenario: I want to create a new patch of a non existing object
    When user sends patch AA for accession id-service-A
    Then user receives accession does not exist

  Scenario Outline: I want to update the fields that identify uniquely an object.
  without losing access to the old information.
    Given already accessioned <objects>
    When user updates <accession> patch <patch> with <input>
    Then user should receive version list for <accession> with 1 version
    And hash of version 1 should be <hash>
    Examples:
      | objects | input | accession    | patch | hash                                     |
      | A       | AA    | id-service-A | 1     | 801C34269F74ED383FC97DE33604B8A905ADB635 |
      | B       | BB    | id-service-B | 1     | 71C9DB717578B9EE49A59E69375C16C0627DFFEF |
      | A,B,C,E | AA    | id-service-A | 1     | 801C34269F74ED383FC97DE33604B8A905ADB635 |

  Scenario: I want to update the fields that identify uniquely an object, but we already have an object with the
  same values.
    Given already accessioned A,B
    When user updates id-service-A patch 1 with B
    Then user receives an unknown error

  Scenario: I want to update an object that does not exist
    When  user updates id-service-A patch 1 with AB
    Then user receives accession does not exist

