package org.steveblackmon.utils;

import org.apache.juneau.PropertyStore;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.transforms.DateSwap;
import org.apache.juneau.urlencoding.UrlEncodingSerializer;

public class JuneauSerializers {

  public static class ResourceJsonSerializer extends JsonSerializer {

    public ResourceJsonSerializer(PropertyStore ps) {
      super(JSON.getPropertyStore());
    }

  };

  public static final JsonSerializer JSON = JsonSerializer.DEFAULT.
    builder().
    beansRequireDefaultConstructor().
    beansRequireSettersForGetters().
    beansRequireSerializable().
    ignoreUnknownBeanProperties().
    pojoSwaps(DateSwap.ISO8601DTZ.class).
    build();

  public static final UrlEncodingSerializer URL = UrlEncodingSerializer.DEFAULT.
    builder().
    beansRequireDefaultConstructor().
    beansRequireSettersForGetters().
    beansRequireSerializable().
    ignoreUnknownBeanProperties().
    pojoSwaps(DateSwap.ISO8601DTZ.class).
    build();

}
