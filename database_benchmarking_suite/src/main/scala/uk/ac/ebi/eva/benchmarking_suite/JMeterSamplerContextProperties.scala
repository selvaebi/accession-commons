package uk.ac.ebi.eva.benchmarking_suite

class JMeterSamplerContextProperties(val threadChoice: Int, val threadNum: Int, val loopIndex: Int)
object JMeterSamplerContextProperties {
  def getIDsForRecord(samplerCtxProps: JMeterSamplerContextProperties, recordIndex: Int): (String, String) = {
    //Accession and Entity IDs suffix format
    //Total number of threads for the current sampler
    //Thread number of the current sampler (for ex: has a value 6 for the 6th thread of a sampler running with 8 threads)
    //Iteration number of the current sampler (how many times has the current sampler already run thus far + 1)
    //Index of the record currently being inserted
    val accessionId = "acc_%d_%d_%d_%d".format(samplerCtxProps.threadChoice, samplerCtxProps.threadNum,
      samplerCtxProps.loopIndex, recordIndex)
    val entityId = "ent_%d_%d_%d_%d".format(samplerCtxProps.threadChoice, samplerCtxProps.threadNum,
      samplerCtxProps.loopIndex, recordIndex)
    (accessionId, entityId)
  }
}