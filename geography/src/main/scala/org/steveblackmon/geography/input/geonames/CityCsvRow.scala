package org.steveblackmon.geography.input.geonames

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
