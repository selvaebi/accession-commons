package uk.ac.ebi.eva.BenchmarkingSuite

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.ConsistencyLevel
import org.apache.jmeter.samplers.AbstractSampler
import org.apache.jmeter.samplers.Entry
import org.apache.jmeter.samplers.SampleResult

class CassandraWriteSampler() extends AbstractSampler {

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        var batch: BatchStatement = getBatchStmt
        (1 to numReadsPerThread).foreach(i => insertData(threadNum, batch, i))
      })
  }

  private def getBatchStmt = {
    var batch = new BatchStatement(BatchStatement.Type.UNLOGGED)
    batch.setConsistencyLevel(ConsistencyLevel.QUORUM)
    batch.setReadTimeoutMillis(600000)
    batch
  }

  private def insertData(threadNum: Int, batch: BatchStatement, i: Int): Unit = {
    batch.add(
      CassandraConnection.stmt.bind(
        "eva_hsapiens_grch37", "" + threadNum,
        new Integer(i + 100),
        "ent_%d_%d".format(threadNum, i),
        "acc_%d_%d".format(threadNum, i),
        new Integer(i)
      ))
    if (i % 10 == 0) {
      CassandraConnection.session.execute(batch)
      batch.clear
    }
  }
}