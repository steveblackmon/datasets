package org.steveblackmon.googleapis.civicinfo.test

import java.io.File

import com.typesafe.config.ConfigBeanFactory
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import org.apache.juneau.json.JsonParser
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.streams.config.StreamsConfigurator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtract
import org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtract.OcdDivisionIdCsvRow_schema
import org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtractRequest
import org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyUtils
import org.steveblackmon.googleapis.civicinfo.RepresentativeInfoByDivisionResponse
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class GoogleCivicOfficialsTaxonomyExtractIT {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[GoogleCivicOfficialsTaxonomyExtractIT])

  val master = StreamsConfigurator.getConfig.getString("spark.master")

  implicit final val session: SparkSession = SparkSession.builder().master(master).getOrCreate()

  import session.implicits._

  val root = "target/test-classes/GoogleCivicOfficialsTaxonomyExtractIT"

  private val configfile = "target/test-classes/GoogleCivicOfficialsTaxonomyExtractIT/application.conf"

  @BeforeClass(alwaysRun = true)
  @throws[Exception]
  @throws[SkipException]
  def setup(): Unit = {
    try {
      val conf = new File(configfile)
      Assert.assertTrue(conf.exists)
      Assert.assertTrue(conf.canRead)
      Assert.assertTrue(conf.isFile)
      StreamsConfigurator.addConfig(ConfigFactory.parseFileAnySyntax(conf))
      ConfigFactory.invalidateCaches()
      StreamsConfigurator.getConfig.getConfig("org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtractRequest")
    } catch {
      case e : Exception => throw new SkipException("Skipping GoogleCivicOfficialsTaxonomyExtractIT because no GoogleCivicOfficialsTaxonomyExtractRequest configuration has been provided", e)
    }
  }

  @Test
  @throws[Exception]
  def testGoogleCivicOfficialsTaxonomyExtract = {

    import org.steveblackmon.utils.test.DatasetsTestUtils._

    val requestConfig = StreamsConfigurator.getConfig().getConfig(classOf[GoogleCivicOfficialsTaxonomyExtractRequest].getCanonicalName)

    val requestConfigJson = requestConfig.root().render(ConfigRenderOptions.concise())

    val request: GoogleCivicOfficialsTaxonomyExtractRequest = JsonParser.DEFAULT.parse(requestConfigJson, classOf[GoogleCivicOfficialsTaxonomyExtractRequest])

    val job = new GoogleCivicOfficialsTaxonomyExtract(request)

    val thread = new Thread(job)
    thread.start()
    thread.join()

    Assert.assertEquals(job.ocdidentifiers_missing_limited_rdd.count, request.repinfoByDivisionMaxRequests)

    Assert.assertEquals(job.requests_rdd.count(), request.repinfoByDivisionMaxRequests)

    Assert.assertEquals(job.repinfo_newresponses_rdd.count(), request.repinfoByDivisionMaxRequests)

    Assert.assertEquals(job.repinfo_newresponses_jsonl.count(), request.repinfoByDivisionMaxRequests)

    val repinfo_newresponses_out = session.read.json(request.repinfoNewResponsesJsonlPath)
    Assert.assertEquals(repinfo_newresponses_out.count(), request.repinfoByDivisionMaxRequests)
    repinfo_newresponses_out.printSchema()

  }
}