package br.com.alcidesbezerra.bff.generica.security.util;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class ValueUtil {

    public static String getOnlyNumbers(final String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.replaceAll("[^0-9]+", "");
    }

}
