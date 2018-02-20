package uk.ac.ebi.eva.benchmarking_suite.mongodb

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import uk.ac.ebi.eva.benchmarking_suite.DBConnectionParams

object MongoDBConnectionParams {
  def apply(mongoClient: MongoClient, mongoDatabase: MongoDatabase,
            mongoCollection: MongoCollection[Document]): MongoDBConnectionParams
  = new MongoDBConnectionParams (mongoClient, mongoDatabase, mongoCollection)
}

class MongoDBConnectionParams(val mongoClient: MongoClient, val mongoDatabase: MongoDatabase,
                              val mongoCollection: MongoCollection[Document]) extends  DBConnectionParams {
  override def cleanup(): Unit = {
    mongoClient.close()
  }
}