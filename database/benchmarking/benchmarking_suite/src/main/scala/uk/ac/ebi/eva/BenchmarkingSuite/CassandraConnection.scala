package uk.ac.ebi.eva.BenchmarkingSuite

import com.datastax.driver.core.{Cluster, PreparedStatement, Session}

object CassandraConnection {
    var cluster: Cluster = _
    var session: Session = _
    var stmt: PreparedStatement = _
}
