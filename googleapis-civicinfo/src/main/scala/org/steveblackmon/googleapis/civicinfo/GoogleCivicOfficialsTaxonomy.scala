package org.steveblackmon.googleapis.civicinfo

import org.apache.juneau.annotation.BeanProperty
import org.apache.juneau.http.annotation.Path
import org.apache.juneau.http.annotation.Query
import org.apache.juneau.rest.client.remote.RemoteMethod
import org.apache.juneau.rest.client.remote.RemoteResource

class Division extends Serializable {
  @BeanProperty var name : String = _
  @BeanProperty var alsoKnownAs : java.util.List[String] = _
  @BeanProperty var officeIndices : java.util.List[Integer] = _
}

class Source extends Serializable {
  @BeanProperty var name : String = _
  @BeanProperty var official : String = _
}

class Office extends Serializable {
  @BeanProperty var name : String = _
  @BeanProperty var divisionId : String = _
  @BeanProperty var levels : java.util.List[String] = _
  @BeanProperty var roles : java.util.List[String] = _
  @BeanProperty var sources : java.util.List[Source] = _
  @BeanProperty var officialIndices : java.util.List[Integer] = _
}

class OfficialAddress extends Serializable {
  @BeanProperty var locationName : String = _
  @BeanProperty var line1 : String = _
  @BeanProperty var line2 : String = _
  @BeanProperty var line3 : String = _
  @BeanProperty var city : String = _
  @BeanProperty var state: String = _
  @BeanProperty var zip : String = _
}

class Channel extends Serializable {
  @BeanProperty var `type` : String = _
  @BeanProperty var id : String = _
}

class Official extends Serializable {
  @BeanProperty var name : String = _
  @BeanProperty var address : java.util.List[OfficialAddress] = _
  @BeanProperty var party : String = _
  @BeanProperty var phones : java.util.List[String] = _
  @BeanProperty var urls : java.util.List[String] = _
  @BeanProperty var photoUrl : String = _
  @BeanProperty var emails : java.util.List[String] = _
  @BeanProperty var channels : java.util.List[Channel] = _
}

class RepresentativeInfoByAddressResponse extends Serializable {
  @BeanProperty var kind : String = _
  @BeanProperty var normalizedInput : OfficialAddress = _
  @BeanProperty var divisions : java.util.Map[String, Division] = _
  @BeanProperty var offices : java.util.List[Office] = _
  @BeanProperty var officials : java.util.List[Official] = _
}

class RepresentativeInfoByDivisionResponse extends Serializable {
  @BeanProperty var divisions : java.util.Map[String, Division] = _
  @BeanProperty var offices : java.util.List[Office] = _
  @BeanProperty var officials : java.util.List[Official] = _
}

//case class Division (
//  @BeanProperty var name : String,
//  @BeanProperty var alsoKnownAs : List[String],
//  @BeanProperty var officeIndices : List[Integer]
//                    )
//
//case class Source (
//  @BeanProperty var name: String,
//  @BeanProperty var official: String
//                  )
//
//case class Office (
//  @BeanProperty var name : String,
//  @BeanProperty var divisionId : String,
//  @BeanProperty var levels : List[String],
//  @BeanProperty var roles : List[String],
//  @BeanProperty var sources : List[Source],
//  @BeanProperty var officialIndices : List[Integer]
//                 )
//
//case class OfficialAddress (
//  @BeanProperty var locationName : String,
//  @BeanProperty var line1 : String,
//  @BeanProperty var line2 : String,
//  @BeanProperty var line3 : String,
//  @BeanProperty var city : String,
//  @BeanProperty var state: String,
//  @BeanProperty var zip : String
//                           )
//
//case class Channel (
//  @BeanProperty var `type` : String,
//  @BeanProperty var id : String
//                   )
//
//case class Official (
//  @BeanProperty var name : String,
//  @BeanProperty var address : List[OfficialAddress],
//  @BeanProperty var party : String,
//  @BeanProperty var phones : List[String],
//  @BeanProperty var urls : List[String],
//  @BeanProperty var photoUrl : String,
//  @BeanProperty var emails : List[String],
//  @BeanProperty var channels : List[Channel]
//                    )
//
//case class RepresentativeInfoByAddressResponse (
//  @BeanProperty var kind : String,
//  @BeanProperty var normalizedInput : OfficialAddress,
//  @BeanProperty var divisions : Map[String, Division],
//  @BeanProperty var offices : List[Office],
//  @BeanProperty var officials : List[Official]
//                                               )
//
//case class RepresentativeInfoByDivisionResponse (
//  @BeanProperty var divisions : Map[String, Division],
//  @BeanProperty var offices : List[Office],
//  @BeanProperty var officials : List[Official]
//                                                )

@RemoteResource (path = "https://www.googleapis.com/civicinfo/v2/")
trait GoogleCivicOfficialsTaxonomy {

  @RemoteMethod(method = "GET", path = "/representatives")
  def getRepresentativeInfoByAddress(@Query("address") address : String) : RepresentativeInfoByAddressResponse

  @RemoteMethod(method = "GET", path = "/representatives/{ocdId}")
  def getRepresentativeInfoByDivision(@Path("ocdId") ocdId : String) : RepresentativeInfoByDivisionResponse

}
