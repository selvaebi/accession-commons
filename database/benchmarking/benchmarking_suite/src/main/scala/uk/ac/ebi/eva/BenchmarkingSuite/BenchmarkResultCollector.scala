package uk.ac.ebi.eva.BenchmarkingSuite

import org.apache.jmeter.reporters.{ResultCollector, Summariser}
import org.apache.jmeter.samplers.SampleEvent


class BenchmarkResultCollector(val summer: Summariser) extends ResultCollector(summer) {
  override def sampleOccurred(e: SampleEvent): Unit = {
    super.sampleOccurred(e)
    val r = e.getResult
    if (!r.isSuccessful) {
      System.out.println("Run Failed!")
      System.out.println(r.getResponseMessage)
    }
  }
}
