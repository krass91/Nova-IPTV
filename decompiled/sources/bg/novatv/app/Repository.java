package bg.novatv.app;

import androidx.core.app.NotificationCompat;
import androidx.media3.extractor.text.ttml.TtmlNode;
import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public final class Repository {
    final Models.Profile profile;
    String portal = "";
    String token = "";
    long tokenAt = 0;

    public Repository(Models.Profile profile) {
        this.profile = profile;
    }

    Map<String, String> headers() {
        HashMap map = new HashMap();
        map.put("User-Agent", this.profile.agent.isEmpty() ? "NovaIPTV/1.0" : this.profile.agent);
        return map;
    }

    Object json(String str, Map<String, String> map) throws Exception {
        String strTrim = Net.get(str, map).trim();
        if (strTrim.startsWith("{")) {
            return new JSONObject(strTrim);
        }
        if (strTrim.startsWith("[")) {
            return new JSONArray(strTrim);
        }
        throw new IOException("Сървърът не върна IPTV данни.");
    }

    JSONObject obj(Object obj) throws IOException {
        if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        }
        throw new IOException("Невалиден отговор от сървъра.");
    }

    JSONArray arr(Object obj) throws IOException {
        if (obj instanceof JSONArray) {
            return (JSONArray) obj;
        }
        throw new IOException("Не е получен списък от сървъра.");
    }

    String api(String str) throws IOException {
        String str2;
        StringBuilder sb = new StringBuilder(String.valueOf(Net.root(this.profile.url)));
        sb.append("/player_api.php?username=");
        sb.append(Net.enc(this.profile.user));
        sb.append("&password=");
        sb.append(Net.enc(this.profile.pass));
        if (str.isEmpty()) {
            str2 = "";
        } else {
            str2 = "&action=" + str;
        }
        sb.append(str2);
        return sb.toString();
    }

    public Models.Catalog load(String str) throws Exception {
        if (this.profile.type.equals("m3u")) {
            Models.Catalog catalog = M3u.parse(Net.get(this.profile.url, headers()), this.profile.url);
            if (!this.profile.epg.isEmpty()) {
                catalog.epg = this.profile.epg;
            }
            return catalog;
        }
        if (this.profile.type.equals("file")) {
            throw new IOException("Локалната листа се зарежда от избрания файл.");
        }
        if (this.profile.type.equals("xtream")) {
            return xtream(str);
        }
        if (!this.profile.type.equals("stb")) {
            throw new IOException("Непознат тип профил.");
        }
        if (str.equals("live")) {
            return stalker();
        }
        throw new IOException("Този STB профил поддържа телевизия. За филми и сериали използвай Xtream Codes.");
    }

    /* JADX WARN: Code duplicated, block: B:32:0x0102  */
    Models.Catalog xtream(String str) throws Exception {
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        JSONObject jSONObjectOptJSONObject = obj(json(api(""), headers())).optJSONObject("user_info");
        if (jSONObjectOptJSONObject != null) {
            int i = 0;
            if (jSONObjectOptJSONObject.optInt("auth", 0) == 1) {
                if (!jSONObjectOptJSONObject.optString(NotificationCompat.CATEGORY_STATUS, "Active").equalsIgnoreCase("Active")) {
                    throw new IOException("Абонаментът не е активен.");
                }
                if (str.equals("vod")) {
                    str2 = "vod";
                } else {
                    str2 = str.equals("series") ? "series" : "live";
                }
                HashMap map = new HashMap();
                JSONArray jSONArrayArr = arr(json(api("get_" + str2 + "_categories"), headers()));
                int i2 = 0;
                while (true) {
                    str3 = "Други";
                    if (i2 >= jSONArrayArr.length()) {
                        break;
                    }
                    String str7 = str2;
                    JSONObject jSONObjectOptJSONObject2 = jSONArrayArr.optJSONObject(i2);
                    if (jSONObjectOptJSONObject2 != null) {
                        map.put(jSONObjectOptJSONObject2.optString("category_id"), jSONObjectOptJSONObject2.optString("category_name", "Други"));
                    }
                    i2++;
                    str2 = str7;
                }
                if (str2.equals("series")) {
                    str4 = "get_series";
                } else {
                    str4 = "get_" + str2 + "_streams";
                }
                JSONArray jSONArrayArr2 = arr(json(api(str4), headers()));
                Models.Catalog catalog = new Models.Catalog();
                String strRoot = Net.root(this.profile.url);
                if (this.profile.epg.isEmpty()) {
                    str5 = String.valueOf(strRoot) + "/xmltv.php?username=" + Net.enc(this.profile.user) + "&password=" + Net.enc(this.profile.pass);
                } else {
                    str5 = this.profile.epg;
                }
                catalog.epg = str5;
                while (i < jSONArrayArr2.length()) {
                    JSONObject jSONObjectOptJSONObject3 = jSONArrayArr2.optJSONObject(i);
                    if (jSONObjectOptJSONObject3 == null) {
                        str2 = str2;
                        str6 = str3;
                    } else {
                        Models.Channel channel = new Models.Channel();
                        String strOptString = jSONObjectOptJSONObject3.optString(str2.equals("series") ? "series_id" : "stream_id");
                        if (strOptString.isEmpty()) {
                            str2 = str2;
                            str6 = str3;
                        } else {
                            StringBuilder sb = new StringBuilder(str2);
                            str6 = str3;
                            sb.append(":");
                            sb.append(strOptString);
                            channel.id = sb.toString();
                            channel.name = jSONObjectOptJSONObject3.optString("name", "Канал " + (i + 1));
                            channel.group = map.containsKey(jSONObjectOptJSONObject3.optString("category_id")) ? (String) map.get(jSONObjectOptJSONObject3.optString("category_id")) : str6;
                            channel.logo = jSONObjectOptJSONObject3.optString(str2.equals("series") ? "cover" : "stream_icon", "");
                            channel.epgId = jSONObjectOptJSONObject3.optString("epg_channel_id", "");
                            channel.kind = str2;
                            if (str2.equals("series")) {
                                channel.seriesId = strOptString;
                                str2 = str2;
                            } else {
                                String strOptString2 = str2.equals("live") ? "ts" : jSONObjectOptJSONObject3.optString("container_extension", "mp4");
                                String str8 = strOptString2.matches("[a-zA-Z0-9]{1,8}") ? strOptString2 : "mp4";
                                StringBuilder sb2 = new StringBuilder(String.valueOf(strRoot));
                                sb2.append("/");
                                sb2.append(str2.equals("vod") ? "movie" : "live");
                                sb2.append("/");
                                sb2.append(Net.enc(this.profile.user));
                                sb2.append("/");
                                sb2.append(Net.enc(this.profile.pass));
                                sb2.append("/");
                                sb2.append(Net.enc(strOptString));
                                sb2.append(".");
                                sb2.append(str8);
                                channel.url = sb2.toString();
                                channel.headers.putAll(headers());
                            }
                            catalog.items.add(channel);
                        }
                    }
                    i++;
                    jSONArrayArr2 = jSONArrayArr2;
                    str3 = str6;
                    str2 = str2;
                }
                return catalog;
            }
        }
        throw new IOException("Грешно потребителско име или парола.");
    }

    static /* synthetic */ int lambda$0(String str, String str2) {
        try {
            return Integer.compare(Integer.parseInt(str), Integer.parseInt(str2));
        } catch (Exception unused) {
            return str.compareTo(str2);
        }
    }

    public List<Models.Channel> episodes(Models.Channel channel) throws Exception {
        JSONObject jSONObjectOptJSONObject = obj(json(String.valueOf(api("get_series_info")) + "&series_id=" + Net.enc(channel.seriesId), headers())).optJSONObject("episodes");
        ArrayList arrayList = new ArrayList();
        if (jSONObjectOptJSONObject == null) {
            return arrayList;
        }
        ArrayList<String> arrayList2 = new ArrayList();
        Iterator<String> itKeys = jSONObjectOptJSONObject.keys();
        while (itKeys.hasNext()) {
            arrayList2.add(itKeys.next());
        }
        Collections.sort(arrayList2, new Comparator() { // from class: bg.novatv.app.Repository$$ExternalSyntheticLambda0
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return Repository.lambda$0((String) obj, (String) obj2);
            }
        });
        for (String str : arrayList2) {
            JSONArray jSONArrayOptJSONArray = jSONObjectOptJSONObject.optJSONArray(str);
            if (jSONArrayOptJSONArray != null) {
                for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
                    JSONObject jSONObjectOptJSONObject2 = jSONArrayOptJSONArray.optJSONObject(i);
                    if (jSONObjectOptJSONObject2 != null) {
                        String strOptString = jSONObjectOptJSONObject2.optString(TtmlNode.ATTR_ID);
                        String strOptString2 = jSONObjectOptJSONObject2.optString("container_extension", "mp4");
                        if (!strOptString.isEmpty()) {
                            String str2 = strOptString2.matches("[a-zA-Z0-9]{1,8}") ? strOptString2 : "mp4";
                            Models.Channel channel2 = new Models.Channel("episode:" + strOptString, jSONObjectOptJSONObject2.optString("title", "Епизод " + (i + 1)), String.valueOf(Net.root(this.profile.url)) + "/series/" + Net.enc(this.profile.user) + "/" + Net.enc(this.profile.pass) + "/" + Net.enc(strOptString) + "." + str2, "Сезон " + str);
                            channel2.kind = "vod";
                            channel2.headers.putAll(headers());
                            arrayList.add(channel2);
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    Map<String, String> stbHeaders() {
        Map<String, String> mapHeaders = headers();
        mapHeaders.put("User-Agent", this.profile.agent.equals("NovaIPTV/1.0") ? "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG250 stbapp ver: 4 rev: 1812 Mobile Safari/533.3" : this.profile.agent);
        mapHeaders.put("X-User-Agent", "Model: MAG250; Link: Ethernet");
        mapHeaders.put(HttpHeaders.REFERER, String.valueOf(this.portal.replaceAll("/(server/load|portal)\\.php$", "")) + "/c/");
        mapHeaders.put(HttpHeaders.COOKIE, "mac=" + Net.enc(this.profile.mac.toUpperCase(Locale.ROOT)) + "; stb_lang=en; timezone=" + Net.enc(TimeZone.getDefault().getID()));
        if (!this.token.isEmpty()) {
            mapHeaders.put("Authorization", "Bearer " + this.token);
        }
        return mapHeaders;
    }

    Object stb(String str) throws Exception {
        Object objOpt = obj(json(String.valueOf(this.portal) + "?" + str + "&JsHttpRequest=1-xml", stbHeaders())).opt("js");
        if (objOpt == null || objOpt == JSONObject.NULL) {
            throw new IOException("Порталът отказа заявката.");
        }
        return objOpt;
    }

    void handshake() throws Exception {
        String str;
        if (!this.profile.mac.matches("(?i)[0-9a-f]{2}(:[0-9a-f]{2}){5}")) {
            throw new IOException("MAC адресът трябва да е във формат 00:1A:79:12:34:56.");
        }
        ArrayList arrayList = new ArrayList();
        String strRoot = Net.root(this.profile.url.trim());
        if (strRoot.endsWith(".php")) {
            arrayList.add(strRoot);
        } else {
            String strReplaceAll = strRoot.replaceAll("/c/?$", "");
            arrayList.add(String.valueOf(strReplaceAll) + "/portal.php");
            arrayList.add(String.valueOf(strReplaceAll) + "/server/load.php");
            if (!strReplaceAll.endsWith("/stalker_portal")) {
                strRoot = String.valueOf(strReplaceAll) + "/stalker_portal/server/load.php";
                arrayList.add(strRoot);
            }
        }
        Iterator it = arrayList.iterator();
        Exception e = null;
        while (it.hasNext()) {
            this.portal = (String) it.next();
            this.token = "";
            try {
                String strOptString = obj(stb("type=stb&action=handshake&token=")).optString("token");
                this.token = strOptString;
                if (strOptString.isEmpty()) {
                    throw new IOException("Порталът не предостави токен.");
                }
                this.tokenAt = System.currentTimeMillis();
                if (this.profile.serial.isEmpty()) {
                    str = "";
                } else {
                    str = "&sn=" + Net.enc(this.profile.serial);
                }
                if (!this.profile.device.isEmpty()) {
                    str = String.valueOf(str) + "&device_id=" + Net.enc(this.profile.device) + "&device_id2=" + Net.enc(this.profile.device);
                }
                JSONObject jSONObjectObj = obj(stb("type=stb&action=get_profile&hd=1&stb_type=MAG250&image_version=218&auth_second_step=1" + str));
                if (jSONObjectObj.optInt("blocked", 0) == 1 || jSONObjectObj.optInt(NotificationCompat.CATEGORY_STATUS, 0) == 1) {
                    throw new IOException("MAC профилът е блокиран от доставчика.");
                }
                return;
            } catch (Exception e2) {
                e = e2;
            }
        }
        this.token = "";
        if (e != null) {
            throw e;
        }
        throw new IOException("Неуспешна връзка с портала.");
    }

    Models.Catalog stalker() throws Exception {
        handshake();
        HashMap map = new HashMap();
        Object objStb = stb("type=itv&action=get_genres");
        if (objStb instanceof JSONArray) {
            JSONArray jSONArray = (JSONArray) objStb;
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObjectOptJSONObject = jSONArray.optJSONObject(i);
                if (jSONObjectOptJSONObject != null) {
                    map.put(jSONObjectOptJSONObject.optString(TtmlNode.ATTR_ID), jSONObjectOptJSONObject.optString("title", "Други"));
                }
            }
        }
        Models.Catalog catalog = new Models.Catalog();
        catalog.epg = this.profile.epg;
        HashSet hashSet = new HashSet();
        for (int i2 = 1; i2 <= 1000; i2++) {
            JSONObject jSONObjectObj = obj(stb("type=itv&action=get_ordered_list&genre=*&fav=0&sortby=number&p=" + i2 + "&from_ch_id=0"));
            JSONArray jSONArrayOptJSONArray = jSONObjectObj.optJSONArray("data");
            if (jSONArrayOptJSONArray == null || jSONArrayOptJSONArray.length() == 0) {
                break;
            }
            int i3 = 0;
            for (int i4 = 0; i4 < jSONArrayOptJSONArray.length(); i4++) {
                JSONObject jSONObjectOptJSONObject2 = jSONArrayOptJSONArray.optJSONObject(i4);
                if (jSONObjectOptJSONObject2 != null) {
                    String strOptString = jSONObjectOptJSONObject2.optString(TtmlNode.ATTR_ID);
                    if (hashSet.add(strOptString)) {
                        Models.Channel channel = new Models.Channel();
                        channel.id = "stb:" + strOptString;
                        channel.name = jSONObjectOptJSONObject2.optString("name", "Канал");
                        channel.group = map.containsKey(jSONObjectOptJSONObject2.optString("tv_genre_id")) ? (String) map.get(jSONObjectOptJSONObject2.optString("tv_genre_id")) : "Други";
                        channel.cmd = jSONObjectOptJSONObject2.optString("cmd");
                        channel.logo = jSONObjectOptJSONObject2.optString("logo");
                        channel.epgId = jSONObjectOptJSONObject2.optString("xmltv_id", strOptString);
                        catalog.items.add(channel);
                        i3++;
                    }
                }
            }
            if (i3 == 0 || catalog.items.size() >= jSONObjectObj.optInt("total_items", Integer.MAX_VALUE)) {
                break;
            }
            if (catalog.items.size() > 50000) {
                throw new IOException("Прекалено много канали от портала.");
            }
        }
        if (catalog.items.isEmpty()) {
            throw new IOException("Няма канали за този MAC профил.");
        }
        return catalog;
    }

    public Models.Channel resolve(Models.Channel channel) throws Exception {
        Object objStb;
        if (!this.profile.type.equals("stb")) {
            return channel;
        }
        if (this.token.isEmpty() || System.currentTimeMillis() - this.tokenAt > 1200000) {
            handshake();
        }
        try {
            objStb = stb("type=itv&action=create_link&cmd=" + Net.enc(channel.cmd) + "&series=0&forced_storage=0&disable_ad=0&download=0");
        } catch (IOException unused) {
            handshake();
            objStb = stb("type=itv&action=create_link&cmd=" + Net.enc(channel.cmd) + "&series=0&forced_storage=0&disable_ad=0&download=0");
        }
        String strReplaceFirst = obj(objStb).optString("cmd").trim().replaceFirst("^(ffmpeg|ffrt|auto)\\s+", "");
        int iIndexOf = strReplaceFirst.indexOf("http");
        if (iIndexOf < 0) {
            throw new IOException("Порталът не предостави адрес за възпроизвеждане.");
        }
        String str = strReplaceFirst.substring(iIndexOf).split("\\s+")[0];
        Models.Channel channel2 = new Models.Channel(channel.id, channel.name, "", channel.group);
        channel2.epgId = channel.epgId;
        channel2.kind = channel.kind;
        M3u.parseUrl(channel2, str, this.portal);
        channel2.headers.putAll(stbHeaders());
        return channel2;
    }
}
