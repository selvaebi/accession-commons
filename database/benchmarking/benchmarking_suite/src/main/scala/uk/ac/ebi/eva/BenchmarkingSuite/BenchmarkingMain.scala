package uk.ac.ebi.eva.BenchmarkingSuite

import java.nio.file.Paths

import com.datastax.driver.core.{Cluster, PreparedStatement, Session}
import org.apache.jmeter.control.LoopController
import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.reporters.Summariser
import org.apache.jmeter.samplers.Sampler
import org.apache.jmeter.testelement.TestPlan
import org.apache.jmeter.threads.ThreadGroup
import org.apache.jorphan.collections.HashTree

object BenchmarkingMain extends App {

  //JMeter initialization
  initJMeterEnv(sys.env.getOrElse("JMETER_HOME",
    scala.io.StdIn.readLine("Enter full path to JMeter installation (ex: /home/centos/apache-jmeter-3.3): ")))
  var jm = new StandardJMeterEngine

  //Cassandra initialization
  val (cassandra_cluster, cassandra_session, preparedInsertStatement) = initCassandra()
  CassandraConnection.cluster = cassandra_cluster
  CassandraConnection.session = cassandra_session
  CassandraConnection.stmt = preparedInsertStatement


  //Result collector settings
  java.nio.file.Files.deleteIfExists(Paths.get("results.jtl"))
  var resultCollector = new BenchmarkResultCollector(new Summariser)
  resultCollector.setFilename("results.jtl")

  // Workload settings - WU => Workload Unit
  class Workload (val desc: String, val threadChoices: List[Int], val numOpsPerWU: Int, val numWU: Int) {}
  val writeWorkloads = List(
    //Description Format - <Type of Operation>-<Inserts per WU>-<Parallel(par)/Sequential(seq)>
    new Workload(desc = "ins-256k-par", threadChoices = List(4, 8, 16), numOpsPerWU =  256000, numWU = 50),
    new Workload(desc = "ins-1B-par", threadChoices = List(4, 8, 16), numOpsPerWU =  1e9.toInt, numWU = 2),
    new Workload(desc = "ins-32k-seq", threadChoices = List(1), numOpsPerWU =  32000, numWU = 50)
  )

  val readWorkloads = List(
    //Description Format - <Type of Operation>-<Reads per WU>-<Parallel(par)/Sequential(seq)>
    new Workload(desc = "ins-256k-par", threadChoices = List(8, 16, 32), numOpsPerWU =  256000, numWU = 50),
    new Workload(desc = "ins-1B-par", threadChoices = List(16, 32), numOpsPerWU =  1e9.toInt, numWU = 2),
    new Workload(desc = "ins-32k-seq", threadChoices = List(1), numOpsPerWU =  32000, numWU = 50)
  )


  try {
    var testPlanTree = new HashTree
    var threadGroupHashTree = new HashTree
    var testPlan = new TestPlan("Cassandra Benchmarking plan")
    testPlan.setSerialized(true)
    testPlanTree.add(testPlan)
    testPlanTree.add(testPlanTree.getArray()(0), resultCollector)

    //Add a separate thread group + sampler to the Test plan for each thread choice in a given workload
    getJMeterWorkloadObjects(writeWorkloads, new CassandraWriteSampler)
      .foreach(threadGroupAndSamplerTuple => {
        threadGroupHashTree = testPlanTree.add(testPlan, threadGroupAndSamplerTuple._1)
        threadGroupHashTree.add(threadGroupAndSamplerTuple._2)
      })

//    getJMeterWorkloadObjects(readWorkloads, new CassandraReadSampler)
//      .foreach(threadGroupAndSamplerTuple => {
//        threadGroupHashTree = testPlanTree.add(testPlan, threadGroupAndSamplerTuple._1)
//        threadGroupHashTree.add(threadGroupAndSamplerTuple._2)
//      })

    jm.configure(testPlanTree)
    jm.run()
  } catch {
    case ex: Exception => ex.printStackTrace()
  }
  finally {
    cassandra_session.close()
    cassandra_cluster.close()
  }

  def getJMeterWorkloadObjects(workloads: List[Workload], sampler: Sampler) : List[(ThreadGroup, Sampler)] = {
    workloads
      .flatMap(workload =>
        workload.threadChoices.map(numThreads =>
          getThreadGroupAndSamplerForWorkload(
            name = workload.desc + "-%d-threads".format(numThreads),
            numWU = workload.numWU,
            numThreads = numThreads,
            numInsertsPerThread = workload.numOpsPerWU/numThreads,
            sampler = sampler)))
  }


  /**
    * Initialize JMeter Environment
    *
    * @param JMETER_HOME Full path to JMeter home (ex: /home/centos/apache-jmeter-3.3)
    */
  def initJMeterEnv(JMETER_HOME: String): Unit = {
    import org.apache.jmeter.util.JMeterUtils._
    setJMeterHome(JMETER_HOME)
    loadJMeterProperties("%s/bin/jmeter.properties".format(JMETER_HOME))
    initLocale()
  }

  def initCassandra(): (Cluster, Session, PreparedStatement) = {
    val (cassandra_nodes, cassandra_keyspace, cassandra_table_name) =
      (Array("192.168.0.26", "192.168.0.42"), "accessioning", "global_variant_lkp")
    val cassandra_cluster = Cluster.builder().addContactPoints(cassandra_nodes: _*).build()
    val cassandra_session = cassandra_cluster.connect(cassandra_keyspace)
    cassandra_session.execute("truncate %s".format(cassandra_table_name))
    val insertEntityString = "insert into %s (species, chromosome, start_pos, entity_id, accession_id, raw_numeric_id) ".format(cassandra_table_name) +
      "values (?, ?, ?, ?, ?, ?);"
    val preparedInsertStatement: PreparedStatement = cassandra_session.prepare(insertEntityString)

    (cassandra_cluster, cassandra_session, preparedInsertStatement)
  }

  def getThreadGroupAndSamplerForWorkload(name: String, numWU: Int, numThreads: Int, numInsertsPerThread: Int,
                                          sampler: Sampler): (ThreadGroup, Sampler) = {
    //Sampler
    sampler.setName(name)
    sampler.setProperty("numOpsPerThread", numInsertsPerThread)

    // Setup Loop controller
    var loopCtrl = new LoopController
    loopCtrl.setLoops(numWU)
    loopCtrl.setFirst(true)

    // Setup Thread group
    var threadGroup = new ThreadGroup
    threadGroup.setNumThreads(numThreads)
    threadGroup.setRampUp(1)
    threadGroup.setSamplerController(loopCtrl)

    (threadGroup, sampler)
  }
}
