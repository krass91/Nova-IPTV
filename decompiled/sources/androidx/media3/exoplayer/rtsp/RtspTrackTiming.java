package androidx.media3.exoplayer.rtsp;

import android.net.Uri;
import androidx.media3.common.C;
import androidx.media3.common.ParserException;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.UriUtil;
import androidx.media3.common.util.Util;
import com.google.common.collect.ImmutableList;

/* JADX INFO: loaded from: classes.dex */
final class RtspTrackTiming {
    public final long rtpTimestamp;
    public final int sequenceNumber;
    public final Uri uri;

    /* JADX WARN: Code duplicated, block: B:23:0x0067  */
    public static ImmutableList<RtspTrackTiming> parseTrackTiming(String str, Uri uri) throws ParserException {
        byte b;
        ImmutableList.Builder builder = new ImmutableList.Builder();
        String[] strArrSplit = Util.split(str, ",");
        int length = strArrSplit.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            String str2 = strArrSplit[i2];
            String[] strArrSplit2 = Util.split(str2, ";");
            int length2 = strArrSplit2.length;
            int i3 = i;
            Uri uriResolveUri = null;
            int i4 = -1;
            long j = C.TIME_UNSET;
            while (i3 < length2) {
                String str3 = strArrSplit2[i3];
                try {
                    String[] strArrSplitAtFirst = Util.splitAtFirst(str3, "=");
                    String str4 = strArrSplitAtFirst[i];
                    String str5 = strArrSplitAtFirst[1];
                    int iHashCode = str4.hashCode();
                    String[] strArr = strArrSplit;
                    if (iHashCode != 113759) {
                        if (iHashCode != 116079) {
                            if (iHashCode == 1524180539 && str4.equals("rtptime")) {
                                b = 2;
                            } else {
                                b = -1;
                            }
                        } else if (str4.equals("url")) {
                            b = 0;
                        } else {
                            b = -1;
                        }
                    } else if (str4.equals("seq")) {
                        b = 1;
                    } else {
                        b = -1;
                    }
                    if (b == 0) {
                        uriResolveUri = resolveUri(str5, uri);
                    } else if (b == 1) {
                        i4 = Integer.parseInt(str5);
                    } else if (b == 2) {
                        j = Long.parseLong(str5);
                    } else {
                        throw ParserException.createForMalformedManifest(str4, null);
                    }
                    i3++;
                    strArrSplit = strArr;
                    i = 0;
                } catch (Exception e) {
                    throw ParserException.createForMalformedManifest(str3, e);
                }
            }
            String[] strArr2 = strArrSplit;
            if (uriResolveUri != null && uriResolveUri.getScheme() != null) {
                long j2 = j;
                if (i4 != -1 || j2 != C.TIME_UNSET) {
                    builder.add(new RtspTrackTiming(j2, i4, uriResolveUri));
                    i2++;
                    strArrSplit = strArr2;
                    i = 0;
                }
            }
            throw ParserException.createForMalformedManifest(str2, null);
        }
        return builder.build();
    }

    static Uri resolveUri(String str, Uri uri) {
        Assertions.checkArgument(((String) Assertions.checkNotNull(uri.getScheme())).equals("rtsp"));
        Uri uri2 = Uri.parse(str);
        if (uri2.isAbsolute()) {
            return uri2;
        }
        Uri uri3 = Uri.parse("rtsp://" + str);
        String string = uri.toString();
        if (((String) Assertions.checkNotNull(uri3.getHost())).equals(uri.getHost())) {
            return uri3;
        }
        if (string.endsWith("/")) {
            return UriUtil.resolveToUri(string, str);
        }
        return UriUtil.resolveToUri(string + "/", str);
    }

    private RtspTrackTiming(long j, int i, Uri uri) {
        this.rtpTimestamp = j;
        this.sequenceNumber = i;
        this.uri = uri;
    }
}
