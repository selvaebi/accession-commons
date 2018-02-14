package uk.ac.ebi.eva.BenchmarkingSuite

import com.datastax.driver.core.{Cluster, PreparedStatement, Session}

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