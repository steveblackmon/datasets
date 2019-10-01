package org.steveblackmon.sparkutils

object SparkPipelineUDFs {

  import org.apache.spark.sql.SparkSession
  val sparkSession = SparkSession.builder().getOrCreate()

  import java.util.UUID

  import org.apache.spark.sql.functions.udf
  val uuidUDF = udf(() => UUID.randomUUID().toString)

  def stripLineEndings = (input: String) => {
    if( input == null || input.isEmpty )
      ""
    else
      input.replaceAll("\n\r", " ")
  }
  val stripLineEndingsUdf = udf(stripLineEndings)
  sparkSession.sqlContext.udf.register("stripLineEndings", stripLineEndings)

}
