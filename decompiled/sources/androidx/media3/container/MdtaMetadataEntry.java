package androidx.media3.container;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.media3.common.Format;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.ParsableByteArray;
import androidx.media3.common.util.Util;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kotlin.UByte$$ExternalSyntheticBackport0;

/* JADX INFO: loaded from: classes.dex */
public final class MdtaMetadataEntry implements Metadata.Entry {
    public static final Parcelable.Creator<MdtaMetadataEntry> CREATOR = new Parcelable.Creator<MdtaMetadataEntry>() { // from class: androidx.media3.container.MdtaMetadataEntry.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MdtaMetadataEntry createFromParcel(Parcel parcel) {
            return new MdtaMetadataEntry(parcel);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MdtaMetadataEntry[] newArray(int i) {
            return new MdtaMetadataEntry[i];
        }
    };
    public static final int DEFAULT_LOCALE_INDICATOR = 0;
    public static final byte EDITABLE_TRACKS_SAMPLES_LOCATION_INTERLEAVED = 1;
    public static final byte EDITABLE_TRACKS_SAMPLES_LOCATION_IN_EDIT_DATA_MP4 = 0;
    public static final String KEY_ANDROID_CAPTURE_FPS = "com.android.capture.fps";
    public static final String KEY_EDITABLE_TRACKS_LENGTH = "editable.tracks.length";
    public static final String KEY_EDITABLE_TRACKS_MAP = "editable.tracks.map";
    public static final String KEY_EDITABLE_TRACKS_OFFSET = "editable.tracks.offset";
    public static final String KEY_EDITABLE_TRACKS_SAMPLES_LOCATION = "editable.tracks.samples.location";
    public static final int TYPE_INDICATOR_8_BIT_UNSIGNED_INT = 75;
    public static final int TYPE_INDICATOR_FLOAT32 = 23;
    public static final int TYPE_INDICATOR_INT32 = 67;
    public static final int TYPE_INDICATOR_RESERVED = 0;
    public static final int TYPE_INDICATOR_STRING = 1;
    public static final int TYPE_INDICATOR_UNSIGNED_INT64 = 78;
    public final String key;
    public final int localeIndicator;
    public final int typeIndicator;
    public final byte[] value;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // androidx.media3.common.Metadata.Entry
    public /* synthetic */ byte[] getWrappedMetadataBytes() {
        return Metadata.Entry.CC.$default$getWrappedMetadataBytes(this);
    }

    @Override // androidx.media3.common.Metadata.Entry
    public /* synthetic */ Format getWrappedMetadataFormat() {
        return Metadata.Entry.CC.$default$getWrappedMetadataFormat(this);
    }

    @Override // androidx.media3.common.Metadata.Entry
    public /* synthetic */ void populateMediaMetadata(MediaMetadata.Builder builder) {
        Metadata.Entry.CC.$default$populateMediaMetadata(this, builder);
    }

    public MdtaMetadataEntry(String str, byte[] bArr, int i) {
        this(str, bArr, 0, i);
    }

    public MdtaMetadataEntry(String str, byte[] bArr, int i, int i2) {
        validateData(str, bArr, i2);
        this.key = str;
        this.value = bArr;
        this.localeIndicator = i;
        this.typeIndicator = i2;
    }

    private MdtaMetadataEntry(Parcel parcel) {
        String str = (String) Util.castNonNull(parcel.readString());
        this.key = str;
        byte[] bArr = (byte[]) Util.castNonNull(parcel.createByteArray());
        this.value = bArr;
        this.localeIndicator = parcel.readInt();
        int i = parcel.readInt();
        this.typeIndicator = i;
        validateData(str, bArr, i);
    }

    public List<Integer> getEditableTrackTypesFromMap() {
        Assertions.checkState(this.key.equals(KEY_EDITABLE_TRACKS_MAP), "Metadata is not an editable tracks map");
        byte b = this.value[1];
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < b; i++) {
            arrayList.add(Integer.valueOf(this.value[i + 2]));
        }
        return arrayList;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MdtaMetadataEntry mdtaMetadataEntry = (MdtaMetadataEntry) obj;
        return this.key.equals(mdtaMetadataEntry.key) && Arrays.equals(this.value, mdtaMetadataEntry.value) && this.localeIndicator == mdtaMetadataEntry.localeIndicator && this.typeIndicator == mdtaMetadataEntry.typeIndicator;
    }

    public int hashCode() {
        return ((((((527 + this.key.hashCode()) * 31) + Arrays.hashCode(this.value)) * 31) + this.localeIndicator) * 31) + this.typeIndicator;
    }

    /* JADX WARN: Code duplicated, block: B:23:0x006a  */
    public String toString() {
        String formattedValueForEditableTracksMap;
        int i = this.typeIndicator;
        if (i != 0) {
            if (i == 1) {
                formattedValueForEditableTracksMap = Util.fromUtf8Bytes(this.value);
            } else if (i == 23) {
                formattedValueForEditableTracksMap = String.valueOf(Float.intBitsToFloat(Ints.fromByteArray(this.value)));
            } else if (i == 67) {
                formattedValueForEditableTracksMap = String.valueOf(Ints.fromByteArray(this.value));
            } else if (i == 75) {
                formattedValueForEditableTracksMap = String.valueOf(UByte$$ExternalSyntheticBackport0.m(this.value[0]));
            } else if (i == 78) {
                formattedValueForEditableTracksMap = String.valueOf(new ParsableByteArray(this.value).readUnsignedLongToLong());
            } else {
                formattedValueForEditableTracksMap = Util.toHexString(this.value);
            }
        } else if (this.key.equals(KEY_EDITABLE_TRACKS_MAP)) {
            formattedValueForEditableTracksMap = getFormattedValueForEditableTracksMap(getEditableTrackTypesFromMap());
        } else {
            formattedValueForEditableTracksMap = Util.toHexString(this.value);
        }
        return "mdta: key=" + this.key + ", value=" + formattedValueForEditableTracksMap;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.key);
        parcel.writeByteArray(this.value);
        parcel.writeInt(this.localeIndicator);
        parcel.writeInt(this.typeIndicator);
    }

    private static void validateData(String str, byte[] bArr, int i) {
        byte b;
        str.hashCode();
        boolean z = false;
        switch (str) {
            case "com.android.capture.fps":
                if (i == 23 && bArr.length == 4) {
                    z = true;
                }
                Assertions.checkArgument(z);
                break;
            case "editable.tracks.samples.location":
                if (i == 75 && bArr.length == 1 && ((b = bArr[0]) == 0 || b == 1)) {
                    z = true;
                }
                Assertions.checkArgument(z);
                break;
            case "editable.tracks.length":
            case "editable.tracks.offset":
                if (i == 78 && bArr.length == 8) {
                    z = true;
                }
                Assertions.checkArgument(z);
                break;
            case "editable.tracks.map":
                Assertions.checkArgument(i == 0);
                break;
        }
    }

    private static String getFormattedValueForEditableTracksMap(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("track types = ");
        Joiner.on(',').appendTo(sb, (Iterable<? extends Object>) list);
        return sb.toString();
    }
}
