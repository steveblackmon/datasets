package org.steveblackmon.geography.input.geonames

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

