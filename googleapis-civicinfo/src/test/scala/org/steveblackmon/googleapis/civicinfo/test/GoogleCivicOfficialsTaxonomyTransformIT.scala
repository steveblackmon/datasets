package org.steveblackmon.googleapis.civicinfo.test

import java.io.File

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import org.apache.juneau.json.JsonParser
import org.apache.spark.sql.SparkSession
import org.apache.streams.config.StreamsConfigurator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyTransform
import org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyTransformRequest
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class GoogleCivicOfficialsTaxonomyTransformIT {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[GoogleCivicOfficialsTaxonomyTransformIT])

  val master = StreamsConfigurator.getConfig.getString("spark.master")

  implicit final val session: SparkSession = SparkSession.builder().master(master).getOrCreate()

  val root = "target/test-classes/GoogleCivicOfficialsTaxonomyTransformIT"

  private val configfile = "target/test-classes/GoogleCivicOfficialsTaxonomyTransformIT/application.conf"

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
      StreamsConfigurator.getConfig.getConfig("org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyTransformRequest")
    } catch {
      case e : Exception => throw new SkipException("Skipping GoogleCivicOfficialsTaxonomyTransformIT because no GoogleCivicOfficialsTaxonomyTransformRequest configuration has been provided", e)
    }
  }

  @Test
  @throws[Exception]
  def testGoogleCivicOfficialsTaxonomyTransform = {

    val requestConfig = StreamsConfigurator.getConfig().getConfig(classOf[GoogleCivicOfficialsTaxonomyTransformRequest].getCanonicalName)

    val requestConfigJson = requestConfig.root().render(ConfigRenderOptions.concise())

    val request: GoogleCivicOfficialsTaxonomyTransformRequest = JsonParser.DEFAULT.parse(requestConfigJson, classOf[GoogleCivicOfficialsTaxonomyTransformRequest])

    val job = new GoogleCivicOfficialsTaxonomyTransform(request)

    val thread = new Thread(job)
    thread.start()
    thread.join()

    Assert.assertNotEquals(job.repinfo_responses_json_rdd.count, 0)

    Assert.assertNotEquals(job.officials_rdd.count(), 0)

    Assert.assertNotEquals(job.officials_ds.count(), 0)

    Assert.assertNotEquals(job.officials_outputrows_df.count(), 0)

    val officials_csv_out = session.read.csv(request.officialsCsvOutputPath)
    Assert.assertNotEquals(officials_csv_out.count(), 0)
    officials_csv_out.printSchema()

    val officials_jsonl_out = session.read.format("json").load(request.officialsJsonlOutputPath)
    Assert.assertNotEquals(officials_jsonl_out.count(), 0)
    officials_jsonl_out.printSchema()

  }
}