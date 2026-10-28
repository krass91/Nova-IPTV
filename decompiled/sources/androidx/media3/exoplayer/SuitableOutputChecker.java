package androidx.media3.exoplayer;

/* JADX INFO: loaded from: classes.dex */
public interface SuitableOutputChecker {

    public interface Callback {
        void onSelectedOutputSuitabilityChanged(boolean z);
    }

    void disable();

    void enable(Callback callback);

    boolean isSelectedOutputSuitableForPlayback();
}
