package uk.ac.ebi.eva.BenchmarkingSuite

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.ConsistencyLevel
import org.apache.jmeter.samplers.AbstractSampler
import org.apache.jmeter.samplers.Entry
import org.apache.jmeter.samplers.SampleResult

class CassandraReadSampler() extends AbstractSampler {

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        var batch: BatchStatement = getBatchStmt
        (1 to numReadsPerThread).foreach(i => readData(threadNum, batch, i, (i / 1000) + 1))
      })

  }

  private def getBatchStmt = {
    var batch = new BatchStatement(BatchStatement.Type.LOGGED)
    batch.setConsistencyLevel(ConsistencyLevel.QUORUM)
    batch.setReadTimeoutMillis(600000)
    batch
  }

  private def readData(threadNum: Int, batch: BatchStatement, i: Int, j: Int): Unit = {
    batch.add(
      CassandraConnection.stmt.bind(
        "eva_hsapiens_grch37", "" + j,
        new Integer(i + 100),
        "ent_par_%d_%d".format(threadNum, i),
        "acc_par_%d_%d".format(threadNum, i),
        new Integer(i)
      ))
    if (i % 1000 == 0) {
      CassandraConnection.session.execute(batch)
      batch.clear
    }
  }
}