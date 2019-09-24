package org.steveblackmon.googleapis.civicinfo

import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.steveblackmon.utils.JuneauParsers
import org.steveblackmon.utils.JuneauSerializers
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.ObjectMap
import org.apache.juneau.json.JsonParser
import org.apache.juneau.json.JsonSerializer
import org.apache.juneau.rest.client.RestCall
import org.apache.juneau.rest.client.RestClient
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.types._
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfigurator
import org.slf4j.LoggerFactory
import org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtract.OcdDivisionIdCsvRow
import org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtract.RepresentativeInfoByDivisionRequest
import com.typesafe.config.ConfigBeanFactory
import org.apache.spark.sql.Dataset

import scala.util.Try
import scala.collection.JavaConversions._

object GoogleCivicOfficialsTaxonomyExtract {

  val LOGGER = LoggerFactory.getLogger("GoogleCivicOfficialsTaxonomyExtract")

  final implicit val sparkSession = SparkSession.builder().getOrCreate()

  lazy val executorService: ExecutorService = Executors.newFixedThreadPool(1);

  implicit lazy val googleCivicConfiguration = new ComponentConfigurator[GoogleCivicConfiguration](classOf[GoogleCivicConfiguration]).detectConfiguration()

  def main(args: Array[String]) = {
    val job : GoogleCivicOfficialsTaxonomyExtract = new GoogleCivicOfficialsTaxonomyExtract()
    val future = executorService.submit(job)
    val execution = Try(future.get())
    if( execution.isFailure ) {
      execution.failed.get.printStackTrace(System.err)
      executorService.shutdown()
      System.exit(-1)
    } else {
      val response = execution.get
      val result = JuneauSerializers.URL.
        serialize(response)
      System.out.println(result)
      executorService.shutdown()
      System.exit(0)
    }
  }

  case class OcdDivisionIdCsvRow(
                                  id : String,
                                  name : String
                                )
  val OcdDivisionIdCsvRow_schema = ScalaReflection.schemaFor[OcdDivisionIdCsvRow].dataType.asInstanceOf[StructType]

  case class RepresentativeInfoByDivisionRequest( ocdId : String, recursive : Boolean = false )

  def clean(json : String) : String = JsonSerializer.DEFAULT.serialize(JsonParser.DEFAULT.parse(json, classOf[ObjectMap]))

  def callRepresentativeInfoByDivision(req : RepresentativeInfoByDivisionRequest)(implicit googleCivicConfiguration : GoogleCivicConfiguration) : RepresentativeInfoByDivisionResponse = {
    import scala.collection.JavaConversions._
    val id = URLEncoder.encode(req.ocdId, "UTF-8")
    val url = s"https://www.googleapis.com/civicinfo/v2/representatives/${id}"
    val uri : URI = new URIBuilder(url).build()
    lazy val parser = JsonParser.create().
      debug().
      ignoreUnknownBeanProperties(true).
      ignorePropertiesWithoutSetters(true).
      build()
    lazy val serializer = JsonSerializer.create().
      debug().
      trimEmptyCollections(true).
      trimNullProperties(true).
      trimEmptyMaps(true).
      build()
    val restClientBuilder = RestClient.
      create().
      beansRequireSerializable(true).
      debug().
      disableAutomaticRetries().
      disableCookieManagement().
      disableRedirectHandling().
      json().
      parser(parser).
      rootUrl(uri).
      serializer(serializer)

    val call : RestCall = restClientBuilder.
      query("key", googleCivicConfiguration.token).
      build().
      doGet(uri)

    Thread.sleep(1000)

    val responseString = call.getResponseAsString
    val responseObject = JsonParser.DEFAULT.parse(responseString, classOf[RepresentativeInfoByDivisionResponse])
//    val responseObject = call.getResponse(classOf[RepresentativeInfoByDivisionResponse])

    responseObject
  }
  def safeCallRepresentativeInfoByDivision(req : RepresentativeInfoByDivisionRequest) : Option[RepresentativeInfoByDivisionResponse] = {
    val try_callRepresentativeInfoByDivision = Try(callRepresentativeInfoByDivision(req))
    try_callRepresentativeInfoByDivision.toOption
  }
  def jsonToRepresentativeInfoByDivision(in : String) : RepresentativeInfoByDivisionResponse = {
    val out = JsonParser.DEFAULT.parse[RepresentativeInfoByDivisionResponse](in, classOf[RepresentativeInfoByDivisionResponse])
    out
  }
  def safeJsonToRepresentativeInfoByDivision(in : String) : Option[RepresentativeInfoByDivisionResponse] = {
    val try_callRepresentativeInfoByDivision = Try(jsonToRepresentativeInfoByDivision(in))
    try_callRepresentativeInfoByDivision.toOption
  }
  def representativeInfoByDivisionToJson(in : RepresentativeInfoByDivisionResponse) : String = {
    val out = JsonSerializer.DEFAULT.serialize(in)
    out
  }
  def safeRepresentativeInfoByDivisionToJson(in : RepresentativeInfoByDivisionResponse) : Option[String] = {
    val try_callRepresentativeInfoByDivision = Try(representativeInfoByDivisionToJson(in))
    try_callRepresentativeInfoByDivision.toOption
  }
}

class GoogleCivicOfficialsTaxonomyExtract(
     val request : GoogleCivicOfficialsTaxonomyExtractRequest =
        ConfigBeanFactory.create (StreamsConfigurator.getConfig().getConfig(classOf[GoogleCivicOfficialsTaxonomyExtractRequest].getCanonicalName), classOf[GoogleCivicOfficialsTaxonomyExtractRequest] )
    )( implicit sparkSession : SparkSession ) extends Serializable with Runnable {

  import GoogleCivicOfficialsTaxonomyUtils._
  import GoogleCivicOfficialsTaxonomyExtract.OcdDivisionIdCsvRow_schema
  import GoogleCivicOfficialsTaxonomyExtract.safeCallRepresentativeInfoByDivision
  import GoogleCivicOfficialsTaxonomyExtract.safeJsonToRepresentativeInfoByDivision
  import GoogleCivicOfficialsTaxonomyExtract.safeRepresentativeInfoByDivisionToJson

  val sparkContext = sparkSession.sparkContext
  val sqlContext = sparkSession.sqlContext

  //import sparkSession.sqlContext.implicits._
  import sparkSession.implicits._

  /*
  Load a list of ocd identifiers to extract
   */
  val try_ocdidentifiers_csv_df: Try[DataFrame] = Try(sqlContext.read.option("header", true).format("csv").schema(OcdDivisionIdCsvRow_schema).load(request.ocdIdentifiersCsvPath))

  val try_ocdidentifiers_rdd: Try[RDD[String]] = Try(try_ocdidentifiers_csv_df.get.select($"id").rdd.map(x => x.getString(0)))

  /*
  See which identifiers have already been extracted, if any
   */

//  val try_repinfo_responses_jsonl: Try[DataFrame] = Try(sqlContext.read.json(request.repinfoExistingResponsesJsonlPath))
  val try_repinfo_responses_jsonl: Try[RDD[String]] = Try {
    val textFile = sparkContext.textFile(request.repinfoExistingResponsesJsonlPath)
    assert( textFile.count() > 0 )
    textFile
  }

//  val try_repinfo_responses_ds: Try[Dataset[RepresentativeInfoByDivisionResponse]] = Try(try_repinfo_responses_jsonl.get.as[RepresentativeInfoByDivisionResponse]/*(RepresentativeInfoByDivisionResponseEncoder)*/)
  val try_repinfo_responses_rdd: Try[RDD[RepresentativeInfoByDivisionResponse]] = Try {
    try_repinfo_responses_jsonl.get.flatMap(safeJsonToRepresentativeInfoByDivision)
  }

  val try_repinfo_responses_divisionids_rdd: Try[RDD[String]] = Try {
    try_repinfo_responses_rdd.get.flatMap(x => x.divisions.keysIterator)
  }

  /*
  Filter out items that we've already extracted
   */
  val ocdidentifiers_missing_rdd: RDD[String] = (try_ocdidentifiers_rdd.isSuccess, try_repinfo_responses_divisionids_rdd.isSuccess) match {
    case (true, true) => try_ocdidentifiers_rdd.get.subtract(try_repinfo_responses_divisionids_rdd.get)
    case (true, false) => try_ocdidentifiers_rdd.get
    case (false, _) => throw new Exception("no list of ocdidentifiers available")
  }

  val ocdidentifiers_missing_ordered_rdd: RDD[String] = ocdidentifiers_missing_rdd.sortBy[String]({x => x})

  /*
  Generate requests
   */
  val ocdidentifiers_missing_limited_rdd: RDD[String] = sparkContext.parallelize(ocdidentifiers_missing_ordered_rdd.take(request.repinfoByDivisionMaxRequests.intValue())).persist

  val requests_rdd : RDD[RepresentativeInfoByDivisionRequest] = ocdidentifiers_missing_limited_rdd
    .map(x => RepresentativeInfoByDivisionRequest(ocdId = x))

  /*
  Call getRepresentativeInfoByDivision
   */
//  val responses_raw_json_ds: Dataset[RepresentativeInfoByDivisionResponse] = requests_ds.flatMap(safeCallRepresentativeInfoByDivision(_)).persist
//  responses_raw_json_ds.createOrReplaceTempView("responses_raw_json_ds")
//  val responses_clean_json_ds = responses_raw_json_ds.map(x => clean(x)).persist
//  responses_clean_json_ds.createOrReplaceTempView("responses_clean_json_ds")
  val repinfo_newresponses_rdd: RDD[RepresentativeInfoByDivisionResponse] = requests_rdd.flatMap(safeCallRepresentativeInfoByDivision(_)).persist
  val repinfo_newresponses_jsonl: RDD[String] = repinfo_newresponses_rdd.flatMap(safeRepresentativeInfoByDivisionToJson(_)).persist

  /*
  Prepare dataset to write to disk
   */
//  val repinfo_newresponses_jsonl_ds: Dataset[String] = repinfo_newresponses_ds.map(x => JsonSerializer.DEFAULT.serialize(x)).persist
//  val repinfo_newresponses_jsonl_rdd: RDD[String] = repinfo_newresponses_jsonl_ds.toDF.rdd.map(x => x.getString(0)).persist

  override def run() = {
    repinfo_newresponses_jsonl.saveAsTextFile(request.repinfoNewResponsesJsonlPath)
  }

}
