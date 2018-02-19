package uk.ac.ebi.eva.benchmarking_suite.cassandra

import com.datastax.driver.core.{BatchStatement, ConsistencyLevel}
import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import uk.ac.ebi.eva.benchmarking_suite.DBSamplerProcessor

class CassandraWriteSampler() extends AbstractSampler {

  val defaultBatchSize = 100
  var cassandraTestParams: CassandraConnectionParams = _
  var batchStmt: BatchStatement = _

  override def sample(entry: Entry): SampleResult = {
    cassandraTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[CassandraConnectionParams]
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        val numInsertsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        batchStmt = getBatchStmt
        insertData(threadNum, counter = 0, numInsertsPerThread)
      })
  }

  private def getBatchStmt = {
    var batchStmt = new BatchStatement(BatchStatement.Type.UNLOGGED)
    batchStmt.setConsistencyLevel(ConsistencyLevel.QUORUM)
    batchStmt.setReadTimeoutMillis(600000)
    batchStmt
  }

  @annotation.tailrec
  private def insertData(threadNum: Int, counter: Int, numInsertsPerThread: Int): Unit = {
    def batchWrite = {
      cassandraTestParams.session.execute(batchStmt)
      batchStmt.clear()
    }

    if (counter <= numInsertsPerThread) {
      val timeForBatchWrite = counter % defaultBatchSize == 0 && counter > 0
      if (timeForBatchWrite) {
        batchWrite
      }
      batchStmt.add(cassandraTestParams.insertStmt.bind(
        "eva_hsapiens_grch37", threadNum.toString, new Integer(counter + 100),
        "ent_%d_%d".format(threadNum, counter), "acc_%d_%d".format(threadNum, counter),
        new Integer(counter)))
      insertData(threadNum, counter + 1, numInsertsPerThread)
    }
    else {
      batchWrite
    }
  }
}