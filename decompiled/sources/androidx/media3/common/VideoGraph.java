package androidx.media3.common;

/* JADX INFO: loaded from: classes.dex */
public interface VideoGraph {

    public interface Listener {

        /* JADX INFO: renamed from: androidx.media3.common.VideoGraph$Listener$-CC, reason: invalid class name */
        public final /* synthetic */ class CC {
            public static void $default$onEnded(Listener _this, long j) {
            }

            public static void $default$onError(Listener _this, VideoFrameProcessingException videoFrameProcessingException) {
            }

            public static void $default$onOutputFrameAvailableForRendering(Listener _this, long j) {
            }

            public static void $default$onOutputSizeChanged(Listener _this, int i, int i2) {
            }
        }

        void onEnded(long j);

        void onError(VideoFrameProcessingException videoFrameProcessingException);

        void onOutputFrameAvailableForRendering(long j);

        void onOutputSizeChanged(int i, int i2);
    }

    VideoFrameProcessor getProcessor(int i);

    boolean hasProducedFrameWithTimestampZero();

    void initialize() throws VideoFrameProcessingException;

    void registerInput(int i) throws VideoFrameProcessingException;

    void release();

    void setOutputSurfaceInfo(SurfaceInfo surfaceInfo);
}
