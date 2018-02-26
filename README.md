# amp-t2d-accession
Generic service for accessioning of any type of object. 

## How does it work
A series of objects are passed to the accessioning service. This service will generate a summary of the relevant fields of the object and then a hash value will be calculated from it. This hash will be used as a key to check if the object has been accessioned already, if it exists already on the system, the existing accession will be returned for that object, otherwise a new accession will be generated and stored in the database. Finally, the user will receive a map of accessions/objects.

## Accessioning service
An accessioning service is composed of
- Summary function
- Hashing function
- Accession generator service
- Database service layer to store the object in the desired database

### Summary function
A summary function is a java lambda or Function implementation from your object model to a string. It is meant to give the minimum text string that identifies uniquely your object model.

### Hashing function
The hashing function is a java lambda or Function implementation from a String value to your Hash representation. It is also possible that the hash representation is also a string. This function will be used to hash the summary of your object to use as unique key. The library already contains a SHA1 function implementation.

### Accession generator service
Service to generate accessions. It will receive a map of hash/model and will generate an accession for each pair creating a list of triplets accession/hash/model. The generated accession will depend on the specific implementation. The library currently has a timestamp based accession, accession hashed from the model and monotonic accession implementations.

### Database service
This service will handle the database persistence of the triplet accession/hash/model and the related queries offered through the accessioning service. Currently in the library there is a basic implementation that supports the use of Spring Data repositories / entities.
 
## BasicAccessionGeneratorRestController
The library offers a basic implementation of the methods to generate accessions and some queries of the existing ones. This implementation also allows to use the validation of the rest message against a DTO that represents your model.
