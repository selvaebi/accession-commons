package uk.ac.ebi.eva.benchmarking_suite.cassandra

import com.datastax.driver.core.{BatchStatement, ConsistencyLevel}
import org.apache.jmeter.samplers.{AbstractSampler, Entry, SampleResult}
import org.apache.jmeter.util.JMeterUtils
import uk.ac.ebi.eva.benchmarking_suite.{DBSamplerProcessor, JMeterSamplerContextProperties}

class CassandraWriteSampler() extends AbstractSampler {

  val defaultBatchSize = 100
  var cassandraTestParams: CassandraConnectionParams = _
  var batchStmt: BatchStatement = _

  override def sample(entry: Entry): SampleResult = {
    DBSamplerProcessor.process(sampler = this,
      databaseAction = () => {
        cassandraTestParams = JMeterUtils.getJMeterProperties.get("connectionParams").asInstanceOf[CassandraConnectionParams]
        val numInsertsPerThread = this.getPropertyAsInt("numOpsPerThread")
        val samplerCtxProps = new JMeterSamplerContextProperties(this.getPropertyAsInt("threadChoice") ,
          this.getThreadContext.getThreadNum, this.getThreadContext.getVariables.getIteration)
        batchStmt = getBatchStmt
        insertData(samplerCtxProps, counter = 0, numInsertsPerThread)
      })
  }

  private def getBatchStmt = {
    //Use logged batches to maintain atomicity/consistency since we are inserting into two tables
    var batchStmt = new BatchStatement(BatchStatement.Type.LOGGED)
    batchStmt.setConsistencyLevel(ConsistencyLevel.QUORUM)
    batchStmt.setReadTimeoutMillis(600000)
    batchStmt
  }

  @annotation.tailrec
  private def insertData(samplerCtxProps: JMeterSamplerContextProperties, counter: Int, numInsertsPerThread: Int): Unit = {
      def batchWrite = {
        cassandraTestParams.session.execute(batchStmt)
        batchStmt.clear()
      }

      if (counter < numInsertsPerThread) {
        val (accessionId, entityId) = JMeterSamplerContextProperties.generateIDsForRecord(samplerCtxProps, counter)
        val timeForBatchWrite = counter % defaultBatchSize == 0 && counter > 0
        if (timeForBatchWrite) {
          batchWrite
        }
        val (species, chromosome, start_pos, entity_id, accession_id, raw_numeric_id) =
          ("eva_hsapiens_grch37", samplerCtxProps.threadNum.toString, new Integer(counter + 100), entityId,
            accessionId, new Integer(counter))

        //Write to 2 tables one for the look-up and the other for the reverse look-up
        batchStmt.add(cassandraTestParams.lookupTableInsertStmt.bind
        (species, chromosome, start_pos, entity_id, accession_id, raw_numeric_id))
        batchStmt.add(cassandraTestParams.reverseLookupTableInsertStmt.bind
        (accession_id, raw_numeric_id, species, chromosome, start_pos, entity_id))

        insertData(samplerCtxProps, counter + 1, numInsertsPerThread)
      }
      else {
        batchWrite
      }
  }
}