package bg.novatv.app;

import androidx.media3.exoplayer.upstream.CmcdConfiguration;
import com.google.common.net.HttpHeaders;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* JADX INFO: loaded from: classes.dex */
public final class M3u {
    static final Pattern ATTR = Pattern.compile("([\\w-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s]+))");

    static Map<String, String> attrs(String str) {
        HashMap map = new HashMap();
        Matcher matcher = ATTR.matcher(str);
        while (matcher.find()) {
            String lowerCase = matcher.group(1).toLowerCase(Locale.ROOT);
            int i = 2;
            if (matcher.group(2) == null) {
                i = 3;
                if (matcher.group(3) == null) {
                    i = 4;
                }
            }
            map.put(lowerCase, matcher.group(i));
        }
        return map;
    }

    static int comma(String str) {
        char c = 0;
        for (int i = 0; i < str.length(); i++) {
            char cCharAt = str.charAt(i);
            if (c == 0 && (cCharAt == '\"' || cCharAt == '\'')) {
                c = cCharAt;
            } else if (c == cCharAt) {
                c = 0;
            } else if (cCharAt == ',' && c == 0) {
                return i;
            }
        }
        return -1;
    }

    public static Models.Catalog parse(String str, String str2) throws IOException {
        String strTrim;
        String strValue;
        String str3;
        Models.Catalog catalog = new Models.Catalog();
        BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
        String strTrim2 = "Други";
        Models.Channel channel = null;
        int i = 0;
        boolean z = false;
        while (true) {
            String line = bufferedReader.readLine();
            if (line != null) {
                String strReplace = line.trim().replace("\ufeff", "");
                if (strReplace.length() != 0) {
                    if (strReplace.startsWith("#EXTM3U")) {
                        Map<String, String> mapAttrs = attrs(strReplace);
                        catalog.epg = mapAttrs.get(mapAttrs.containsKey("url-tvg") ? "url-tvg" : "x-tvg-url");
                        if (catalog.epg == null) {
                            catalog.epg = "";
                        }
                        if (catalog.epg.contains(",")) {
                            catalog.epg = catalog.epg.split(",")[0];
                        }
                        z = true;
                    } else if (strReplace.startsWith("#EXTINF:")) {
                        channel = new Models.Channel();
                        Map<String, String> mapAttrs2 = attrs(strReplace);
                        int iComma = comma(strReplace);
                        if (iComma < 0) {
                            strTrim = "Канал " + (i + 1);
                        } else {
                            strTrim = strReplace.substring(iComma + 1).trim();
                        }
                        channel.name = strTrim;
                        channel.epgId = value(mapAttrs2, "tvg-id", "");
                        channel.logo = value(mapAttrs2, "tvg-logo", "");
                        channel.group = value(mapAttrs2, "group-title", strTrim2);
                        if (channel.name.isEmpty()) {
                            strValue = value(mapAttrs2, "tvg-name", "Канал " + (i + 1));
                        } else {
                            strValue = channel.name;
                        }
                        channel.name = strValue;
                        channel.id = channel.epgId;
                    } else if (strReplace.startsWith("#EXTGRP:")) {
                        strTrim2 = strReplace.substring(8).trim();
                        if (channel != null) {
                            channel.group = strTrim2;
                        }
                    } else if (strReplace.startsWith("#EXTVLCOPT:") && channel != null) {
                        String strSubstring = strReplace.substring(11);
                        int iIndexOf = strSubstring.indexOf(61);
                        if (iIndexOf > 0) {
                            String strSubstring2 = strSubstring.substring(0, iIndexOf);
                            String strSubstring3 = strSubstring.substring(iIndexOf + 1);
                            if (strSubstring2.equals("http-user-agent")) {
                                channel.headers.put("User-Agent", strSubstring3);
                            }
                            if (strSubstring2.equals("http-referrer")) {
                                channel.headers.put(HttpHeaders.REFERER, strSubstring3);
                            }
                        }
                    } else if (strReplace.startsWith("#")) {
                        continue;
                    } else {
                        if (channel == null) {
                            if (!z && !strReplace.matches("(?i)^(https?|rtsp|udp|rtp)://.*")) {
                                throw new IOException("Отговорът не е валидна M3U листа.");
                            }
                            channel = new Models.Channel("", "Канал " + (i + 1), "", strTrim2);
                        }
                        parseUrl(channel, strReplace, str2);
                        i++;
                        if (channel.id.isEmpty()) {
                            str3 = channel.url;
                        } else {
                            str3 = String.valueOf(channel.id) + "|" + channel.url;
                        }
                        channel.id = str3;
                        catalog.items.add(channel);
                        if (i > 50000) {
                            throw new IOException("Листата съдържа повече от 50 000 канала.");
                        }
                        channel = null;
                    }
                }
            } else {
                if (catalog.items.isEmpty()) {
                    throw new IOException("Няма канали в M3U листата. Провери адреса или файла.");
                }
                return catalog;
            }
        }
    }

    static String value(Map<String, String> map, String str, String str2) {
        String str3 = map.get(str);
        return (str3 == null || str3.isEmpty()) ? str2 : str3;
    }

    public static void parseUrl(Models.Channel channel, String str, String str2) throws IOException {
        String[] strArrSplit = str.split("\\|", 2);
        String strTrim = strArrSplit[0].trim();
        try {
            URI uri = new URI(strTrim);
            if (!uri.isAbsolute()) {
                if (str2 == null || str2.isEmpty()) {
                    throw new IOException("Относителен адрес без източник.");
                }
                strTrim = new URI(str2).resolve(uri).toString();
            }
            if (!Arrays.asList("http", "https", "rtsp", "asset", "content", "file", "udp", CmcdConfiguration.KEY_MAXIMUM_REQUESTED_BITRATE).contains(new URI(strTrim).getScheme().toLowerCase(Locale.ROOT))) {
                throw new IOException("Неподдържан протокол на канала.");
            }
            channel.url = strTrim;
            if (strArrSplit.length > 1) {
                for (String str3 : strArrSplit[1].split("&")) {
                    int iIndexOf = str3.indexOf(61);
                    if (iIndexOf > 0) {
                        String strDecode = decode(str3.substring(0, iIndexOf));
                        String strDecode2 = decode(str3.substring(iIndexOf + 1));
                        if (strDecode.equalsIgnoreCase("user-agent")) {
                            strDecode = "User-Agent";
                        }
                        if (strDecode.equalsIgnoreCase("referer") || strDecode.equalsIgnoreCase("referrer")) {
                            strDecode = HttpHeaders.REFERER;
                        }
                        if (!strDecode.contains("\n") && !strDecode2.contains("\n") && !strDecode.contains("\r") && !strDecode2.contains("\r")) {
                            channel.headers.put(strDecode, strDecode2);
                        }
                    }
                }
            }
        } catch (URISyntaxException unused) {
            throw new IOException("Невалиден адрес на канал.");
        }
    }

    static String decode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (Exception unused) {
            return str;
        }
    }
}
