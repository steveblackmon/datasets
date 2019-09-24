package org.steveblackmon.utils;

import org.apache.juneau.PropertyStore;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.transforms.DateSwap;
import org.apache.juneau.urlencoding.UrlEncodingParser;

public class JuneauParsers {

    public static class ResourceJsonParser extends JsonParser {

        public ResourceJsonParser(PropertyStore ps) {
            super(JSON.getPropertyStore());
        }

    };

    public static final JsonParser JSON = JsonParser.DEFAULT.
            builder().
            beansRequireDefaultConstructor().
            beansRequireSettersForGetters().
            beansRequireSerializable().
            ignoreUnknownBeanProperties().
            ignoreUnknownNullBeanProperties(true).
            pojoSwaps(DateSwap.ISO8601DTZ.class).
            build();

    public static final UrlEncodingParser URL = UrlEncodingParser.DEFAULT.
            builder().
            beansRequireDefaultConstructor().
            beansRequireSettersForGetters().
            beansRequireSerializable().
            ignoreUnknownBeanProperties().
            ignoreUnknownNullBeanProperties(true).
            pojoSwaps(DateSwap.ISO8601DTZ.class).
            build();

}
