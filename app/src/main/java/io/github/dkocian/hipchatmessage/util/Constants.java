package io.github.dkocian.hipchatmessage.util;

/**
 * Created by dkocian on 8/4/2015.
 */
public final class Constants {
    public static final String OPEN_PARENTHESIS = "(";
    public static final String CLOSE_PARENTHESIS = ")";
    public static final String CLOSE_PAREN_OR = ")|";
    public static final String PARSE_ACTION = "io.github.dkocian.hipchatmessage.PARSE_MESSAGE";
    public static final String HTTP = "http://";
    public static final String NEW_LINE = "\n";
    public static final int PROTOCOL_INDENTIFIER_INDEX = 4;
    public static String MENTIONS_REGEX = "@\\w+";
    public static String EMOTICONS_REGEX = "\\([a-zA-Z0-9]{1,15}\\)";
    public static String LINK_REGEX = "(https?:\\/\\/)?(?:[\\da-z\\.-]+)\\.(?:[a-z\\.]{2,6})(?:[\\/\\w \\.-]*)*\\/?";
}
