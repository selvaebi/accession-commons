package uk.ac.ebi.eva.benchmarking_suite

import java.nio.file.Paths

import slick.jdbc.PostgresProfile.api._
import com.datastax.driver.core.{Cluster, ConsistencyLevel, PreparedStatement, SimpleStatement}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, ReadConcern, WriteConcern}
import org.apache.jmeter.control.LoopController
import org.apache.jmeter.samplers.{AbstractSampler, Sampler}
import org.apache.jmeter.threads.ThreadGroup
import org.apache.jmeter.util.JMeterUtils
import org.rogach.scallop._
import uk.ac.ebi.eva.benchmarking_suite.cassandra.{CassandraConnectionParams, CassandraLookupSampler, CassandraReadSampler, CassandraWriteSampler}
import uk.ac.ebi.eva.benchmarking_suite.mongodb.{MongoDBConnectionParams, MongoDBLookupSampler, MongoDBReadSampler, MongoDBWriteSampler}
import uk.ac.ebi.eva.benchmarking_suite.postgres.{PostgresConnectionParams, PostgresLookupSampler, PostgresReadSampler, PostgresWriteSampler}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ReadBenchmarkingConstants {
  //Upper bound on the number of records
  val numRecordsUB: Int = 500e6.toInt
  //Thread choices
  val threadChoices = List(8,16,32)
  //Upper bound on the number of threads
  val threadChoiceUB: Int = 32
  //Upper bound on the number of loops a workload is repeated for any given thread choice
  //In other words, upper bound on num-ops-per-wu - see README for a detailed description
  val numLoopsUB: Int = 2

  def getRandomChromosomeAndStartPos(randomNumGen: scala.util.Random): (String, Int) = {
    (randomNumGen.nextInt(ReadBenchmarkingConstants.threadChoiceUB).toString,
      randomNumGen.nextInt(ReadBenchmarkingConstants.numRecordsUB/ReadBenchmarkingConstants.threadChoiceUB) + 100)
  }
}

object BenchmarkingMain extends App {

  //Supported databases
  val validDatabases = List("cassandra", "mongodb", "postgres")

  //Parse command line arguments
  val config = new BenchmarkingArgParser(args, validDatabases)

  //Stateful JMeter initialization
  initJMeterEnvironment(config.JMeterHome())
  var jmeterProperties = JMeterUtils.getJMeterProperties

  //Database initialization
  val (db, connectionString, schemaName, variantTableName) =
    (config.databaseType(), config.connectionString(), config.schemaName(), config.variantTableName())
  val dbConnectionParams = getDBConnectionParams(db, connectionString)
  jmeterProperties.put("connectionParams", dbConnectionParams)

  val readSamplers = Map("cassandra" -> classOf[CassandraReadSampler], "mongodb" -> classOf[MongoDBReadSampler],
  "postgres" -> classOf[PostgresReadSampler])
  val writeSamplers = Map("cassandra" -> classOf[CassandraWriteSampler], "mongodb" -> classOf[MongoDBWriteSampler],
  "postgres" -> classOf[PostgresWriteSampler])
  val lookupSamplers = Map("cassandra" -> classOf[CassandraLookupSampler], "mongodb" -> classOf[MongoDBLookupSampler],
    "postgres" -> classOf[PostgresLookupSampler])

  //Load workload configuration from a JSON configuration file
  val workloadConfig = pureconfig.loadConfigFromFiles[WorkloadConfig](Seq(Paths.get(config.workloadConfigFile())))
  match {
    case Left(failure) =>
      println("Invalid Workload Configuration:" + failure.head.description)
      sys.exit(1)
    case Right(conf) => conf
  }

  try {
    var jMeterTestPlan = new JMeterTestPlan(testOutputFile = config.outputFile())
    //Add a separate thread group + sampler to the Test plan for each thread choice in a given workload
    workloadConfig.writeWorkloads.flatMap(workload => getJMeterThreadGroupAndSampler(workload, writeSamplers(db)))
      .foreach(x => jMeterTestPlan.addThreadGroupAndSampler(x._1, x._2))
    workloadConfig.readWorkloads.flatMap(workload => getJMeterThreadGroupAndSampler(workload, readSamplers(db)))
      .foreach(x => jMeterTestPlan.addThreadGroupAndSampler(x._1, x._2))
    workloadConfig.lookupWorkloads.flatMap(workload => getJMeterThreadGroupAndSampler(workload, lookupSamplers(db)))
      .foreach(x => jMeterTestPlan.addThreadGroupAndSampler(x._1, x._2))
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
      case "postgres" => getPostgresConnectionParams(connectionString, schemaName, variantTableName)
    }
  }

  def getPostgresConnectionParams(connectionString: String, schemaName: String, variantTableName: String):
  PostgresConnectionParams = {
    val postgresDBConnection = Database.forURL(connectionString, driver = "org.postgresql.Driver", user = "postgres")
    PostgresConnectionParams(postgresDBConnection, schemaName, variantTableName)
  }

  def getCassandraConnectionParams(connectionString: String, keyspaceName: String, variantTableName: String):
  CassandraConnectionParams = {
    val cassandraNodes = connectionString.split(",")
    val cassandraCluster = Cluster.builder().addContactPoints(cassandraNodes: _*).build()
    val cassandraSession = cassandraCluster.connect(keyspaceName)

    //Use two separate tables to facilitate reverse lookup instead of materialized views
    // See https://www.mail-archive.com/user@cassandra.apache.org/msg54073.html for materialized view deprecation
    val insertIntoLookup = "insert into %s (species, chromosome, start_pos, entity_id, accession_id, raw_numeric_id) "
      .format(variantTableName) + "values (?, ?, ?, ?, ?, ?);"
    val insertIntoReverseLookup = ("insert into %s_reverse (accession_id, raw_numeric_id, species, " +
      "chromosome, start_pos, entity_id) ").format(variantTableName) + "values (?, ?, ?, ?, ?, ?);"
    val blockReadString = "select * from %s where species = ? and chromosome = ? and start_pos >= ? and start_pos <= ?"
      .format(variantTableName)
    val lookupString = "select * from %s_reverse where accession_id = ?"
      .format(variantTableName)
    val lookupInsertStmt: PreparedStatement = cassandraSession.prepare(insertIntoLookup)
    val reverseLookupInsertStmt: PreparedStatement = cassandraSession.prepare(insertIntoReverseLookup)
    val blockReadStatement: PreparedStatement = cassandraSession.prepare(blockReadString)
      .setConsistencyLevel(ConsistencyLevel.QUORUM)
    val lookupStmt: PreparedStatement = cassandraSession.prepare(lookupString)
      .setConsistencyLevel(ConsistencyLevel.QUORUM)

    CassandraConnectionParams(cassandraCluster, cassandraSession, lookupInsertStmt,
      reverseLookupInsertStmt, blockReadStatement, lookupStmt)
  }

  def getMongoDBConnectionParams(connectionString: String, databaseName: String, collectionName: String):
  MongoDBConnectionParams = {
    val mongoClient = MongoClient(connectionString)
    val mongoDatabase = mongoClient.getDatabase(databaseName)
    var mongoCollection: MongoCollection[Document] = mongoDatabase.getCollection(collectionName)
      .withWriteConcern(WriteConcern.MAJORITY).withReadConcern(ReadConcern.MAJORITY)

    MongoDBConnectionParams(mongoClient, mongoDatabase, mongoCollection)
  }

  def getJMeterThreadGroupAndSampler(workload: Workload, sampler: Class[_<: AbstractSampler]):
  List[(ThreadGroup, Sampler)] = {
    workload.threadChoices.map(numThreads =>
      getThreadGroupAndSamplerForWorkload(
        name = "%s-%d-threads".format(workload.desc, numThreads),
        numWU = workload.numWU,
        numThreads = numThreads,
        numOpsPerThread = workload.numOpsPerWU / numThreads,
        sampler = sampler.newInstance()))
  }

  /**
    * Initialize JMeter Environment
    *
    * @param JMETER_HOME Full path to JMeter home (ex: /home/centos/apache-jmeter-3.3)
    */
  def initJMeterEnvironment(JMETER_HOME: String): Unit = {
    import org.apache.jmeter.util.JMeterUtils._
    setJMeterHome(JMETER_HOME)
    loadJMeterProperties("%s/bin/jmeter.properties".format(JMETER_HOME))
    initLocale()
  }

  def getThreadGroupAndSamplerForWorkload(name: String, numWU: Int, numThreads: Int, numOpsPerThread: Int,
                                          sampler: Sampler): (ThreadGroup, Sampler) = {
    //Sampler
    sampler.setName(name)
    sampler.setProperty("numOpsPerThread", numOpsPerThread)
    sampler.setProperty("threadChoice", numThreads)

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

class WorkloadConfig(val writeWorkloads: List[Workload], val readWorkloads: List[Workload],
                     val lookupWorkloads: List[Workload])

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
        |For Postgres, connection URL: jdbc:postgresql://pghost:5432/db1
      """.stripMargin)
  val schemaName: arg[String] = opt[String](required = true, short = 's',
    descr = "Schema or keyspace to run the tests (ex: accessioning)")
  val variantTableName: arg[String] = opt[String](required = true, short = 't',
    descr = "Variant table on which to run the tests (ex: global_variant_lkp)")
  val JMeterHome: arg[String] = opt[String](required = true, short = 'j',
    descr = "Full path to the JMeter installation directory (ex: /opt/jmeter)")
  val outputFile: arg[String] = opt[String](required = true, short = 'o',
    descr = "Path to store the test output (ex: /opt/cassandra_test/results.jtl)")
  val workloadConfigFile: arg[String] = opt[String](required = true, short = 'w',
    descr = "Path to workload configuration file (ex: /opt/cassandra_test/read_workloads.json")

  validate(databaseType) { (databaseName) =>
    if (validDatabases.contains(databaseName)) Right(Unit)
    else Left("Database name must be one of %s".format(validDatabases.mkString(",")))
  }
  verify()
}