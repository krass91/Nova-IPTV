package androidx.media3.extractor.mp4;

import android.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.media3.common.C;
import androidx.media3.common.ColorInfo;
import androidx.media3.common.DrmInitData;
import androidx.media3.common.Format;
import androidx.media3.common.Metadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.ParserException;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.CodecSpecificDataUtil;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.ParsableBitArray;
import androidx.media3.common.util.ParsableByteArray;
import androidx.media3.common.util.Util;
import androidx.media3.container.MdtaMetadataEntry;
import androidx.media3.container.Mp4Box;
import androidx.media3.container.Mp4LocationData;
import androidx.media3.container.Mp4TimestampData;
import androidx.media3.container.NalUnitUtil;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.extractor.AacUtil;
import androidx.media3.extractor.Ac3Util;
import androidx.media3.extractor.Ac4Util;
import androidx.media3.extractor.AvcConfig;
import androidx.media3.extractor.DolbyVisionConfig;
import androidx.media3.extractor.ExtractorUtil;
import androidx.media3.extractor.GaplessInfoHolder;
import androidx.media3.extractor.HevcConfig;
import androidx.media3.extractor.OpusUtil;
import androidx.media3.extractor.VorbisUtil;
import androidx.media3.extractor.ts.PsExtractor;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class BoxParser {
    private static final int MAX_GAPLESS_TRIM_SIZE_SAMPLES = 4;
    private static final String TAG = "BoxParsers";
    private static final int TYPE_clcp = 1668047728;
    private static final int TYPE_mdta = 1835299937;
    private static final int TYPE_meta = 1835365473;
    private static final int TYPE_nclc = 1852009571;
    private static final int TYPE_nclx = 1852009592;
    private static final int TYPE_sbtl = 1935832172;
    private static final int TYPE_soun = 1936684398;
    private static final int TYPE_subt = 1937072756;
    private static final int TYPE_text = 1952807028;
    private static final int TYPE_vide = 1986618469;
    private static final byte[] opusMagic = Util.getUtf8Bytes("OpusHead");

    private interface SampleSizeBox {
        int getFixedSampleSize();

        int getSampleCount();

        int readNextSampleSize();
    }

    private static int getTrackTypeForHdlr(int i) {
        if (i == TYPE_soun) {
            return 1;
        }
        if (i == TYPE_vide) {
            return 2;
        }
        if (i == TYPE_text || i == TYPE_sbtl || i == TYPE_subt || i == TYPE_clcp) {
            return 3;
        }
        return i == 1835365473 ? 5 : -1;
    }

    public static int parseFullBoxFlags(int i) {
        return i & ViewCompat.MEASURED_SIZE_MASK;
    }

    public static int parseFullBoxVersion(int i) {
        return (i >> 24) & 255;
    }

    public static List<TrackSampleTable> parseTraks(Mp4Box.ContainerBox containerBox, GaplessInfoHolder gaplessInfoHolder, long j, DrmInitData drmInitData, boolean z, boolean z2, Function<Track, Track> function) throws ParserException {
        Track trackApply;
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < containerBox.containerChildren.size(); i++) {
            Mp4Box.ContainerBox containerBox2 = containerBox.containerChildren.get(i);
            if (containerBox2.type == 1953653099 && (trackApply = function.apply(parseTrak(containerBox2, (Mp4Box.LeafBox) Assertions.checkNotNull(containerBox.getLeafBoxOfType(Mp4Box.TYPE_mvhd)), j, drmInitData, z, z2))) != null) {
                arrayList.add(parseStbl(trackApply, (Mp4Box.ContainerBox) Assertions.checkNotNull(((Mp4Box.ContainerBox) Assertions.checkNotNull(((Mp4Box.ContainerBox) Assertions.checkNotNull(containerBox2.getContainerBoxOfType(Mp4Box.TYPE_mdia))).getContainerBoxOfType(Mp4Box.TYPE_minf))).getContainerBoxOfType(Mp4Box.TYPE_stbl)), gaplessInfoHolder));
            }
        }
        return arrayList;
    }

    public static Metadata parseUdta(Mp4Box.LeafBox leafBox) {
        ParsableByteArray parsableByteArray = leafBox.data;
        parsableByteArray.setPosition(8);
        Metadata metadata = new Metadata(new Metadata.Entry[0]);
        while (parsableByteArray.bytesLeft() >= 8) {
            int position = parsableByteArray.getPosition();
            int i = parsableByteArray.readInt();
            int i2 = parsableByteArray.readInt();
            if (i2 == 1835365473) {
                parsableByteArray.setPosition(position);
                metadata = metadata.copyWithAppendedEntriesFrom(parseUdtaMeta(parsableByteArray, position + i));
            } else if (i2 == 1936553057) {
                parsableByteArray.setPosition(position);
                metadata = metadata.copyWithAppendedEntriesFrom(SmtaAtomUtil.parseSmta(parsableByteArray, position + i));
            } else if (i2 == -1451722374) {
                metadata = metadata.copyWithAppendedEntriesFrom(parseXyz(parsableByteArray));
            }
            parsableByteArray.setPosition(position + i);
        }
        return metadata;
    }

    public static Mp4TimestampData parseMvhd(ParsableByteArray parsableByteArray) {
        long unsignedInt;
        long unsignedInt2;
        parsableByteArray.setPosition(8);
        if (parseFullBoxVersion(parsableByteArray.readInt()) == 0) {
            unsignedInt = parsableByteArray.readUnsignedInt();
            unsignedInt2 = parsableByteArray.readUnsignedInt();
        } else {
            unsignedInt = parsableByteArray.readLong();
            unsignedInt2 = parsableByteArray.readLong();
        }
        return new Mp4TimestampData(unsignedInt, unsignedInt2, parsableByteArray.readUnsignedInt());
    }

    public static Metadata parseMdtaFromMeta(Mp4Box.ContainerBox containerBox) {
        Mp4Box.LeafBox leafBoxOfType = containerBox.getLeafBoxOfType(Mp4Box.TYPE_hdlr);
        Mp4Box.LeafBox leafBoxOfType2 = containerBox.getLeafBoxOfType(Mp4Box.TYPE_keys);
        Mp4Box.LeafBox leafBoxOfType3 = containerBox.getLeafBoxOfType(Mp4Box.TYPE_ilst);
        if (leafBoxOfType == null || leafBoxOfType2 == null || leafBoxOfType3 == null || parseHdlr(leafBoxOfType.data) != TYPE_mdta) {
            return null;
        }
        ParsableByteArray parsableByteArray = leafBoxOfType2.data;
        parsableByteArray.setPosition(12);
        int i = parsableByteArray.readInt();
        String[] strArr = new String[i];
        for (int i2 = 0; i2 < i; i2++) {
            int i3 = parsableByteArray.readInt();
            parsableByteArray.skipBytes(4);
            strArr[i2] = parsableByteArray.readString(i3 - 8);
        }
        ParsableByteArray parsableByteArray2 = leafBoxOfType3.data;
        parsableByteArray2.setPosition(8);
        ArrayList arrayList = new ArrayList();
        while (parsableByteArray2.bytesLeft() > 8) {
            int position = parsableByteArray2.getPosition();
            int i4 = parsableByteArray2.readInt();
            int i5 = parsableByteArray2.readInt() - 1;
            if (i5 >= 0 && i5 < i) {
                MdtaMetadataEntry mdtaMetadataEntryFromIlst = MetadataUtil.parseMdtaMetadataEntryFromIlst(parsableByteArray2, position + i4, strArr[i5]);
                if (mdtaMetadataEntryFromIlst != null) {
                    arrayList.add(mdtaMetadataEntryFromIlst);
                }
            } else {
                Log.w(TAG, "Skipped metadata with unknown key index: " + i5);
            }
            parsableByteArray2.setPosition(position + i4);
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        return new Metadata(arrayList);
    }

    public static void maybeSkipRemainingMetaBoxHeaderBytes(ParsableByteArray parsableByteArray) {
        int position = parsableByteArray.getPosition();
        parsableByteArray.skipBytes(4);
        if (parsableByteArray.readInt() != 1751411826) {
            position += 4;
        }
        parsableByteArray.setPosition(position);
    }

    public static Track parseTrak(Mp4Box.ContainerBox containerBox, Mp4Box.LeafBox leafBox, long j, DrmInitData drmInitData, boolean z, boolean z2) throws ParserException {
        long[] jArr;
        long[] jArr2;
        Mp4Box.ContainerBox containerBoxOfType;
        Pair<long[], long[]> edts;
        Mp4Box.ContainerBox containerBox2 = (Mp4Box.ContainerBox) Assertions.checkNotNull(containerBox.getContainerBoxOfType(Mp4Box.TYPE_mdia));
        int trackTypeForHdlr = getTrackTypeForHdlr(parseHdlr(((Mp4Box.LeafBox) Assertions.checkNotNull(containerBox2.getLeafBoxOfType(Mp4Box.TYPE_hdlr))).data));
        if (trackTypeForHdlr == -1) {
            return null;
        }
        TkhdData tkhd = parseTkhd(((Mp4Box.LeafBox) Assertions.checkNotNull(containerBox.getLeafBoxOfType(Mp4Box.TYPE_tkhd))).data);
        long jScaleLargeTimestamp = C.TIME_UNSET;
        long j2 = j == C.TIME_UNSET ? tkhd.duration : j;
        long j3 = parseMvhd(leafBox.data).timescale;
        if (j2 != C.TIME_UNSET) {
            jScaleLargeTimestamp = Util.scaleLargeTimestamp(j2, 1000000L, j3);
        }
        long j4 = jScaleLargeTimestamp;
        Mp4Box.ContainerBox containerBox3 = (Mp4Box.ContainerBox) Assertions.checkNotNull(((Mp4Box.ContainerBox) Assertions.checkNotNull(containerBox2.getContainerBoxOfType(Mp4Box.TYPE_minf))).getContainerBoxOfType(Mp4Box.TYPE_stbl));
        MdhdData mdhd = parseMdhd(((Mp4Box.LeafBox) Assertions.checkNotNull(containerBox2.getLeafBoxOfType(Mp4Box.TYPE_mdhd))).data);
        Mp4Box.LeafBox leafBoxOfType = containerBox3.getLeafBoxOfType(Mp4Box.TYPE_stsd);
        if (leafBoxOfType == null) {
            throw ParserException.createForMalformedContainer("Malformed sample table (stbl) missing sample description (stsd)", null);
        }
        StsdData stsd = parseStsd(leafBoxOfType.data, tkhd.id, tkhd.rotationDegrees, mdhd.language, drmInitData, z2);
        if (z || (containerBoxOfType = containerBox.getContainerBoxOfType(Mp4Box.TYPE_edts)) == null || (edts = parseEdts(containerBoxOfType)) == null) {
            jArr = null;
            jArr2 = null;
        } else {
            long[] jArr3 = (long[]) edts.first;
            jArr2 = (long[]) edts.second;
            jArr = jArr3;
        }
        if (stsd.format == null) {
            return null;
        }
        return new Track(tkhd.id, trackTypeForHdlr, mdhd.timescale, j3, j4, mdhd.mediaDurationUs, stsd.format, stsd.requiredSampleTransformation, stsd.trackEncryptionBoxes, stsd.nalUnitLengthFieldLength, jArr, jArr2);
    }

    public static TrackSampleTable parseStbl(Track track, Mp4Box.ContainerBox containerBox, GaplessInfoHolder gaplessInfoHolder) throws ParserException {
        SampleSizeBox stz2SampleSizeBox;
        boolean z;
        int unsignedIntToInt;
        int unsignedIntToInt2;
        int unsignedIntToInt3;
        int i;
        int i2;
        boolean z2;
        int i3;
        int[] iArr;
        long j;
        long[] jArr;
        long[] jArr2;
        int i4;
        int[] iArr2;
        int i5;
        int i6;
        int[] iArr3;
        int[] iArr4;
        int i7;
        int[] iArr5;
        int i8;
        Track trackCopyWithFormat = track;
        Mp4Box.LeafBox leafBoxOfType = containerBox.getLeafBoxOfType(Mp4Box.TYPE_stsz);
        if (leafBoxOfType != null) {
            stz2SampleSizeBox = new StszSampleSizeBox(leafBoxOfType, trackCopyWithFormat.format);
        } else {
            Mp4Box.LeafBox leafBoxOfType2 = containerBox.getLeafBoxOfType(Mp4Box.TYPE_stz2);
            if (leafBoxOfType2 == null) {
                throw ParserException.createForMalformedContainer("Track has no sample table size information", null);
            }
            stz2SampleSizeBox = new Stz2SampleSizeBox(leafBoxOfType2);
        }
        int sampleCount = stz2SampleSizeBox.getSampleCount();
        if (sampleCount == 0) {
            return new TrackSampleTable(track, new long[0], new int[0], 0, new long[0], new int[0], 0L);
        }
        if (trackCopyWithFormat.type == 2 && trackCopyWithFormat.mediaDurationUs > 0) {
            trackCopyWithFormat = trackCopyWithFormat.copyWithFormat(trackCopyWithFormat.format.buildUpon().setFrameRate(sampleCount / (trackCopyWithFormat.mediaDurationUs / 1000000.0f)).build());
        }
        Track trackCopyWithFormat2 = trackCopyWithFormat;
        Mp4Box.LeafBox leafBoxOfType3 = containerBox.getLeafBoxOfType(Mp4Box.TYPE_stco);
        if (leafBoxOfType3 == null) {
            leafBoxOfType3 = (Mp4Box.LeafBox) Assertions.checkNotNull(containerBox.getLeafBoxOfType(Mp4Box.TYPE_co64));
            z = true;
        } else {
            z = false;
        }
        ParsableByteArray parsableByteArray = leafBoxOfType3.data;
        ParsableByteArray parsableByteArray2 = ((Mp4Box.LeafBox) Assertions.checkNotNull(containerBox.getLeafBoxOfType(Mp4Box.TYPE_stsc))).data;
        ParsableByteArray parsableByteArray3 = ((Mp4Box.LeafBox) Assertions.checkNotNull(containerBox.getLeafBoxOfType(Mp4Box.TYPE_stts))).data;
        Mp4Box.LeafBox leafBoxOfType4 = containerBox.getLeafBoxOfType(Mp4Box.TYPE_stss);
        ParsableByteArray parsableByteArray4 = leafBoxOfType4 != null ? leafBoxOfType4.data : null;
        Mp4Box.LeafBox leafBoxOfType5 = containerBox.getLeafBoxOfType(Mp4Box.TYPE_ctts);
        ParsableByteArray parsableByteArray5 = leafBoxOfType5 != null ? leafBoxOfType5.data : null;
        ChunkIterator chunkIterator = new ChunkIterator(parsableByteArray2, parsableByteArray, z);
        parsableByteArray3.setPosition(12);
        int unsignedIntToInt4 = parsableByteArray3.readUnsignedIntToInt() - 1;
        int unsignedIntToInt5 = parsableByteArray3.readUnsignedIntToInt();
        int unsignedIntToInt6 = parsableByteArray3.readUnsignedIntToInt();
        if (parsableByteArray5 != null) {
            parsableByteArray5.setPosition(12);
            unsignedIntToInt = parsableByteArray5.readUnsignedIntToInt();
        } else {
            unsignedIntToInt = 0;
        }
        if (parsableByteArray4 != null) {
            parsableByteArray4.setPosition(12);
            unsignedIntToInt3 = parsableByteArray4.readUnsignedIntToInt();
            if (unsignedIntToInt3 > 0) {
                unsignedIntToInt2 = parsableByteArray4.readUnsignedIntToInt() - 1;
            } else {
                unsignedIntToInt2 = -1;
                parsableByteArray4 = null;
            }
        } else {
            unsignedIntToInt2 = -1;
            unsignedIntToInt3 = 0;
        }
        int fixedSampleSize = stz2SampleSizeBox.getFixedSampleSize();
        String str = trackCopyWithFormat2.format.sampleMimeType;
        if (fixedSampleSize != -1 && (MimeTypes.AUDIO_RAW.equals(str) || MimeTypes.AUDIO_MLAW.equals(str) || MimeTypes.AUDIO_ALAW.equals(str)) && unsignedIntToInt4 == 0 && unsignedIntToInt == 0 && unsignedIntToInt3 == 0) {
            long[] jArr3 = new long[chunkIterator.length];
            int[] iArr6 = new int[chunkIterator.length];
            while (chunkIterator.moveNext()) {
                jArr3[chunkIterator.index] = chunkIterator.offset;
                iArr6[chunkIterator.index] = chunkIterator.numSamples;
            }
            FixedSampleSizeRechunker.Results resultsRechunk = FixedSampleSizeRechunker.rechunk(fixedSampleSize, jArr3, iArr6, unsignedIntToInt6);
            long[] jArr4 = resultsRechunk.offsets;
            int[] iArr7 = resultsRechunk.sizes;
            jArr = jArr4;
            iArr2 = iArr7;
            i4 = resultsRechunk.maximumSize;
            jArr2 = resultsRechunk.timestamps;
            iArr = resultsRechunk.flags;
            j = resultsRechunk.duration;
        } else {
            long[] jArrCopyOf = new long[sampleCount];
            int[] iArrCopyOf = new int[sampleCount];
            long[] jArrCopyOf2 = new long[sampleCount];
            int i9 = unsignedIntToInt3;
            int[] iArrCopyOf2 = new int[sampleCount];
            int i10 = unsignedIntToInt4;
            int i11 = unsignedIntToInt5;
            int unsignedIntToInt7 = unsignedIntToInt2;
            int i12 = 0;
            int i13 = 0;
            int i14 = 0;
            long j2 = 0;
            long j3 = 0;
            int i15 = 0;
            int unsignedIntToInt8 = 0;
            int i16 = i9;
            int i17 = unsignedIntToInt;
            int i18 = unsignedIntToInt6;
            while (true) {
                if (i12 >= sampleCount) {
                    i = i11;
                    i2 = i14;
                    break;
                }
                long j4 = j3;
                int i19 = i14;
                boolean zMoveNext = true;
                while (i19 == 0) {
                    zMoveNext = chunkIterator.moveNext();
                    if (!zMoveNext) {
                        break;
                    }
                    int i20 = i18;
                    long j5 = chunkIterator.offset;
                    i19 = chunkIterator.numSamples;
                    j4 = j5;
                    i18 = i20;
                    i11 = i11;
                    sampleCount = sampleCount;
                }
                int i21 = sampleCount;
                int i22 = i18;
                i = i11;
                if (!zMoveNext) {
                    Log.w(TAG, "Unexpected end of chunk data");
                    jArrCopyOf = Arrays.copyOf(jArrCopyOf, i12);
                    iArrCopyOf = Arrays.copyOf(iArrCopyOf, i12);
                    jArrCopyOf2 = Arrays.copyOf(jArrCopyOf2, i12);
                    iArrCopyOf2 = Arrays.copyOf(iArrCopyOf2, i12);
                    sampleCount = i12;
                    i2 = i19;
                    break;
                }
                int i23 = i17;
                if (parsableByteArray5 != null) {
                    while (unsignedIntToInt8 == 0 && i23 > 0) {
                        unsignedIntToInt8 = parsableByteArray5.readUnsignedIntToInt();
                        i15 = parsableByteArray5.readInt();
                        i23--;
                    }
                    unsignedIntToInt8--;
                }
                int i24 = i15;
                jArrCopyOf[i12] = j4;
                int nextSampleSize = stz2SampleSizeBox.readNextSampleSize();
                iArrCopyOf[i12] = nextSampleSize;
                ChunkIterator chunkIterator2 = chunkIterator;
                if (nextSampleSize > i13) {
                    i13 = nextSampleSize;
                }
                SampleSizeBox sampleSizeBox = stz2SampleSizeBox;
                jArrCopyOf2[i12] = j2 + ((long) i24);
                iArrCopyOf2[i12] = parsableByteArray4 == null ? 1 : 0;
                if (i12 == unsignedIntToInt7) {
                    iArrCopyOf2[i12] = 1;
                    i16--;
                    if (i16 > 0) {
                        unsignedIntToInt7 = ((ParsableByteArray) Assertions.checkNotNull(parsableByteArray4)).readUnsignedIntToInt() - 1;
                    }
                }
                int[] iArr8 = iArrCopyOf2;
                int i25 = unsignedIntToInt7;
                j2 += (long) i22;
                int unsignedIntToInt9 = i - 1;
                if (unsignedIntToInt9 != 0 || i10 <= 0) {
                    i5 = i22;
                } else {
                    unsignedIntToInt9 = parsableByteArray3.readUnsignedIntToInt();
                    i5 = parsableByteArray3.readInt();
                    i10--;
                }
                int i26 = unsignedIntToInt9;
                long j6 = j4 + ((long) iArrCopyOf[i12]);
                int i27 = i19 - 1;
                i12++;
                j3 = j6;
                i15 = i24;
                iArrCopyOf2 = iArr8;
                stz2SampleSizeBox = sampleSizeBox;
                i18 = i5;
                sampleCount = i21;
                unsignedIntToInt7 = i25;
                i17 = i23;
                i11 = i26;
                i14 = i27;
                chunkIterator = chunkIterator2;
            }
            long j7 = j2 + ((long) i15);
            if (parsableByteArray5 == null) {
                z2 = true;
                break;
            }
            int i28 = i17;
            while (true) {
                if (i28 <= 0) {
                    z2 = true;
                    break;
                }
                if (parsableByteArray5.readUnsignedIntToInt() != 0) {
                    z2 = false;
                    break;
                }
                parsableByteArray5.readInt();
                i28--;
            }
            if (i16 == 0 && i == 0 && i2 == 0 && i10 == 0) {
                i3 = unsignedIntToInt8;
                if (i3 == 0 && z2) {
                    trackCopyWithFormat2 = trackCopyWithFormat2;
                }
                iArr = iArrCopyOf2;
                j = j7;
                jArr = jArrCopyOf;
                jArr2 = jArrCopyOf2;
                i4 = i13;
                iArr2 = iArrCopyOf;
            } else {
                i3 = unsignedIntToInt8;
            }
            StringBuilder sb = new StringBuilder("Inconsistent stbl box for track ");
            trackCopyWithFormat2 = trackCopyWithFormat2;
            sb.append(trackCopyWithFormat2.id);
            sb.append(": remainingSynchronizationSamples ");
            sb.append(i16);
            sb.append(", remainingSamplesAtTimestampDelta ");
            sb.append(i);
            sb.append(", remainingSamplesInChunk ");
            sb.append(i2);
            sb.append(", remainingTimestampDeltaChanges ");
            sb.append(i10);
            sb.append(", remainingSamplesAtTimestampOffset ");
            sb.append(i3);
            sb.append(!z2 ? ", ctts invalid" : "");
            Log.w(TAG, sb.toString());
            iArr = iArrCopyOf2;
            j = j7;
            jArr = jArrCopyOf;
            jArr2 = jArrCopyOf2;
            i4 = i13;
            iArr2 = iArrCopyOf;
        }
        long jScaleLargeTimestamp = Util.scaleLargeTimestamp(j, 1000000L, trackCopyWithFormat2.timescale);
        if (trackCopyWithFormat2.editListDurations == null) {
            Util.scaleLargeTimestampsInPlace(jArr2, 1000000L, trackCopyWithFormat2.timescale);
            return new TrackSampleTable(trackCopyWithFormat2, jArr, iArr2, i4, jArr2, iArr, jScaleLargeTimestamp);
        }
        if (trackCopyWithFormat2.editListDurations.length == 1 && trackCopyWithFormat2.type == 1 && jArr2.length >= 2) {
            long j8 = ((long[]) Assertions.checkNotNull(trackCopyWithFormat2.editListMediaTimes))[0];
            long jScaleLargeTimestamp2 = j8 + Util.scaleLargeTimestamp(trackCopyWithFormat2.editListDurations[0], trackCopyWithFormat2.timescale, trackCopyWithFormat2.movieTimescale);
            iArr4 = iArr2;
            i7 = i4;
            int[] iArr9 = iArr;
            if (canApplyEditWithGaplessInfo(jArr2, j, j8, jScaleLargeTimestamp2)) {
                long jScaleLargeTimestamp3 = Util.scaleLargeTimestamp(j8 - jArr2[0], trackCopyWithFormat2.format.sampleRate, trackCopyWithFormat2.timescale);
                i6 = sampleCount;
                long jScaleLargeTimestamp4 = Util.scaleLargeTimestamp(j - jScaleLargeTimestamp2, trackCopyWithFormat2.format.sampleRate, trackCopyWithFormat2.timescale);
                if ((jScaleLargeTimestamp3 != 0 || jScaleLargeTimestamp4 != 0) && jScaleLargeTimestamp3 <= 2147483647L && jScaleLargeTimestamp4 <= 2147483647L) {
                    gaplessInfoHolder.encoderDelay = (int) jScaleLargeTimestamp3;
                    gaplessInfoHolder.encoderPadding = (int) jScaleLargeTimestamp4;
                    Util.scaleLargeTimestampsInPlace(jArr2, 1000000L, trackCopyWithFormat2.timescale);
                    return new TrackSampleTable(trackCopyWithFormat2, jArr, iArr4, i7, jArr2, iArr9, Util.scaleLargeTimestamp(trackCopyWithFormat2.editListDurations[0], 1000000L, trackCopyWithFormat2.movieTimescale));
                }
            } else {
                i6 = sampleCount;
            }
            iArr3 = iArr9;
        } else {
            i6 = sampleCount;
            iArr3 = iArr;
            iArr4 = iArr2;
            i7 = i4;
        }
        if (trackCopyWithFormat2.editListDurations.length == 1 && trackCopyWithFormat2.editListDurations[0] == 0) {
            long j9 = ((long[]) Assertions.checkNotNull(trackCopyWithFormat2.editListMediaTimes))[0];
            for (int i29 = 0; i29 < jArr2.length; i29++) {
                jArr2[i29] = Util.scaleLargeTimestamp(jArr2[i29] - j9, 1000000L, trackCopyWithFormat2.timescale);
            }
            return new TrackSampleTable(trackCopyWithFormat2, jArr, iArr4, i7, jArr2, iArr3, Util.scaleLargeTimestamp(j - j9, 1000000L, trackCopyWithFormat2.timescale));
        }
        boolean z3 = trackCopyWithFormat2.type == 1;
        int[] iArr10 = new int[trackCopyWithFormat2.editListDurations.length];
        int[] iArr11 = new int[trackCopyWithFormat2.editListDurations.length];
        long[] jArr5 = (long[]) Assertions.checkNotNull(trackCopyWithFormat2.editListMediaTimes);
        int i30 = 0;
        boolean z4 = false;
        int i31 = 0;
        int i32 = 0;
        while (i30 < trackCopyWithFormat2.editListDurations.length) {
            long j10 = jArr5[i30];
            if (j10 != -1) {
                int i33 = i31;
                int i34 = i32;
                long jScaleLargeTimestamp5 = Util.scaleLargeTimestamp(trackCopyWithFormat2.editListDurations[i30], trackCopyWithFormat2.timescale, trackCopyWithFormat2.movieTimescale);
                iArr10[i30] = Util.binarySearchFloor(jArr2, j10, true, true);
                while (true) {
                    int i35 = iArr10[i30];
                    if (i35 < 0 || (iArr3[i35] & 1) != 0) {
                        break;
                    }
                    iArr10[i30] = i35 - 1;
                }
                long j11 = j10 + jScaleLargeTimestamp5;
                iArr11[i30] = Util.binarySearchCeil(jArr2, j11, z3, false);
                if (trackCopyWithFormat2.type == 2) {
                    while (true) {
                        int i36 = iArr11[i30];
                        if (i36 >= jArr2.length - 1 || jArr2[i36 + 1] > j11) {
                            break;
                        }
                        iArr11[i30] = i36 + 1;
                    }
                }
                i32 = iArr11[i30];
                int i37 = iArr10[i30];
                i8 = i33 + (i32 - i37);
                z4 |= i34 != i37;
            } else {
                i8 = i31;
            }
            i30++;
            i31 = i8;
        }
        int i38 = i31;
        int i39 = 0;
        boolean z5 = z4 | (i38 != i6);
        long[] jArr6 = z5 ? new long[i38] : jArr;
        int[] iArr12 = z5 ? new int[i38] : iArr4;
        int i40 = z5 ? 0 : i7;
        int[] iArr13 = z5 ? new int[i38] : iArr3;
        long[] jArr7 = new long[i38];
        boolean z6 = false;
        int i41 = 0;
        int i42 = i40;
        long j12 = 0;
        while (i39 < trackCopyWithFormat2.editListDurations.length) {
            long j13 = trackCopyWithFormat2.editListMediaTimes[i39];
            int i43 = iArr10[i39];
            int[] iArr14 = iArr10;
            int i44 = iArr11[i39];
            int[] iArr15 = iArr11;
            if (z5) {
                int i45 = i44 - i43;
                System.arraycopy(jArr, i43, jArr6, i41, i45);
                iArr5 = iArr4;
                System.arraycopy(iArr5, i43, iArr12, i41, i45);
                System.arraycopy(iArr3, i43, iArr13, i41, i45);
            } else {
                iArr5 = iArr4;
            }
            boolean z7 = z6;
            int i46 = i42;
            while (i43 < i44) {
                int[] iArr16 = iArr3;
                int i47 = i39;
                long jScaleLargeTimestamp6 = Util.scaleLargeTimestamp(j12, 1000000L, trackCopyWithFormat2.movieTimescale);
                long j14 = j12;
                long jScaleLargeTimestamp7 = Util.scaleLargeTimestamp(jArr2[i43] - j13, 1000000L, trackCopyWithFormat2.timescale);
                boolean z8 = jScaleLargeTimestamp7 < 0 ? true : z7;
                jArr7[i41] = jScaleLargeTimestamp6 + jScaleLargeTimestamp7;
                if (z5 && iArr12[i41] > i46) {
                    i46 = iArr5[i43];
                }
                i41++;
                i43++;
                z7 = z8;
                j12 = j14;
                iArr3 = iArr16;
                i39 = i47;
            }
            int i48 = i39;
            j12 += trackCopyWithFormat2.editListDurations[i48];
            i39 = i48 + 1;
            z6 = z7;
            i42 = i46;
            iArr4 = iArr5;
            iArr10 = iArr14;
            iArr11 = iArr15;
            jArr = jArr;
            iArr3 = iArr3;
        }
        long jScaleLargeTimestamp8 = Util.scaleLargeTimestamp(j12, 1000000L, trackCopyWithFormat2.movieTimescale);
        if (z6) {
            trackCopyWithFormat2 = trackCopyWithFormat2.copyWithFormat(trackCopyWithFormat2.format.buildUpon().setHasPrerollSamples(true).build());
        }
        return new TrackSampleTable(trackCopyWithFormat2, jArr6, iArr12, i42, jArr7, iArr13, jScaleLargeTimestamp8);
    }

    private static Metadata parseUdtaMeta(ParsableByteArray parsableByteArray, int i) {
        parsableByteArray.skipBytes(8);
        maybeSkipRemainingMetaBoxHeaderBytes(parsableByteArray);
        while (parsableByteArray.getPosition() < i) {
            int position = parsableByteArray.getPosition();
            int i2 = parsableByteArray.readInt();
            if (parsableByteArray.readInt() == 1768715124) {
                parsableByteArray.setPosition(position);
                return parseIlst(parsableByteArray, position + i2);
            }
            parsableByteArray.setPosition(position + i2);
        }
        return null;
    }

    private static Metadata parseIlst(ParsableByteArray parsableByteArray, int i) {
        parsableByteArray.skipBytes(8);
        ArrayList arrayList = new ArrayList();
        while (parsableByteArray.getPosition() < i) {
            Metadata.Entry ilstElement = MetadataUtil.parseIlstElement(parsableByteArray);
            if (ilstElement != null) {
                arrayList.add(ilstElement);
            }
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        return new Metadata(arrayList);
    }

    private static Metadata parseXyz(ParsableByteArray parsableByteArray) {
        short s = parsableByteArray.readShort();
        parsableByteArray.skipBytes(2);
        String string = parsableByteArray.readString(s);
        int iMax = Math.max(string.lastIndexOf(43), string.lastIndexOf(45));
        try {
            return new Metadata(new Mp4LocationData(Float.parseFloat(string.substring(0, iMax)), Float.parseFloat(string.substring(iMax, string.length() - 1))));
        } catch (IndexOutOfBoundsException | NumberFormatException unused) {
            return null;
        }
    }

    private static TkhdData parseTkhd(ParsableByteArray parsableByteArray) {
        long j;
        parsableByteArray.setPosition(8);
        int fullBoxVersion = parseFullBoxVersion(parsableByteArray.readInt());
        parsableByteArray.skipBytes(fullBoxVersion == 0 ? 8 : 16);
        int i = parsableByteArray.readInt();
        parsableByteArray.skipBytes(4);
        int position = parsableByteArray.getPosition();
        int i2 = fullBoxVersion == 0 ? 4 : 8;
        int i3 = 0;
        int i4 = 0;
        while (true) {
            j = C.TIME_UNSET;
            if (i4 < i2) {
                if (parsableByteArray.getData()[position + i4] != -1) {
                    long unsignedInt = fullBoxVersion == 0 ? parsableByteArray.readUnsignedInt() : parsableByteArray.readUnsignedLongToLong();
                    if (unsignedInt == 0) {
                        break;
                    }
                    j = unsignedInt;
                    break;
                }
                i4++;
            } else {
                parsableByteArray.skipBytes(i2);
                break;
            }
        }
        parsableByteArray.skipBytes(16);
        int i5 = parsableByteArray.readInt();
        int i6 = parsableByteArray.readInt();
        parsableByteArray.skipBytes(4);
        int i7 = parsableByteArray.readInt();
        int i8 = parsableByteArray.readInt();
        if (i5 == 0 && i6 == 65536 && i7 == -65536 && i8 == 0) {
            i3 = 90;
        } else if (i5 == 0 && i6 == -65536 && i7 == 65536 && i8 == 0) {
            i3 = 270;
        } else if (i5 == -65536 && i6 == 0 && i7 == 0 && i8 == -65536) {
            i3 = 180;
        }
        return new TkhdData(i, j, i3);
    }

    private static int parseHdlr(ParsableByteArray parsableByteArray) {
        parsableByteArray.setPosition(16);
        return parsableByteArray.readInt();
    }

    private static MdhdData parseMdhd(ParsableByteArray parsableByteArray) {
        long jScaleLargeTimestamp;
        parsableByteArray.setPosition(8);
        int fullBoxVersion = parseFullBoxVersion(parsableByteArray.readInt());
        parsableByteArray.skipBytes(fullBoxVersion == 0 ? 8 : 16);
        long unsignedInt = parsableByteArray.readUnsignedInt();
        int position = parsableByteArray.getPosition();
        int i = fullBoxVersion == 0 ? 4 : 8;
        int i2 = 0;
        while (true) {
            if (i2 < i) {
                if (parsableByteArray.getData()[position + i2] != -1) {
                    long unsignedInt2 = fullBoxVersion == 0 ? parsableByteArray.readUnsignedInt() : parsableByteArray.readUnsignedLongToLong();
                    if (unsignedInt2 == 0) {
                        break;
                    }
                    jScaleLargeTimestamp = Util.scaleLargeTimestamp(unsignedInt2, 1000000L, unsignedInt);
                    int unsignedShort = parsableByteArray.readUnsignedShort();
                    return new MdhdData(unsignedInt, jScaleLargeTimestamp, "" + ((char) (((unsignedShort >> 10) & 31) + 96)) + ((char) (((unsignedShort >> 5) & 31) + 96)) + ((char) ((unsignedShort & 31) + 96)));
                }
                i2++;
            } else {
                parsableByteArray.skipBytes(i);
                break;
            }
        }
        jScaleLargeTimestamp = -9223372036854775807L;
        int unsignedShort2 = parsableByteArray.readUnsignedShort();
        return new MdhdData(unsignedInt, jScaleLargeTimestamp, "" + ((char) (((unsignedShort2 >> 10) & 31) + 96)) + ((char) (((unsignedShort2 >> 5) & 31) + 96)) + ((char) ((unsignedShort2 & 31) + 96)));
    }

    private static StsdData parseStsd(ParsableByteArray parsableByteArray, int i, int i2, String str, DrmInitData drmInitData, boolean z) throws ParserException {
        int i3;
        parsableByteArray.setPosition(12);
        int i4 = parsableByteArray.readInt();
        StsdData stsdData = new StsdData(i4);
        for (int i5 = 0; i5 < i4; i5++) {
            int position = parsableByteArray.getPosition();
            int i6 = parsableByteArray.readInt();
            ExtractorUtil.checkContainerInput(i6 > 0, "childAtomSize must be positive");
            int i7 = parsableByteArray.readInt();
            if (i7 == 1635148593 || i7 == 1635148595 || i7 == 1701733238 || i7 == 1831958048 || i7 == 1836070006 || i7 == 1752589105 || i7 == 1751479857 || i7 == 1932670515 || i7 == 1211250227 || i7 == 1748121139 || i7 == 1987063864 || i7 == 1987063865 || i7 == 1635135537 || i7 == 1685479798 || i7 == 1685479729 || i7 == 1685481573 || i7 == 1685481521) {
                i3 = position;
                parseVideoSampleEntry(parsableByteArray, i7, i3, i6, i, i2, drmInitData, stsdData, i5);
            } else if (i7 == 1836069985 || i7 == 1701733217 || i7 == 1633889587 || i7 == 1700998451 || i7 == 1633889588 || i7 == 1835823201 || i7 == 1685353315 || i7 == 1685353317 || i7 == 1685353320 || i7 == 1685353324 || i7 == 1685353336 || i7 == 1935764850 || i7 == 1935767394 || i7 == 1819304813 || i7 == 1936684916 || i7 == 1953984371 || i7 == 778924082 || i7 == 778924083 || i7 == 1835557169 || i7 == 1835560241 || i7 == 1634492771 || i7 == 1634492791 || i7 == 1970037111 || i7 == 1332770163 || i7 == 1716281667 || i7 == 1767992678) {
                i3 = position;
                parseAudioSampleEntry(parsableByteArray, i7, position, i6, i, str, z, drmInitData, stsdData, i5);
            } else {
                if (i7 == 1414810956 || i7 == 1954034535 || i7 == 2004251764 || i7 == 1937010800 || i7 == 1664495672) {
                    parseTextSampleEntry(parsableByteArray, i7, position, i6, i, str, stsdData);
                } else if (i7 == 1835365492) {
                    parseMetaDataSampleEntry(parsableByteArray, i7, position, i, stsdData);
                } else if (i7 == 1667329389) {
                    stsdData.format = new Format.Builder().setId(i).setSampleMimeType(MimeTypes.APPLICATION_CAMERA_MOTION).build();
                }
                i3 = position;
            }
            parsableByteArray.setPosition(i3 + i6);
        }
        return stsdData;
    }

    private static void parseTextSampleEntry(ParsableByteArray parsableByteArray, int i, int i2, int i3, int i4, String str, StsdData stsdData) {
        parsableByteArray.setPosition(i2 + 16);
        String str2 = MimeTypes.APPLICATION_TTML;
        ImmutableList immutableListOf = null;
        long j = Long.MAX_VALUE;
        if (i != 1414810956) {
            if (i == 1954034535) {
                int i5 = i3 - 16;
                byte[] bArr = new byte[i5];
                parsableByteArray.readBytes(bArr, 0, i5);
                immutableListOf = ImmutableList.of(bArr);
                str2 = MimeTypes.APPLICATION_TX3G;
            } else if (i == 2004251764) {
                str2 = MimeTypes.APPLICATION_MP4VTT;
            } else if (i == 1937010800) {
                j = 0;
            } else if (i == 1664495672) {
                stsdData.requiredSampleTransformation = 1;
                str2 = MimeTypes.APPLICATION_MP4CEA608;
            } else {
                throw new IllegalStateException();
            }
        }
        stsdData.format = new Format.Builder().setId(i4).setSampleMimeType(str2).setLanguage(str).setSubsampleOffsetUs(j).setInitializationData(immutableListOf).build();
    }

    private static void parseVideoSampleEntry(ParsableByteArray parsableByteArray, int i, int i2, int i3, int i4, int i5, DrmInitData drmInitData, StsdData stsdData, int i6) throws ParserException {
        String str;
        DrmInitData drmInitData2;
        int i7;
        int i8;
        float f;
        int i9;
        int i10;
        int i11;
        int i12;
        int i13 = i2;
        int i14 = i3;
        DrmInitData drmInitDataCopyWithSchemeType = drmInitData;
        StsdData stsdData2 = stsdData;
        parsableByteArray.setPosition(i13 + 16);
        parsableByteArray.skipBytes(16);
        int unsignedShort = parsableByteArray.readUnsignedShort();
        int unsignedShort2 = parsableByteArray.readUnsignedShort();
        parsableByteArray.skipBytes(50);
        int position = parsableByteArray.getPosition();
        int iIntValue = i;
        if (iIntValue == 1701733238) {
            Pair<Integer, TrackEncryptionBox> sampleEntryEncryptionData = parseSampleEntryEncryptionData(parsableByteArray, i13, i14);
            if (sampleEntryEncryptionData != null) {
                iIntValue = ((Integer) sampleEntryEncryptionData.first).intValue();
                drmInitDataCopyWithSchemeType = drmInitDataCopyWithSchemeType == null ? null : drmInitDataCopyWithSchemeType.copyWithSchemeType(((TrackEncryptionBox) sampleEntryEncryptionData.second).schemeType);
                stsdData2.trackEncryptionBoxes[i6] = (TrackEncryptionBox) sampleEntryEncryptionData.second;
            }
            parsableByteArray.setPosition(position);
        }
        String str2 = MimeTypes.VIDEO_H263;
        if (iIntValue != 1831958048) {
            str = iIntValue == 1211250227 ? MimeTypes.VIDEO_H263 : null;
        } else {
            str = MimeTypes.VIDEO_MPEG;
        }
        float paspFromParent = 1.0f;
        int i15 = 8;
        int i16 = 8;
        List<byte[]> listOf = null;
        String str3 = null;
        byte[] projFromParent = null;
        int i17 = -1;
        int i18 = -1;
        int i19 = -1;
        int i20 = -1;
        int iIsoTransferCharacteristicsToColorTransfer = -1;
        ByteBuffer byteBufferAllocateHdrStaticInfo = null;
        EsdsData esdsFromParent = null;
        NalUnitUtil.H265VpsData h265VpsData = null;
        boolean z = false;
        while (position - i13 < i14) {
            parsableByteArray.setPosition(position);
            int position2 = parsableByteArray.getPosition();
            int i21 = parsableByteArray.readInt();
            if (i21 == 0 && parsableByteArray.getPosition() - i13 == i14) {
                break;
            }
            ExtractorUtil.checkContainerInput(i21 > 0, "childAtomSize must be positive");
            int i22 = parsableByteArray.readInt();
            if (i22 == 1635148611) {
                ExtractorUtil.checkContainerInput(str == null, null);
                parsableByteArray.setPosition(position2 + 8);
                AvcConfig avcConfig = AvcConfig.parse(parsableByteArray);
                List<byte[]> list = avcConfig.initializationData;
                stsdData2.nalUnitLengthFieldLength = avcConfig.nalUnitLengthFieldLength;
                if (!z) {
                    paspFromParent = avcConfig.pixelWidthHeightRatio;
                }
                String str4 = avcConfig.codecs;
                int i23 = avcConfig.maxNumReorderFrames;
                int i24 = avcConfig.colorSpace;
                int i25 = avcConfig.colorRange;
                int i26 = avcConfig.colorTransfer;
                int i27 = avcConfig.bitdepthLuma;
                int i28 = avcConfig.bitdepthChroma;
                drmInitData2 = drmInitDataCopyWithSchemeType;
                i18 = i23;
                i20 = i25;
                iIsoTransferCharacteristicsToColorTransfer = i26;
                i15 = i27;
                listOf = list;
                str = MimeTypes.VIDEO_H264;
                str3 = str4;
                i7 = i24;
                i16 = i28;
            } else {
                if (i22 == 1752589123) {
                    ExtractorUtil.checkContainerInput(str == null, null);
                    parsableByteArray.setPosition(position2 + 8);
                    HevcConfig hevcConfig = HevcConfig.parse(parsableByteArray);
                    List<byte[]> list2 = hevcConfig.initializationData;
                    stsdData2.nalUnitLengthFieldLength = hevcConfig.nalUnitLengthFieldLength;
                    if (!z) {
                        paspFromParent = hevcConfig.pixelWidthHeightRatio;
                    }
                    int i29 = hevcConfig.maxNumReorderPics;
                    String str5 = hevcConfig.codecs;
                    if (hevcConfig.stereoMode != -1) {
                        i17 = hevcConfig.stereoMode;
                    }
                    int i30 = hevcConfig.colorSpace;
                    int i31 = hevcConfig.colorRange;
                    listOf = list2;
                    int i32 = hevcConfig.colorTransfer;
                    int i33 = hevcConfig.bitdepthLuma;
                    int i34 = hevcConfig.bitdepthChroma;
                    h265VpsData = hevcConfig.vpsData;
                    drmInitData2 = drmInitDataCopyWithSchemeType;
                    i20 = i31;
                    iIsoTransferCharacteristicsToColorTransfer = i32;
                    i15 = i33;
                    i18 = i29;
                    str = MimeTypes.VIDEO_H265;
                    str3 = str5;
                    i7 = i30;
                    i16 = i34;
                } else {
                    drmInitData2 = drmInitDataCopyWithSchemeType;
                    if (i22 == 1818785347) {
                        ExtractorUtil.checkContainerInput(MimeTypes.VIDEO_H265.equals(str), "lhvC must follow hvcC atom");
                        NalUnitUtil.H265VpsData h265VpsData2 = h265VpsData;
                        ExtractorUtil.checkContainerInput(h265VpsData2 != null && h265VpsData2.layerInfos.size() >= 2, "must have at least two layers");
                        parsableByteArray.setPosition(position2 + 8);
                        HevcConfig layered = HevcConfig.parseLayered(parsableByteArray, (NalUnitUtil.H265VpsData) Assertions.checkNotNull(h265VpsData2));
                        ExtractorUtil.checkContainerInput(stsdData2.nalUnitLengthFieldLength == layered.nalUnitLengthFieldLength, "nalUnitLengthFieldLength must be same for both hvcC and lhvC atoms");
                        if (layered.colorSpace != -1) {
                            i7 = i19;
                            ExtractorUtil.checkContainerInput(i7 == layered.colorSpace, "colorSpace must be the same for both views");
                        } else {
                            i7 = i19;
                        }
                        if (layered.colorRange != -1) {
                            i11 = i20;
                            ExtractorUtil.checkContainerInput(i11 == layered.colorRange, "colorRange must be the same for both views");
                        } else {
                            i11 = i20;
                        }
                        if (layered.colorTransfer != -1) {
                            int i35 = iIsoTransferCharacteristicsToColorTransfer;
                            i12 = i35;
                            ExtractorUtil.checkContainerInput(i35 == layered.colorTransfer, "colorTransfer must be the same for both views");
                        } else {
                            i12 = iIsoTransferCharacteristicsToColorTransfer;
                        }
                        ExtractorUtil.checkContainerInput(i15 == layered.bitdepthLuma, "bitdepthLuma must be the same for both views");
                        ExtractorUtil.checkContainerInput(i16 == layered.bitdepthChroma, "bitdepthChroma must be the same for both views");
                        List<byte[]> list3 = listOf;
                        if (list3 != null) {
                            listOf = ImmutableList.builder().addAll((Iterable) list3).addAll((Iterable) layered.initializationData).build();
                        } else {
                            listOf = list3;
                            ExtractorUtil.checkContainerInput(false, "initializationData must be already set from hvcC atom");
                        }
                        String str6 = layered.codecs;
                        h265VpsData = h265VpsData2;
                        str = MimeTypes.VIDEO_MV_HEVC;
                        i20 = i11;
                        iIsoTransferCharacteristicsToColorTransfer = i12;
                        str3 = str6;
                    } else {
                        List<byte[]> listBuildVp9CodecPrivateInitializationData = listOf;
                        i7 = i19;
                        int i36 = i20;
                        int i37 = iIsoTransferCharacteristicsToColorTransfer;
                        NalUnitUtil.H265VpsData h265VpsData3 = h265VpsData;
                        if (i22 == 1986361461) {
                            VexuData videoExtendedUsageBox = parseVideoExtendedUsageBox(parsableByteArray, position2, i21);
                            if (videoExtendedUsageBox == null || videoExtendedUsageBox.eyesData == null) {
                                i10 = i17;
                                i17 = i10;
                            } else if (h265VpsData3 == null || h265VpsData3.layerInfos.size() < 2) {
                                i10 = i17;
                                if (i10 == -1) {
                                    i17 = videoExtendedUsageBox.eyesData.striData.eyeViewsReversed ? 5 : 4;
                                } else {
                                    i17 = i10;
                                }
                            } else {
                                ExtractorUtil.checkContainerInput(videoExtendedUsageBox.hasBothEyeViews(), "both eye views must be marked as available");
                                ExtractorUtil.checkContainerInput(!videoExtendedUsageBox.eyesData.striData.eyeViewsReversed, "for MV-HEVC, eye_views_reversed must be set to false");
                                i10 = i17;
                                i17 = i10;
                            }
                            h265VpsData = h265VpsData3;
                            listOf = listBuildVp9CodecPrivateInitializationData;
                            i20 = i36;
                            iIsoTransferCharacteristicsToColorTransfer = i37;
                        } else {
                            int i38 = i17;
                            if (i22 == 1685480259 || i22 == 1685485123) {
                                i8 = i38;
                                unsignedShort2 = unsignedShort2;
                                iIntValue = iIntValue;
                                i16 = i16;
                                f = paspFromParent;
                                i15 = i15;
                                i9 = i37;
                                DolbyVisionConfig dolbyVisionConfig = DolbyVisionConfig.parse(parsableByteArray);
                                if (dolbyVisionConfig != null) {
                                    String str7 = dolbyVisionConfig.codecs;
                                    str = MimeTypes.VIDEO_DOLBY_VISION;
                                    str3 = str7;
                                }
                            } else if (i22 == 1987076931) {
                                ExtractorUtil.checkContainerInput(str == null, null);
                                String str8 = iIntValue == 1987063864 ? MimeTypes.VIDEO_VP8 : MimeTypes.VIDEO_VP9;
                                parsableByteArray.setPosition(position2 + 12);
                                byte unsignedByte = (byte) parsableByteArray.readUnsignedByte();
                                byte unsignedByte2 = (byte) parsableByteArray.readUnsignedByte();
                                int unsignedByte3 = parsableByteArray.readUnsignedByte();
                                i16 = unsignedByte3 >> 4;
                                byte b = (byte) ((unsignedByte3 >> 1) & 7);
                                if (str8.equals(MimeTypes.VIDEO_VP9)) {
                                    listBuildVp9CodecPrivateInitializationData = CodecSpecificDataUtil.buildVp9CodecPrivateInitializationData(unsignedByte, unsignedByte2, (byte) i16, b);
                                }
                                boolean z2 = (unsignedByte3 & 1) != 0;
                                int unsignedByte4 = parsableByteArray.readUnsignedByte();
                                int unsignedByte5 = parsableByteArray.readUnsignedByte();
                                int iIsoColorPrimariesToColorSpace = ColorInfo.isoColorPrimariesToColorSpace(unsignedByte4);
                                i20 = z2 ? 1 : 2;
                                iIsoTransferCharacteristicsToColorTransfer = ColorInfo.isoTransferCharacteristicsToColorTransfer(unsignedByte5);
                                str = str8;
                                unsignedShort2 = unsignedShort2;
                                i15 = i16;
                                h265VpsData = h265VpsData3;
                                i7 = iIsoColorPrimariesToColorSpace;
                                listOf = listBuildVp9CodecPrivateInitializationData;
                                i17 = i38;
                                iIntValue = iIntValue;
                            } else if (i22 == 1635135811) {
                                int i39 = i21 - 8;
                                byte[] bArr = new byte[i39];
                                parsableByteArray.readBytes(bArr, 0, i39);
                                listOf = ImmutableList.of(bArr);
                                parsableByteArray.setPosition(position2 + 8);
                                ColorInfo av1c = parseAv1c(parsableByteArray);
                                int i40 = av1c.lumaBitdepth;
                                int i41 = av1c.chromaBitdepth;
                                int i42 = av1c.colorSpace;
                                int i43 = av1c.colorRange;
                                iIsoTransferCharacteristicsToColorTransfer = av1c.colorTransfer;
                                i20 = i43;
                                str = MimeTypes.VIDEO_AV1;
                                h265VpsData = h265VpsData3;
                                i15 = i40;
                                i17 = i38;
                                i16 = i41;
                                i7 = i42;
                            } else if (i22 == 1668050025) {
                                if (byteBufferAllocateHdrStaticInfo == null) {
                                    byteBufferAllocateHdrStaticInfo = allocateHdrStaticInfo();
                                }
                                ByteBuffer byteBuffer = byteBufferAllocateHdrStaticInfo;
                                byteBuffer.position(21);
                                byteBuffer.putShort(parsableByteArray.readShort());
                                byteBuffer.putShort(parsableByteArray.readShort());
                                byteBufferAllocateHdrStaticInfo = byteBuffer;
                                unsignedShort2 = unsignedShort2;
                                iIntValue = iIntValue;
                                h265VpsData = h265VpsData3;
                                listOf = listBuildVp9CodecPrivateInitializationData;
                                i20 = i36;
                                iIsoTransferCharacteristicsToColorTransfer = i37;
                                i17 = i38;
                            } else {
                                if (i22 == 1835295606) {
                                    if (byteBufferAllocateHdrStaticInfo == null) {
                                        byteBufferAllocateHdrStaticInfo = allocateHdrStaticInfo();
                                    }
                                    ByteBuffer byteBuffer2 = byteBufferAllocateHdrStaticInfo;
                                    short s = parsableByteArray.readShort();
                                    short s2 = parsableByteArray.readShort();
                                    iIntValue = iIntValue;
                                    short s3 = parsableByteArray.readShort();
                                    short s4 = parsableByteArray.readShort();
                                    int i44 = i16;
                                    short s5 = parsableByteArray.readShort();
                                    int i45 = i15;
                                    short s6 = parsableByteArray.readShort();
                                    i8 = i38;
                                    short s7 = parsableByteArray.readShort();
                                    float f2 = paspFromParent;
                                    short s8 = parsableByteArray.readShort();
                                    long unsignedInt = parsableByteArray.readUnsignedInt();
                                    long unsignedInt2 = parsableByteArray.readUnsignedInt();
                                    unsignedShort2 = unsignedShort2;
                                    byteBuffer2.position(1);
                                    byteBuffer2.putShort(s5);
                                    byteBuffer2.putShort(s6);
                                    byteBuffer2.putShort(s);
                                    byteBuffer2.putShort(s2);
                                    byteBuffer2.putShort(s3);
                                    byteBuffer2.putShort(s4);
                                    byteBuffer2.putShort(s7);
                                    byteBuffer2.putShort(s8);
                                    byteBuffer2.putShort((short) (unsignedInt / Renderer.DEFAULT_DURATION_TO_PROGRESS_US));
                                    byteBuffer2.putShort((short) (unsignedInt2 / Renderer.DEFAULT_DURATION_TO_PROGRESS_US));
                                    byteBufferAllocateHdrStaticInfo = byteBuffer2;
                                    i16 = i44;
                                    i15 = i45;
                                    listOf = listBuildVp9CodecPrivateInitializationData;
                                    i20 = i36;
                                    iIsoTransferCharacteristicsToColorTransfer = i37;
                                    paspFromParent = f2;
                                } else {
                                    i8 = i38;
                                    unsignedShort2 = unsignedShort2;
                                    iIntValue = iIntValue;
                                    i16 = i16;
                                    f = paspFromParent;
                                    i15 = i15;
                                    if (i22 == 1681012275) {
                                        ExtractorUtil.checkContainerInput(str == null, null);
                                        str = str2;
                                    } else if (i22 == 1702061171) {
                                        ExtractorUtil.checkContainerInput(str == null, null);
                                        esdsFromParent = parseEsdsFromParent(parsableByteArray, position2);
                                        String str9 = esdsFromParent.mimeType;
                                        byte[] bArr2 = esdsFromParent.initializationData;
                                        listOf = bArr2 != null ? ImmutableList.of(bArr2) : listBuildVp9CodecPrivateInitializationData;
                                        str = str9;
                                        i20 = i36;
                                        iIsoTransferCharacteristicsToColorTransfer = i37;
                                        paspFromParent = f;
                                    } else if (i22 == 1885434736) {
                                        paspFromParent = parsePaspFromParent(parsableByteArray, position2);
                                        i16 = i16;
                                        i15 = i15;
                                        listOf = listBuildVp9CodecPrivateInitializationData;
                                        i20 = i36;
                                        iIsoTransferCharacteristicsToColorTransfer = i37;
                                        z = true;
                                    } else if (i22 == 1937126244) {
                                        projFromParent = parseProjFromParent(parsableByteArray, position2, i21);
                                    } else if (i22 == 1936995172) {
                                        int unsignedByte6 = parsableByteArray.readUnsignedByte();
                                        parsableByteArray.skipBytes(3);
                                        if (unsignedByte6 == 0) {
                                            int unsignedByte7 = parsableByteArray.readUnsignedByte();
                                            if (unsignedByte7 == 0) {
                                                i8 = 0;
                                            } else if (unsignedByte7 == 1) {
                                                i8 = 1;
                                            } else if (unsignedByte7 == 2) {
                                                i8 = 2;
                                            } else if (unsignedByte7 == 3) {
                                                i8 = 3;
                                            }
                                        }
                                    } else if (i22 == 1668246642) {
                                        i9 = i37;
                                        if (i7 == -1 && i9 == -1) {
                                            int i46 = parsableByteArray.readInt();
                                            if (i46 == TYPE_nclx || i46 == TYPE_nclc) {
                                                int unsignedShort3 = parsableByteArray.readUnsignedShort();
                                                int unsignedShort4 = parsableByteArray.readUnsignedShort();
                                                parsableByteArray.skipBytes(2);
                                                boolean z3 = i21 == 19 && (parsableByteArray.readUnsignedByte() & 128) != 0;
                                                int iIsoColorPrimariesToColorSpace2 = ColorInfo.isoColorPrimariesToColorSpace(unsignedShort3);
                                                int i47 = z3 ? 1 : 2;
                                                i7 = iIsoColorPrimariesToColorSpace2;
                                                i16 = i16;
                                                i15 = i15;
                                                listOf = listBuildVp9CodecPrivateInitializationData;
                                                paspFromParent = f;
                                                iIsoTransferCharacteristicsToColorTransfer = ColorInfo.isoTransferCharacteristicsToColorTransfer(unsignedShort4);
                                                i20 = i47;
                                            } else {
                                                Log.w(TAG, "Unsupported color type: " + Mp4Box.getBoxTypeString(i46));
                                            }
                                        }
                                    } else {
                                        i9 = i37;
                                    }
                                    listOf = listBuildVp9CodecPrivateInitializationData;
                                    i20 = i36;
                                    iIsoTransferCharacteristicsToColorTransfer = i37;
                                    paspFromParent = f;
                                }
                                int i48 = i8;
                                h265VpsData = h265VpsData3;
                                i17 = i48;
                            }
                            i16 = i16;
                            i15 = i15;
                            listOf = listBuildVp9CodecPrivateInitializationData;
                            i20 = i36;
                            paspFromParent = f;
                            iIsoTransferCharacteristicsToColorTransfer = i9;
                            int i49 = i8;
                            h265VpsData = h265VpsData3;
                            i17 = i49;
                        }
                    }
                    position += i21;
                    i13 = i2;
                    i14 = i3;
                    stsdData2 = stsdData;
                    iIntValue = iIntValue;
                    drmInitDataCopyWithSchemeType = drmInitData2;
                    unsignedShort2 = unsignedShort2;
                    i19 = i7;
                    str2 = str2;
                }
                position += i21;
                i13 = i2;
                i14 = i3;
                stsdData2 = stsdData;
                iIntValue = iIntValue;
                drmInitDataCopyWithSchemeType = drmInitData2;
                unsignedShort2 = unsignedShort2;
                i19 = i7;
                str2 = str2;
            }
            position += i21;
            i13 = i2;
            i14 = i3;
            stsdData2 = stsdData;
            iIntValue = iIntValue;
            drmInitDataCopyWithSchemeType = drmInitData2;
            unsignedShort2 = unsignedShort2;
            i19 = i7;
            str2 = str2;
        }
        DrmInitData drmInitData3 = drmInitDataCopyWithSchemeType;
        int i50 = unsignedShort2;
        float f3 = paspFromParent;
        List<byte[]> list4 = listOf;
        int i51 = i17;
        int i52 = i19;
        int i53 = i20;
        int i54 = iIsoTransferCharacteristicsToColorTransfer;
        int i55 = i16;
        int i56 = i15;
        if (str == null) {
            return;
        }
        Format.Builder colorInfo = new Format.Builder().setId(i4).setSampleMimeType(str).setCodecs(str3).setWidth(unsignedShort).setHeight(i50).setPixelWidthHeightRatio(f3).setRotationDegrees(i5).setProjectionData(projFromParent).setStereoMode(i51).setInitializationData(list4).setMaxNumReorderSamples(i18).setDrmInitData(drmInitData3).setColorInfo(new ColorInfo.Builder().setColorSpace(i52).setColorRange(i53).setColorTransfer(i54).setHdrStaticInfo(byteBufferAllocateHdrStaticInfo != null ? byteBufferAllocateHdrStaticInfo.array() : null).setLumaBitdepth(i56).setChromaBitdepth(i55).build());
        if (esdsFromParent != null) {
            colorInfo.setAverageBitrate(Ints.saturatedCast(esdsFromParent.bitrate)).setPeakBitrate(Ints.saturatedCast(esdsFromParent.peakBitrate));
        }
        stsdData.format = colorInfo.build();
    }

    private static ColorInfo parseAv1c(ParsableByteArray parsableByteArray) {
        ColorInfo.Builder builder = new ColorInfo.Builder();
        ParsableBitArray parsableBitArray = new ParsableBitArray(parsableByteArray.getData());
        parsableBitArray.setPosition(parsableByteArray.getPosition() * 8);
        parsableBitArray.skipBytes(1);
        int bits = parsableBitArray.readBits(3);
        parsableBitArray.skipBits(6);
        boolean bit = parsableBitArray.readBit();
        boolean bit2 = parsableBitArray.readBit();
        if (bits == 2 && bit) {
            builder.setLumaBitdepth(bit2 ? 12 : 10);
            builder.setChromaBitdepth(bit2 ? 12 : 10);
        } else if (bits <= 2) {
            builder.setLumaBitdepth(bit ? 10 : 8);
            builder.setChromaBitdepth(bit ? 10 : 8);
        }
        parsableBitArray.skipBits(13);
        parsableBitArray.skipBit();
        int bits2 = parsableBitArray.readBits(4);
        if (bits2 != 1) {
            Log.i(TAG, "Unsupported obu_type: " + bits2);
            return builder.build();
        }
        if (parsableBitArray.readBit()) {
            Log.i(TAG, "Unsupported obu_extension_flag");
            return builder.build();
        }
        boolean bit3 = parsableBitArray.readBit();
        parsableBitArray.skipBit();
        if (bit3 && parsableBitArray.readBits(8) > 127) {
            Log.i(TAG, "Excessive obu_size");
            return builder.build();
        }
        int bits3 = parsableBitArray.readBits(3);
        parsableBitArray.skipBit();
        if (parsableBitArray.readBit()) {
            Log.i(TAG, "Unsupported reduced_still_picture_header");
            return builder.build();
        }
        if (parsableBitArray.readBit()) {
            Log.i(TAG, "Unsupported timing_info_present_flag");
            return builder.build();
        }
        if (parsableBitArray.readBit()) {
            Log.i(TAG, "Unsupported initial_display_delay_present_flag");
            return builder.build();
        }
        int bits4 = parsableBitArray.readBits(5);
        boolean z = false;
        for (int i = 0; i <= bits4; i++) {
            parsableBitArray.skipBits(12);
            if (parsableBitArray.readBits(5) > 7) {
                parsableBitArray.skipBit();
            }
        }
        int bits5 = parsableBitArray.readBits(4);
        int bits6 = parsableBitArray.readBits(4);
        parsableBitArray.skipBits(bits5 + 1);
        parsableBitArray.skipBits(bits6 + 1);
        if (parsableBitArray.readBit()) {
            parsableBitArray.skipBits(7);
        }
        parsableBitArray.skipBits(7);
        boolean bit4 = parsableBitArray.readBit();
        if (bit4) {
            parsableBitArray.skipBits(2);
        }
        if ((parsableBitArray.readBit() ? 2 : parsableBitArray.readBits(1)) > 0 && !parsableBitArray.readBit()) {
            parsableBitArray.skipBits(1);
        }
        if (bit4) {
            parsableBitArray.skipBits(3);
        }
        parsableBitArray.skipBits(3);
        boolean bit5 = parsableBitArray.readBit();
        if (bits3 == 2 && bit5) {
            parsableBitArray.skipBit();
        }
        if (bits3 != 1 && parsableBitArray.readBit()) {
            z = true;
        }
        if (parsableBitArray.readBit()) {
            int bits7 = parsableBitArray.readBits(8);
            int bits8 = parsableBitArray.readBits(8);
            builder.setColorSpace(ColorInfo.isoColorPrimariesToColorSpace(bits7)).setColorRange(((z || bits7 != 1 || bits8 != 13 || parsableBitArray.readBits(8) != 0) ? parsableBitArray.readBits(1) : 1) != 1 ? 2 : 1).setColorTransfer(ColorInfo.isoTransferCharacteristicsToColorTransfer(bits8));
        }
        return builder.build();
    }

    private static ByteBuffer allocateHdrStaticInfo() {
        return ByteBuffer.allocate(25).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static void parseMetaDataSampleEntry(ParsableByteArray parsableByteArray, int i, int i2, int i3, StsdData stsdData) {
        parsableByteArray.setPosition(i2 + 16);
        if (i == 1835365492) {
            parsableByteArray.readNullTerminatedString();
            String nullTerminatedString = parsableByteArray.readNullTerminatedString();
            if (nullTerminatedString != null) {
                stsdData.format = new Format.Builder().setId(i3).setSampleMimeType(nullTerminatedString).build();
            }
        }
    }

    private static Pair<long[], long[]> parseEdts(Mp4Box.ContainerBox containerBox) {
        Mp4Box.LeafBox leafBoxOfType = containerBox.getLeafBoxOfType(Mp4Box.TYPE_elst);
        if (leafBoxOfType == null) {
            return null;
        }
        ParsableByteArray parsableByteArray = leafBoxOfType.data;
        parsableByteArray.setPosition(8);
        int fullBoxVersion = parseFullBoxVersion(parsableByteArray.readInt());
        int unsignedIntToInt = parsableByteArray.readUnsignedIntToInt();
        long[] jArr = new long[unsignedIntToInt];
        long[] jArr2 = new long[unsignedIntToInt];
        for (int i = 0; i < unsignedIntToInt; i++) {
            jArr[i] = fullBoxVersion == 1 ? parsableByteArray.readUnsignedLongToLong() : parsableByteArray.readUnsignedInt();
            jArr2[i] = fullBoxVersion == 1 ? parsableByteArray.readLong() : parsableByteArray.readInt();
            if (parsableByteArray.readShort() != 1) {
                throw new IllegalArgumentException("Unsupported media rate.");
            }
            parsableByteArray.skipBytes(2);
        }
        return Pair.create(jArr, jArr2);
    }

    private static float parsePaspFromParent(ParsableByteArray parsableByteArray, int i) {
        parsableByteArray.setPosition(i + 8);
        return parsableByteArray.readUnsignedIntToInt() / parsableByteArray.readUnsignedIntToInt();
    }

    /* JADX WARN: Code duplicated, block: B:39:0x008d  */
    /* JADX WARN: Code duplicated, block: B:90:0x014a  */
    private static void parseAudioSampleEntry(ParsableByteArray parsableByteArray, int i, int i2, int i3, int i4, String str, boolean z, DrmInitData drmInitData, StsdData stsdData, int i5) throws ParserException {
        int unsignedShort;
        int unsignedShort2;
        int unsignedFixedPoint1616;
        int i6;
        int i7;
        String str2;
        int i8;
        int iIntValue = i;
        int i9 = i2;
        int i10 = i3;
        DrmInitData drmInitDataCopyWithSchemeType = drmInitData;
        parsableByteArray.setPosition(i9 + 16);
        if (z) {
            unsignedShort = parsableByteArray.readUnsignedShort();
            parsableByteArray.skipBytes(6);
        } else {
            parsableByteArray.skipBytes(8);
            unsignedShort = 0;
        }
        if (unsignedShort == 0 || unsignedShort == 1) {
            unsignedShort2 = parsableByteArray.readUnsignedShort();
            parsableByteArray.skipBytes(6);
            unsignedFixedPoint1616 = parsableByteArray.readUnsignedFixedPoint1616();
            parsableByteArray.setPosition(parsableByteArray.getPosition() - 4);
            i6 = parsableByteArray.readInt();
            if (unsignedShort == 1) {
                parsableByteArray.skipBytes(16);
            }
            i7 = -1;
        } else {
            if (unsignedShort != 2) {
                return;
            }
            parsableByteArray.skipBytes(16);
            unsignedFixedPoint1616 = (int) Math.round(parsableByteArray.readDouble());
            unsignedShort2 = parsableByteArray.readUnsignedIntToInt();
            parsableByteArray.skipBytes(4);
            int unsignedIntToInt = parsableByteArray.readUnsignedIntToInt();
            int unsignedIntToInt2 = parsableByteArray.readUnsignedIntToInt();
            boolean z2 = (unsignedIntToInt2 & 1) != 0;
            boolean z3 = (unsignedIntToInt2 & 2) != 0;
            if (z2) {
                if (unsignedIntToInt == 32) {
                    i7 = 4;
                } else {
                    i7 = -1;
                }
            } else if (unsignedIntToInt == 8) {
                i7 = 3;
            } else if (unsignedIntToInt == 16) {
                i7 = z3 ? 268435456 : 2;
            } else if (unsignedIntToInt == 24) {
                i7 = z3 ? C.ENCODING_PCM_24BIT_BIG_ENDIAN : 21;
            } else if (unsignedIntToInt == 32) {
                i7 = z3 ? C.ENCODING_PCM_32BIT_BIG_ENDIAN : 22;
            } else {
                i7 = -1;
            }
            parsableByteArray.skipBytes(8);
            i6 = 0;
        }
        if (iIntValue == 1767992678) {
            unsignedFixedPoint1616 = -1;
            unsignedShort2 = -1;
        }
        int position = parsableByteArray.getPosition();
        if (iIntValue == 1701733217) {
            Pair<Integer, TrackEncryptionBox> sampleEntryEncryptionData = parseSampleEntryEncryptionData(parsableByteArray, i9, i10);
            if (sampleEntryEncryptionData != null) {
                iIntValue = ((Integer) sampleEntryEncryptionData.first).intValue();
                drmInitDataCopyWithSchemeType = drmInitDataCopyWithSchemeType == null ? null : drmInitDataCopyWithSchemeType.copyWithSchemeType(((TrackEncryptionBox) sampleEntryEncryptionData.second).schemeType);
                stsdData.trackEncryptionBoxes[i5] = (TrackEncryptionBox) sampleEntryEncryptionData.second;
            }
            parsableByteArray.setPosition(position);
        }
        String str3 = MimeTypes.AUDIO_MPEGH_MHM1;
        if (iIntValue == 1633889587) {
            str2 = MimeTypes.AUDIO_AC3;
        } else if (iIntValue == 1700998451) {
            str2 = MimeTypes.AUDIO_E_AC3;
        } else if (iIntValue == 1633889588) {
            str2 = MimeTypes.AUDIO_AC4;
        } else if (iIntValue == 1685353315) {
            str2 = MimeTypes.AUDIO_DTS;
        } else if (iIntValue == 1685353320 || iIntValue == 1685353324) {
            str2 = MimeTypes.AUDIO_DTS_HD;
        } else if (iIntValue == 1685353317) {
            str2 = MimeTypes.AUDIO_DTS_EXPRESS;
        } else if (iIntValue == 1685353336) {
            str2 = MimeTypes.AUDIO_DTS_X;
        } else if (iIntValue == 1935764850) {
            str2 = MimeTypes.AUDIO_AMR_NB;
        } else if (iIntValue == 1935767394) {
            str2 = MimeTypes.AUDIO_AMR_WB;
        } else if (iIntValue == 1936684916) {
            str2 = MimeTypes.AUDIO_RAW;
            i7 = 2;
        } else if (iIntValue == 1953984371) {
            str2 = MimeTypes.AUDIO_RAW;
            i7 = 268435456;
        } else if (iIntValue == 1819304813) {
            if (i7 == -1) {
                str2 = MimeTypes.AUDIO_RAW;
                i7 = 2;
            } else {
                str2 = MimeTypes.AUDIO_RAW;
            }
        } else if (iIntValue == 778924082 || iIntValue == 778924083) {
            str2 = MimeTypes.AUDIO_MPEG;
        } else if (iIntValue == 1835557169) {
            str2 = MimeTypes.AUDIO_MPEGH_MHA1;
        } else if (iIntValue == 1835560241) {
            str2 = MimeTypes.AUDIO_MPEGH_MHM1;
        } else if (iIntValue == 1634492771) {
            str2 = MimeTypes.AUDIO_ALAC;
        } else if (iIntValue == 1634492791) {
            str2 = MimeTypes.AUDIO_ALAW;
        } else if (iIntValue == 1970037111) {
            str2 = MimeTypes.AUDIO_MLAW;
        } else if (iIntValue == 1332770163) {
            str2 = MimeTypes.AUDIO_OPUS;
        } else if (iIntValue == 1716281667) {
            str2 = MimeTypes.AUDIO_FLAC;
        } else if (iIntValue == 1835823201) {
            str2 = MimeTypes.AUDIO_TRUEHD;
        } else {
            str2 = iIntValue == 1767992678 ? MimeTypes.AUDIO_IAMF : null;
        }
        int i11 = i7;
        String str4 = null;
        List<byte[]> listOf = null;
        EsdsData esdsFromParent = null;
        while (position - i9 < i10) {
            parsableByteArray.setPosition(position);
            int i12 = parsableByteArray.readInt();
            ExtractorUtil.checkContainerInput(i12 > 0, "childAtomSize must be positive");
            int i13 = parsableByteArray.readInt();
            if (i13 == 1835557187) {
                parsableByteArray.setPosition(position + 8);
                parsableByteArray.skipBytes(1);
                int unsignedByte = parsableByteArray.readUnsignedByte();
                parsableByteArray.skipBytes(1);
                if (Objects.equals(str2, str3)) {
                    i8 = 0;
                    str4 = String.format("mhm1.%02X", Integer.valueOf(unsignedByte));
                } else {
                    i8 = 0;
                    str4 = String.format("mha1.%02X", Integer.valueOf(unsignedByte));
                }
                int unsignedShort3 = parsableByteArray.readUnsignedShort();
                byte[] bArr = new byte[unsignedShort3];
                parsableByteArray.readBytes(bArr, i8, unsignedShort3);
                if (listOf == null) {
                    listOf = ImmutableList.of(bArr);
                } else {
                    listOf = ImmutableList.of(bArr, listOf.get(i8));
                }
            } else {
                str3 = str3;
                if (i13 == 1835557200) {
                    parsableByteArray.setPosition(position + 8);
                    int unsignedByte2 = parsableByteArray.readUnsignedByte();
                    if (unsignedByte2 > 0) {
                        byte[] bArr2 = new byte[unsignedByte2];
                        parsableByteArray.readBytes(bArr2, 0, unsignedByte2);
                        if (listOf == null) {
                            listOf = ImmutableList.of(bArr2);
                        } else {
                            listOf = ImmutableList.of(listOf.get(0), bArr2);
                        }
                    }
                } else {
                    if (i13 == 1702061171 || (z && i13 == 2002876005)) {
                        int iFindBoxPosition = i13 == 1702061171 ? position : findBoxPosition(parsableByteArray, Mp4Box.TYPE_esds, position, i12);
                        if (iFindBoxPosition != -1) {
                            esdsFromParent = parseEsdsFromParent(parsableByteArray, iFindBoxPosition);
                            str2 = esdsFromParent.mimeType;
                            byte[] bArr3 = esdsFromParent.initializationData;
                            if (bArr3 != null) {
                                if (MimeTypes.AUDIO_VORBIS.equals(str2)) {
                                    listOf = VorbisUtil.parseVorbisCsdFromEsdsInitializationData(bArr3);
                                } else {
                                    if (MimeTypes.AUDIO_AAC.equals(str2)) {
                                        AacUtil.Config audioSpecificConfig = AacUtil.parseAudioSpecificConfig(bArr3);
                                        int i14 = audioSpecificConfig.sampleRateHz;
                                        int i15 = audioSpecificConfig.channelCount;
                                        str4 = audioSpecificConfig.codecs;
                                        unsignedFixedPoint1616 = i14;
                                        unsignedShort2 = i15;
                                    }
                                    listOf = ImmutableList.of(bArr3);
                                }
                            }
                        }
                    } else if (i13 == 1684103987) {
                        parsableByteArray.setPosition(position + 8);
                        stsdData.format = Ac3Util.parseAc3AnnexFFormat(parsableByteArray, Integer.toString(i4), str, drmInitDataCopyWithSchemeType);
                    } else if (i13 == 1684366131) {
                        parsableByteArray.setPosition(position + 8);
                        stsdData.format = Ac3Util.parseEAc3AnnexFFormat(parsableByteArray, Integer.toString(i4), str, drmInitDataCopyWithSchemeType);
                    } else if (i13 == 1684103988) {
                        parsableByteArray.setPosition(position + 8);
                        stsdData.format = Ac4Util.parseAc4AnnexEFormat(parsableByteArray, Integer.toString(i4), str, drmInitDataCopyWithSchemeType);
                    } else if (i13 == 1684892784) {
                        if (i6 <= 0) {
                            throw ParserException.createForMalformedContainer("Invalid sample rate for Dolby TrueHD MLP stream: " + i6, null);
                        }
                        unsignedFixedPoint1616 = i6;
                        unsignedShort2 = 2;
                    } else if (i13 == 1684305011 || i13 == 1969517683) {
                        stsdData.format = new Format.Builder().setId(i4).setSampleMimeType(str2).setChannelCount(unsignedShort2).setSampleRate(unsignedFixedPoint1616).setDrmInitData(drmInitDataCopyWithSchemeType).setLanguage(str).build();
                    } else if (i13 == 1682927731) {
                        int i16 = i12 - 8;
                        byte[] bArr4 = opusMagic;
                        byte[] bArrCopyOf = Arrays.copyOf(bArr4, bArr4.length + i16);
                        parsableByteArray.setPosition(position + 8);
                        parsableByteArray.readBytes(bArrCopyOf, bArr4.length, i16);
                        listOf = OpusUtil.buildInitializationData(bArrCopyOf);
                    } else if (i13 == 1684425825) {
                        byte[] bArr5 = new byte[i12 - 8];
                        bArr5[0] = 102;
                        bArr5[1] = 76;
                        bArr5[2] = 97;
                        bArr5[3] = 67;
                        parsableByteArray.setPosition(position + 12);
                        parsableByteArray.readBytes(bArr5, 4, i12 - 12);
                        listOf = ImmutableList.of(bArr5);
                    } else if (i13 == 1634492771) {
                        int i17 = i12 - 12;
                        byte[] bArr6 = new byte[i17];
                        parsableByteArray.setPosition(position + 12);
                        parsableByteArray.readBytes(bArr6, 0, i17);
                        Pair<Integer, Integer> alacAudioSpecificConfig = CodecSpecificDataUtil.parseAlacAudioSpecificConfig(bArr6);
                        int iIntValue2 = ((Integer) alacAudioSpecificConfig.first).intValue();
                        int iIntValue3 = ((Integer) alacAudioSpecificConfig.second).intValue();
                        listOf = ImmutableList.of(bArr6);
                        unsignedFixedPoint1616 = iIntValue2;
                        unsignedShort2 = iIntValue3;
                    } else if (i13 == 1767990114) {
                        parsableByteArray.setPosition(position + 9);
                        int unsignedLeb128ToInt = parsableByteArray.readUnsignedLeb128ToInt();
                        byte[] bArr7 = new byte[unsignedLeb128ToInt];
                        parsableByteArray.readBytes(bArr7, 0, unsignedLeb128ToInt);
                        listOf = ImmutableList.of(bArr7);
                    }
                    position += i12;
                    i9 = i2;
                    i10 = i3;
                    str3 = str3;
                }
            }
            position += i12;
            i9 = i2;
            i10 = i3;
            str3 = str3;
        }
        if (stsdData.format != null || str2 == null) {
            return;
        }
        Format.Builder language = new Format.Builder().setId(i4).setSampleMimeType(str2).setCodecs(str4).setChannelCount(unsignedShort2).setSampleRate(unsignedFixedPoint1616).setPcmEncoding(i11).setInitializationData(listOf).setDrmInitData(drmInitDataCopyWithSchemeType).setLanguage(str);
        if (esdsFromParent != null) {
            language.setAverageBitrate(Ints.saturatedCast(esdsFromParent.bitrate)).setPeakBitrate(Ints.saturatedCast(esdsFromParent.peakBitrate));
        }
        stsdData.format = language.build();
    }

    private static int findBoxPosition(ParsableByteArray parsableByteArray, int i, int i2, int i3) throws ParserException {
        int position = parsableByteArray.getPosition();
        ExtractorUtil.checkContainerInput(position >= i2, null);
        while (position - i2 < i3) {
            parsableByteArray.setPosition(position);
            int i4 = parsableByteArray.readInt();
            ExtractorUtil.checkContainerInput(i4 > 0, "childAtomSize must be positive");
            if (parsableByteArray.readInt() == i) {
                return position;
            }
            position += i4;
        }
        return -1;
    }

    private static EsdsData parseEsdsFromParent(ParsableByteArray parsableByteArray, int i) {
        parsableByteArray.setPosition(i + 12);
        parsableByteArray.skipBytes(1);
        parseExpandableClassSize(parsableByteArray);
        parsableByteArray.skipBytes(2);
        int unsignedByte = parsableByteArray.readUnsignedByte();
        if ((unsignedByte & 128) != 0) {
            parsableByteArray.skipBytes(2);
        }
        if ((unsignedByte & 64) != 0) {
            parsableByteArray.skipBytes(parsableByteArray.readUnsignedByte());
        }
        if ((unsignedByte & 32) != 0) {
            parsableByteArray.skipBytes(2);
        }
        parsableByteArray.skipBytes(1);
        parseExpandableClassSize(parsableByteArray);
        String mimeTypeFromMp4ObjectType = MimeTypes.getMimeTypeFromMp4ObjectType(parsableByteArray.readUnsignedByte());
        if (MimeTypes.AUDIO_MPEG.equals(mimeTypeFromMp4ObjectType) || MimeTypes.AUDIO_DTS.equals(mimeTypeFromMp4ObjectType) || MimeTypes.AUDIO_DTS_HD.equals(mimeTypeFromMp4ObjectType)) {
            return new EsdsData(mimeTypeFromMp4ObjectType, null, -1L, -1L);
        }
        parsableByteArray.skipBytes(4);
        long unsignedInt = parsableByteArray.readUnsignedInt();
        long unsignedInt2 = parsableByteArray.readUnsignedInt();
        parsableByteArray.skipBytes(1);
        int expandableClassSize = parseExpandableClassSize(parsableByteArray);
        byte[] bArr = new byte[expandableClassSize];
        parsableByteArray.readBytes(bArr, 0, expandableClassSize);
        return new EsdsData(mimeTypeFromMp4ObjectType, bArr, unsignedInt2 > 0 ? unsignedInt2 : -1L, unsignedInt > 0 ? unsignedInt : -1L);
    }

    static VexuData parseVideoExtendedUsageBox(ParsableByteArray parsableByteArray, int i, int i2) throws ParserException {
        parsableByteArray.setPosition(i + 8);
        int position = parsableByteArray.getPosition();
        EyesData stereoViewBox = null;
        while (position - i < i2) {
            parsableByteArray.setPosition(position);
            int i3 = parsableByteArray.readInt();
            ExtractorUtil.checkContainerInput(i3 > 0, "childAtomSize must be positive");
            if (parsableByteArray.readInt() == 1702454643) {
                stereoViewBox = parseStereoViewBox(parsableByteArray, position, i3);
            }
            position += i3;
        }
        if (stereoViewBox == null) {
            return null;
        }
        return new VexuData(stereoViewBox);
    }

    private static EyesData parseStereoViewBox(ParsableByteArray parsableByteArray, int i, int i2) throws ParserException {
        parsableByteArray.setPosition(i + 8);
        int position = parsableByteArray.getPosition();
        while (position - i < i2) {
            parsableByteArray.setPosition(position);
            int i3 = parsableByteArray.readInt();
            ExtractorUtil.checkContainerInput(i3 > 0, "childAtomSize must be positive");
            if (parsableByteArray.readInt() == 1937011305) {
                parsableByteArray.skipBytes(4);
                int unsignedByte = parsableByteArray.readUnsignedByte();
                return new EyesData(new StriData((unsignedByte & 1) == 1, (unsignedByte & 2) == 2, (unsignedByte & 8) == 8, (unsignedByte & 4) == 4));
            }
            position += i3;
        }
        return null;
    }

    private static Pair<Integer, TrackEncryptionBox> parseSampleEntryEncryptionData(ParsableByteArray parsableByteArray, int i, int i2) throws ParserException {
        Pair<Integer, TrackEncryptionBox> commonEncryptionSinfFromParent;
        int position = parsableByteArray.getPosition();
        while (position - i < i2) {
            parsableByteArray.setPosition(position);
            int i3 = parsableByteArray.readInt();
            ExtractorUtil.checkContainerInput(i3 > 0, "childAtomSize must be positive");
            if (parsableByteArray.readInt() == 1936289382 && (commonEncryptionSinfFromParent = parseCommonEncryptionSinfFromParent(parsableByteArray, position, i3)) != null) {
                return commonEncryptionSinfFromParent;
            }
            position += i3;
        }
        return null;
    }

    static Pair<Integer, TrackEncryptionBox> parseCommonEncryptionSinfFromParent(ParsableByteArray parsableByteArray, int i, int i2) throws ParserException {
        int i3 = i + 8;
        int i4 = -1;
        int i5 = 0;
        String string = null;
        Integer numValueOf = null;
        while (i3 - i < i2) {
            parsableByteArray.setPosition(i3);
            int i6 = parsableByteArray.readInt();
            int i7 = parsableByteArray.readInt();
            if (i7 == 1718775137) {
                numValueOf = Integer.valueOf(parsableByteArray.readInt());
            } else if (i7 == 1935894637) {
                parsableByteArray.skipBytes(4);
                string = parsableByteArray.readString(4);
            } else if (i7 == 1935894633) {
                i4 = i3;
                i5 = i6;
            }
            i3 += i6;
        }
        if (!C.CENC_TYPE_cenc.equals(string) && !C.CENC_TYPE_cbc1.equals(string) && !C.CENC_TYPE_cens.equals(string) && !C.CENC_TYPE_cbcs.equals(string)) {
            return null;
        }
        ExtractorUtil.checkContainerInput(numValueOf != null, "frma atom is mandatory");
        ExtractorUtil.checkContainerInput(i4 != -1, "schi atom is mandatory");
        TrackEncryptionBox schiFromParent = parseSchiFromParent(parsableByteArray, i4, i5, string);
        ExtractorUtil.checkContainerInput(schiFromParent != null, "tenc atom is mandatory");
        return Pair.create(numValueOf, (TrackEncryptionBox) Util.castNonNull(schiFromParent));
    }

    private static TrackEncryptionBox parseSchiFromParent(ParsableByteArray parsableByteArray, int i, int i2, String str) {
        int i3;
        int i4;
        int i5 = i + 8;
        while (true) {
            byte[] bArr = null;
            if (i5 - i >= i2) {
                return null;
            }
            parsableByteArray.setPosition(i5);
            int i6 = parsableByteArray.readInt();
            if (parsableByteArray.readInt() == 1952804451) {
                int fullBoxVersion = parseFullBoxVersion(parsableByteArray.readInt());
                parsableByteArray.skipBytes(1);
                if (fullBoxVersion == 0) {
                    parsableByteArray.skipBytes(1);
                    i4 = 0;
                    i3 = 0;
                } else {
                    int unsignedByte = parsableByteArray.readUnsignedByte();
                    i3 = unsignedByte & 15;
                    i4 = (unsignedByte & PsExtractor.VIDEO_STREAM_MASK) >> 4;
                }
                boolean z = parsableByteArray.readUnsignedByte() == 1;
                int unsignedByte2 = parsableByteArray.readUnsignedByte();
                byte[] bArr2 = new byte[16];
                parsableByteArray.readBytes(bArr2, 0, 16);
                if (z && unsignedByte2 == 0) {
                    int unsignedByte3 = parsableByteArray.readUnsignedByte();
                    bArr = new byte[unsignedByte3];
                    parsableByteArray.readBytes(bArr, 0, unsignedByte3);
                }
                return new TrackEncryptionBox(z, str, unsignedByte2, bArr2, i4, i3, bArr);
            }
            i5 += i6;
        }
    }

    private static byte[] parseProjFromParent(ParsableByteArray parsableByteArray, int i, int i2) {
        int i3 = i + 8;
        while (i3 - i < i2) {
            parsableByteArray.setPosition(i3);
            int i4 = parsableByteArray.readInt();
            if (parsableByteArray.readInt() == 1886547818) {
                return Arrays.copyOfRange(parsableByteArray.getData(), i3, i4 + i3);
            }
            i3 += i4;
        }
        return null;
    }

    private static int parseExpandableClassSize(ParsableByteArray parsableByteArray) {
        int unsignedByte = parsableByteArray.readUnsignedByte();
        int i = unsignedByte & 127;
        while ((unsignedByte & 128) == 128) {
            unsignedByte = parsableByteArray.readUnsignedByte();
            i = (i << 7) | (unsignedByte & 127);
        }
        return i;
    }

    private static boolean canApplyEditWithGaplessInfo(long[] jArr, long j, long j2, long j3) {
        int length = jArr.length - 1;
        return jArr[0] <= j2 && j2 < jArr[Util.constrainValue(4, 0, length)] && jArr[Util.constrainValue(jArr.length - 4, 0, length)] < j3 && j3 <= j;
    }

    private BoxParser() {
    }

    private static final class ChunkIterator {
        private final ParsableByteArray chunkOffsets;
        private final boolean chunkOffsetsAreLongs;
        public int index;
        public final int length;
        private int nextSamplesPerChunkChangeIndex;
        public int numSamples;
        public long offset;
        private int remainingSamplesPerChunkChanges;
        private final ParsableByteArray stsc;

        public ChunkIterator(ParsableByteArray parsableByteArray, ParsableByteArray parsableByteArray2, boolean z) throws ParserException {
            this.stsc = parsableByteArray;
            this.chunkOffsets = parsableByteArray2;
            this.chunkOffsetsAreLongs = z;
            parsableByteArray2.setPosition(12);
            this.length = parsableByteArray2.readUnsignedIntToInt();
            parsableByteArray.setPosition(12);
            this.remainingSamplesPerChunkChanges = parsableByteArray.readUnsignedIntToInt();
            ExtractorUtil.checkContainerInput(parsableByteArray.readInt() == 1, "first_chunk must be 1");
            this.index = -1;
        }

        public boolean moveNext() {
            long unsignedInt;
            int i = this.index + 1;
            this.index = i;
            if (i == this.length) {
                return false;
            }
            if (this.chunkOffsetsAreLongs) {
                unsignedInt = this.chunkOffsets.readUnsignedLongToLong();
            } else {
                unsignedInt = this.chunkOffsets.readUnsignedInt();
            }
            this.offset = unsignedInt;
            if (this.index == this.nextSamplesPerChunkChangeIndex) {
                this.numSamples = this.stsc.readUnsignedIntToInt();
                this.stsc.skipBytes(4);
                int i2 = this.remainingSamplesPerChunkChanges - 1;
                this.remainingSamplesPerChunkChanges = i2;
                this.nextSamplesPerChunkChangeIndex = i2 > 0 ? this.stsc.readUnsignedIntToInt() - 1 : -1;
            }
            return true;
        }
    }

    private static final class TkhdData {
        private final long duration;
        private final int id;
        private final int rotationDegrees;

        public TkhdData(int i, long j, int i2) {
            this.id = i;
            this.duration = j;
            this.rotationDegrees = i2;
        }
    }

    private static final class StsdData {
        public static final int STSD_HEADER_SIZE = 8;
        public Format format;
        public int nalUnitLengthFieldLength;
        public int requiredSampleTransformation = 0;
        public final TrackEncryptionBox[] trackEncryptionBoxes;

        public StsdData(int i) {
            this.trackEncryptionBoxes = new TrackEncryptionBox[i];
        }
    }

    private static final class EsdsData {
        private final long bitrate;
        private final byte[] initializationData;
        private final String mimeType;
        private final long peakBitrate;

        public EsdsData(String str, byte[] bArr, long j, long j2) {
            this.mimeType = str;
            this.initializationData = bArr;
            this.bitrate = j;
            this.peakBitrate = j2;
        }
    }

    private static final class StriData {
        private final boolean eyeViewsReversed;
        private final boolean hasAdditionalViews;
        private final boolean hasLeftEyeView;
        private final boolean hasRightEyeView;

        public StriData(boolean z, boolean z2, boolean z3, boolean z4) {
            this.hasLeftEyeView = z;
            this.hasRightEyeView = z2;
            this.eyeViewsReversed = z3;
            this.hasAdditionalViews = z4;
        }
    }

    private static final class EyesData {
        private final StriData striData;

        public EyesData(StriData striData) {
            this.striData = striData;
        }
    }

    private static final class MdhdData {
        private final String language;
        private final long mediaDurationUs;
        private final long timescale;

        public MdhdData(long j, long j2, String str) {
            this.timescale = j;
            this.mediaDurationUs = j2;
            this.language = str;
        }
    }

    static final class VexuData {
        private final EyesData eyesData;

        public VexuData(EyesData eyesData) {
            this.eyesData = eyesData;
        }

        public boolean hasBothEyeViews() {
            EyesData eyesData = this.eyesData;
            return eyesData != null && eyesData.striData.hasLeftEyeView && this.eyesData.striData.hasRightEyeView;
        }
    }

    static final class StszSampleSizeBox implements SampleSizeBox {
        private final ParsableByteArray data;
        private final int fixedSampleSize;
        private final int sampleCount;

        public StszSampleSizeBox(Mp4Box.LeafBox leafBox, Format format) {
            ParsableByteArray parsableByteArray = leafBox.data;
            this.data = parsableByteArray;
            parsableByteArray.setPosition(12);
            int unsignedIntToInt = parsableByteArray.readUnsignedIntToInt();
            if (MimeTypes.AUDIO_RAW.equals(format.sampleMimeType)) {
                int pcmFrameSize = Util.getPcmFrameSize(format.pcmEncoding, format.channelCount);
                if (unsignedIntToInt == 0 || unsignedIntToInt % pcmFrameSize != 0) {
                    Log.w(BoxParser.TAG, "Audio sample size mismatch. stsd sample size: " + pcmFrameSize + ", stsz sample size: " + unsignedIntToInt);
                    unsignedIntToInt = pcmFrameSize;
                }
            }
            this.fixedSampleSize = unsignedIntToInt == 0 ? -1 : unsignedIntToInt;
            this.sampleCount = parsableByteArray.readUnsignedIntToInt();
        }

        @Override // androidx.media3.extractor.mp4.BoxParser.SampleSizeBox
        public int getSampleCount() {
            return this.sampleCount;
        }

        @Override // androidx.media3.extractor.mp4.BoxParser.SampleSizeBox
        public int getFixedSampleSize() {
            return this.fixedSampleSize;
        }

        @Override // androidx.media3.extractor.mp4.BoxParser.SampleSizeBox
        public int readNextSampleSize() {
            int i = this.fixedSampleSize;
            return i == -1 ? this.data.readUnsignedIntToInt() : i;
        }
    }

    static final class Stz2SampleSizeBox implements SampleSizeBox {
        private int currentByte;
        private final ParsableByteArray data;
        private final int fieldSize;
        private final int sampleCount;
        private int sampleIndex;

        @Override // androidx.media3.extractor.mp4.BoxParser.SampleSizeBox
        public int getFixedSampleSize() {
            return -1;
        }

        public Stz2SampleSizeBox(Mp4Box.LeafBox leafBox) {
            ParsableByteArray parsableByteArray = leafBox.data;
            this.data = parsableByteArray;
            parsableByteArray.setPosition(12);
            this.fieldSize = parsableByteArray.readUnsignedIntToInt() & 255;
            this.sampleCount = parsableByteArray.readUnsignedIntToInt();
        }

        @Override // androidx.media3.extractor.mp4.BoxParser.SampleSizeBox
        public int getSampleCount() {
            return this.sampleCount;
        }

        @Override // androidx.media3.extractor.mp4.BoxParser.SampleSizeBox
        public int readNextSampleSize() {
            int i = this.fieldSize;
            if (i == 8) {
                return this.data.readUnsignedByte();
            }
            if (i == 16) {
                return this.data.readUnsignedShort();
            }
            int i2 = this.sampleIndex;
            this.sampleIndex = i2 + 1;
            if (i2 % 2 == 0) {
                int unsignedByte = this.data.readUnsignedByte();
                this.currentByte = unsignedByte;
                return (unsignedByte & PsExtractor.VIDEO_STREAM_MASK) >> 4;
            }
            return this.currentByte & 15;
        }
    }
}
