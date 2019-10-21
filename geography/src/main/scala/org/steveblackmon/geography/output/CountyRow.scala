package org.steveblackmon.geography.output

case class CountyRow(
                      id : Integer,
                      code : String,
                      country_id : Integer,
                      state_id : Integer,
                      name : String,
                      fips : Integer
                    )

