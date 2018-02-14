package uk.ac.ebi.eva.BenchmarkingSuite

import com.datastax.driver.core.{Cluster, PreparedStatement}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, WriteConcern}
import org.apache.jmeter.control.LoopController
import org.apache.jmeter.samplers.{AbstractSampler, Sampler}
import org.apache.jmeter.threads.ThreadGroup
import org.apache.jmeter.util.JMeterUtils
import org.rogach.scallop._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object BenchmarkingMain extends App {

  //Supported databases
  val validDatabases = List("cassandra", "mongodb", "postgres")

  //Parse command line arguments
  val config = new BenchmarkingArgParser(args, validDatabases)

  //Stateful JMeter initialization
  initJMeterEnv(config.JMeterHome())
  var jmeterProperties = JMeterUtils.getJMeterProperties

  //Database initialization
  val (db, connectionString, schemaName, variantTableName) =
    (config.databaseType(), config.connectionString(), config.schemaName(), config.variantTableName())
  val dbConnectionParams = getDBConnectionParams(db, connectionString)
  jmeterProperties.put("connectionParams", dbConnectionParams)

  val readSamplers = Map("cassandra" -> classOf[CassandraReadSampler], "mongodb" -> classOf[MongoDBReadSampler])
  val writeSamplers = Map("cassandra" -> classOf[CassandraWriteSampler], "mongodb" -> classOf[MongoDBWriteSampler])

  val writeWorkloads = List(
    //Description Format - <Type of Operation>-<Inserts per WU>-<Parallel(par)/Sequential(seq)>
    //new Workload(desc = "ins-256k-par", threadChoices = List(4, 8, 16), numOpsPerWU =  2560, numWU = 30),
    new Workload(desc = "ins-1B-par", threadChoices = List(16), numOpsPerWU = 1e9.toInt, numWU = 1),
    //new Workload(desc = "ins-32k-seq", threadChoices = List(1), numOpsPerWU =  32000, numWU = 50)
  )
  val readWorkloads = List(
    //Description Format - <Type of Operation>-<Reads per WU>-<Parallel(par)/Sequential(seq)>
    new Workload(desc = "ins-256k-par", threadChoices = List(8, 16, 32), numOpsPerWU = 256000, numWU = 50),
    //new Workload(desc = "ins-1B-par", threadChoices = List(16, 32), numOpsPerWU = 1e9.toInt, numWU = 2),
    //new Workload(desc = "ins-32k-seq", threadChoices = List(1), numOpsPerWU = 32000, numWU = 50)
  )

  try {
    var jMeterTestPlan = new JMeterTestPlan(testOutputFile = config.outputFile())
    //Add a separate thread group + sampler to the Test plan for each thread choice in a given workload
    writeWorkloads.flatMap(workload => getJMeterThreadGroupAndSampler(workload, writeSamplers(db)))
      .foreach(x => jMeterTestPlan.addThreadGroupAndSampler(x._1, x._2))
//    readWorkloads.flatMap(workload => getJMeterThreadGroupAndSampler(workload, readSamplers(db)))
//      .foreach(x => jMeterTestPlan.addThreadGroupAndSampler(x._1, x._2))
    jMeterTestPlan.runPlan()
  } catch {
    case ex: Exception => ex.printStackTrace()
  }
  finally {
    dbConnectionParams.cleanup()
  }

  def getDBConnectionParams(db: String, connectionString: String): DBConnectionParams = {
    db match {
      case "cassandra" => getCassandraConnectionParams(connectionString, schemaName, variantTableName)
      case "mongodb" => getMongoDBConnectionParams(connectionString, schemaName, variantTableName)
    }
  }

  def getCassandraConnectionParams(connectionString: String, keyspaceName: String, variantTableName: String):
  CassandraConnectionParams = {
    val cassandra_nodes = connectionString.split(",")
    val cassandra_cluster = Cluster.builder().addContactPoints(cassandra_nodes: _*).build()
    val cassandra_session = cassandra_cluster.connect(keyspaceName)
    cassandra_session.execute("truncate %s".format(variantTableName))
    val insertEntityString = "insert into %s (species, chromosome, start_pos, entity_id, accession_id, raw_numeric_id) "
      .format(variantTableName) +
      "values (?, ?, ?, ?, ?, ?);"
    val blockReadString = "select * from %s where species = ? and chromosome = ? and start_pos >= ? and start_pos <= ?"
      .format(variantTableName)
    val preparedInsertStatement: PreparedStatement = cassandra_session.prepare(insertEntityString)
    val blockReadStatement: PreparedStatement = cassandra_session.prepare(blockReadString)

    CassandraConnectionParams(cassandra_cluster, cassandra_session, preparedInsertStatement, blockReadStatement)
  }

  def getMongoDBConnectionParams(connectionString: String, databaseName: String, collectionName: String):
  MongoDBConnectionParams = {
    val mongoClient = MongoClient(connectionString)
    val mongoDatabase = mongoClient.getDatabase(databaseName)
    var mongoCollection: MongoCollection[Document] = mongoDatabase.getCollection(collectionName)
      .withWriteConcern(WriteConcern.MAJORITY)
    Await.result(mongoCollection.deleteMany(Document()).toFuture(), Duration.Inf)
    MongoDBConnectionParams(mongoClient, mongoDatabase, mongoCollection)
  }

  def getJMeterThreadGroupAndSampler(workload: Workload, sampler: Class[_<: AbstractSampler]):
  List[(ThreadGroup, Sampler)] = {

    workload.threadChoices.map(numThreads =>
      getThreadGroupAndSamplerForWorkload(
        name = workload.desc + "-%d-threads".format(numThreads),
        numWU = workload.numWU,
        numThreads = numThreads,
        numInsertsPerThread = workload.numOpsPerWU / numThreads,
        sampler = sampler.newInstance()))
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

// Workload settings - WU => Workload Unit
class Workload(val desc: String, val threadChoices: List[Int], val numOpsPerWU: Int, val numWU: Int) {}

//Command line validation
class BenchmarkingArgParser(arguments: Seq[String], validDatabases: List[String]) extends ScallopConf(arguments) {

  type arg[T] = ScallopOption[T]

  val databaseType: arg[String] = opt[String](required = true, short = 'd',
    descr = "Database to test (ex: cassandra, mongodb etc.,)")
  val connectionString: arg[String] = opt[String](required = true, short = 'c',
    descr =
      """Database Connection Params:
        |For Cassandra, list of IPs: 192.168.0.1,192.168.0.2
        |For MongoDB, connection URL: mongodb://username:password@db1.example.net:27017,db2.example.net:27017
      """.stripMargin)
  val schemaName: arg[String] = opt[String](required = true, short = 's',
    descr = "Schema or keyspace to run the tests (ex: accessioning)")
  val variantTableName: arg[String] = opt[String](required = true, short = 't',
    descr = "Variant table on which to run the tests (ex: global_variant_lkp)")
  val JMeterHome: arg[String] = opt[String](required = true, short = 'j',
    descr = "Full path to the JMeter installation directory (ex: /opt/jmeter)")
  val outputFile: arg[String] = opt[String](required = true, short = 'o',
    descr = "Path to store the test output (ex: /opt/cassandra_test/results.jtl)")

  validate(databaseType) { (databaseName) =>
    if (validDatabases.contains(databaseName)) Right(Unit)
    else Left("Database name must be one of %s".format(validDatabases.mkString(",")))
  }
  verify()
}