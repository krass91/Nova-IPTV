package bg.novatv.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
final class LogoLoader {
    final Context context;
    final File dir;
    final ExecutorService worker = Executors.newFixedThreadPool(2);
    final Handler ui = new Handler();
    final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(8388608) { // from class: bg.novatv.app.LogoLoader.1
        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.util.LruCache
        public int sizeOf(String str, Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };
    final Map<String, List<WeakReference<ImageView>>> pending = new HashMap();
    final Set<String> failed = new HashSet();

    LogoLoader(Context context) {
        this.context = context;
        File file = new File(context.getCacheDir(), "logos");
        this.dir = file;
        file.mkdirs();
    }

    void load(final String str, ImageView imageView) {
        imageView.setImageDrawable(null);
        imageView.setTag(str);
        if (str == null || !str.matches("(?i)^https?://.*")) {
            return;
        }
        Bitmap bitmap = this.cache.get(str);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        synchronized (this.pending) {
            if (this.failed.contains(str)) {
                return;
            }
            if (this.pending.containsKey(str)) {
                this.pending.get(str).add(new WeakReference<>(imageView));
            } else {
                if (this.pending.size() > 100) {
                    return;
                }
                ArrayList arrayList = new ArrayList();
                arrayList.add(new WeakReference(imageView));
                this.pending.put(str, arrayList);
                this.worker.submit(new Runnable() { // from class: bg.novatv.app.LogoLoader$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() throws Throwable {
                        this.f$0.m198lambda$0$bgnovatvappLogoLoader(str);
                    }
                });
            }
        }
    }

    /* JADX INFO: renamed from: lambda$0$bg-novatv-app-LogoLoader, reason: not valid java name */
    /* synthetic */ void m198lambda$0$bgnovatvappLogoLoader(final String str) throws Throwable {
        Throwable th;
        byte[] bArrBytes;
        Throwable th2;
        Throwable th3;
        final Bitmap bitmapDecodeByteArray = null;
        try {
            File file = new File(this.dir, hash(str));
            if (file.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    try {
                        bArrBytes = bytes(fileInputStream);
                        fileInputStream.close();
                    } catch (Throwable th4) {
                        th = th4;
                        try {
                            fileInputStream.close();
                            throw th;
                        } catch (Throwable th5) {
                            th = th5;
                            if (th == null) {
                                throw th;
                            }
                            if (th != th) {
                                th.addSuppressed(th);
                            }
                            throw th;
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    th = null;
                }
            } else {
                try {
                    InputStream inputStreamOpen = Net.open(str, null);
                    try {
                        byte[] bArrBytes2 = bytes(inputStreamOpen);
                        if (inputStreamOpen != null) {
                            inputStreamOpen.close();
                        }
                        File[] fileArrListFiles = this.dir.listFiles();
                        if (fileArrListFiles != null && fileArrListFiles.length > 300) {
                            Arrays.sort(fileArrListFiles, new Comparator() { // from class: bg.novatv.app.LogoLoader$$ExternalSyntheticLambda0
                                @Override // java.util.Comparator
                                public final int compare(Object obj, Object obj2) {
                                    return Long.compare(((File) obj).lastModified(), ((File) obj2).lastModified());
                                }
                            });
                            for (int i = 0; i < 100; i++) {
                                fileArrListFiles[i].delete();
                            }
                        }
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            try {
                                fileOutputStream.write(bArrBytes2);
                                fileOutputStream.close();
                                bArrBytes = bArrBytes2;
                            } catch (Throwable th7) {
                                th3 = th7;
                                try {
                                    fileOutputStream.close();
                                    throw th3;
                                } catch (Throwable th8) {
                                    th = th8;
                                    if (th3 == null) {
                                        throw th;
                                    }
                                    if (th3 != th) {
                                        th3.addSuppressed(th);
                                    }
                                    throw th3;
                                }
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            th3 = null;
                        }
                    } catch (Throwable th10) {
                        th2 = th10;
                        if (inputStreamOpen != null) {
                            try {
                                inputStreamOpen.close();
                            } catch (Throwable th11) {
                                th = th11;
                                if (th2 == null) {
                                    throw th;
                                }
                                if (th2 != th) {
                                    th2.addSuppressed(th);
                                }
                                throw th2;
                            }
                        }
                        throw th2;
                    }
                } catch (Throwable th12) {
                    th = th12;
                    th2 = null;
                }
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            int i2 = 1;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bArrBytes, 0, bArrBytes.length, options);
            if (options.outWidth > 0 && options.outHeight > 0 && options.outWidth < 20000 && options.outHeight < 20000) {
                while (true) {
                    if (options.outWidth / i2 <= 256 && options.outHeight / i2 <= 256) {
                        break;
                    } else {
                        i2 *= 2;
                    }
                }
                options.inJustDecodeBounds = false;
                options.inSampleSize = i2;
                bitmapDecodeByteArray = BitmapFactory.decodeByteArray(bArrBytes, 0, bArrBytes.length, options);
            }
        } catch (Exception unused) {
        }
        this.ui.post(new Runnable() { // from class: bg.novatv.app.LogoLoader$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m199lambda$2$bgnovatvappLogoLoader(str, bitmapDecodeByteArray);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$2$bg-novatv-app-LogoLoader, reason: not valid java name */
    /* synthetic */ void m199lambda$2$bgnovatvappLogoLoader(String str, Bitmap bitmap) {
        List<WeakReference<ImageView>> listRemove;
        synchronized (this.pending) {
            listRemove = this.pending.remove(str);
            if (bitmap == null) {
                this.failed.add(str);
            }
        }
        if (bitmap != null) {
            this.cache.put(str, bitmap);
            if (listRemove != null) {
                Iterator<WeakReference<ImageView>> it = listRemove.iterator();
                while (it.hasNext()) {
                    ImageView imageView = it.next().get();
                    if (imageView != null && str.equals(imageView.getTag())) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }

    static byte[] bytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[4096];
        while (true) {
            int i = inputStream.read(bArr);
            if (i <= 0) {
                return byteArrayOutputStream.toByteArray();
            }
            if (byteArrayOutputStream.size() + i > 1048576) {
                throw new IOException();
            }
            byteArrayOutputStream.write(bArr, 0, i);
        }
    }

    static String hash(String str) throws Exception {
        byte[] bArrDigest = MessageDigest.getInstance("SHA-256").digest(str.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : bArrDigest) {
            sb.append(String.format(Locale.US, "%02x", Integer.valueOf(b & 255)));
        }
        return sb.toString();
    }

    void close() {
        this.worker.shutdownNow();
        this.ui.removeCallbacksAndMessages(null);
    }
}
