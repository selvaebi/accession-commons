package uk.ac.ebi.eva.benchmarking_suite.cassandra

import com.datastax.driver.core.{Cluster, PreparedStatement, Session}
import uk.ac.ebi.eva.benchmarking_suite.DBConnectionParams

object CassandraConnectionParams {
    def apply(cluster: Cluster, session: Session, insertStmt: PreparedStatement,
              blockReadStmt: PreparedStatement) = new CassandraConnectionParams(cluster, session, insertStmt, blockReadStmt)
}

class CassandraConnectionParams(val cluster: Cluster, val session: Session, val insertStmt: PreparedStatement,
                                val blockReadStmt: PreparedStatement) extends  DBConnectionParams {

  override def cleanup(): Unit = {
    session.close()
    cluster.close()
  }

}