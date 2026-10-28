package bg.novatv.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/* JADX INFO: loaded from: classes.dex */
public final class Models {

    public static class Catalog {
        public List<Channel> items = new ArrayList();
        public String epg = "";
        public String note = "";
    }

    public static class Profile {
        public String id = UUID.randomUUID().toString();
        public String name = "";
        public String type = "m3u";
        public String url = "";
        public String user = "";
        public String pass = "";
        public String mac = "";
        public String epg = "";
        public String agent = "NovaIPTV/1.0";
        public String serial = "";
        public String device = "";
    }

    public static class Channel {
        public String cmd;
        public String epgId;
        public String group;
        public Map<String, String> headers;
        public String id;
        public String kind;
        public String logo;
        public String mime;
        public String name;
        public String seriesId;
        public String url;

        public Channel() {
            this.id = "";
            this.name = "";
            this.url = "";
            this.group = "Други";
            this.logo = "";
            this.epgId = "";
            this.kind = "live";
            this.cmd = "";
            this.seriesId = "";
            this.mime = "";
            this.headers = new LinkedHashMap();
        }

        public Channel(String str, String str2, String str3, String str4) {
            this.id = "";
            this.name = "";
            this.url = "";
            this.group = "Други";
            this.logo = "";
            this.epgId = "";
            this.kind = "live";
            this.cmd = "";
            this.seriesId = "";
            this.mime = "";
            this.headers = new LinkedHashMap();
            this.id = str;
            this.name = str2;
            this.url = str3;
            this.group = str4;
        }
    }

    public static class Program {
        public String channel;
        public String description;
        public long end;
        public long start;
        public String title;

        public Program(String str, String str2, String str3, long j, long j2) {
            this.channel = str;
            this.title = str2;
            this.description = str3;
            this.start = j;
            this.end = j2;
        }
    }
}
