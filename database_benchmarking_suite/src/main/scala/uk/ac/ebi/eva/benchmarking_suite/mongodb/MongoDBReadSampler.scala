package uk.ac.ebi.eva.benchmarking_suite.mongodb

import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import org.mongodb.scala.model.Filters._
import uk.ac.ebi.eva.benchmarking_suite.{DBSamplerProcessor, ReadBenchmarkingConstants}

import scala.concurrent.Await
import scala.concurrent.duration._

class MongoDBReadSampler() extends AbstractSampler {

  var mongoDBTestParams: MongoDBConnectionParams = _
  var randomNumGen: scala.util.Random = _

  val blockReadSize = 100

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        mongoDBTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[MongoDBConnectionParams]
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        //Use thread number and timestamp to vary the random seed across multiple runs for a same thread choice
        randomNumGen = new scala.util.Random(threadNum + System.currentTimeMillis())
        (1 to numReadsPerThread).foreach(_ => readData())
      })
  }

  def readData(): Unit = {
    val chromosome = randomNumGen.nextInt(ReadBenchmarkingConstants.threadChoiceUB)
    val startPos = randomNumGen.nextInt(ReadBenchmarkingConstants.numRecordsUB/ReadBenchmarkingConstants.threadChoiceUB)
    val findResults = mongoDBTestParams.mongoCollection.find(
      and(equal("chromosome", chromosome.toString),
        gt("start_pos", startPos),
        lte("start_pos", startPos + blockReadSize))).maxTime(10.minutes)
    Await.result(findResults.toFuture(), 10.minutes).foreach(doc => {
      doc.get("start_pos").head.toString //Force document retrieval by getting one attribute
    })
  }


}