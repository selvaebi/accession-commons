package uk.ac.ebi.eva.benchmarking_suite

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.jmeter.reporters.{ResultCollector, Summariser}
import org.apache.jmeter.samplers.SampleEvent


class BenchmarkResultCollector(val summer: Summariser) extends ResultCollector(summer) {
  override def sampleOccurred(e: SampleEvent): Unit = {
    super.sampleOccurred(e)
    val r = e.getResult
    if (!r.isSuccessful) {
      System.out.println("Sampler %s failed at %s!".format(e.getThreadGroup,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())))
      System.out.println(r.getResponseMessage)
    }
  }
}
