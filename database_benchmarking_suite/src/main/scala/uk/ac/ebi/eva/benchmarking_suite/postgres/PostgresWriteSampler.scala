package uk.ac.ebi.eva.benchmarking_suite.postgres

import slick.jdbc.PostgresProfile.api._
import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import uk.ac.ebi.eva.benchmarking_suite.{DBSamplerProcessor, JMeterSamplerContextProperties}

import scala.collection.immutable.Queue
import scala.concurrent.Await
import scala.concurrent.duration._

class PostgresWriteSampler extends AbstractSampler {

  val defaultBatchSize = 1000
  var postgresTestParams: PostgresConnectionParams = _

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        postgresTestParams = JMeterUtils.getJMeterProperties.get("connectionParams")
          .asInstanceOf[PostgresConnectionParams]
        val numInsertsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val samplerCtxProps = new JMeterSamplerContextProperties(this.getPropertyAsInt("threadChoice") ,
          this.getThreadContext.getThreadNum, this.getThreadContext.getVariables.getIteration)
        insertData(samplerCtxProps, counter = 0, numInsertsPerThread, Queue[String]())
      })
  }

  @annotation.tailrec
  private def insertData(samplerCtxProps: JMeterSamplerContextProperties, counter: Int, numInsertsPerThread: Int,
                         insertStatements: Queue[String]): Unit = {
    def batchWrite = {
      Await.result(postgresTestParams.postgresDBConnection.run(
        DBIO.sequence(insertStatements.map(stmt => sqlu"""#$stmt""")).transactionally), 15.minutes)
    }
    if (counter < numInsertsPerThread) {
      val (accessionId, entityId) = JMeterSamplerContextProperties.generateIDsForRecord(samplerCtxProps, counter)
      val startPos = counter + 100
      val rawNumericId = counter

      val (schemaName, variantTableName) = (postgresTestParams.schemaName, postgresTestParams.tableName)
      val sqlString = "insert into %s.%s values ('eva_hsapiens_grch37', '%s', %d, '%s', '%s', %d)"
        .format(schemaName, variantTableName, samplerCtxProps.threadNum,
          startPos, entityId, accessionId, rawNumericId)

      val timeForBatchWrite = counter % defaultBatchSize == 0 && counter > 0
      if (timeForBatchWrite) {
        batchWrite
      }
      insertData(samplerCtxProps, counter + 1, numInsertsPerThread,
        if (timeForBatchWrite) Queue[String]() else insertStatements.enqueue(sqlString))
    }
    else {
      batchWrite
    }
  }

}
