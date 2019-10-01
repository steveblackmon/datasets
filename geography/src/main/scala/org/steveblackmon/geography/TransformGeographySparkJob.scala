package org.steveblackmon.geography

import org.apache.spark.serializer.KryoSerializer
import org.steveblackmon.geography.LoadGeographySchemaSparkJob.ContinentRow
import org.steveblackmon.geography.LoadGeographySchemaSparkJob.CountryRow
import org.steveblackmon.geography.LoadGeographySchemaSparkJob.CityRow
import org.steveblackmon.geography.LoadGeographySchemaSparkJob.CountyRow
import org.steveblackmon.geography.LoadGeographySchemaSparkJob.PostalRow
import org.steveblackmon.geography.LoadGeographySchemaSparkJob.StateRow
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.geosparksql.expressions.ST_Point
import org.apache.spark.sql.types.StructType
import org.apache.streams.config.StreamsConfigurator
import org.datasyslab.geospark.serde.GeoSparkKryoRegistrator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.datasyslab.geosparksql.UDF.UdfRegistrator
object TransformGeographySparkJob {

  val LOGGER: Logger = LoggerFactory.getLogger(classOf[TransformGeographySparkJob])

  val sparkSession = SparkSession.builder()
    .config("spark.serializer", classOf[KryoSerializer].getName)
    .config("spark.kryo.registrator", classOf[GeoSparkKryoRegistrator].getName)
    .getOrCreate()

  val sparkContext = sparkSession.sparkContext
  val sqlContext = sparkSession.sqlContext

  val rootconfig = StreamsConfigurator.getConfig
  lazy val config = rootconfig.getConfig(classOf[TransformGeographySparkJob].getCanonicalName)

  import sparkSession.implicits._

  case class ContinentCsvRow(
                              Code : String,
                              Name : String
                            )

  val continents_csv_schema = ScalaReflection.schemaFor[ContinentCsvRow].dataType.asInstanceOf[org.apache.spark.sql.types.StructType]

  case class CountryCsvRow(
                            ISO : String,
                            ISO3 : String,
                            `ISO-Numeric` : Int,
                            fips : String,
                            Country : String,
                            Capital : String,
                            `Area(in sq km)` : Double,
                            Population : Int,
                            Continent : String,
                            tld : String,
                            CurrencyCode : String,
                            CurrencyName : String,
                            Phone : String,
                            `Postal Code Format` : String,
                            `Postal Code Regex` : String,
                            Languages : String,
                            geonameid : Int,
                            neighbours : String,
                            EquivalentFipsCode : String
                          )

  val countries_csv_schema = ScalaReflection.schemaFor[CountryCsvRow].dataType.asInstanceOf[StructType]

  case class Admin1CodesTsvRow(
                                `code` : String,
                                `name` : String,
                                asciiname : String,
                                geonameId : Integer
                              )

  val admin1codes_tsv_schema = ScalaReflection.schemaFor[Admin1CodesTsvRow].dataType.asInstanceOf[StructType]

  case class Admin2CodesTsvRow(
                                `concatenated` : String,
                                `country_code` : String,
                                `state_code` : String,
                                name : String,
                                asciiname : String,
                                geonameId : Integer
                              )

  val admin2codes_tsv_schema = ScalaReflection.schemaFor[Admin2CodesTsvRow].dataType.asInstanceOf[StructType]

  case class CityCsvRow(
    geonameid : Int,
    name : String,
    asciiname : String,
    alternatenames : String,
    latitude: Double,
    longitude: Double,
    `feature class` : String,
    `feature code` : String,
    `country code` : String,
    cc2 : String,
    `admin1 code` : String,
    `admin2 code` : String,
    `admin3 code` : String,
    `admin4 code` : String,
    population : Int,
    elevation : Int,
    dem : Int,
    timezone : String
  )

  val cities_csv_schema = ScalaReflection.schemaFor[CityCsvRow].dataType.asInstanceOf[StructType]

  case class PostalCodesTsvRow(
                                `country code` : String,
                                `postal code` : String,
                                `place name` : String,
                                `admin name1` : String,
                                `admin code1` : String,
                                `admin name2` : String,
                                `admin code2` : String,
                                `admin name3` : String,
                                `admin code3` : String,
                                latitude : Double,
                                longitude : Double,
                                accuracy : Integer
                              )

  val postalcodes_tsv_schema = ScalaReflection.schemaFor[PostalCodesTsvRow].dataType.asInstanceOf[StructType]

  case class GeonamePoint( geonameid : Int, center : ST_Point, radius : Double)

  def continents_csv_ds_to_continents_db(input : Dataset[ContinentCsvRow]) : Dataset[ContinentRow] = {
    input.map(row => ContinentRow(row.Code, row.Name))
  }

  def countries_csv_ds_to_countries_db(input : Dataset[CountryCsvRow]) : Dataset[CountryRow] = {
    input.map(row => CountryRow(row.geonameid, row.ISO, row.Country, row.Continent))
  }

}

class TransformGeographySparkJob extends Runnable with java.io.Serializable {

  import TransformGeographySparkJob._

  import org.apache.spark.sql.catalyst.expressions._

  val sparkSession = SparkSession.builder().getOrCreate()

  import org.apache.spark.sql.functions._
  import sqlContext.implicits._

  val inputRoot = config.getString("inputRoot")
  val outputRoot = config.getString("outputRoot")
  val tableRoot = config.getString("tableRoot")

  val continents_csv_path = config.getString("continents_csv_path")
  val countries_csv_path = config.getString("countries_csv_path")
  val admin1codes_tsv_path = config.getString("admin1codes_tsv_path")
  val admin2codes_tsv_path = config.getString("admin2codes_tsv_path")
  val cities_csv_path = config.getString("cities_csv_path")
  val postalcodes_tsv_path = config.getString("postalcodes_tsv_path")

  // begin define file inputs

  val continents_csv_df = sqlContext.read.format("com.databricks.spark.csv").
    option("header", true).
    option("inferSchema", true).
    load(inputRoot + continents_csv_path)
  continents_csv_df.createOrReplaceTempView("continents_csv_df")

  val continents_csv_ds = continents_csv_df.as[ContinentCsvRow]
  continents_csv_ds.createOrReplaceTempView("continents_csv_ds")

  val countries_csv_df = sqlContext.read.format("com.databricks.spark.csv").
    option("delimiter", "\t").
    option("header", true).
    option("inferSchema", true).
    load(inputRoot + countries_csv_path)
  countries_csv_df.createOrReplaceTempView("countries_csv_df")

  val countries_csv_ds = countries_csv_df.as[CountryCsvRow]
  countries_csv_ds.createOrReplaceTempView("countries_csv_ds")

  val admin1codes_tsv_df = sqlContext.read.format("com.databricks.spark.csv").
    option("delimiter", "\t").
    option("header", "false").
    schema(admin1codes_tsv_schema).
    load(inputRoot + admin1codes_tsv_path)
  admin1codes_tsv_df.createOrReplaceTempView("admin1codes_tsv_df")

  val admin1codes_tsv_ds = admin1codes_tsv_df.as[Admin1CodesTsvRow]
  admin1codes_tsv_ds.createOrReplaceTempView("admin1codes_tsv_ds")

  val admin2codes_tsv_df = sqlContext.read.format("com.databricks.spark.csv").
    option("delimiter", "\t").
    option("header", "false").
    schema(admin2codes_tsv_schema).
    load(inputRoot + admin2codes_tsv_path).
    filter($"concatenated".isNotNull)
  admin2codes_tsv_df.createOrReplaceTempView("admin2codes_tsv_df")

  val admin2codes_tsv_ds = admin2codes_tsv_df.as[Admin2CodesTsvRow]
  admin2codes_tsv_ds.createOrReplaceTempView("admin2codes_tsv_ds")

  val cities_csv_df = sqlContext.read.format("com.databricks.spark.csv").
    option("delimiter", "\t").
    option("header", "false").
    schema(cities_csv_schema).
    load(inputRoot + cities_csv_path)
  cities_csv_df.createOrReplaceTempView("cities_csv_df")

  val cities_csv_ds = cities_csv_df.as[CityCsvRow]
  cities_csv_ds.createOrReplaceTempView("cities_csv_ds")

  val postalcodes_tsv_df = sqlContext.read.format("com.databricks.spark.csv").
    option("delimiter", "\t").
    option("header", "true").
    option("ignoreHeader", "true").
    schema(postalcodes_tsv_schema).
    load(inputRoot + postalcodes_tsv_path)
  postalcodes_tsv_df.createOrReplaceTempView("postalcodes_tsv_df")

  val postalcodes_tsv_ds = postalcodes_tsv_df.
    filter( postalcodes_tsv_df("country code").isNotNull ).
    filter( postalcodes_tsv_df("country code") === "US" ).
    filter( postalcodes_tsv_df("admin code1").isNotNull ).
    filter( postalcodes_tsv_df("admin code2").isNotNull ).
    as[PostalCodesTsvRow]
  postalcodes_tsv_ds.createOrReplaceTempView("postalcodes_tsv_ds")

  // end define file inputs

  // start transformations

  val continents_db: Dataset[ContinentRow] = continents_csv_ds.transform(continents_csv_ds_to_continents_db)
  continents_db.persist().createOrReplaceTempView("continents_db")

  val countries_db: Dataset[CountryRow] = countries_csv_ds.transform(countries_csv_ds_to_countries_db)
  countries_db.persist().createOrReplaceTempView("countries_db")

  val states_join_df = admin1codes_tsv_ds.
    withColumn("country_code", $"code".substr(lit(0), lit(2))).
    join(countries_db).
    where( $"country_code" === countries_db("code"))

  val states_db: Dataset[StateRow] = states_join_df.
    select(
      admin1codes_tsv_ds("geonameid").as("id"),
      admin1codes_tsv_ds("code").as("code"),
      substring_index(admin1codes_tsv_ds("code"), ".", -1).as("state_code"),
      countries_db("code").as("country_code"),
      countries_db("id").as("country_id"),
      admin1codes_tsv_ds("asciiname").as("name")
    ).as[StateRow]
  states_db.persist().createOrReplaceTempView("states_db")

  val counties_join_df = admin2codes_tsv_ds.
    filter( admin2codes_tsv_ds("concatenated").isNotNull).
    withColumn("county_fips", substring_index(admin2codes_tsv_ds("concatenated"), ".", -1)).
    join(countries_db, admin2codes_tsv_ds("country_code") === countries_db("code"), "outer").
    withColumn("state_concat", concat(admin2codes_tsv_ds("country_code"), lit("."), admin2codes_tsv_ds("state_code"))).
    join(states_db, $"state_concat" === states_db("code"), "outer")

  val counties_db: Dataset[CountyRow] = counties_join_df.
    select(
      admin2codes_tsv_ds("geonameid").as("id"),
      admin2codes_tsv_ds("concatenated").as("code"),
      //countries_db("code").as("country_code"),
      countries_db("id").as("country_id"),
      //states_db("code").as("state_code"),
      states_db("id").as("state_id"),
      admin2codes_tsv_ds("asciiname").as("name"),
      $"county_fips".cast("Integer").as("fips")
    ).filter($"code".startsWith("US")).as[CountyRow]
  counties_db.persist().createOrReplaceTempView("counties_db")

  val postalcodes_join_df = postalcodes_tsv_ds.
    join(countries_db, postalcodes_tsv_ds("country code") === countries_db("code")).
    withColumn("state_concat", concat(postalcodes_tsv_ds("country code"), lit("."), postalcodes_tsv_ds("admin code1"))).
    join(states_db, $"state_concat" === states_db("code")).
    withColumn("county_concat", concat(postalcodes_tsv_ds("country code"), lit("."), postalcodes_tsv_ds("admin code1"), lit("."), postalcodes_tsv_ds("admin code2"))).
    join(counties_db, $"county_concat" === counties_db("code"))

  val postalcodes_db: Dataset[PostalRow] = postalcodes_join_df.
    select(
      postalcodes_tsv_ds("postal code").cast("Integer").as("id"),
      postalcodes_tsv_ds("place name").as("name"),
      //countries_db("code").as("country_code"),
      countries_db("id").as("country_id"),
      //states_db("code").as("state_code"),
      states_db("id").as("state_id"),
      counties_db("id").as("county_id"),
      postalcodes_tsv_ds("latitude").as("latitude"),
      postalcodes_tsv_ds("longitude").as("longitude")
    ).as[PostalRow]
  postalcodes_db.persist().createOrReplaceTempView("postalcodes_db")

  val topcities_csv_ds = cities_csv_ds.
    orderBy(desc("population")).
    limit(5000)

  val markets_join_df = topcities_csv_ds.
    join(countries_db, cities_csv_ds("country code") === countries_db("code") ).
    withColumn("state_concat", concat(topcities_csv_ds("country code"), lit("."), topcities_csv_ds("admin1 code"))).
    join(states_db, $"state_concat" === states_db("code") ).
    withColumn("county_concat", concat(topcities_csv_ds("country code"), lit("."), topcities_csv_ds("admin1 code"), lit("."), topcities_csv_ds("admin2 code"))).
    join(counties_db, $"county_concat" === counties_db("code"), "outer")

  val cities_db = markets_join_df.
    select(
      cities_csv_ds("geonameid").as("id"),
      cities_csv_ds("name").as("city"),
      countries_db("id").as("country_id"),
      states_db("id").as("state_id"),
      counties_db("id").as("county_id"),
      cities_csv_ds("latitude").as("latitude"),
      cities_csv_ds("longitude").as("longitude")
    ).filter($"id".isNotNull).as[CityRow]
  cities_db.persist().createOrReplaceTempView("cities_db")

  val cities_points_ds: Dataset[GeonamePoint] = cities_db.
    filter(cities_db("id").isNotNull).
    select(
      cities_db("id").as("geonameid"),
      ST_Point(cities_db("latitude"), cities_db("longitude")).as("center"),
      lit(.5).as("radius")
    ).as[GeonamePoint]
  cities_points_ds.persist().createOrReplaceTempView("cities_points_ds")

  val postals_points_ds: Dataset[GeonamePoint] = postalcodes_db.
    filter(postalcodes_db("id").isNotNull).
    select(
      postalcodes_db("id").as("geonameid"),
      ST_Point(postalcodes_db("latitude").cast("Decimal"), postalcodes_db("longitude").cast("Decimal")).as("center"),
      lit(.2).as("radius")
    ).as[GeonamePoint]
  postals_points_ds.persist().createOrReplaceTempView("postals_points_ds")

  override def run() = {

    sparkSession.sqlContext.cacheTable("countries_db")

    continents_db.coalesce(1).write.option("header",true).csv(outputRoot + "continents")
    countries_db.coalesce(1).write.option("header",true).csv(outputRoot + "countries")
    states_db.coalesce(1).write.option("header",true).csv(outputRoot + "states")
    counties_db.repartition(1).write.option("header",true).csv(outputRoot + "counties")
    cities_db.repartition(1).write.option("header",true).csv(outputRoot + "cities")
    postalcodes_db.repartition(1).write.option("header",true).csv(outputRoot + "postals")

  }
}