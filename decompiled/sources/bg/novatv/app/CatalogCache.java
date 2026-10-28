package bg.novatv.app;

import android.content.Context;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
final class CatalogCache {
    CatalogCache() {
    }

    static File file(Context context, Models.Profile profile, String str) {
        return new File(context.getFilesDir(), "catalog-" + profile.id + "-" + str + ".bin");
    }

    static void save(Context context, Store store, Models.Profile profile, String str, Models.Catalog catalog) throws Exception {
        JSONArray jSONArray = new JSONArray();
        for (Models.Channel channel : catalog.items) {
            JSONObject jSONObject = new JSONObject();
            for (Field field : Models.Channel.class.getFields()) {
                if (field.getName().equals("headers")) {
                    jSONObject.put("headers", new JSONObject(channel.headers));
                } else {
                    jSONObject.put(field.getName(), field.get(channel));
                }
            }
            jSONArray.put(jSONObject);
        }
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put("channels", jSONArray);
        jSONObject2.put("epg", catalog.epg);
        jSONObject2.put("time", System.currentTimeMillis());
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, store.key());
        byte[] bArrDoFinal = cipher.doFinal(jSONObject2.toString().getBytes("UTF-8"));
        File file = file(context, profile, str);
        File file2 = new File(file + ".tmp");
        Throwable th = null;
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file2));
            try {
                byte[] iv = cipher.getIV();
                dataOutputStream.writeInt(iv.length);
                dataOutputStream.write(iv);
                dataOutputStream.write(bArrDoFinal);
                dataOutputStream.close();
                if (!file2.renameTo(file)) {
                    throw new IOException();
                }
            } catch (Throwable th2) {
                dataOutputStream.close();
                throw th2;
            }
        } catch (Throwable th3) {
            if (0 == 0) {
                throw th3;
            }
            if (null != th3) {
                th.addSuppressed(th3);
            }
            throw null;
        }
    }

    static Models.Catalog read(Context context, Store store, Models.Profile profile, String str) throws Exception {
        File file = file(context, profile, str);
        Throwable th = null;
        if (!file.exists() || file.length() > 41943040) {
            return null;
        }
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            try {
                int i = dataInputStream.readInt();
                if (i < 8 || i > 32) {
                    throw new IOException();
                }
                byte[] bArr = new byte[i];
                dataInputStream.readFully(bArr);
                byte[] bArr2 = new byte[(((int) file.length()) - 4) - i];
                dataInputStream.readFully(bArr2);
                dataInputStream.close();
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(2, store.key(), new GCMParameterSpec(128, bArr));
                JSONObject jSONObject = new JSONObject(new String(cipher.doFinal(bArr2), "UTF-8"));
                JSONArray jSONArray = jSONObject.getJSONArray("channels");
                Models.Catalog catalog = new Models.Catalog();
                catalog.epg = jSONObject.optString("epg");
                for (int i2 = 0; i2 < jSONArray.length(); i2++) {
                    JSONObject jSONObject2 = jSONArray.getJSONObject(i2);
                    Models.Channel channel = new Models.Channel();
                    for (Field field : Models.Channel.class.getFields()) {
                        if (!field.getName().equals("headers")) {
                            field.set(channel, jSONObject2.optString(field.getName(), ""));
                        }
                    }
                    JSONObject jSONObjectOptJSONObject = jSONObject2.optJSONObject("headers");
                    if (jSONObjectOptJSONObject != null) {
                        Iterator<String> itKeys = jSONObjectOptJSONObject.keys();
                        while (itKeys.hasNext()) {
                            String next = itKeys.next();
                            channel.headers.put(next, jSONObjectOptJSONObject.optString(next));
                        }
                    }
                    catalog.items.add(channel);
                }
                return catalog;
            } catch (Throwable th2) {
                dataInputStream.close();
                throw th2;
            }
        } catch (Throwable th3) {
            if (0 == 0) {
                throw th3;
            }
            if (null != th3) {
                th.addSuppressed(th3);
            }
            throw null;
        }
    }

    static void clear(Context context, Models.Profile profile) {
        File[] fileArrListFiles = context.getFilesDir().listFiles();
        if (fileArrListFiles != null) {
            for (File file : fileArrListFiles) {
                if (file.getName().startsWith("catalog-" + profile.id + "-") && !file.equals(file(context, profile, "source"))) {
                    file.delete();
                }
            }
        }
    }
}
