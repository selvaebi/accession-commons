package uk.ac.ebi.eva.benchmarking_suite.postgres

import slick.jdbc.PostgresProfile.backend.DatabaseDef
import uk.ac.ebi.eva.benchmarking_suite.DBConnectionParams

object PostgresConnectionParams {
  def apply(postgresDBConnection: DatabaseDef, schemaName: String, tableName: String): PostgresConnectionParams
  = new PostgresConnectionParams (postgresDBConnection, schemaName, tableName)
}

class PostgresConnectionParams(val postgresDBConnection: DatabaseDef, val schemaName: String, val tableName: String,
                               val readTimeOutInMillis: Int = 1200000)
  extends  DBConnectionParams {
  override def cleanup(): Unit = {
    postgresDBConnection.close()
  }
}