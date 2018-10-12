Feature: Accession generation

  Scenario Outline: User wants to receive unique accession for their newly submitted / created objects, so they are
  identifiable in the system.
    When user submits <input> to accessioning service
    Then user received accessions: <accessions>
    Examples:
      | input | accessions                             |
      | A     | id-service-A                           |
      | B,C   | id-service-B,id-service-C              |
      | D,E,F | id-service-D,id-service-E,id-service-F |

  Scenario Outline: User wants to receive accessions for a mix of new and pre-existing objects.
    Given already accessioned <objects>
    When user submits <input> to accessioning service
    Then user received accessions: <accessions>
    Examples:
      | objects | input | accessions                             |
      | B       | A     | id-service-A                           |
      | A       | A     | id-service-A                           |
      | C       | B,C   | id-service-B,id-service-C              |
      | A,B,C,E | D,E,F | id-service-D,id-service-E,id-service-F |

  @ignore
  Scenario: User wants to generate accession in a space, based on clustering accessions from another existing space.
    When user provides a list of ss accessions
    Then user receives a rs list of accessions
    And all rs must have at least one ss accessioned