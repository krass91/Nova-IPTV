package androidx.media3.exoplayer.source.preload;

import androidx.media3.common.MediaItem;

/* JADX INFO: loaded from: classes.dex */
public final class PreloadException extends Exception {
    public final MediaItem mediaItem;

    public PreloadException(MediaItem mediaItem, String str, Throwable th) {
        super(str, th);
        this.mediaItem = mediaItem;
    }

    /* JADX WARN: Code restructure failed: missing block: B:20:0x003e, code lost:
    
        if (r3 == null) goto L22;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean errorInfoEquals(androidx.media3.exoplayer.source.preload.PreloadException r7) {
        /*
            r6 = this;
            r0 = 1
            if (r6 != r7) goto L4
            return r0
        L4:
            r1 = 0
            if (r7 == 0) goto L5c
            java.lang.Class r2 = r6.getClass()
            java.lang.Class r3 = r7.getClass()
            if (r2 == r3) goto L12
            goto L5c
        L12:
            java.lang.Throwable r2 = r6.getCause()
            java.lang.Throwable r3 = r7.getCause()
            if (r2 == 0) goto L3c
            if (r3 == 0) goto L3c
            java.lang.String r4 = r2.getMessage()
            java.lang.String r5 = r3.getMessage()
            boolean r4 = java.util.Objects.equals(r4, r5)
            if (r4 != 0) goto L2d
            return r1
        L2d:
            java.lang.Class r2 = r2.getClass()
            java.lang.Class r3 = r3.getClass()
            boolean r2 = java.util.Objects.equals(r2, r3)
            if (r2 != 0) goto L41
            return r1
        L3c:
            if (r2 != 0) goto L5c
            if (r3 == 0) goto L41
            goto L5c
        L41:
            androidx.media3.common.MediaItem r2 = r6.mediaItem
            androidx.media3.common.MediaItem r3 = r7.mediaItem
            boolean r2 = java.util.Objects.equals(r2, r3)
            if (r2 == 0) goto L5a
            java.lang.String r2 = r6.getMessage()
            java.lang.String r7 = r7.getMessage()
            boolean r7 = java.util.Objects.equals(r2, r7)
            if (r7 == 0) goto L5a
            goto L5b
        L5a:
            r0 = r1
        L5b:
            return r0
        L5c:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.media3.exoplayer.source.preload.PreloadException.errorInfoEquals(androidx.media3.exoplayer.source.preload.PreloadException):boolean");
    }
}
