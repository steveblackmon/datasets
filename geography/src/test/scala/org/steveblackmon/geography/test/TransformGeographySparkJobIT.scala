package org.steveblackmon.geography.test

import java.io.File
import java.io.FileReader
import java.io.LineNumberReader

import com.typesafe.config.ConfigFactory
import org.steveblackmon.geography.TransformGeographySparkJob
import org.apache.spark.sql.SparkSession
import org.apache.streams.config.StreamsConfigurator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class TransformGeographySparkJobIT {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[TransformGeographySparkJobIT])

  val master = StreamsConfigurator.getConfig.getString("spark.master")

  val session: SparkSession = SparkSession.builder().master(master).getOrCreate()

  val root = "target/test-classes/TransformGeographySparkJobIT"

  private val configfile = "target/test-classes/TransformGeographySparkJobIT/application.conf"

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
      StreamsConfigurator.getConfig.getConfig("io.emetry.pipelines.geography.TransformGeographySparkJob")
    } catch {
      case e : Exception => throw new SkipException("Skipping TransformGeographySparkJobIT because no TransformGeographySparkJob configuration has been provided", e)
    }
  }

  @Test
  @throws[Exception]
  def testTransformGeographySparkJob = {

    val job = new TransformGeographySparkJob()

    val thread = new Thread(job)
    thread.start()
    thread.join()

    //Assert.assertTrue(session.table("baseWines_df").count() == 1)
    //Assert.assertTrue(job.count() == 1)

    //checkPartFilesExistsAndNotEmpty(root, "basewines.jsonl")

    //Assert.assertTrue(session.table("wineprofiles_df").count() >= 1)
//    Assert.assertTrue(job.wineprofiles_lines.count() >= 1)

    //checkPartFilesExistsAndNotEmpty(root, "wineprofiles.jsonl")

    //Assert.assertTrue(session.table("priceItems_df").count() >= 1)
//    Assert.assertTrue(job.priceItems_lines.count() >= 1)

    //checkPartFilesExistsAndNotEmpty(root, "prices.jsonl")

    //Assert.assertTrue(session.table("ratingsUpdates_df").count() > 100)
//    Assert.assertTrue(job.ratings_lines.count() > 100)

    //checkPartFilesExistsAndNotEmpty(root, "ratings.jsonl")

  }

  @throws[Exception]
  def checkPartFilesExistsAndNotEmpty(root : String, folder : String) = {
    val partFilePath = s"${root}/${folder}/part-*"
 //   Assert.assertTrue(Files.list(partFilePath) > 0)
    val partFile = new File(partFilePath);
    val lineNumberReader = new LineNumberReader(new FileReader(partFile));
    lineNumberReader.skip(Long.MaxValue);
    val lines: Int = lineNumberReader.getLineNumber();
    Assert.assertNotEquals(lines, 0)
  }

}
