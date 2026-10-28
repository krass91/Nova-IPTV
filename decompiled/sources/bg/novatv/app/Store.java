package bg.novatv.app;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public final class Store {
    private final Context context;
    private final File file;
    public ArrayList<Models.Profile> profiles = new ArrayList<>();
    public Set<String> favorites = new HashSet();
    public String active = "";

    Store(Context context) throws Exception {
        this.context = context;
        this.file = new File(context.getFilesDir(), "profiles.secure");
        load();
    }

    SecretKey key() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (!keyStore.containsAlias("nova_profiles_v1")) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder("nova_profiles_v1", 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
            keyGenerator.generateKey();
        }
        return (SecretKey) keyStore.getKey("nova_profiles_v1", null);
    }

    static JSONObject profileJson(Models.Profile profile) throws Exception {
        JSONObject jSONObject = new JSONObject();
        for (Field field : Models.Profile.class.getFields()) {
            jSONObject.put(field.getName(), field.get(profile));
        }
        return jSONObject;
    }

    static Models.Profile profileFrom(JSONObject jSONObject) throws Exception {
        Models.Profile profile = new Models.Profile();
        for (Field field : Models.Profile.class.getFields()) {
            if (jSONObject.has(field.getName())) {
                field.set(profile, jSONObject.optString(field.getName(), ""));
            }
        }
        return profile;
    }

    void load() throws Exception {
        if (this.file.exists()) {
            Throwable th = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(this.file);
                try {
                    String str = Net.read(fileInputStream, 10485760);
                    fileInputStream.close();
                    JSONObject jSONObject = new JSONObject(str);
                    byte[] bArrDecode = Base64.decode(jSONObject.getString("iv"), 2);
                    byte[] bArrDecode2 = Base64.decode(jSONObject.getString("data"), 2);
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    cipher.init(2, key(), new GCMParameterSpec(128, bArrDecode));
                    JSONObject jSONObject2 = new JSONObject(new String(cipher.doFinal(bArrDecode2), "UTF-8"));
                    this.active = jSONObject2.optString("active");
                    JSONArray jSONArrayOptJSONArray = jSONObject2.optJSONArray("profiles");
                    if (jSONArrayOptJSONArray != null) {
                        for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
                            this.profiles.add(profileFrom(jSONArrayOptJSONArray.getJSONObject(i)));
                        }
                    }
                    JSONArray jSONArrayOptJSONArray2 = jSONObject2.optJSONArray("favorites");
                    if (jSONArrayOptJSONArray2 != null) {
                        for (int i2 = 0; i2 < jSONArrayOptJSONArray2.length(); i2++) {
                            this.favorites.add(jSONArrayOptJSONArray2.getString(i2));
                        }
                    }
                } catch (Throwable th2) {
                    fileInputStream.close();
                    throw th2;
                }
            } catch (Throwable th3) {
                if (0 == 0) {
                    throw th3;
                }
                if (null == th3) {
                    throw null;
                }
                th.addSuppressed(th3);
            }
        }
    }

    synchronized void save() throws Exception {
        JSONObject jSONObject = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        Iterator<Models.Profile> it = this.profiles.iterator();
        while (it.hasNext()) {
            jSONArray.put(profileJson(it.next()));
        }
        jSONObject.put("profiles", jSONArray);
        jSONObject.put("favorites", new JSONArray((Collection) this.favorites));
        jSONObject.put("active", this.active);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(1, key());
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put("iv", Base64.encodeToString(cipher.getIV(), 2));
        jSONObject2.put("data", Base64.encodeToString(cipher.doFinal(jSONObject.toString().getBytes("UTF-8")), 2));
        File file = new File(this.context.getFilesDir(), "profiles.tmp");
        Throwable th = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(jSONObject2.toString().getBytes("UTF-8"));
                fileOutputStream.getFD().sync();
                fileOutputStream.close();
                if (!file.renameTo(this.file)) {
                    throw new IOException("Профилът не може да бъде запазен.");
                }
            } catch (Throwable th2) {
                fileOutputStream.close();
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

    Models.Profile current() {
        for (Models.Profile profile : this.profiles) {
            if (profile.id.equals(this.active)) {
                return profile;
            }
        }
        if (this.profiles.isEmpty()) {
            return null;
        }
        return this.profiles.get(0);
    }

    String favKey(Models.Profile profile, Models.Channel channel) {
        return String.valueOf(profile.id) + "|" + channel.id;
    }

    boolean favorite(Models.Profile profile, Models.Channel channel) {
        return this.favorites.contains(favKey(profile, channel));
    }

    void toggle(Models.Profile profile, Models.Channel channel) throws Exception {
        String strFavKey = favKey(profile, channel);
        if (!this.favorites.remove(strFavKey)) {
            this.favorites.add(strFavKey);
        }
        save();
    }

    void remove(Models.Profile profile) throws Exception {
        CatalogCache.clear(this.context, profile);
        CatalogCache.file(this.context, profile, "source").delete();
        this.profiles.remove(profile);
        Iterator<String> it = this.favorites.iterator();
        while (it.hasNext()) {
            if (it.next().startsWith(String.valueOf(profile.id) + "|")) {
                it.remove();
            }
        }
        if (this.active.equals(profile.id)) {
            this.active = this.profiles.isEmpty() ? "" : this.profiles.get(0).id;
        }
        save();
    }
}
