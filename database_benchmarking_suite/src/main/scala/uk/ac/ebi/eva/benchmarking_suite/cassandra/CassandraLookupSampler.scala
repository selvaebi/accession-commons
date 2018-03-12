package uk.ac.ebi.eva.benchmarking_suite.cassandra

import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import uk.ac.ebi.eva.benchmarking_suite.{DBSamplerProcessor, RandomEntityIDGenerator, ReadBenchmarkingConstants}

class CassandraLookupSampler extends AbstractSampler {

  var cassandraTestParams: CassandraConnectionParams = _
  var randomNumGen: scala.util.Random = _

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        cassandraTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[CassandraConnectionParams]
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        //Use thread number and timestamp to vary the random seed across multiple runs for a same thread choice
        randomNumGen = new scala.util.Random(threadNum + System.currentTimeMillis())
        (1 to numReadsPerThread).foreach(_ => lookupData())
      })
  }

  def lookupData(): Unit = {
    val entityToLookup = RandomEntityIDGenerator.getRandomEntityID(randomNumGen)
    val entityIdComponents = entityToLookup.split("_")
    val (chromosome, startPos) = (entityIdComponents(2), new Integer(new Integer(entityIdComponents(4)) + 100))

    val rows = cassandraTestParams.session.execute(
      cassandraTestParams.lookupStmt.bind("eva_hsapiens_grch37", chromosome, startPos, entityToLookup)
        .setReadTimeoutMillis(cassandraTestParams.readTimeOutInMillis)
    )
    rows.iterator().forEachRemaining(row => row.getInt("start_pos"))
  }

}