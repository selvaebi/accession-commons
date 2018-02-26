# amp-t2d-accession
Generic library for accessioning any object type. It provides the core interfaces to generate hash-based and monotonically increasing accessions, which must be extended by each application to support a particular object type.

## How does it work
A set of objects must be passed to the accessioning service layer. This service will generate a summary with the relevant fields of the object, and then a hash value will be calculated based on it. This hash will be used as a key to check if the object has been previously accessioned; if it already exists in the system, the existing accession will be returned for that object, otherwise a new accession will be generated and stored in the database. Finally, the user will receive a mapping between accessions/objects.

The following sections describe the components involved in the process, starting from the top layer the user interacts with, to the lowest layer where the data is stored.

## REST API
Via the `BasicAccessionGeneratorRestController` class, the library offers a basic implementation of the REST andpoints to generate accessions and query the existing ones. This implementation also allows to use the validation of the rest message against a DTO that represents your model.

## Accessioning service
This layer acts as intermediary between user interfaces and the lower layers that generate and store the accessions. An accessioning service requires the following:

- Summary function
- Hash function
- Accession generator service
- Database service layer

### Summary function
A summary function is a Java lambda or `Function` implementation from your object model to a string. It is meant to generate the minimum text string that uniquely identifies your object model.

### Hash function
The hash function is a Java lambda or `Function` implementation from the summary string to your preferred hash representation (which could also be a string). This hash value will be used as unique key for your object. The library already contains a SHA1 implementation.

## Accession generator service
Service layer that generates accessions. It receives a mapping (hash, model) and generates an accession for each pair, resulting in a triplet (accession, hash, model). The generated accession will depend on the specific implementation. The library currently includes a timestamp based, hash based and monotonic accession implementations.

## Database service
This service layer handles the database persistence of the triplet (accession, hash, model) and the related queries offered through the accessioning service. The library currently includes a basic implementation that supports the use of Spring Data repositories and entities.
