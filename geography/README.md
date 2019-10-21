geography
====================

### Summary

This project creates structured relational and/or graph datasets 
with the information available from geonames.org and related sources.

### Building

Requirements: 
- Sources compilation require Java SE 8.
- Sources compilation require Apache Maven 3.3.9 or higher.

### Prerequisites:
- Files from [geonames.org](http://geonames.org)

### Modules

#### [TransformGeographySparkJob]

Purpose: Transform raw data from geonames.org and related sources

Inputs:
- TransformGeographySparkJob requires certain files from [geonames.org](http://download.geonames.org/export/dump/)

Outputs:
- csv files

Configuration:
- Requires paths to various geonames input files under org.steveblackmon.geography.TransformGeographySparkJob
  * continents_csv_path
  * countries_csv_path
  * admin1codes_tsv_path
  * admin2codes_tsv_path
  * cities_csv_path
  * postalcodes_tsv_path
  
#### [LoadGeographySchemaSparkJob]

Purpose:
Load SQL database schema using outputs of TransformGeographySparkJob
 
Inputs:
- Requires an input path of TransformGeographySparkJob output file(s)

Outputs:
- Requires an output path for results

Configuration:
- JDBC details
- Table names

### References