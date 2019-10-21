package org.steveblackmon.geography

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.types.StructType
import org.apache.streams.config.StreamsConfigurator
import org.steveblackmon.geography.output.CityRow
import org.steveblackmon.geography.output.ContinentRow
import org.steveblackmon.geography.output.CountryRow
import org.steveblackmon.geography.output.CountyRow
import org.steveblackmon.geography.output.PostalRow
import org.steveblackmon.geography.output.StateRow

object LoadGeographySchemaSparkJob {

  val sparkSession = SparkSession.builder().getOrCreate()
  val sparkContext = sparkSession.sparkContext
  val sqlContext = sparkSession.sqlContext

  val rootconfig = StreamsConfigurator.getConfig
  lazy val config = rootconfig.getConfig(classOf[LoadGeographySchemaSparkJob].getCanonicalName)

  val continents_schema = ScalaReflection.schemaFor[ContinentRow].dataType.asInstanceOf[StructType]
  val countries_schema = ScalaReflection.schemaFor[CountryRow].dataType.asInstanceOf[StructType]
  val states_schema = ScalaReflection.schemaFor[StateRow].dataType.asInstanceOf[StructType]
  val counties_schema = ScalaReflection.schemaFor[CountyRow].dataType.asInstanceOf[StructType]
  val cities_schema = ScalaReflection.schemaFor[CityRow].dataType.asInstanceOf[StructType]
  val postals_schema = ScalaReflection.schemaFor[PostalRow].dataType.asInstanceOf[StructType]

}

class LoadGeographySchemaSparkJob extends Serializable with Runnable {

  import LoadGeographySchemaSparkJob._
  import org.apache.spark.sql.SparkSession

  val sparkSession = SparkSession.builder().getOrCreate()

  val inputRoot = config.getString("inputRoot")
  val jdbc_driver = config.getString("jdbc_driver")
  val jdbc_url = config.getString("jdbc_url")
  val jdbc_user = config.getString("jdbc_user")
  val jdbc_password = config.getString("jdbc_password")

  val jdbc_properties = new java.util.Properties
  jdbc_properties.setProperty("driver", jdbc_driver)
  jdbc_properties.setProperty("user", jdbc_user)
  jdbc_properties.setProperty("password", jdbc_password)
  jdbc_properties.setProperty("stringtype", "unspecified")

  val continents_db_path = inputRoot + config.getString("continents_db_path")
  val countries_db_path = inputRoot + config.getString("countries_db_path")
  val states_db_path = inputRoot + config.getString("states_db_path")
  val counties_db_path = inputRoot + config.getString("counties_db_path")
  val cities_db_path = inputRoot + config.getString("cities_db_path")
  val postals_db_path = inputRoot + config.getString("postals_db_path")

  val continents_db_table = config.getString("continents_db_table")
  val countries_db_table = config.getString("countries_db_table")
  val states_db_table = config.getString("states_db_table")
  val counties_db_table = config.getString("counties_db_table")
  val cities_db_table = config.getString("cities_db_table")
  val postals_db_table = config.getString("postals_db_table")

  val continents_db = sparkSession.read.option("header",true).schema(continents_schema).csv(continents_db_path)
  val countries_db = sparkSession.read.option("header",true).schema(countries_schema).csv(countries_db_path)
  val states_db = sparkSession.read.option("header",true).schema(states_schema).csv(states_db_path)
  val counties_db = sparkSession.read.option("header",true).schema(counties_schema).csv(counties_db_path)
  val cities_db = sparkSession.read.option("header",true).schema(cities_schema).csv(cities_db_path)
  val postals_db = sparkSession.read.option("header",true).schema(postals_schema).csv(postals_db_path)

  override def run(): Unit = {
    continents_db.write.mode("append").jdbc(jdbc_url, continents_db_table, jdbc_properties)
    countries_db.write.mode("append").jdbc(jdbc_url, countries_db_table, jdbc_properties)
    states_db.write.mode("append").jdbc(jdbc_url, states_db_table, jdbc_properties)
    counties_db.write.mode("append").jdbc(jdbc_url, counties_db_table, jdbc_properties)
    cities_db.write.mode("append").jdbc(jdbc_url, cities_db_table, jdbc_properties)
    postals_db.write.mode("append").jdbc(jdbc_url, postals_db_table, jdbc_properties)
  }
}
