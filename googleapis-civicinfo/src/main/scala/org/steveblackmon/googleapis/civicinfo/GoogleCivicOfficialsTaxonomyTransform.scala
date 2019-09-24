package org.steveblackmon.googleapis.civicinfo

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.steveblackmon.utils.JuneauParsers
import org.steveblackmon.utils.JuneauSerializers
import org.apache.streams.config.ComponentConfigurator
import org.slf4j.LoggerFactory

import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.apache.juneau.ObjectList
import org.apache.juneau.ObjectMap
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession

import scala.collection.JavaConversions._

object GoogleCivicOfficialsTaxonomyTransform {

  val LOGGER = LoggerFactory.getLogger("GoogleCivicOfficialsTaxonomyTransform")

  final implicit val sparkSession = SparkSession.builder().getOrCreate()

  lazy val executorService: ExecutorService = Executors.newFixedThreadPool(1);

  def main(args: Array[String]) = {
    val argString = args.mkString(" ")
    val request = JuneauParsers.URL.
      parse(argString, classOf[GoogleCivicOfficialsTaxonomyTransformRequest])
    val job : GoogleCivicOfficialsTaxonomyTransform = new GoogleCivicOfficialsTaxonomyTransform(request)
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

  case class Division( id : String, name : String )

  case class Office( divisionId : String, name : String )

  case class Geography( country : String, state : Option[String], county : Option[String], place : Option[String] )

  case class Official( name : String, party : String, photoUrl : Option[String], phones : List[String], emails : List[String], urls : List[String], twitter : Option[String], facebook : Option[String], youtube : Option[String] )

  case class OutputRow( division : Division, geography : Geography, office : Office, official : Official )

  def Entry_to_Division( e : java.util.Map.Entry[String, Object] ) : Division = {
    val id = e.getKey()
    val map = e.getValue().asInstanceOf[ObjectMap]
    Division(id, map.getString("name"))
  }

  def Item_to_Office( m : ObjectMap ) : Office = {
    Office(m.getString("divisionId"), m.getString("name"))
  }

  def IdGivenTypeFromObjectList( objectList : ObjectList, `type` : String ) : Option[String] = {
    import scala.collection.JavaConversions._
    Try { objectList.elements(classOf[ObjectMap]).iterator().filter(x => x.get("type") == `type`).toList.get(0).getString("id") }.toOption
  }

  def Item_to_Official( m : ObjectMap ) : Official = {
    import scala.collection.JavaConversions._
    val name = m.getString("name")
    val party = m.getString("party")
    val photoUrl = Try { m.getString("photoUrl") }.toOption
    val phones = m.getList("phones", classOf[String], new java.util.ArrayList[String]())
    val emails = m.getList("emails", classOf[String], new java.util.ArrayList[String]())
    val urls = m.getList("urls", classOf[String], new java.util.ArrayList[String]())
    val channels = m.getObjectList("channels")
    val twitter = IdGivenTypeFromObjectList(channels, "Twitter")
    val facebook = IdGivenTypeFromObjectList(channels, "Facebook")
    val youtube = IdGivenTypeFromObjectList(channels, "YouTube")
    Official(name, party, photoUrl, phones.toList, emails.toList, urls.toList, twitter, facebook, youtube)
  }

  def officialIndexMatch(officeMap : ObjectMap, index : Integer) : Boolean = {
    println(s"officeMap: ${officeMap}")
    println(s"index: ${index}")
    val officialIndices = officeMap.getList("officialIndices", classOf[Integer], new java.util.ArrayList[Integer]())
    println(s"officialIndices: ${officialIndices}")
    val doesmatch = if( officialIndices.contains(index)) true else false
    println(s"doesmatch: ${doesmatch}")
    doesmatch
  }

  def Office_with_officialIndex(officesList : ObjectList, index : Integer) : Office = {
    import scala.collection.JavaConversions._
    println(s"officesList: ${officesList.toString}")
    println(s"index: ${index}")
    Try {
      val officeElements = officesList.elements(classOf[ObjectMap]).toList
      println(s"officeElements Size: ${officeElements.size}")
      val officialMatching : List[ObjectMap] = officeElements.filter(officeMap => officialIndexMatch(officeMap, index)).toList
      println(s"officialMatching Size: ${officialMatching.size}")
      val indexmatch = officialMatching.toList.head
      println(s"indexmatch: ${indexmatch}")
      val officematch = Office(indexmatch.getString("divisionId"), indexmatch.getString("name"))
      println(s"officematch: ${officematch}")
      officematch
    } match {
      case Success(x) => x
      case Failure(x) => Office("none", "none")
    }
  }

  def geographyFromOcdId( ocdId : String ) : Geography = {

    val countryPattern = "country:(\\w+)".r
    val country = countryPattern.findAllMatchIn(ocdId).next.group(1)
    val statePattern = "state:(\\w+)".r
    val stateMatch = statePattern.findAllMatchIn(ocdId)
    val state = stateMatch.hasNext match {
      case true => Some(stateMatch.next.group(1));
      case false => None
    }
    val countyPattern = "county:(\\w+)".r
    val countyMatch = countyPattern.findAllMatchIn(ocdId)
    val county = countyMatch.hasNext match {
      case true => Some(countyMatch.next.group(1));
      case false => None
    }
    val placePattern = "place:(\\w+)".r
    val placeMatch = placePattern.findAllMatchIn(ocdId)
    val place = placeMatch.hasNext match {
      case true => Some(placeMatch.next.group(1));
      case false => None
    }
    val geography = Geography( country, state, county, place )
    geography
  }

  def representativeInfoByDivisionList_Officials(representativeInfoByDivision_json : String) : Iterator[OutputRow] = {
    import org.apache.juneau.json.JsonParser
    import org.apache.juneau.ObjectList
    import org.apache.juneau.ObjectMap
    import org.apache.juneau.utils.PojoRest
    import scala.collection.JavaConversions._
    import scala.collection.mutable.ListBuffer
    var outputList: ListBuffer[OutputRow] = ListBuffer()
    Try {
      val root = JsonParser.DEFAULT.parse(representativeInfoByDivision_json, classOf[ObjectMap])
      val divisions = root.get("divisions", classOf[ObjectMap]).entrySet().toList
      println(s"divisions Size: ${divisions.size()}")
      println(s"divisions: ${divisions}")
      val offices = root.get("offices", classOf[ObjectList])
      println(s"offices Size: ${offices.size()}")
      println(s"offices: ${offices}")
      val officials = root.get("officials", classOf[ObjectList])
      println(s"officials Size: ${officials.size()}")
      println(s"officials: ${officials}")
      for((x,i) <- officials.view.zipWithIndex) {
        val official = Item_to_Official(x.asInstanceOf[ObjectMap])
        println(s"official: ${official}")
        // find office that refers to this official index 'i'
        val office = Office_with_officialIndex(offices, i.asInstanceOf[Integer])
        println(s"office: ${office}")
        // find division with id matching office
        val division = Entry_to_Division(divisions.head)
        println(s"division: ${division}")
        val geography = geographyFromOcdId(division.id)
        println(s"geography: ${geography}")
        val out : OutputRow = OutputRow(division, geography, office, official)
        println(s"out: ${out}")
        outputList += out
      }
      println(s"outputList.size: ${outputList.size}")
      outputList.iterator()
    } match {
      case Success(x) => x
      case Failure(x) => Iterator()
    }
  }

}

class GoogleCivicOfficialsTaxonomyTransform(
    val request : GoogleCivicOfficialsTaxonomyTransformRequest = new ComponentConfigurator[GoogleCivicOfficialsTaxonomyTransformRequest](classOf[GoogleCivicOfficialsTaxonomyTransformRequest]).detectConfiguration()
  ) extends Serializable with Runnable {

  import GoogleCivicOfficialsTaxonomyTransform.representativeInfoByDivisionList_Officials
  import org.apache.spark.sql.SparkSession

  val sparkSession = SparkSession.builder().getOrCreate()
  val sparkContext = sparkSession.sparkContext
  val sqlContext = sparkSession.sqlContext

  import sparkSession.sqlContext.implicits._

  val repinfo_responses_json_rdd: RDD[String] = sparkContext.textFile(request.repinfoResponsesJsonlPath)

  val officials_rdd: RDD[GoogleCivicOfficialsTaxonomyTransform.OutputRow] = repinfo_responses_json_rdd.flatMap(representativeInfoByDivisionList_Officials)

  val officials_ds: Dataset[GoogleCivicOfficialsTaxonomyTransform.OutputRow] = officials_rdd.toDS

  val outputrows_df: DataFrame = officials_ds.select(
    $"division.id".as("ocdId"),
    $"geography.country".as("country"),
    $"geography.state".as("state"),
    $"geography.county".as("county"),
    $"geography.place".as("place"),
    $"division.name".as("division"),
    $"office.name".as("office"),
    $"official.name",
    $"official.party",
    $"official.photoUrl",
    $"official.phones".cast("string"),
    $"official.emails".cast("string"),
    $"official.urls".cast("string"),
    $"official.twitter",
    $"official.facebook",
    $"official.youtube"
  )
  override def run() = {
    outputrows_df.coalesce(1).write.mode("overwrite").option("header", "true").option("quoteMode", "always").csv(request.officialsCsvOutputPath)
    outputrows_df.coalesce(1).write.mode("overwrite").format("json").save(request.officialsJsonlOutputPath)
  }

}
