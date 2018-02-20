package uk.ac.ebi.eva.benchmarking_suite

import org.apache.jmeter.samplers.{AbstractSampler, SampleResult}

object DBSamplerProcessor {

  def process(sampler: AbstractSampler, databaseAction: () => Unit): SampleResult = {
    var sampleResult = new SampleResult
    sampleResult.setSampleLabel(sampler.getName)
    sampleResult.sampleStart()
    try {
      databaseAction()
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        sampleResult.setSuccessful(false)
        sampleResult.setResponseMessage(ex.getMessage)
        return sampleResult
    }
    sampleResult.sampleEnd()
    sampleResult.setResponseOK()
    sampleResult.setSuccessful(true)
    sampleResult.setResponseCodeOK()
    sampleResult
  }

}
