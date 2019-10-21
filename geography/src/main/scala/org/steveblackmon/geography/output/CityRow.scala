package org.steveblackmon.geography.output

case class CityRow(
                    id : Int,
                    city : String,
                    country_id : Integer,
                    state_id : Integer,
                    county_id : Integer,
                    latitude : Double,
                    longitude : Double
                  )

