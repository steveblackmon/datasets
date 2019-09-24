googleapis-civicinfo
====================

### Summary

This project creates structured relational and/or graph datasets 
with the information available through Google's CivicInfo API.

### Building

Requirements: 
- Sources compilation require Java SE 8.
- Sources compilation require Apache Maven 3.3.9 or higher.

### Modules

#### [GoogleCivicOfficialsTaxonomyExtract]

Purpose:

Inputs:
- GoogleCivicOfficialsTaxonomyExtract requires a list of ocd division codes to work from.
  * [ocdidentifiers](https://github.com/opencivicdata/ocd-division-ids) is a good source for this

Outputs:
- Json-delimited files from the civic info getRepresentativeInfoByDivision

Configuration:
- Requires a GoogleCivic token
  * org.steveblackmon.googleapis.civicinfo.GoogleCivicConfiguration.token)

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

#### [GoogleCivicOfficialsTaxonomyLoadNeo4j]

#### [GoogleCivicOfficialsTaxonomyLoadPostgres]

#### [GoogleCivicOfficialsTaxonomyLoadTurtle]

### References