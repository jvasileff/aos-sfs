package org.anodyneos.sfs.impl.util;

public class Util {

    public static String escapeStringQuoted(String string) {
        if (null != string) {
            return "\"" + escapeString(string) + "\"";
        } else {
            return "null";
        }
    }

    /**
     * Escapes '"' and '\' characters in a String (add a '\' before them) so that it can
     * be inserted in java source.
     */
    public static String escapeString(String string) {
        char chr[] = string.toCharArray();
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < chr.length; i++) {
            switch (chr[i]) {
                case '\t':
                    buffer.append("\\t");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\b':
                    buffer.append("\\b");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '"':
                case '\\':
                    buffer.append('\\');
                    buffer.append(chr[i]);
                    break;
                default:
                    if (' ' <= chr[i] && chr[i] <= 127) {
                        buffer.append(chr[i]);
                    } else {
                        buffer.append("\\u");
                        buffer.append(int2digit(chr[i] >> 12));
                        buffer.append(int2digit(chr[i] >> 8));
                        buffer.append(int2digit(chr[i] >> 4));
                        buffer.append(int2digit(chr[i]));
                    }
                    break;
            }
        }

        return buffer.toString();
    }

    private static char int2digit(int x) {
        x &= 0xF;
        if (x <= 9) return (char)(x + '0');
        else return (char)(x - 10 + 'A');
    }

}
