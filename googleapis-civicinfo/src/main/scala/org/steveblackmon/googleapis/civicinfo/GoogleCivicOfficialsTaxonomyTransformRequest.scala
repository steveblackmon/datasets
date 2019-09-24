package org.steveblackmon.googleapis.civicinfo

import org.apache.juneau.annotation.BeanProperty

class GoogleCivicOfficialsTaxonomyTransformRequest extends Serializable {
  @BeanProperty var repinfoResponsesJsonlPath : String = _
  @BeanProperty var officialsCsvOutputPath : String = _
  @BeanProperty var officialsJsonlOutputPath : String = _
}

