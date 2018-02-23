package uk.ac.ebi.eva.benchmarking_suite.cassandra

import com.datastax.driver.core.{Cluster, PreparedStatement, Session}
import uk.ac.ebi.eva.benchmarking_suite.DBConnectionParams

object CassandraConnectionParams {
    def apply(cluster: Cluster, session: Session, lkpTableInsertStmt: PreparedStatement,
              reverseLkpTableInsertStmt: PreparedStatement, blockReadStmt: PreparedStatement) =
      new CassandraConnectionParams(cluster, session, lkpTableInsertStmt, reverseLkpTableInsertStmt, blockReadStmt)
}

class CassandraConnectionParams(val cluster: Cluster, val session: Session, val lkpTableInsertStmt: PreparedStatement,
                                val reverseLkpTableInsertStmt: PreparedStatement, val blockReadStmt: PreparedStatement)
  extends  DBConnectionParams {

  override def cleanup(): Unit = {
    session.close()
    cluster.close()
  }

}