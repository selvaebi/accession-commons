package uk.ac.ebi.eva.BenchmarkingSuite

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.ConsistencyLevel
import org.apache.jmeter.samplers.AbstractSampler
import org.apache.jmeter.samplers.Entry
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.util.JMeterUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CassandraWriteSampler() extends AbstractSampler {

  val defaultBatchSize = 100
  var cassandraTestParams: CassandraConnectionParams = _
  var batch: BatchStatement = _

  override def sample(entry: Entry): SampleResult = {
    cassandraTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[CassandraConnectionParams]
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        val numInsertsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        batch = getBatchStmt
        insertData(threadNum, counter = 0, numInsertsPerThread)
      })
  }

  private def getBatchStmt = {
    var batch = new BatchStatement(BatchStatement.Type.UNLOGGED)
    batch.setConsistencyLevel(ConsistencyLevel.QUORUM)
    batch.setReadTimeoutMillis(600000)
    batch
  }

  @annotation.tailrec
  private def insertData(threadNum: Int, counter: Int, numInsertsPerThread: Int): Unit = {
    def batchWrite = {
      cassandraTestParams.session.execute(batch)
      batch.clear()
    }

    if (counter != numInsertsPerThread) {
      val timeForBatchWrite = counter % defaultBatchSize == 0 && counter > 0
      if (timeForBatchWrite) {
        batchWrite
      }
      batch.add(cassandraTestParams.insertStmt.bind(
        "eva_hsapiens_grch37", "" + threadNum, new Integer(counter + 100),
        "ent_%d_%d".format(threadNum, counter), "acc_%d_%d".format(threadNum, counter),
        new Integer(counter)))
      insertData(threadNum, counter + 1, numInsertsPerThread)
    }
    else {
      batchWrite
    }
  }
}