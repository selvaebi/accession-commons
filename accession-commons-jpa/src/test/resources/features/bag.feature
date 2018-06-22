Feature: Accession generation

  Scenario Outline: I want to receive unique accession for my newly submitted / created objects, so they are
  identifiable in the system.
    When I submit <objects> to accessioning service
    Then I should receive accessions <accessions>
    Examples:
      | objects | accessions                             |
      | A       | id-service-A                           |
      | B,C     | id-service-B,id-service-C              |
      | D,E,F   | id-service-D,id-service-E,id-service-F |


  Scenario: Putting few things in the bag
    Given the bag is empty
    When I put 1 potato in the bag
    And I put 2 cucumber in the bag
    Then the bag should contain 1 potato
    And the bag should contain 2 cucumber