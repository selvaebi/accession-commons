package uk.ac.ebi.eva.benchmarking_suite.postgres

import slick.jdbc.PostgresProfile.api._
import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import uk.ac.ebi.eva.benchmarking_suite.{DBSamplerProcessor, RandomEntityIDGenerator}

import scala.concurrent.Await
import scala.concurrent.duration._

class PostgresLookupSampler extends AbstractSampler {

  var postgresTestParams: PostgresConnectionParams = _
  var randomNumGen: scala.util.Random = _

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        postgresTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[PostgresConnectionParams]
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        //Use thread number and timestamp to vary the random seed across multiple runs for a same thread choice
        randomNumGen = new scala.util.Random(threadNum + System.currentTimeMillis())
        (1 to numReadsPerThread).foreach(_ => lookupData())
      })
  }

  def lookupData(): Unit = {
    val entityToLookup = RandomEntityIDGenerator.getRandomEntityID(randomNumGen)
    val results = Await.result(postgresTestParams.postgresDBConnection.run (
      sql"""select entity_id from #${postgresTestParams.schemaName}.#${postgresTestParams.tableName}
           where entity_id = $entityToLookup
         """.as[String]
    ), postgresTestParams.readTimeOutInMillis.milliseconds)
    if (results.isEmpty) throw new Exception("Empty lookup results for entity ID: %s".format(entityToLookup))
    results.foreach(result => result)
  }

}