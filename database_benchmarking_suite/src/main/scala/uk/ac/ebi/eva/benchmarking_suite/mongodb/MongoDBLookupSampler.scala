package uk.ac.ebi.eva.benchmarking_suite.mongodb

import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import org.mongodb.scala.model.Filters._
import uk.ac.ebi.eva.benchmarking_suite.{DBSamplerProcessor, RandomEntityIDGenerator}

import scala.concurrent.Await
import scala.concurrent.duration._

class MongoDBLookupSampler extends AbstractSampler {

  var mongoDBTestParams: MongoDBConnectionParams = _
  var randomNumGen: scala.util.Random = _

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        mongoDBTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[MongoDBConnectionParams]
        val numReadsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        //Use thread number and timestamp to vary the random seed across multiple runs for a same thread choice
        randomNumGen = new scala.util.Random(threadNum + System.currentTimeMillis())
        (1 to numReadsPerThread).foreach(_ => lookupData())
      })
  }

  def lookupData(): Unit = {
    val entityToLookup = RandomEntityIDGenerator.getRandomEntityID(randomNumGen)
    val findResults = mongoDBTestParams.mongoCollection.find(equal("entity_id", entityToLookup)).maxTime(10.minutes)
    //Force document retrieval by getting one attribute
    val results = Await.result(findResults.toFuture(), 10.minutes)
    if (results.isEmpty) throw new Exception("Empty lookup results for entity ID: %s".format(entityToLookup))
    results.foreach(doc => doc.get("start_pos").head.toString)
  }

}