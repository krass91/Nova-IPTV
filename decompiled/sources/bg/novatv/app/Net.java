package bg.novatv.app;

import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLException;

/* JADX INFO: loaded from: classes.dex */
public final class Net {
    public static String enc(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception unused) {
            return "";
        }
    }

    public static String root(String str) throws IOException {
        try {
            URI uri = new URI(str.trim());
            if ((!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new Exception();
            }
            String path = uri.getPath();
            if (path == null) {
                path = "";
            }
            String[] strArr = {"/player_api.php", "/get.php", "/xmltv.php"};
            for (int i = 0; i < 3; i++) {
                String str2 = strArr[i];
                if (path.endsWith(str2)) {
                    path = path.substring(0, path.length() - str2.length());
                }
            }
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), path.replaceAll("/+$", ""), null, null).toASCIIString();
        } catch (Exception unused) {
            throw new IOException("Въведи валиден HTTP или HTTPS адрес на сървъра.");
        }
    }

    public static InputStream open(String str, Map<String, String> map) throws IOException {
        String strOrigin = origin(str);
        for (int i = 0; i < 6; i++) {
            URL url = new URL(str);
            if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
                throw new IOException("Адресът трябва да е HTTP или HTTPS.");
            }
            final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(12000);
            httpURLConnection.setReadTimeout(22000);
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestProperty("User-Agent", "NovaIPTV/1.0");
            httpURLConnection.setRequestProperty(HttpHeaders.ACCEPT_ENCODING, "gzip");
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (!entry.getKey().contains("\r") && !entry.getKey().contains("\n") && !entry.getValue().contains("\r") && !entry.getValue().contains("\n") && (strOrigin.equals(origin(str)) || (!entry.getKey().equalsIgnoreCase("Authorization") && !entry.getKey().equalsIgnoreCase(HttpHeaders.COOKIE)))) {
                        httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
            }
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode < 300 || responseCode >= 400) {
                if (responseCode < 200 || responseCode >= 300) {
                    httpURLConnection.disconnect();
                    throw new IOException((responseCode == 401 || responseCode == 403) ? "Достъпът е отказан. Провери профила и абонамента." : "Сървърът върна HTTP " + responseCode + ".");
                }
                InputStream inputStream = httpURLConnection.getInputStream();
                if ("gzip".equalsIgnoreCase(httpURLConnection.getContentEncoding()) || url.getPath().endsWith(".gz")) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                return new FilterInputStream(inputStream) { // from class: bg.novatv.app.Net.1
                    @Override // java.io.FilterInputStream, java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            httpURLConnection.disconnect();
                        }
                    }
                };
            }
            String headerField = httpURLConnection.getHeaderField("Location");
            httpURLConnection.disconnect();
            if (headerField == null) {
                throw new IOException("Празно пренасочване от сървъра.");
            }
            str = new URL(url, headerField).toString();
        }
        throw new IOException("Твърде много пренасочвания.");
    }

    static String origin(String str) throws IOException {
        URL url = new URL(str);
        return String.valueOf(url.getProtocol()) + "://" + url.getHost() + ":" + url.getPort();
    }

    public static String get(String str, Map<String, String> map) throws IOException {
        Throwable th = null;
        try {
            InputStream inputStreamOpen = open(str, map);
            try {
                return read(inputStreamOpen, 26214400);
            } finally {
                if (inputStreamOpen != null) {
                    inputStreamOpen.close();
                }
            }
        } catch (Throwable th2) {
            if (0 == 0) {
                throw th2;
            }
            if (null != th2) {
                th.addSuppressed(th2);
            }
            throw null;
        }
    }

    public static String read(InputStream inputStream, int i) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[16384];
        while (true) {
            int i2 = inputStream.read(bArr);
            if (i2 == -1) {
                return new String(byteArrayOutputStream.toByteArray(), "UTF-8");
            }
            if (Thread.currentThread().isInterrupted()) {
                throw new IOException("Отменено.");
            }
            if (byteArrayOutputStream.size() + i2 > i) {
                throw new IOException("Файлът е твърде голям.");
            }
            byteArrayOutputStream.write(bArr, 0, i2);
        }
    }

    public static String friendly(Throwable th) {
        if (th instanceof SocketTimeoutException) {
            return "Сървърът не отговори навреме. Опитай отново.";
        }
        if (th instanceof UnknownHostException) {
            return "Сървърът не е намерен. Провери адреса и мрежата.";
        }
        if (th instanceof SSLException) {
            return "Сертификатът на сървъра не е валиден.";
        }
        if (th instanceof IOException) {
            return th.getMessage() == null ? "Проблем с връзката." : th.getMessage();
        }
        return "Невалиден отговор от доставчика. Провери типа на профила.";
    }
}
