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
- Requires an input path to ocd division codes
  * org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtractRequest.ocdIdentifiersCsvPath
  
- Requires an output path for results
  * org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtractRequest.repinfoNewResponsesJsonlPath

- Optional input path existing results that need not be re-requested
  * org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtractRequest.repinfoExistingResponsesJsonlPath

- Optional maximum requests
  * org.steveblackmon.googleapis.civicinfo.GoogleCivicOfficialsTaxonomyExtractRequest.repinfoByDivisionMaxRequests

#### [GoogleCivicOfficialsTaxonomyTransform]

Purpose:
Derive entities and relationships from all available outputs of GoogleCivicOfficialsTaxonomyExtract
 
Inputs:
- Requires an input path of GoogleCivicOfficialsTaxonomyExtract output file(s)

Outputs:
- Requires an output path for results

Configuration:
- List of entity types to write as csv
- List of relationship types to write as csv

#### [GoogleCivicOfficialsTaxonomyLoadDataDotWorld]

Purpose:
Upload generated Entity and Relationship Data to data.world

#### [GoogleCivicOfficialsTaxonomyLoadNeo4j]

#### [GoogleCivicOfficialsTaxonomyLoadPostgres]

Purpose:
Create relational database from generated Entity and Relationship Data

#### [GoogleCivicOfficialsTaxonomyLoadTurtle]

### References