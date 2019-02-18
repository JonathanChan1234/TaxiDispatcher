package com.jonathan.taxidispatcher.utils;

import java.util.List;

public class Utils {
    public static String stringListToString(List<String> list) {
        if (list.size() > 0) {
            StringBuilder string = new StringBuilder();
            for (String item : list) {
                string.append(item);
                string.append(", ");
            }
            return string.toString();
        }
        return "";
    }
}
