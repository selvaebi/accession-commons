# amp-t2d-accession
Generic library for accessioning any object type. It provides the core interfaces to generate hash-based and monotonically increasing accessions, which must be extended by each application to support a particular object type.

## How does it work
A set of objects must be passed to the accessioning service layer. This service will generate a summary with the relevant fields of the object, and then a hash value will be calculated based on it. This hash will be used as a key to check if the object has been previously accessioned; if it already exists in the system, the existing accession will be returned for that object, otherwise a new accession will be generated and stored in the database. Finally, the user will receive a mapping between accessions/objects.

## Accessioning service
An accessioning service requires the following:

- Summary function
- Hash function
- Accession generator service
- Database service layer

### Summary function
A summary function is a Java lambda or `Function` implementation from your object model to a string. It is meant to generate the minimum text string that uniquely identifies your object model.

### Hash function
The hash function is a Java lambda or `Function` implementation from the summary string to your preferred hash representation (which could also be a string). This hash value will be used as unique key for your object. The library already contains a SHA1 implementation.

## Accession generator service
Service to generate accessions. It will receive a map of hash/model and will generate an accession for each pair creating a list of triplets accession/hash/model. The generated accession will depend on the specific implementation. The library currently has a timestamp based accession, accession hashed from the model and monotonic accession implementations.

## Database service
This service will handle the database persistence of the triplet accession/hash/model and the related queries offered through the accessioning service. Currently in the library there is a basic implementation that supports the use of Spring Data repositories / entities.
 
## REST interface
Via the `BasicAccessionGeneratorRestController` class, the library offers a basic implementation of the REST andpoints to generate accessions and query the existing ones. This implementation also allows to use the validation of the rest message against a DTO that represents your model.
