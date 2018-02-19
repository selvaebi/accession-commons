package uk.ac.ebi.eva.BenchmarkingSuite

import org.mongodb.scala.model._
import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import org.mongodb.scala.Document

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

    if (counter != numInsertsPerThread) {
      val timestamp = System.currentTimeMillis()
      val accession_id = "acc_%d_%d_%s".format(threadNum, counter, timestamp)
      val documentToInsert: doc = InsertOneModel(Document(
        "_id" -> accession_id, "species" -> "eva_hsapiens_grch37", "chromosome" -> ("" + threadNum),
        "start_pos" -> (counter + 100), "entity_id" -> "ent_%d_%d".format(threadNum, counter),
        "accession_id" -> accession_id, "raw_numeric_id" -> counter)
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