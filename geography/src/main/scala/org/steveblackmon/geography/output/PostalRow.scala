package org.steveblackmon.geography.output

case class PostalRow(
                      id : Integer,
                      name : String,
                      country_id : Integer,
                      state_id : Integer,
                      county_id : Integer,
                      latitude : Double,
                      longitude : Double
                    )
