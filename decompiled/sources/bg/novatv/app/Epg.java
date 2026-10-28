package bg.novatv.app;

import androidx.media3.extractor.text.ttml.TtmlNode;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/* JADX INFO: loaded from: classes.dex */
public final class Epg {
    public int count;
    public Map<String, List<Models.Program>> programs = new HashMap();

    public static Epg parse(InputStream inputStream, final long j) throws Exception {
        final Epg epg = new Epg();
        SAXParserFactory sAXParserFactoryNewInstance = SAXParserFactory.newInstance();
        sAXParserFactoryNewInstance.setNamespaceAware(false);
        String[] strArr = {"http://xml.org/sax/features/external-general-entities", "http://xml.org/sax/features/external-parameter-entities", "http://apache.org/xml/features/nonvalidating/load-external-dtd"};
        for (int i = 0; i < 3; i++) {
            try {
                sAXParserFactoryNewInstance.setFeature(strArr[i], false);
            } catch (Exception unused) {
            }
        }
        XMLReader xMLReader = sAXParserFactoryNewInstance.newSAXParser().getXMLReader();
        xMLReader.setEntityResolver(new EntityResolver() { // from class: bg.novatv.app.Epg$$ExternalSyntheticLambda0
            @Override // org.xml.sax.EntityResolver
            public final InputSource resolveEntity(String str, String str2) {
                return Epg.lambda$0(str, str2);
            }
        });
        xMLReader.setContentHandler(new DefaultHandler() { // from class: bg.novatv.app.Epg.1
            boolean active;
            long end;
            long start;
            String channel = "";
            String title = "";
            String desc = "";
            String field = "";
            StringBuilder buf = new StringBuilder();

            /* JADX WARN: Code duplicated, block: B:11:0x004a  */
            @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
            public void startElement(String str, String str2, String str3, Attributes attributes) throws SAXException {
                boolean z;
                if (str3.equals("programme")) {
                    this.channel = attributes.getValue("channel");
                    this.start = Epg.time(attributes.getValue(TtmlNode.START));
                    long jTime = Epg.time(attributes.getValue("stop"));
                    this.end = jTime;
                    this.title = "";
                    this.desc = "";
                    if (this.channel != null) {
                        long j2 = j;
                        if (jTime <= j2 - 3600000 || this.start >= j2 + 604800000) {
                            z = false;
                        } else {
                            z = true;
                        }
                    } else {
                        z = false;
                    }
                    this.active = z;
                }
                if (this.active) {
                    if (str3.equals("title") || str3.equals("desc")) {
                        this.field = str3;
                        this.buf.setLength(0);
                    }
                }
            }

            @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
            public void characters(char[] cArr, int i2, int i3) {
                if (!this.active || this.field.isEmpty() || this.buf.length() >= 8000) {
                    return;
                }
                StringBuilder sb = this.buf;
                sb.append(cArr, i2, Math.min(i3, 8000 - sb.length()));
            }

            @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
            public void endElement(String str, String str2, String str3) throws SAXException {
                if (str3.equals(this.field)) {
                    if (this.field.equals("title") && this.title.isEmpty()) {
                        this.title = this.buf.toString();
                    }
                    if (this.field.equals("desc") && this.desc.isEmpty()) {
                        this.desc = this.buf.toString();
                    }
                    this.field = "";
                }
                if (str3.equals("programme")) {
                    if (this.active) {
                        long j2 = this.start;
                        if (j2 > 0 && this.end > j2) {
                            List<Models.Program> arrayList = epg.programs.get(this.channel);
                            if (arrayList == null) {
                                arrayList = new ArrayList<>();
                                epg.programs.put(this.channel, arrayList);
                            }
                            arrayList.add(new Models.Program(this.channel, this.title, this.desc, this.start, this.end));
                            Epg epg2 = epg;
                            int i2 = epg2.count + 1;
                            epg2.count = i2;
                            if (i2 > 300000) {
                                throw new SAXException("Програмата е твърде голяма.");
                            }
                        }
                    }
                    this.active = false;
                    this.field = "";
                }
            }
        });
        xMLReader.parse(new InputSource(new Limited(inputStream, 83886080)));
        Iterator<List<Models.Program>> it = epg.programs.values().iterator();
        while (it.hasNext()) {
            Collections.sort(it.next(), new Comparator() { // from class: bg.novatv.app.Epg$$ExternalSyntheticLambda1
                @Override // java.util.Comparator
                public final int compare(Object obj, Object obj2) {
                    return Long.compare(((Models.Program) obj).start, ((Models.Program) obj2).start);
                }
            });
        }
        return epg;
    }

    static /* synthetic */ InputSource lambda$0(String str, String str2) throws SAXException, IOException {
        return new InputSource(new StringReader(""));
    }

    static class Limited extends FilterInputStream {
        int limit;

        Limited(InputStream inputStream, int i) {
            super(inputStream);
            this.limit = i;
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int read() throws IOException {
            int i = this.limit - 1;
            this.limit = i;
            if (i >= 0) {
                return super.read();
            }
            throw new IOException("Програмата е твърде голяма.");
        }

        @Override // java.io.FilterInputStream, java.io.InputStream
        public int read(byte[] bArr, int i, int i2) throws IOException {
            int i3 = this.limit;
            if (i3 <= 0) {
                throw new IOException("Програмата е твърде голяма.");
            }
            int i4 = super.read(bArr, i, Math.min(i2, i3));
            if (i4 > 0) {
                this.limit -= i4;
            }
            return i4;
        }
    }

    public static long time(String str) {
        if (str == null || str.length() < 14) {
            return 0L;
        }
        try {
            String strTrim = str.trim();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strTrim.length() > 14 ? "yyyyMMddHHmmss Z" : "yyyyMMddHHmmss", Locale.US);
            simpleDateFormat.setLenient(false);
            if (strTrim.length() == 14) {
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            return simpleDateFormat.parse(strTrim).getTime();
        } catch (Exception unused) {
            return 0L;
        }
    }

    public List<Models.Program> forChannel(Models.Channel channel) {
        List<Models.Program> list = this.programs.get(channel.epgId);
        if (list == null) {
            list = this.programs.get(channel.name);
        }
        return list == null ? Collections.emptyList() : list;
    }

    public Models.Program now(Models.Channel channel) {
        long jCurrentTimeMillis = System.currentTimeMillis();
        for (Models.Program program : forChannel(channel)) {
            if (program.start <= jCurrentTimeMillis && program.end > jCurrentTimeMillis) {
                return program;
            }
        }
        return null;
    }
}
