package uk.ac.ebi.eva.benchmarking_suite

/**
  * Properties for a given sampler context
  * @param threadChoice Thread Choice with which the sampler was invoked
  * @param threadNum Current thread number of the sampler
  * @param loopIndex Current iteration of the sampler for a given thread choice and thread
  */
class JMeterSamplerContextProperties(val threadChoice: Int, val threadNum: Int, val loopIndex: Int)
object JMeterSamplerContextProperties {
  /**
    * Generate entity and accession IDs for a given sampler context and a given record with the format described below.
    * ==Accession and Entity IDs suffix format==
    * <li> Total number of threads for the current sampler
    * <li> Thread number of the current sampler (for ex: has a value 6 for the 6th thread of a sampler running with 8 threads).
    * <li> Iteration number of the current sampler (how many times has the current sampler already run thus far + 1).
    * <li> Index of the record currently being inserted
    * @param samplerCtxProps Sampler Context properties object
    * @param recordIndex Index of the record currently being inserted by the current sampler thread
    * @return Accession and Entity IDs for a given sampler context and a given record
    */
  def generateIDsForRecord(samplerCtxProps: JMeterSamplerContextProperties, recordIndex: Int): (String, String) = {
    val accessionId = "acc_%d_%d_%d_%d".format(samplerCtxProps.threadChoice, samplerCtxProps.threadNum,
      samplerCtxProps.loopIndex, recordIndex)
    val entityId = "ent_%d_%d_%d_%d".format(samplerCtxProps.threadChoice, samplerCtxProps.threadNum,
      samplerCtxProps.loopIndex, recordIndex)
    (accessionId, entityId)
  }
}