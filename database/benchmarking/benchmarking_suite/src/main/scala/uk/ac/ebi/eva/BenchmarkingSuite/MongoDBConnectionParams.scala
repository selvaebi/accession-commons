package uk.ac.ebi.eva.BenchmarkingSuite

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

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