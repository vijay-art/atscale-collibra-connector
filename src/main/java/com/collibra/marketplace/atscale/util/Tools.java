package com.collibra.marketplace.atscale.util;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

import static com.collibra.marketplace.atscale.util.Constants.NEW_LINE_WITH_ASTERISK;

public class Tools {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tools.class);

    /***
     * utility class pattern
     * The constructor is made private to prevent the class from being instantiated from outside the class.
     */
    private Tools() {
        throw new IllegalStateException("Tools Utility class");
    }


    public static void printMsg(String toPrint) {
        if (Constants.PRINT_MSGS) {
            LOGGER.info(toPrint);
        }
    }

    public static String printSet(Set<String> list, String prefix) {
        StringBuilder retVal = new StringBuilder();
        for (String item : list) {
            if (retVal.length() == 0) {
                retVal.append(prefix).append(item);
            } else {
                retVal.append(", ").append(item);
            }
        }
        return retVal.toString();
    }

    public static String printSetInLines(Set<String> list, String prefix) {
        StringBuilder retVal = new StringBuilder();
        for (String item : list) {
            if (retVal.length() == 0) {
                retVal.append(NEW_LINE_WITH_ASTERISK+prefix + item);
            } else {
                retVal.append(NEW_LINE_WITH_ASTERISK + item);
            }
        }
        return retVal.toString();
    }

    public static String printList(List<String> list, String prefix) {
        StringBuilder retVal = new StringBuilder();
        for (String item : list) {
            if (retVal.length() == 0) {
                retVal.append(prefix).append(item);
            } else {
                retVal.append(",  ").append(item);
            }
        }
        return retVal.toString();
    }

    public static String printListInLines(List<String> list, String prefix) {
        StringBuilder retVal = new StringBuilder();
        for (String item : list) {
            if (retVal.length() == 0) {
                retVal.append(NEW_LINE_WITH_ASTERISK + prefix + item);
            } else {
                retVal.append(NEW_LINE_WITH_ASTERISK + item);
            }
        }
        return retVal.toString();
    }

    public static String printFirstChars(String val, Integer numChars) {
        if (val.length() > numChars) {
            return val.substring(0,numChars);
        }
        return val;
    }

    public static <T> T coalesce(T ...items) {
        for(T i : items) {
            if (i != null && i.getClass().equals(String.class)) {
                if (((String) i).length() > 0) {
                    return i;
                }
            } else {
                if (i != null) return i;
            }
        }
        return null;
    }

    public static String printShort(String toPrint, Integer numChars) {
        toPrint = toPrint.replace("\n", " ");
        if (toPrint.length() > numChars) {
            return toPrint.substring(0,numChars) + "...";
        }
        return toPrint;
    }

    public static void printWithFilter(String filter, String toPrint) {
        for (String lookFor: Constants.FILTER) {
            if (lookFor.toLowerCase(Locale.ROOT).equals(filter.toLowerCase(Locale.ROOT))) {
                LOGGER.info("CCC {}", toPrint);

            }
        }
    }

    public static void printWithFilterAnySubset(String filter, String toPrint) {
        for (String lookFor: Constants.FILTER) {
            if (filter.toLowerCase(Locale.ROOT).contains(lookFor.toLowerCase(Locale.ROOT))) {
                LOGGER.info("CCC {}", toPrint);
            }
        }
    }

    public static void printHeader(String header, Integer depth) {
        if (Constants.PRINT_HEADERS) {
            StringBuilder indent = new StringBuilder();
            for (Integer i = 1; i < depth; i++) {
                indent.append("   ");
            }
            LOGGER.info("{}** Top of {}",indent, header);
        }
    }

    public static Integer hashStringToInt(String toHash) {
        return new HashCodeBuilder(17, 37).append(toHash).toHashCode();
    }

    public static boolean isEmpty(String in) {
        return (in == null || in.equals(""));
    }

    public static boolean inList(String in, List<String> list) {
        for (String item : list) {
            if (item.equals(in)) {
                return true;
            }
        }
        return false;
    }

    public static boolean inListSubset(String in, List<String> list) {
        for (String item : list) {
            if (in.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public static String getAggregationByKey(int key) {
        switch (key) {
            case 1:
                return "Sum";
            case 2:
                return "Non-Distinct Count";
            case 3:
                return "Minimum";
            case 4:
                return "Maximum";
            case 5:
                return "Average";
            case 7:
                return "Standard Deviation";
            case 8:
                return "Distinct Count";
            default:
                return "";
        }
    }

    public static void addToMapList(Map<String, List<String>> map, String key, String addVal) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(addVal);
    }

    public static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (Exception e) {
            LOGGER.error("Error parsing XML: ",e);
        }
        return null;
    }

    public static void printDebug(boolean debugFlag, String s) {
        if (debugFlag) LOGGER.debug(s);
    }


    public static boolean hasStringValue(String in) {
        return !(in == null || in.isEmpty());
    }
}
