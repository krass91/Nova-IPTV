package androidx.media3.exoplayer.rtsp;

import androidx.media3.datasource.DataSource;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
interface RtpDataChannel extends DataSource {

    public interface Factory {

        /* JADX INFO: renamed from: androidx.media3.exoplayer.rtsp.RtpDataChannel$Factory$-CC, reason: invalid class name */
        public final /* synthetic */ class CC {
            public static Factory $default$createFallbackDataChannelFactory(Factory _this) {
                return null;
            }
        }

        RtpDataChannel createAndOpenDataChannel(int i) throws IOException;

        Factory createFallbackDataChannelFactory();
    }

    RtspMessageChannel.InterleavedBinaryDataListener getInterleavedBinaryDataListener();

    int getLocalPort();

    String getTransport();

    boolean needsClosingOnLoadCompletion();
}
