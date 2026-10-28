package androidx.media3.common.util;

/* JADX INFO: loaded from: classes.dex */
public interface TimestampIterator {

    /* JADX INFO: renamed from: androidx.media3.common.util.TimestampIterator$-CC, reason: invalid class name */
    public final /* synthetic */ class CC {
    }

    TimestampIterator copyOf();

    long getLastTimestampUs();

    boolean hasNext();

    long next();
}
