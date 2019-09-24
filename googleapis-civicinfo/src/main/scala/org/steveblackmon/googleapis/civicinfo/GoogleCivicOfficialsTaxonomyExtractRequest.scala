package org.steveblackmon.googleapis.civicinfo

import org.apache.juneau.annotation.BeanProperty

class GoogleCivicOfficialsTaxonomyExtractRequest extends Serializable {
  import scala.collection.JavaConversions._
  @BeanProperty var ocdIdentifiersCsvPath : String = _
  @BeanProperty var repinfoByDivisionMaxRequests : Int = _
  @BeanProperty var repinfoExistingResponsesJsonlPath : String = _
  @BeanProperty var repinfoNewResponsesJsonlPath : String = _
}