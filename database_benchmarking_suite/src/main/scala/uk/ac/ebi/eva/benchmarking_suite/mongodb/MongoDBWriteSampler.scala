package uk.ac.ebi.eva.benchmarking_suite.mongodb

import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import org.mongodb.scala.Document
import org.mongodb.scala.model._
import uk.ac.ebi.eva.benchmarking_suite.DBSamplerProcessor

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MongoDBWriteSampler() extends AbstractSampler {

  val defaultBatchSize = 1000
  val bulkWriteOptions: BulkWriteOptions = BulkWriteOptions().ordered(false)
  type doc = WriteModel[_ <: Document]

  var mongoDBTestParams: MongoDBConnectionParams = _

  override def sample(entry: Entry): SampleResult = {
    mongoDBTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[MongoDBConnectionParams]
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        mongoDBTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[MongoDBConnectionParams]
        val numInsertsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val threadNum = this.getThreadContext.getThreadNum
        insertData(threadNum, counter = 0, numInsertsPerThread, List[doc]())
      })
  }

  @annotation.tailrec
  private def insertData(threadNum: Int, counter: Int, numInsertsPerThread: Int,
                         documents: List[doc]): Unit = {
    def batchWrite = {
      Await.result(mongoDBTestParams.mongoCollection.bulkWrite(documents, bulkWriteOptions).toFuture(),
        Duration.Inf)
    }

    if (counter < numInsertsPerThread) {
      val timeStamp = System.currentTimeMillis()
      val accessionId = "acc_%d_%d_%s".format(threadNum, counter, timeStamp)
      val entityId = "ent_%d_%d_%s".format(threadNum, counter, timeStamp)
      val documentToInsert: doc = InsertOneModel(Document(
        "_id" -> accessionId, "species" -> "eva_hsapiens_grch37", "chromosome" -> threadNum.toString,
        "start_pos" -> (counter + 100), "entity_id" -> entityId,
        "accession_id" -> accessionId, "raw_numeric_id" -> counter)
      )
      val timeForBatchWrite = counter % defaultBatchSize == 0 && counter > 0
      if (timeForBatchWrite) {
        batchWrite
      }
      insertData(threadNum, counter + 1, numInsertsPerThread,
        documentToInsert :: (if (timeForBatchWrite) List[doc]() else documents))
    }
    else {
      batchWrite
    }
  }
}