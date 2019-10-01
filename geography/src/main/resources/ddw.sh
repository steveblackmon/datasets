# assert API token set
# assert owner Id set
# assert project Id set
export DDW_TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwcm9kLXVzZXItY2xpZW50OnN0ZXZlYmxhY2ttb24iLCJpc3MiOiJhZ2VudDpzdGV2ZWJsYWNrbW9uOjphYWQxODEzNS0xNzhkLTQ5MzAtOTFmNi1lOTY0OGQzNmI5ZDkiLCJpYXQiOjE1MjAzNjc1MTQsInJvbGUiOlsidXNlcl9hcGlfcmVhZCIsInVzZXJfYXBpX3dyaXRlIl0sImdlbmVyYWwtcHVycG9zZSI6dHJ1ZSwic2FtbCI6e319.yo98-QMFLSg756Dtj-1SWSb5Ho_udX_DAkTqnvC2XFXh-KTfHRB_yqhcdniLdZ_tmbC-zGH_GrXj51TlWyw7nA"
export DDW_OWNER="steveblackmon"
export DDW_DATASETID="geography"
export WORKING_DIR_PATH="/Users/Shared/Dropbox/git/emetry-parent/pipelines/target/test-classes/TransformGeographySparkJobIT/"

export CONTINENTS_CSV_FILE=`ls ${WORKING_DIR_PATH}/continents/part*.csv`

curl \
  -H "Authorization: Bearer ${DDW_TOKEN}" \
  -X PUT -H "Content-Type: application/octet-stream" \
  --data-binary @${CONTINENTS_CSV_FILE} \
  https://api.data.world/v0/uploads/${DDW_OWNER}/${DDW_DATASETID}/files/continents.csv

export COUNTRIES_CSV_FILE=`ls ${WORKING_DIR_PATH}/countries/part*.csv`

curl \
  -H "Authorization: Bearer ${DDW_TOKEN}" \
  -X PUT -H "Content-Type: application/octet-stream" \
  --data-binary @${COUNTRIES_CSV_FILE} \
  https://api.data.world/v0/uploads/${DDW_OWNER}/${DDW_DATASETID}/files/countries.csv

export STATES_CSV_FILE=`ls ${WORKING_DIR_PATH}/states/part*.csv`

curl \
  -H "Authorization: Bearer ${DDW_TOKEN}" \
  -X PUT -H "Content-Type: application/octet-stream" \
  --data-binary @${STATES_CSV_FILE} \
  https://api.data.world/v0/uploads/${DDW_OWNER}/${DDW_DATASETID}/files/states.csv

export COUNTIES_CSV_FILE=`ls ${WORKING_DIR_PATH}/counties/part*.csv`

curl \
  -H "Authorization: Bearer ${DDW_TOKEN}" \
  -X PUT -H "Content-Type: application/octet-stream" \
  --data-binary @${COUNTIES_CSV_FILE} \
  https://api.data.world/v0/uploads/${DDW_OWNER}/${DDW_DATASETID}/files/counties.csv

export CITIES_CSV_FILE=`ls ${WORKING_DIR_PATH}/cities/part*.csv`

curl \
  -H "Authorization: Bearer ${DDW_TOKEN}" \
  -X PUT -H "Content-Type: application/octet-stream" \
  --data-binary @${CITIES_CSV_FILE} \
  https://api.data.world/v0/uploads/${DDW_OWNER}/${DDW_DATASETID}/files/cities.csv

export POSTALS_CSV_FILE=`ls ${WORKING_DIR_PATH}/counties/part*.csv`

curl \
  -H "Authorization: Bearer ${DDW_TOKEN}" \
  -X PUT -H "Content-Type: application/octet-stream" \
  --data-binary @${POSTALS_CSV_FILE} \
  https://api.data.world/v0/uploads/${DDW_OWNER}/${DDW_DATASETID}/files/postals.csv
