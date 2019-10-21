package org.steveblackmon.geography.input.geonames

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
