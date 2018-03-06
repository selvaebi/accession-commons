package uk.ac.ebi.eva.benchmarking_suite.cassandra

import com.datastax.driver.core.ResultSet
import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import uk.ac.ebi.eva.benchmarking_suite.DBSamplerProcessor

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
        //Use thread number and timestamp to vary the random seed across multiple runs for a same thread choice
        randomNumGen = new scala.util.Random(threadNum  + System.currentTimeMillis())
        (1 to numReadsPerThread).foreach(_ => readData())
      })

  }

  private def readData(): Unit = {
    val chromosome = randomNumGen.nextInt(32)
    val startPos = randomNumGen.nextInt(1e8.toInt/32)
    val rows: ResultSet = cassandraTestParams.session.execute(cassandraTestParams.blockReadStmt.bind(
      "eva_hsapiens_grch37",
      chromosome.toString,
      new Integer(startPos),
      new Integer(startPos + blockReadSize)
    ).setReadTimeoutMillis(600000))
    rows.iterator().forEachRemaining(row => row.getInt("start_pos")) //Force row retrieval by getting one attribute
    }
}