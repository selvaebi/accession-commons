package uk.ac.ebi.eva.benchmarking_suite.postgres

import slick.jdbc.PostgresProfile.api._
import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import uk.ac.ebi.eva.benchmarking_suite.{DBSamplerProcessor, ReadBenchmarkingConstants}

import scala.concurrent.Await
import scala.concurrent.duration._

class PostgresReadSampler extends AbstractSampler {

  var postgresTestParams: PostgresConnectionParams = _
  var randomNumGen: scala.util.Random = _

  val blockReadSize = 100

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        postgresTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[PostgresConnectionParams]
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        //Use thread number and timestamp to vary the random seed across multiple runs for a same thread choice
        randomNumGen = new scala.util.Random(threadNum  + System.currentTimeMillis())
        (1 to numReadsPerThread).foreach(_ => readData())
      })

  }

  private def readData(): Unit = {
    val (chromosome, startPos) = ReadBenchmarkingConstants.getRandomChromosomeAndStartPos(randomNumGen)
    Await.result(postgresTestParams.postgresDBConnection.run (
      sql"""select * from #${postgresTestParams.schemaName}.#${postgresTestParams.tableName}
           where chromosome = $chromosome and start_pos >= $startPos and start_pos <= ${startPos + 100}
         """.as[(String, String, Int, String, String, Int)]
    ), postgresTestParams.readTimeOutInMillis.milliseconds).foreach(result => result)
  }

}
