package uk.ac.ebi.eva.BenchmarkingSuite

import com.datastax.driver.core.{BatchStatement, ConsistencyLevel, ResultSet, Row}
import org.apache.jmeter.samplers.AbstractSampler
import org.apache.jmeter.samplers.Entry
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.util.JMeterUtils

class CassandraReadSampler() extends AbstractSampler {

  var cassandraTestParams: CassandraConnectionParams = _
  var randomNumGen: scala.util.Random = _

  val blockReadSize = 100

  override def sample(entry: Entry): SampleResult = {
    cassandraTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[CassandraConnectionParams]
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        randomNumGen = new scala.util.Random(threadNum)
        (1 to numReadsPerThread).foreach(_ => readData())
      })

  }

  private def readData(): Unit = {
    val chromosome = randomNumGen.nextInt(16)
    val start_pos = randomNumGen.nextInt(1e9.toInt/16)
    val rows: ResultSet = cassandraTestParams.session.execute(cassandraTestParams.blockReadStmt.bind(
      "eva_hsapiens_grch37",
      ""+chromosome,
      new Integer(start_pos),
      new Integer(start_pos + blockReadSize)
    ))
    rows.iterator().forEachRemaining(row => row.getInt("start_pos")) //Force row retrieval by getting one attribute
    }
}