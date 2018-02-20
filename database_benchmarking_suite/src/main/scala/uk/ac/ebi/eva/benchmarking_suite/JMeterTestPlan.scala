package uk.ac.ebi.eva.benchmarking_suite

import java.nio.file.Paths

import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.reporters.Summariser
import org.apache.jmeter.samplers.Sampler
import org.apache.jmeter.testelement.TestPlan
import org.apache.jorphan.collections.HashTree
import org.apache.jmeter.threads.ThreadGroup

class JMeterTestPlan (testOutputFile: String)  {

  var jm = new StandardJMeterEngine

  var resultCollector = new BenchmarkResultCollector(new Summariser)
  //Result collector settings
  java.nio.file.Files.deleteIfExists(Paths.get(testOutputFile))
  resultCollector.setFilename(testOutputFile)

  var testPlanTree = new HashTree
  var threadGroupHashTree = new HashTree
  var testPlan = new TestPlan()

  testPlan.setSerialized(true)
  testPlanTree.add(testPlan)
  testPlanTree.add(testPlanTree.getArray()(0), resultCollector)


  def addThreadGroupAndSampler(threadGroup: ThreadGroup, sampler: Sampler): Unit = {
    println("Added thread group")
    threadGroupHashTree = testPlanTree.add(testPlan, threadGroup)
    threadGroupHashTree.add(sampler)
  }

  def runPlan() : Unit = {
    testPlanTree.getArray.foreach(x => println(x))
    jm.configure(testPlanTree)
    jm.run()
  }

}
