package androidx.media3.container;

import androidx.media3.common.ColorInfo;
import androidx.media3.common.Format;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.CodecSpecificDataUtil;
import androidx.media3.common.util.Log;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import com.google.common.math.DoubleMath;
import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public final class NalUnitUtil {
    public static final int EXTENDED_SAR = 255;
    public static final int H264_NAL_UNIT_TYPE_AUD = 9;
    public static final int H264_NAL_UNIT_TYPE_IDR = 5;
    public static final int H264_NAL_UNIT_TYPE_NON_IDR = 1;
    public static final int H264_NAL_UNIT_TYPE_PARTITION_A = 2;
    public static final int H264_NAL_UNIT_TYPE_PPS = 8;
    public static final int H264_NAL_UNIT_TYPE_PREFIX = 14;
    public static final int H264_NAL_UNIT_TYPE_SEI = 6;
    public static final int H264_NAL_UNIT_TYPE_SPS = 7;
    public static final int H265_NAL_UNIT_TYPE_AUD = 35;
    public static final int H265_NAL_UNIT_TYPE_BLA_W_LP = 16;
    public static final int H265_NAL_UNIT_TYPE_CRA = 21;
    public static final int H265_NAL_UNIT_TYPE_PPS = 34;
    public static final int H265_NAL_UNIT_TYPE_PREFIX_SEI = 39;
    public static final int H265_NAL_UNIT_TYPE_RASL_R = 9;
    public static final int H265_NAL_UNIT_TYPE_SPS = 33;
    public static final int H265_NAL_UNIT_TYPE_SUFFIX_SEI = 40;
    public static final int H265_NAL_UNIT_TYPE_VPS = 32;
    private static final int INVALID_ID = -1;

    @Deprecated
    public static final int NAL_UNIT_TYPE_AUD = 9;

    @Deprecated
    public static final int NAL_UNIT_TYPE_IDR = 5;

    @Deprecated
    public static final int NAL_UNIT_TYPE_NON_IDR = 1;

    @Deprecated
    public static final int NAL_UNIT_TYPE_PARTITION_A = 2;

    @Deprecated
    public static final int NAL_UNIT_TYPE_PPS = 8;

    @Deprecated
    public static final int NAL_UNIT_TYPE_PREFIX = 14;

    @Deprecated
    public static final int NAL_UNIT_TYPE_SEI = 6;

    @Deprecated
    public static final int NAL_UNIT_TYPE_SPS = 7;
    private static final String TAG = "NalUnitUtil";
    public static final byte[] NAL_START_CODE = {0, 0, 0, 1};
    public static final float[] ASPECT_RATIO_IDC_VALUES = {1.0f, 1.0f, 1.0909091f, 0.90909094f, 1.4545455f, 1.2121212f, 2.1818182f, 1.8181819f, 2.909091f, 2.4242425f, 1.6363636f, 1.3636364f, 1.939394f, 1.6161616f, 1.3333334f, 1.5f, 2.0f};
    private static final Object scratchEscapePositionsLock = new Object();
    private static int[] scratchEscapePositions = new int[10];

    private static int applyConformanceWindowToHeight(int i, int i2, int i3, int i4) {
        return i - ((i2 == 1 ? 2 : 1) * (i3 + i4));
    }

    private static int applyConformanceWindowToWidth(int i, int i2, int i3, int i4) {
        int i5 = 2;
        if (i2 != 1 && i2 != 2) {
            i5 = 1;
        }
        return i - (i5 * (i3 + i4));
    }

    public static boolean isH264NalUnitDependedOn(byte b) {
        if (((b & 96) >> 5) != 0) {
            return true;
        }
        int i = b & Ascii.US;
        return (i == 1 || i == 9 || i == 14) ? false : true;
    }

    public static final class SpsData {
        public final int bitDepthChromaMinus8;
        public final int bitDepthLumaMinus8;
        public final int colorRange;
        public final int colorSpace;
        public final int colorTransfer;
        public final int constraintsFlagsAndReservedZero2Bits;
        public final boolean deltaPicOrderAlwaysZeroFlag;
        public final boolean frameMbsOnlyFlag;
        public final int frameNumLength;
        public final int height;
        public final int levelIdc;
        public final int maxNumRefFrames;
        public final int maxNumReorderFrames;
        public final int picOrderCntLsbLength;
        public final int picOrderCountType;
        public final float pixelWidthHeightRatio;
        public final int profileIdc;
        public final boolean separateColorPlaneFlag;
        public final int seqParameterSetId;
        public final int width;

        public SpsData(int i, int i2, int i3, int i4, int i5, int i6, int i7, float f, int i8, int i9, boolean z, boolean z2, int i10, int i11, int i12, boolean z3, int i13, int i14, int i15, int i16) {
            this.profileIdc = i;
            this.constraintsFlagsAndReservedZero2Bits = i2;
            this.levelIdc = i3;
            this.seqParameterSetId = i4;
            this.maxNumRefFrames = i5;
            this.width = i6;
            this.height = i7;
            this.pixelWidthHeightRatio = f;
            this.bitDepthLumaMinus8 = i8;
            this.bitDepthChromaMinus8 = i9;
            this.separateColorPlaneFlag = z;
            this.frameMbsOnlyFlag = z2;
            this.frameNumLength = i10;
            this.picOrderCountType = i11;
            this.picOrderCntLsbLength = i12;
            this.deltaPicOrderAlwaysZeroFlag = z3;
            this.colorSpace = i13;
            this.colorRange = i14;
            this.colorTransfer = i15;
            this.maxNumReorderFrames = i16;
        }
    }

    public static final class H265NalHeader {
        public final int layerId;
        public final int nalUnitType;
        public final int temporalId;

        public H265NalHeader(int i, int i2, int i3) {
            this.nalUnitType = i;
            this.layerId = i2;
            this.temporalId = i3;
        }
    }

    public static final class H265LayerInfo {
        public final int layerIdInVps;
        public final int viewId;

        public H265LayerInfo(int i, int i2) {
            this.layerIdInVps = i;
            this.viewId = i2;
        }
    }

    public static final class H265ProfileTierLevel {
        public final int[] constraintBytes;
        public final int generalLevelIdc;
        public final int generalProfileCompatibilityFlags;
        public final int generalProfileIdc;
        public final int generalProfileSpace;
        public final boolean generalTierFlag;

        public H265ProfileTierLevel(int i, boolean z, int i2, int i3, int[] iArr, int i4) {
            this.generalProfileSpace = i;
            this.generalTierFlag = z;
            this.generalProfileIdc = i2;
            this.generalProfileCompatibilityFlags = i3;
            this.constraintBytes = iArr;
            this.generalLevelIdc = i4;
        }
    }

    public static final class H265ProfileTierLevelsAndIndices {
        public final int[] indices;
        public final ImmutableList<H265ProfileTierLevel> profileTierLevels;

        public H265ProfileTierLevelsAndIndices(List<H265ProfileTierLevel> list, int[] iArr) {
            this.profileTierLevels = ImmutableList.copyOf((Collection) list);
            this.indices = iArr;
        }
    }

    public static final class H265RepFormat {
        public final int bitDepthChromaMinus8;
        public final int bitDepthLumaMinus8;
        public final int chromaFormatIdc;
        public final int height;
        public final int width;

        public H265RepFormat(int i, int i2, int i3, int i4, int i5) {
            this.chromaFormatIdc = i;
            this.bitDepthLumaMinus8 = i2;
            this.bitDepthChromaMinus8 = i3;
            this.width = i4;
            this.height = i5;
        }
    }

    public static final class H265RepFormatsAndIndices {
        public final int[] indices;
        public final ImmutableList<H265RepFormat> repFormats;

        public H265RepFormatsAndIndices(List<H265RepFormat> list, int[] iArr) {
            this.repFormats = ImmutableList.copyOf((Collection) list);
            this.indices = iArr;
        }
    }

    public static final class H265VideoSignalInfo {
        public final int colorRange;
        public final int colorSpace;
        public final int colorTransfer;

        public H265VideoSignalInfo(int i, int i2, int i3) {
            this.colorSpace = i;
            this.colorRange = i2;
            this.colorTransfer = i3;
        }
    }

    public static final class H265VideoSignalInfosAndIndices {
        public final int[] indices;
        public final ImmutableList<H265VideoSignalInfo> videoSignalInfos;

        public H265VideoSignalInfosAndIndices(List<H265VideoSignalInfo> list, int[] iArr) {
            this.videoSignalInfos = ImmutableList.copyOf((Collection) list);
            this.indices = iArr;
        }
    }

    public static final class H265VpsData {
        public final ImmutableList<H265LayerInfo> layerInfos;
        public final H265NalHeader nalHeader;
        public final H265ProfileTierLevelsAndIndices profileTierLevelsAndIndices;
        public final H265RepFormatsAndIndices repFormatsAndIndices;
        public final H265VideoSignalInfosAndIndices videoSignalInfosAndIndices;

        public H265VpsData(H265NalHeader h265NalHeader, List<H265LayerInfo> list, H265ProfileTierLevelsAndIndices h265ProfileTierLevelsAndIndices, H265RepFormatsAndIndices h265RepFormatsAndIndices, H265VideoSignalInfosAndIndices h265VideoSignalInfosAndIndices) {
            this.nalHeader = h265NalHeader;
            this.layerInfos = list != null ? ImmutableList.copyOf((Collection) list) : ImmutableList.of();
            this.profileTierLevelsAndIndices = h265ProfileTierLevelsAndIndices;
            this.repFormatsAndIndices = h265RepFormatsAndIndices;
            this.videoSignalInfosAndIndices = h265VideoSignalInfosAndIndices;
        }
    }

    public static final class H265SpsData {
        public final int bitDepthChromaMinus8;
        public final int bitDepthLumaMinus8;
        public final int chromaFormatIdc;
        public final int colorRange;
        public final int colorSpace;
        public final int colorTransfer;
        public final int height;
        public final int maxNumReorderPics;
        public final H265NalHeader nalHeader;
        public final float pixelWidthHeightRatio;
        public final H265ProfileTierLevel profileTierLevel;
        public final int seqParameterSetId;
        public final int width;

        public H265SpsData(H265NalHeader h265NalHeader, H265ProfileTierLevel h265ProfileTierLevel, int i, int i2, int i3, int i4, int i5, int i6, float f, int i7, int i8, int i9, int i10) {
            this.nalHeader = h265NalHeader;
            this.profileTierLevel = h265ProfileTierLevel;
            this.chromaFormatIdc = i;
            this.bitDepthLumaMinus8 = i2;
            this.bitDepthChromaMinus8 = i3;
            this.seqParameterSetId = i4;
            this.width = i5;
            this.height = i6;
            this.pixelWidthHeightRatio = f;
            this.maxNumReorderPics = i7;
            this.colorSpace = i8;
            this.colorRange = i9;
            this.colorTransfer = i10;
        }
    }

    public static final class PpsData {
        public final boolean bottomFieldPicOrderInFramePresentFlag;
        public final int picParameterSetId;
        public final int seqParameterSetId;

        public PpsData(int i, int i2, boolean z) {
            this.picParameterSetId = i;
            this.seqParameterSetId = i2;
            this.bottomFieldPicOrderInFramePresentFlag = z;
        }
    }

    public static final class H265Sei3dRefDisplayInfoData {
        public final int exponentRefDisplayWidth;
        public final int exponentRefViewingDist;
        public final int leftViewId;
        public final int mantissaRefDisplayWidth;
        public final int mantissaRefViewingDist;
        public final int numRefDisplays;
        public final int precRefDisplayWidth;
        public final int precRefViewingDist;
        public final int rightViewId;

        public H265Sei3dRefDisplayInfoData(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
            this.precRefDisplayWidth = i;
            this.precRefViewingDist = i2;
            this.numRefDisplays = i3;
            this.leftViewId = i4;
            this.rightViewId = i5;
            this.exponentRefDisplayWidth = i6;
            this.mantissaRefDisplayWidth = i7;
            this.exponentRefViewingDist = i8;
            this.mantissaRefViewingDist = i9;
        }
    }

    public static int unescapeStream(byte[] bArr, int i) {
        int i2;
        synchronized (scratchEscapePositionsLock) {
            int iFindNextUnescapeIndex = 0;
            int i3 = 0;
            while (iFindNextUnescapeIndex < i) {
                try {
                    iFindNextUnescapeIndex = findNextUnescapeIndex(bArr, iFindNextUnescapeIndex, i);
                    if (iFindNextUnescapeIndex < i) {
                        int[] iArr = scratchEscapePositions;
                        if (iArr.length <= i3) {
                            scratchEscapePositions = Arrays.copyOf(iArr, iArr.length * 2);
                        }
                        scratchEscapePositions[i3] = iFindNextUnescapeIndex;
                        iFindNextUnescapeIndex += 3;
                        i3++;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            i2 = i - i3;
            int i4 = 0;
            int i5 = 0;
            for (int i6 = 0; i6 < i3; i6++) {
                int i7 = scratchEscapePositions[i6] - i5;
                System.arraycopy(bArr, i5, bArr, i4, i7);
                int i8 = i4 + i7;
                int i9 = i8 + 1;
                bArr[i8] = 0;
                i4 = i8 + 2;
                bArr[i9] = 0;
                i5 += i7 + 3;
            }
            System.arraycopy(bArr, i5, bArr, i4, i2 - i4);
        }
        return i2;
    }

    public static void discardToSps(ByteBuffer byteBuffer) {
        int iPosition = byteBuffer.position();
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = i + 1;
            if (i3 < iPosition) {
                int i4 = byteBuffer.get(i) & 255;
                if (i2 == 3) {
                    if (i4 == 1 && (byteBuffer.get(i3) & Ascii.US) == 7) {
                        ByteBuffer byteBufferDuplicate = byteBuffer.duplicate();
                        byteBufferDuplicate.position(i - 3);
                        byteBufferDuplicate.limit(iPosition);
                        byteBuffer.position(0);
                        byteBuffer.put(byteBufferDuplicate);
                        return;
                    }
                } else if (i4 == 0) {
                    i2++;
                }
                if (i4 != 0) {
                    i2 = 0;
                }
                i = i3;
            } else {
                byteBuffer.clear();
                return;
            }
        }
    }

    public static boolean isNalUnitSei(String str, byte b) {
        if (MimeTypes.VIDEO_H264.equals(str) && (b & Ascii.US) == 6) {
            return true;
        }
        return MimeTypes.VIDEO_H265.equals(str) && ((b & 126) >> 1) == 39;
    }

    public static boolean isNalUnitSei(Format format, byte b) {
        if ((Objects.equals(format.sampleMimeType, MimeTypes.VIDEO_H264) || MimeTypes.containsCodecsCorrespondingToMimeType(format.codecs, MimeTypes.VIDEO_H264)) && (b & Ascii.US) == 6) {
            return true;
        }
        return (Objects.equals(format.sampleMimeType, MimeTypes.VIDEO_H265) || MimeTypes.containsCodecsCorrespondingToMimeType(format.codecs, MimeTypes.VIDEO_H265)) && ((b & 126) >> 1) == 39;
    }

    public static int getNalUnitType(byte[] bArr, int i) {
        return bArr[i + 3] & Ascii.US;
    }

    public static int getH265NalUnitType(byte[] bArr, int i) {
        return (bArr[i + 3] & 126) >> 1;
    }

    public static SpsData parseSpsNalUnit(byte[] bArr, int i, int i2) {
        return parseSpsNalUnitPayload(bArr, i + 1, i2);
    }

    /* JADX WARN: Code duplicated, block: B:102:0x01ac  */
    /* JADX WARN: Code duplicated, block: B:105:0x01b3  */
    /* JADX WARN: Code duplicated, block: B:106:0x01c9  */
    /* JADX WARN: Code duplicated, block: B:107:0x01cc  */
    /* JADX WARN: Code duplicated, block: B:110:0x01d5  */
    /* JADX WARN: Code duplicated, block: B:113:0x01e1  */
    /* JADX WARN: Code duplicated, block: B:116:0x01ec  */
    /* JADX WARN: Code duplicated, block: B:119:0x01f5  */
    /* JADX WARN: Code duplicated, block: B:122:0x01fc  */
    /* JADX WARN: Code duplicated, block: B:125:0x0208  */
    /* JADX WARN: Code duplicated, block: B:127:0x0227  */
    /* JADX WARN: Code duplicated, block: B:51:0x00ec  */
    /* JADX WARN: Code duplicated, block: B:54:0x00fa  */
    /* JADX WARN: Code duplicated, block: B:56:0x010c  */
    /* JADX WARN: Code duplicated, block: B:57:0x0111 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:58:0x0113  */
    /* JADX WARN: Code duplicated, block: B:59:0x0116  */
    /* JADX WARN: Code duplicated, block: B:61:0x011a  */
    /* JADX WARN: Code duplicated, block: B:62:0x011c  */
    /* JADX WARN: Code duplicated, block: B:82:0x0157  */
    /* JADX WARN: Code duplicated, block: B:84:0x015d  */
    /* JADX WARN: Code duplicated, block: B:86:0x0167  */
    /* JADX WARN: Code duplicated, block: B:90:0x0178  */
    /* JADX WARN: Code duplicated, block: B:92:0x017d  */
    /* JADX WARN: Code duplicated, block: B:93:0x0180  */
    /* JADX WARN: Code duplicated, block: B:96:0x0199  */
    /* JADX WARN: Code duplicated, block: B:99:0x01a2  */
    /* JADX WARN: Instruction removed from duplicated block: B:93:0x0180, please report this as an issue */
    public static SpsData parseSpsNalUnitPayload(byte[] bArr, int i, int i2) {
        int unsignedExpGolombCodedInt;
        boolean bit;
        int unsignedExpGolombCodedInt2;
        int unsignedExpGolombCodedInt3;
        int unsignedExpGolombCodedInt4;
        boolean z;
        boolean bit2;
        int i3;
        int i4;
        int unsignedExpGolombCodedInt5;
        int iIsoColorPrimariesToColorSpace;
        float f;
        int i5;
        int i6;
        float f2;
        int i7;
        int iIsoTransferCharacteristicsToColorTransfer;
        int i8;
        boolean bit3;
        boolean bit4;
        int bits;
        float[] fArr;
        int bits2;
        int bits3;
        int i9;
        int i10;
        int i11;
        ParsableNalUnitBitArray parsableNalUnitBitArray = new ParsableNalUnitBitArray(bArr, i, i2);
        int bits4 = parsableNalUnitBitArray.readBits(8);
        int bits5 = parsableNalUnitBitArray.readBits(8);
        int bits6 = parsableNalUnitBitArray.readBits(8);
        int unsignedExpGolombCodedInt6 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        if (bits4 == 100 || bits4 == 110 || bits4 == 122 || bits4 == 244 || bits4 == 44 || bits4 == 83 || bits4 == 86 || bits4 == 118 || bits4 == 128 || bits4 == 138) {
            unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            bit = unsignedExpGolombCodedInt == 3 ? parsableNalUnitBitArray.readBit() : false;
            unsignedExpGolombCodedInt2 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            unsignedExpGolombCodedInt3 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            parsableNalUnitBitArray.skipBit();
            if (parsableNalUnitBitArray.readBit()) {
                int i12 = unsignedExpGolombCodedInt == 3 ? 12 : 8;
                int i13 = 0;
                while (i13 < i12) {
                    if (parsableNalUnitBitArray.readBit()) {
                        skipScalingList(parsableNalUnitBitArray, i13 < 6 ? 16 : 64);
                    }
                    i13++;
                }
            }
        } else {
            unsignedExpGolombCodedInt = 1;
            bit = false;
            unsignedExpGolombCodedInt2 = 0;
            unsignedExpGolombCodedInt3 = 0;
        }
        int unsignedExpGolombCodedInt7 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 4;
        int unsignedExpGolombCodedInt8 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        if (unsignedExpGolombCodedInt8 == 0) {
            unsignedExpGolombCodedInt4 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 4;
        } else {
            if (unsignedExpGolombCodedInt8 == 1) {
                boolean bit5 = parsableNalUnitBitArray.readBit();
                parsableNalUnitBitArray.readSignedExpGolombCodedInt();
                parsableNalUnitBitArray.readSignedExpGolombCodedInt();
                bits4 = bits4;
                long unsignedExpGolombCodedInt9 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                for (int i14 = 0; i14 < unsignedExpGolombCodedInt9; i14++) {
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                }
                z = bit5;
                unsignedExpGolombCodedInt4 = 0;
            } else {
                unsignedExpGolombCodedInt4 = 0;
            }
            int unsignedExpGolombCodedInt10 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            parsableNalUnitBitArray.skipBit();
            int unsignedExpGolombCodedInt11 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1;
            int unsignedExpGolombCodedInt12 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1;
            bit2 = parsableNalUnitBitArray.readBit();
            int i15 = (2 - (bit2 ? 1 : 0)) * unsignedExpGolombCodedInt12;
            if (!bit2) {
                parsableNalUnitBitArray.skipBit();
            }
            parsableNalUnitBitArray.skipBit();
            i3 = unsignedExpGolombCodedInt11 * 16;
            i4 = i15 * 16;
            if (parsableNalUnitBitArray.readBit()) {
                int unsignedExpGolombCodedInt13 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                int unsignedExpGolombCodedInt14 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                int unsignedExpGolombCodedInt15 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                int unsignedExpGolombCodedInt16 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                if (unsignedExpGolombCodedInt == 0) {
                    i11 = 2 - (bit2 ? 1 : 0);
                    i9 = 1;
                } else {
                    if (unsignedExpGolombCodedInt == 3) {
                        i9 = 1;
                    } else {
                        i9 = 2;
                    }
                    if (unsignedExpGolombCodedInt == 1) {
                        i10 = 2;
                    } else {
                        i10 = 1;
                    }
                    i11 = i10 * (2 - (bit2 ? 1 : 0));
                }
                i3 -= (unsignedExpGolombCodedInt13 + unsignedExpGolombCodedInt14) * i9;
                i4 -= (unsignedExpGolombCodedInt15 + unsignedExpGolombCodedInt16) * i11;
            }
            int i16 = i4;
            int i17 = bits4;
            int i18 = i3;
            unsignedExpGolombCodedInt5 = ((i17 != 44 || i17 == 86 || i17 == 100 || i17 == 110 || i17 == 122 || i17 == 244) && (bits5 & 16) != 0) ? 0 : 16;
            iIsoColorPrimariesToColorSpace = -1;
            f = 1.0f;
            if (parsableNalUnitBitArray.readBit()) {
                if (parsableNalUnitBitArray.readBit()) {
                    bits = parsableNalUnitBitArray.readBits(8);
                    if (bits == 255) {
                        bits2 = parsableNalUnitBitArray.readBits(16);
                        bits3 = parsableNalUnitBitArray.readBits(16);
                        if (bits2 != 0 && bits3 != 0) {
                            f = bits2 / bits3;
                        }
                    } else {
                        fArr = ASPECT_RATIO_IDC_VALUES;
                        if (bits < fArr.length) {
                            f = fArr[bits];
                        } else {
                            Log.w(TAG, "Unexpected aspect_ratio_idc value: " + bits);
                        }
                    }
                }
                if (parsableNalUnitBitArray.readBit()) {
                    parsableNalUnitBitArray.skipBit();
                }
                if (parsableNalUnitBitArray.readBit()) {
                    parsableNalUnitBitArray.skipBits(3);
                    i8 = parsableNalUnitBitArray.readBit() ? 1 : 2;
                    if (parsableNalUnitBitArray.readBit()) {
                        int bits7 = parsableNalUnitBitArray.readBits(8);
                        int bits8 = parsableNalUnitBitArray.readBits(8);
                        parsableNalUnitBitArray.skipBits(8);
                        iIsoColorPrimariesToColorSpace = ColorInfo.isoColorPrimariesToColorSpace(bits7);
                        iIsoTransferCharacteristicsToColorTransfer = ColorInfo.isoTransferCharacteristicsToColorTransfer(bits8);
                    } else {
                        iIsoTransferCharacteristicsToColorTransfer = -1;
                    }
                } else {
                    iIsoTransferCharacteristicsToColorTransfer = -1;
                    i8 = -1;
                }
                if (parsableNalUnitBitArray.readBit()) {
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                }
                if (parsableNalUnitBitArray.readBit()) {
                    parsableNalUnitBitArray.skipBits(65);
                }
                bit3 = parsableNalUnitBitArray.readBit();
                if (bit3) {
                    skipHrdParameters(parsableNalUnitBitArray);
                }
                bit4 = parsableNalUnitBitArray.readBit();
                if (bit4) {
                    skipHrdParameters(parsableNalUnitBitArray);
                }
                if (!bit3 || bit4) {
                    parsableNalUnitBitArray.skipBit();
                }
                parsableNalUnitBitArray.skipBit();
                if (parsableNalUnitBitArray.readBit()) {
                    parsableNalUnitBitArray.skipBit();
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    unsignedExpGolombCodedInt5 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                }
                i5 = unsignedExpGolombCodedInt5;
                i6 = iIsoTransferCharacteristicsToColorTransfer;
                f2 = f;
                i7 = i8;
            } else {
                i5 = unsignedExpGolombCodedInt5;
                i6 = -1;
                f2 = 1.0f;
                i7 = -1;
            }
            return new SpsData(i17, bits5, bits6, unsignedExpGolombCodedInt6, unsignedExpGolombCodedInt10, i18, i16, f2, unsignedExpGolombCodedInt2, unsignedExpGolombCodedInt3, bit, bit2, unsignedExpGolombCodedInt7, unsignedExpGolombCodedInt8, unsignedExpGolombCodedInt4, z, iIsoColorPrimariesToColorSpace, i7, i6, i5);
        }
        z = false;
        int unsignedExpGolombCodedInt17 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        parsableNalUnitBitArray.skipBit();
        int unsignedExpGolombCodedInt18 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1;
        int unsignedExpGolombCodedInt19 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1;
        bit2 = parsableNalUnitBitArray.readBit();
        int i19 = (2 - (bit2 ? 1 : 0)) * unsignedExpGolombCodedInt19;
        if (!bit2) {
            parsableNalUnitBitArray.skipBit();
        }
        parsableNalUnitBitArray.skipBit();
        i3 = unsignedExpGolombCodedInt18 * 16;
        i4 = i19 * 16;
        if (parsableNalUnitBitArray.readBit()) {
            int unsignedExpGolombCodedInt110 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            int unsignedExpGolombCodedInt111 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            int unsignedExpGolombCodedInt112 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            int unsignedExpGolombCodedInt113 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            if (unsignedExpGolombCodedInt == 0) {
                i11 = 2 - (bit2 ? 1 : 0);
                i9 = 1;
            } else {
                if (unsignedExpGolombCodedInt == 3) {
                    i9 = 1;
                } else {
                    i9 = 2;
                }
                if (unsignedExpGolombCodedInt == 1) {
                    i10 = 2;
                } else {
                    i10 = 1;
                }
                i11 = i10 * (2 - (bit2 ? 1 : 0));
            }
            i3 -= (unsignedExpGolombCodedInt110 + unsignedExpGolombCodedInt111) * i9;
            i4 -= (unsignedExpGolombCodedInt112 + unsignedExpGolombCodedInt113) * i11;
        }
        int i110 = i4;
        int i111 = bits4;
        int i112 = i3;
        if (i111 != 44) {
        }
        iIsoColorPrimariesToColorSpace = -1;
        f = 1.0f;
        if (parsableNalUnitBitArray.readBit()) {
            if (parsableNalUnitBitArray.readBit()) {
                bits = parsableNalUnitBitArray.readBits(8);
                if (bits == 255) {
                    bits2 = parsableNalUnitBitArray.readBits(16);
                    bits3 = parsableNalUnitBitArray.readBits(16);
                    if (bits2 != 0) {
                        f = bits2 / bits3;
                    }
                } else {
                    fArr = ASPECT_RATIO_IDC_VALUES;
                    if (bits < fArr.length) {
                        f = fArr[bits];
                    } else {
                        Log.w(TAG, "Unexpected aspect_ratio_idc value: " + bits);
                    }
                }
            }
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.skipBit();
            }
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.skipBits(3);
                if (parsableNalUnitBitArray.readBit()) {
                }
                if (parsableNalUnitBitArray.readBit()) {
                    int bits9 = parsableNalUnitBitArray.readBits(8);
                    int bits10 = parsableNalUnitBitArray.readBits(8);
                    parsableNalUnitBitArray.skipBits(8);
                    iIsoColorPrimariesToColorSpace = ColorInfo.isoColorPrimariesToColorSpace(bits9);
                    iIsoTransferCharacteristicsToColorTransfer = ColorInfo.isoTransferCharacteristicsToColorTransfer(bits10);
                } else {
                    iIsoTransferCharacteristicsToColorTransfer = -1;
                }
            } else {
                iIsoTransferCharacteristicsToColorTransfer = -1;
                i8 = -1;
            }
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            }
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.skipBits(65);
            }
            bit3 = parsableNalUnitBitArray.readBit();
            if (bit3) {
                skipHrdParameters(parsableNalUnitBitArray);
            }
            bit4 = parsableNalUnitBitArray.readBit();
            if (bit4) {
                skipHrdParameters(parsableNalUnitBitArray);
            }
            if (!bit3) {
                parsableNalUnitBitArray.skipBit();
            } else {
                parsableNalUnitBitArray.skipBit();
            }
            parsableNalUnitBitArray.skipBit();
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.skipBit();
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                unsignedExpGolombCodedInt5 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            }
            i5 = unsignedExpGolombCodedInt5;
            i6 = iIsoTransferCharacteristicsToColorTransfer;
            f2 = f;
            i7 = i8;
        } else {
            i5 = unsignedExpGolombCodedInt5;
            i6 = -1;
            f2 = 1.0f;
            i7 = -1;
        }
        return new SpsData(i111, bits5, bits6, unsignedExpGolombCodedInt6, unsignedExpGolombCodedInt17, i112, i110, f2, unsignedExpGolombCodedInt2, unsignedExpGolombCodedInt3, bit, bit2, unsignedExpGolombCodedInt7, unsignedExpGolombCodedInt8, unsignedExpGolombCodedInt4, z, iIsoColorPrimariesToColorSpace, i7, i6, i5);
    }

    public static H265VpsData parseH265VpsNalUnit(byte[] bArr, int i, int i2) {
        ParsableNalUnitBitArray parsableNalUnitBitArray = new ParsableNalUnitBitArray(bArr, i, i2);
        return parseH265VpsNalUnitPayload(parsableNalUnitBitArray, parseH265NalHeader(parsableNalUnitBitArray));
    }

    private static H265NalHeader parseH265NalHeader(ParsableNalUnitBitArray parsableNalUnitBitArray) {
        parsableNalUnitBitArray.skipBit();
        return new H265NalHeader(parsableNalUnitBitArray.readBits(6), parsableNalUnitBitArray.readBits(6), parsableNalUnitBitArray.readBits(3) - 1);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static H265VpsData parseH265VpsNalUnitPayload(ParsableNalUnitBitArray parsableNalUnitBitArray, H265NalHeader h265NalHeader) {
        int[][] iArr;
        int i;
        int i2;
        int i3;
        H265VideoSignalInfosAndIndices h265VideoSignalInfosAndIndices;
        int i4;
        int i5;
        boolean[][] zArr;
        int i6;
        boolean[][] zArr2;
        int[] iArr2;
        int[] iArr3;
        parsableNalUnitBitArray.skipBits(4);
        boolean bit = parsableNalUnitBitArray.readBit();
        boolean bit2 = parsableNalUnitBitArray.readBit();
        int bits = parsableNalUnitBitArray.readBits(6);
        int i7 = bits + 1;
        int bits2 = parsableNalUnitBitArray.readBits(3);
        parsableNalUnitBitArray.skipBits(17);
        boolean z = true;
        H265ProfileTierLevel h265ProfileTierLevel = parseH265ProfileTierLevel(parsableNalUnitBitArray, true, bits2, null);
        for (int i8 = parsableNalUnitBitArray.readBit() ? 0 : bits2; i8 <= bits2; i8++) {
            parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        }
        int bits3 = parsableNalUnitBitArray.readBits(6);
        int unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1;
        H265ProfileTierLevelsAndIndices h265ProfileTierLevelsAndIndices = new H265ProfileTierLevelsAndIndices(ImmutableList.of(h265ProfileTierLevel), new int[1]);
        Object[] objArr = i7 >= 2 && unsignedExpGolombCodedInt >= 2;
        Object[] objArr2 = bit && bit2;
        int i9 = bits3 + 1;
        Object[] objArr3 = i9 >= i7;
        if (objArr == false || objArr2 == false || objArr3 == false) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        int[][] iArr4 = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, unsignedExpGolombCodedInt, i9);
        int[] iArr5 = new int[unsignedExpGolombCodedInt];
        int[] iArr6 = new int[unsignedExpGolombCodedInt];
        iArr4[0][0] = 0;
        iArr5[0] = 1;
        iArr6[0] = 0;
        for (int i10 = 1; i10 < unsignedExpGolombCodedInt; i10++) {
            int i11 = 0;
            for (int i12 = 0; i12 <= bits3; i12++) {
                if (parsableNalUnitBitArray.readBit()) {
                    iArr4[i10][i11] = i12;
                    iArr6[i10] = i12;
                    i11++;
                }
                iArr5[i10] = i11;
            }
        }
        if (parsableNalUnitBitArray.readBit()) {
            parsableNalUnitBitArray.skipBits(64);
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            }
            int unsignedExpGolombCodedInt2 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            int i13 = 0;
            while (i13 < unsignedExpGolombCodedInt2) {
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                if (i13 != 0 && !parsableNalUnitBitArray.readBit()) {
                    z = false;
                }
                skipH265HrdParameters(parsableNalUnitBitArray, z, bits2);
                i13++;
                z = true;
            }
        }
        if (!parsableNalUnitBitArray.readBit()) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        parsableNalUnitBitArray.byteAlign();
        H265ProfileTierLevel h265ProfileTierLevel2 = parseH265ProfileTierLevel(parsableNalUnitBitArray, false, bits2, h265ProfileTierLevel);
        boolean bit3 = parsableNalUnitBitArray.readBit();
        boolean[] zArr3 = new boolean[16];
        int i14 = 0;
        for (int i15 = 0; i15 < 16; i15++) {
            boolean bit4 = parsableNalUnitBitArray.readBit();
            zArr3[i15] = bit4;
            if (bit4) {
                i14++;
            }
        }
        if (i14 == 0 || !zArr3[1]) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        int[] iArr7 = new int[i14];
        for (int i16 = 0; i16 < i14 - (bit3 ? 1 : 0); i16++) {
            iArr7[i16] = parsableNalUnitBitArray.readBits(3);
        }
        int[] iArr8 = new int[i14 + 1];
        if (bit3) {
            int i17 = 1;
            while (i17 < i14) {
                int[][] iArr9 = iArr4;
                for (int i18 = 0; i18 < i17; i18++) {
                    iArr8[i17] = iArr8[i17] + iArr7[i18] + 1;
                }
                i17++;
                iArr4 = iArr9;
            }
            iArr = iArr4;
            iArr8[i14] = 6;
        } else {
            iArr = iArr4;
        }
        int[][] iArr10 = (int[][]) Array.newInstance((Class<?>) Integer.TYPE, i7, i14);
        int[] iArr11 = new int[i7];
        iArr11[0] = 0;
        boolean bit5 = parsableNalUnitBitArray.readBit();
        int i19 = 1;
        while (i19 < i7) {
            if (bit5) {
                iArr11[i19] = parsableNalUnitBitArray.readBits(6);
            } else {
                iArr11[i19] = i19;
            }
            if (bit3) {
                iArr3 = iArr7;
                int i20 = 0;
                while (i20 < i14) {
                    int i21 = i20 + 1;
                    iArr10[i19][i20] = (iArr11[i19] & ((1 << iArr8[i21]) - 1)) >> iArr8[i20];
                    i20 = i21;
                }
            } else {
                int i22 = 0;
                while (i22 < i14) {
                    iArr10[i19][i22] = parsableNalUnitBitArray.readBits(iArr7[i22] + 1);
                    i22++;
                    iArr7 = iArr7;
                }
                iArr3 = iArr7;
            }
            i19++;
            bits2 = bits2;
            iArr7 = iArr3;
        }
        int i23 = bits2;
        int[] iArr12 = new int[i9];
        int i24 = 1;
        for (int i25 = 0; i25 < i7; i25++) {
            iArr12[iArr11[i25]] = -1;
            int i26 = 0;
            for (int i27 = 0; i27 < 16; i27++) {
                if (zArr3[i27]) {
                    if (i27 == 1) {
                        iArr12[iArr11[i25]] = iArr10[i25][i26];
                    }
                    i26++;
                }
            }
            if (i25 > 0) {
                int i28 = 0;
                while (true) {
                    if (i28 >= i25) {
                        i24++;
                        break;
                    }
                    if (iArr12[iArr11[i25]] == iArr12[iArr11[i28]]) {
                        break;
                    }
                    i28++;
                }
            }
        }
        int bits4 = parsableNalUnitBitArray.readBits(4);
        if (i24 < 2 || bits4 == 0) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        int[] iArr13 = new int[i24];
        for (int i29 = 0; i29 < i24; i29++) {
            iArr13[i29] = parsableNalUnitBitArray.readBits(bits4);
        }
        int[] iArr14 = new int[i9];
        for (int i30 = 0; i30 < i7; i30++) {
            iArr14[Math.min(iArr11[i30], bits3)] = i30;
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        int i31 = 0;
        while (i31 <= bits3) {
            int iMin = Math.min(iArr12[i31], i24 - 1);
            builder.add(new H265LayerInfo(iArr14[i31], iMin >= 0 ? iArr13[iMin] : -1));
            i31++;
            iArr12 = iArr12;
        }
        ImmutableList immutableListBuild = builder.build();
        if (((H265LayerInfo) immutableListBuild.get(0)).viewId == -1) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        int i32 = 1;
        while (true) {
            if (i32 > bits3) {
                i = -1;
                i2 = -1;
                break;
            }
            i = -1;
            if (((H265LayerInfo) immutableListBuild.get(i32)).viewId != -1) {
                i2 = i32;
                break;
            }
            i32++;
        }
        if (i2 == i) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        boolean[][] zArr4 = (boolean[][]) Array.newInstance((Class<?>) Boolean.TYPE, i7, i7);
        boolean[][] zArr5 = (boolean[][]) Array.newInstance((Class<?>) Boolean.TYPE, i7, i7);
        for (int i33 = 1; i33 < i7; i33++) {
            for (int i34 = 0; i34 < i33; i34++) {
                boolean[] zArr6 = zArr4[i33];
                boolean[] zArr7 = zArr5[i33];
                boolean bit6 = parsableNalUnitBitArray.readBit();
                zArr7[i34] = bit6;
                zArr6[i34] = bit6;
            }
        }
        for (int i35 = 1; i35 < i7; i35++) {
            for (int i36 = 0; i36 < bits; i36++) {
                for (int i37 = 0; i37 < i35; i37++) {
                    boolean[] zArr8 = zArr5[i35];
                    if (zArr8[i37] && zArr5[i37][i36]) {
                        zArr8[i36] = true;
                        break;
                    }
                }
            }
        }
        int[] iArr15 = new int[i9];
        for (int i38 = 0; i38 < i7; i38++) {
            int i39 = 0;
            for (int i40 = 0; i40 < i38; i40++) {
                i39 += zArr4[i38][i40] ? 1 : 0;
            }
            iArr15[iArr11[i38]] = i39;
        }
        int i41 = 0;
        for (int i42 = 0; i42 < i7; i42++) {
            if (iArr15[iArr11[i42]] == 0) {
                i41++;
            }
        }
        if (i41 > 1) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        int[] iArr16 = new int[i7];
        int[] iArr17 = new int[unsignedExpGolombCodedInt];
        if (parsableNalUnitBitArray.readBit()) {
            for (int i43 = 0; i43 < i7; i43++) {
                iArr16[i43] = parsableNalUnitBitArray.readBits(3);
            }
            i3 = i23;
        } else {
            i3 = i23;
            Arrays.fill(iArr16, 0, i7, i3);
        }
        int i44 = 0;
        while (i44 < unsignedExpGolombCodedInt) {
            boolean[][] zArr9 = zArr5;
            int[] iArr18 = iArr15;
            int[] iArr19 = iArr11;
            int iMax = 0;
            for (int i45 = 0; i45 < iArr5[i44]; i45++) {
                iMax = Math.max(iMax, iArr16[((H265LayerInfo) immutableListBuild.get(iArr[i44][i45])).layerIdInVps]);
            }
            iArr17[i44] = iMax + 1;
            i44++;
            iArr11 = iArr19;
            iArr15 = iArr18;
            zArr5 = zArr9;
        }
        boolean[][] zArr10 = zArr5;
        int[] iArr20 = iArr15;
        int[] iArr21 = iArr11;
        if (parsableNalUnitBitArray.readBit()) {
            int i46 = 0;
            while (i46 < bits) {
                int i47 = i46 + 1;
                for (int i48 = i47; i48 < i7; i48++) {
                    if (zArr4[i48][i46]) {
                        parsableNalUnitBitArray.skipBits(3);
                    }
                }
                i46 = i47;
            }
        }
        parsableNalUnitBitArray.skipBit();
        int unsignedExpGolombCodedInt3 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1;
        ImmutableList.Builder builder2 = ImmutableList.builder();
        builder2.add(h265ProfileTierLevel);
        if (unsignedExpGolombCodedInt3 > 1) {
            H265ProfileTierLevel h265ProfileTierLevel3 = h265ProfileTierLevel2;
            builder2.add(h265ProfileTierLevel3);
            for (int i49 = 2; i49 < unsignedExpGolombCodedInt3; i49++) {
                h265ProfileTierLevel3 = parseH265ProfileTierLevel(parsableNalUnitBitArray, parsableNalUnitBitArray.readBit(), i3, h265ProfileTierLevel3);
                builder2.add(h265ProfileTierLevel3);
            }
        }
        ImmutableList immutableListBuild2 = builder2.build();
        int unsignedExpGolombCodedInt4 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + unsignedExpGolombCodedInt;
        if (unsignedExpGolombCodedInt4 > unsignedExpGolombCodedInt) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        int bits5 = parsableNalUnitBitArray.readBits(2);
        boolean[][] zArr11 = (boolean[][]) Array.newInstance((Class<?>) Boolean.TYPE, unsignedExpGolombCodedInt4, i9);
        int[] iArr22 = new int[unsignedExpGolombCodedInt4];
        int[] iArr23 = new int[unsignedExpGolombCodedInt4];
        int i50 = 0;
        while (i50 < unsignedExpGolombCodedInt) {
            int i51 = unsignedExpGolombCodedInt;
            iArr22[i50] = 0;
            iArr23[i50] = iArr6[i50];
            if (bits5 == 0) {
                zArr2 = zArr4;
                iArr2 = iArr17;
                i6 = i7;
                Arrays.fill(zArr11[i50], 0, iArr5[i50], true);
                iArr22[i50] = iArr5[i50];
            } else {
                i6 = i7;
                zArr2 = zArr4;
                iArr2 = iArr17;
                if (bits5 == 1) {
                    int i52 = iArr6[i50];
                    for (int i53 = 0; i53 < iArr5[i50]; i53++) {
                        zArr11[i50][i53] = iArr[i50][i53] == i52;
                    }
                    iArr22[i50] = 1;
                } else {
                    zArr11[0][0] = true;
                    iArr22[0] = 1;
                }
                i50++;
                unsignedExpGolombCodedInt = i51;
                zArr4 = zArr2;
                iArr17 = iArr2;
                i7 = i6;
            }
            i50++;
            unsignedExpGolombCodedInt = i51;
            zArr4 = zArr2;
            iArr17 = iArr2;
            i7 = i6;
        }
        int i54 = i7;
        boolean[][] zArr12 = zArr4;
        int[] iArr24 = iArr17;
        int i55 = unsignedExpGolombCodedInt;
        int[] iArr25 = new int[i9];
        int i56 = 2;
        boolean[][] zArr13 = (boolean[][]) Array.newInstance((Class<?>) Boolean.TYPE, unsignedExpGolombCodedInt4, i9);
        int i57 = 1;
        int i58 = 0;
        while (i57 < unsignedExpGolombCodedInt4) {
            if (bits5 == i56) {
                for (int i59 = 0; i59 < iArr5[i57]; i59++) {
                    zArr11[i57][i59] = parsableNalUnitBitArray.readBit();
                    int i60 = iArr22[i57];
                    boolean z2 = zArr11[i57][i59];
                    iArr22[i57] = i60 + (z2 ? 1 : 0);
                    if (z2) {
                        iArr23[i57] = iArr[i57][i59];
                    }
                }
            }
            if (i58 == 0 && iArr[i57][0] == 0 && zArr11[i57][0]) {
                for (int i61 = 1; i61 < iArr5[i57]; i61++) {
                    if (iArr[i57][i61] == i2 && zArr11[i57][i2]) {
                        i58 = i57;
                    }
                }
            }
            int i62 = 0;
            while (i62 < iArr5[i57]) {
                if (unsignedExpGolombCodedInt3 > 1) {
                    zArr13[i57][i62] = zArr11[i57][i62];
                    zArr = zArr11;
                    i4 = unsignedExpGolombCodedInt3;
                    int iLog2 = DoubleMath.log2(unsignedExpGolombCodedInt3, RoundingMode.CEILING);
                    if (zArr13[i57][i62]) {
                        i5 = i2;
                        break;
                    }
                    int i63 = ((H265LayerInfo) immutableListBuild.get(iArr[i57][i62])).layerIdInVps;
                    int i64 = 0;
                    while (true) {
                        if (i64 >= i62) {
                            i5 = i2;
                            break;
                        }
                        i5 = i2;
                        if (zArr10[i63][((H265LayerInfo) immutableListBuild.get(iArr[i57][i64])).layerIdInVps]) {
                            zArr13[i57][i62] = true;
                            break;
                        }
                        i64++;
                        i2 = i5;
                    }
                    if (zArr13[i57][i62]) {
                        if (i58 > 0 && i57 == i58) {
                            iArr25[i62] = parsableNalUnitBitArray.readBits(iLog2);
                        } else {
                            parsableNalUnitBitArray.skipBits(iLog2);
                        }
                    }
                } else {
                    i4 = unsignedExpGolombCodedInt3;
                    i5 = i2;
                    zArr = zArr11;
                }
                i62++;
                zArr11 = zArr;
                unsignedExpGolombCodedInt3 = i4;
                i2 = i5;
            }
            int i65 = unsignedExpGolombCodedInt3;
            int i66 = i2;
            boolean[][] zArr14 = zArr11;
            if (iArr22[i57] == 1 && iArr20[iArr23[i57]] > 0) {
                parsableNalUnitBitArray.skipBit();
            }
            i57++;
            zArr11 = zArr14;
            unsignedExpGolombCodedInt3 = i65;
            i2 = i66;
            i56 = 2;
        }
        if (i58 == 0) {
            return new H265VpsData(h265NalHeader, null, h265ProfileTierLevelsAndIndices, null, null);
        }
        H265RepFormatsAndIndices h265RepFormatsAndIndices = parseH265RepFormatsAndIndices(parsableNalUnitBitArray, i54);
        parsableNalUnitBitArray.skipBits(2);
        for (int i67 = 1; i67 < i54; i67++) {
            if (iArr20[iArr21[i67]] == 0) {
                parsableNalUnitBitArray.skipBit();
            }
        }
        skipH265DpbSize(parsableNalUnitBitArray, unsignedExpGolombCodedInt4, iArr24, iArr5, zArr13);
        skipToH265VuiPresentFlagAfterDpbSize(parsableNalUnitBitArray, i54, zArr12);
        if (parsableNalUnitBitArray.readBit()) {
            parsableNalUnitBitArray.byteAlign();
            h265VideoSignalInfosAndIndices = parseH265VideoSignalInfosAndIndices(parsableNalUnitBitArray, i54, i55, iArr24);
        } else {
            h265VideoSignalInfosAndIndices = null;
        }
        return new H265VpsData(h265NalHeader, immutableListBuild, new H265ProfileTierLevelsAndIndices(immutableListBuild2, iArr25), h265RepFormatsAndIndices, h265VideoSignalInfosAndIndices);
    }

    public static H265SpsData parseH265SpsNalUnit(byte[] bArr, int i, int i2, H265VpsData h265VpsData) {
        return parseH265SpsNalUnitPayload(bArr, i + 2, i2, parseH265NalHeader(new ParsableNalUnitBitArray(bArr, i, i2)), h265VpsData);
    }

    /* JADX WARN: Code duplicated, block: B:35:0x00b3  */
    public static H265SpsData parseH265SpsNalUnitPayload(byte[] bArr, int i, int i2, H265NalHeader h265NalHeader, H265VpsData h265VpsData) {
        int i3;
        int unsignedExpGolombCodedInt;
        int i4;
        int i5;
        int i6;
        int i7;
        int iMax;
        float f;
        int i8;
        int i9;
        int i10;
        int i11;
        int iIsoColorPrimariesToColorSpace;
        int i12;
        int i13;
        int iIsoTransferCharacteristicsToColorTransfer;
        ParsableNalUnitBitArray parsableNalUnitBitArray = new ParsableNalUnitBitArray(bArr, i, i2);
        parsableNalUnitBitArray.skipBits(4);
        int bits = parsableNalUnitBitArray.readBits(3);
        boolean z = h265NalHeader.layerId != 0 && bits == 7;
        int i14 = (h265VpsData == null || h265VpsData.layerInfos.isEmpty()) ? 0 : h265VpsData.layerInfos.get(Math.min(h265NalHeader.layerId, h265VpsData.layerInfos.size() - 1)).layerIdInVps;
        H265ProfileTierLevel h265ProfileTierLevel = null;
        if (!z) {
            parsableNalUnitBitArray.skipBit();
            h265ProfileTierLevel = parseH265ProfileTierLevel(parsableNalUnitBitArray, true, bits, null);
        } else if (h265VpsData != null && h265VpsData.profileTierLevelsAndIndices.profileTierLevels.size() > (i3 = h265VpsData.profileTierLevelsAndIndices.indices[i14])) {
            h265ProfileTierLevel = h265VpsData.profileTierLevelsAndIndices.profileTierLevels.get(i3);
        }
        int unsignedExpGolombCodedInt2 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        if (z) {
            int bits2 = parsableNalUnitBitArray.readBit() ? parsableNalUnitBitArray.readBits(8) : -1;
            if (h265VpsData == null || h265VpsData.repFormatsAndIndices == null) {
                i6 = 0;
                i7 = 0;
                i5 = 0;
                unsignedExpGolombCodedInt = 0;
                i4 = 0;
            } else {
                if (bits2 == -1) {
                    bits2 = h265VpsData.repFormatsAndIndices.indices[i14];
                }
                if (bits2 == -1 || h265VpsData.repFormatsAndIndices.repFormats.size() <= bits2) {
                    i6 = 0;
                    i7 = 0;
                    i5 = 0;
                    unsignedExpGolombCodedInt = 0;
                    i4 = 0;
                } else {
                    H265RepFormat h265RepFormat = h265VpsData.repFormatsAndIndices.repFormats.get(bits2);
                    int i15 = h265RepFormat.chromaFormatIdc;
                    i4 = h265RepFormat.width;
                    i7 = h265RepFormat.height;
                    i5 = h265RepFormat.bitDepthLumaMinus8;
                    unsignedExpGolombCodedInt = h265RepFormat.bitDepthChromaMinus8;
                    i6 = i15;
                }
            }
        } else {
            int unsignedExpGolombCodedInt3 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            if (unsignedExpGolombCodedInt3 == 3) {
                parsableNalUnitBitArray.skipBit();
            }
            int unsignedExpGolombCodedInt4 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            int unsignedExpGolombCodedInt5 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            if (parsableNalUnitBitArray.readBit()) {
                int unsignedExpGolombCodedInt6 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                int unsignedExpGolombCodedInt7 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                int unsignedExpGolombCodedInt8 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                int unsignedExpGolombCodedInt9 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                unsignedExpGolombCodedInt4 = applyConformanceWindowToWidth(unsignedExpGolombCodedInt4, unsignedExpGolombCodedInt3, unsignedExpGolombCodedInt6, unsignedExpGolombCodedInt7);
                unsignedExpGolombCodedInt5 = applyConformanceWindowToHeight(unsignedExpGolombCodedInt5, unsignedExpGolombCodedInt3, unsignedExpGolombCodedInt8, unsignedExpGolombCodedInt9);
            }
            int unsignedExpGolombCodedInt10 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            i4 = unsignedExpGolombCodedInt4;
            i5 = unsignedExpGolombCodedInt10;
            i6 = unsignedExpGolombCodedInt3;
            i7 = unsignedExpGolombCodedInt5;
        }
        int unsignedExpGolombCodedInt11 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        if (z) {
            iMax = -1;
        } else {
            iMax = -1;
            for (int i16 = parsableNalUnitBitArray.readBit() ? 0 : bits; i16 <= bits; i16++) {
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                iMax = Math.max(parsableNalUnitBitArray.readUnsignedExpGolombCodedInt(), iMax);
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            }
        }
        parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        if (parsableNalUnitBitArray.readBit()) {
            if (z ? parsableNalUnitBitArray.readBit() : false) {
                parsableNalUnitBitArray.skipBits(6);
            } else if (parsableNalUnitBitArray.readBit()) {
                skipH265ScalingList(parsableNalUnitBitArray);
            }
        }
        parsableNalUnitBitArray.skipBits(2);
        if (parsableNalUnitBitArray.readBit()) {
            parsableNalUnitBitArray.skipBits(8);
            parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            parsableNalUnitBitArray.skipBit();
        }
        skipH265ShortTermReferencePictureSets(parsableNalUnitBitArray);
        if (parsableNalUnitBitArray.readBit()) {
            int unsignedExpGolombCodedInt12 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            for (int i17 = 0; i17 < unsignedExpGolombCodedInt12; i17++) {
                parsableNalUnitBitArray.skipBits(unsignedExpGolombCodedInt11 + 5);
            }
        }
        parsableNalUnitBitArray.skipBits(2);
        float f2 = 1.0f;
        if (parsableNalUnitBitArray.readBit()) {
            if (parsableNalUnitBitArray.readBit()) {
                int bits3 = parsableNalUnitBitArray.readBits(8);
                if (bits3 == 255) {
                    int bits4 = parsableNalUnitBitArray.readBits(16);
                    int bits5 = parsableNalUnitBitArray.readBits(16);
                    if (bits4 != 0 && bits5 != 0) {
                        f2 = bits4 / bits5;
                    }
                } else {
                    float[] fArr = ASPECT_RATIO_IDC_VALUES;
                    if (bits3 < fArr.length) {
                        f2 = fArr[bits3];
                    } else {
                        Log.w(TAG, "Unexpected aspect_ratio_idc value: " + bits3);
                    }
                }
            }
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.skipBit();
            }
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.skipBits(3);
                i12 = parsableNalUnitBitArray.readBit() ? 1 : 2;
                if (parsableNalUnitBitArray.readBit()) {
                    int bits6 = parsableNalUnitBitArray.readBits(8);
                    int bits7 = parsableNalUnitBitArray.readBits(8);
                    parsableNalUnitBitArray.skipBits(8);
                    iIsoColorPrimariesToColorSpace = ColorInfo.isoColorPrimariesToColorSpace(bits6);
                    iIsoTransferCharacteristicsToColorTransfer = ColorInfo.isoTransferCharacteristicsToColorTransfer(bits7);
                } else {
                    iIsoColorPrimariesToColorSpace = -1;
                    iIsoTransferCharacteristicsToColorTransfer = -1;
                }
            } else if (h265VpsData == null || h265VpsData.videoSignalInfosAndIndices == null || h265VpsData.videoSignalInfosAndIndices.videoSignalInfos.size() <= (i13 = h265VpsData.videoSignalInfosAndIndices.indices[i14])) {
                iIsoColorPrimariesToColorSpace = -1;
                i12 = -1;
                iIsoTransferCharacteristicsToColorTransfer = -1;
            } else {
                H265VideoSignalInfo h265VideoSignalInfo = h265VpsData.videoSignalInfosAndIndices.videoSignalInfos.get(i13);
                int i18 = h265VideoSignalInfo.colorSpace;
                int i19 = h265VideoSignalInfo.colorRange;
                iIsoTransferCharacteristicsToColorTransfer = h265VideoSignalInfo.colorTransfer;
                iIsoColorPrimariesToColorSpace = i18;
                i12 = i19;
            }
            if (parsableNalUnitBitArray.readBit()) {
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            }
            parsableNalUnitBitArray.skipBit();
            if (parsableNalUnitBitArray.readBit()) {
                i7 *= 2;
            }
            i9 = iIsoColorPrimariesToColorSpace;
            i10 = i12;
            i11 = iIsoTransferCharacteristicsToColorTransfer;
            f = f2;
            i8 = i7;
        } else {
            f = 1.0f;
            i8 = i7;
            i9 = -1;
            i10 = -1;
            i11 = -1;
        }
        return new H265SpsData(h265NalHeader, h265ProfileTierLevel, i6, i5, unsignedExpGolombCodedInt, unsignedExpGolombCodedInt2, i4, i8, f, iMax, i9, i10, i11);
    }

    public static PpsData parsePpsNalUnit(byte[] bArr, int i, int i2) {
        return parsePpsNalUnitPayload(bArr, i + 1, i2);
    }

    public static PpsData parsePpsNalUnitPayload(byte[] bArr, int i, int i2) {
        ParsableNalUnitBitArray parsableNalUnitBitArray = new ParsableNalUnitBitArray(bArr, i, i2);
        int unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        int unsignedExpGolombCodedInt2 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        parsableNalUnitBitArray.skipBit();
        return new PpsData(unsignedExpGolombCodedInt, unsignedExpGolombCodedInt2, parsableNalUnitBitArray.readBit());
    }

    public static H265Sei3dRefDisplayInfoData parseH265Sei3dRefDisplayInfo(byte[] bArr, int i, int i2) {
        byte b;
        int iMax;
        int iMax2;
        int i3 = i + 2;
        do {
            i2--;
            b = bArr[i2];
            if (b != 0) {
                break;
            }
        } while (i2 > i3);
        if (b != 0 && i2 > i3) {
            ParsableNalUnitBitArray parsableNalUnitBitArray = new ParsableNalUnitBitArray(bArr, i3, i2 + 1);
            while (parsableNalUnitBitArray.canReadBits(16)) {
                int bits = parsableNalUnitBitArray.readBits(8);
                int i4 = 0;
                while (bits == 255) {
                    i4 += 255;
                    bits = parsableNalUnitBitArray.readBits(8);
                }
                int i5 = i4 + bits;
                int bits2 = parsableNalUnitBitArray.readBits(8);
                int i6 = 0;
                while (bits2 == 255) {
                    i6 += 255;
                    bits2 = parsableNalUnitBitArray.readBits(8);
                }
                int i7 = i6 + bits2;
                if (i7 == 0 || !parsableNalUnitBitArray.canReadBits(i7)) {
                    break;
                }
                if (i5 == 176) {
                    int unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    boolean bit = parsableNalUnitBitArray.readBit();
                    int unsignedExpGolombCodedInt2 = bit ? parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() : 0;
                    int unsignedExpGolombCodedInt3 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    int unsignedExpGolombCodedInt4 = -1;
                    int unsignedExpGolombCodedInt5 = -1;
                    int bits3 = -1;
                    int bits4 = -1;
                    int bits5 = -1;
                    int bits6 = -1;
                    for (int i8 = 0; i8 <= unsignedExpGolombCodedInt3; i8++) {
                        unsignedExpGolombCodedInt4 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                        unsignedExpGolombCodedInt5 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                        bits3 = parsableNalUnitBitArray.readBits(6);
                        if (bits3 == 63) {
                            return null;
                        }
                        if (bits3 == 0) {
                            iMax = Math.max(0, unsignedExpGolombCodedInt - 30);
                        } else {
                            iMax = Math.max(0, (bits3 + unsignedExpGolombCodedInt) - 31);
                        }
                        bits4 = parsableNalUnitBitArray.readBits(iMax);
                        if (bit) {
                            bits5 = parsableNalUnitBitArray.readBits(6);
                            if (bits5 == 63) {
                                return null;
                            }
                            if (bits5 == 0) {
                                iMax2 = Math.max(0, unsignedExpGolombCodedInt2 - 30);
                            } else {
                                iMax2 = Math.max(0, (bits5 + unsignedExpGolombCodedInt2) - 31);
                            }
                            bits6 = parsableNalUnitBitArray.readBits(iMax2);
                        }
                        if (parsableNalUnitBitArray.readBit()) {
                            parsableNalUnitBitArray.skipBits(10);
                        }
                    }
                    return new H265Sei3dRefDisplayInfoData(unsignedExpGolombCodedInt, unsignedExpGolombCodedInt2, unsignedExpGolombCodedInt3 + 1, unsignedExpGolombCodedInt4, unsignedExpGolombCodedInt5, bits3, bits4, bits5, bits6);
                }
            }
        }
        return null;
    }

    public static int findNalUnit(byte[] bArr, int i, int i2, boolean[] zArr) {
        int i3 = i2 - i;
        Assertions.checkState(i3 >= 0);
        if (i3 == 0) {
            return i2;
        }
        if (zArr[0]) {
            clearPrefixFlags(zArr);
            return i - 3;
        }
        if (i3 > 1 && zArr[1] && bArr[i] == 1) {
            clearPrefixFlags(zArr);
            return i - 2;
        }
        if (i3 > 2 && zArr[2] && bArr[i] == 0 && bArr[i + 1] == 1) {
            clearPrefixFlags(zArr);
            return i - 1;
        }
        int i4 = i2 - 1;
        int i5 = i + 2;
        while (i5 < i4) {
            byte b = bArr[i5];
            if ((b & 254) == 0) {
                int i6 = i5 - 2;
                if (bArr[i6] == 0 && bArr[i5 - 1] == 0 && b == 1) {
                    clearPrefixFlags(zArr);
                    return i6;
                }
                i5 -= 2;
            }
            i5 += 3;
        }
        zArr[0] = i3 <= 2 ? !(i3 != 2 ? !(zArr[1] && bArr[i4] == 1) : !(zArr[2] && bArr[i2 + (-2)] == 0 && bArr[i4] == 1)) : bArr[i2 + (-3)] == 0 && bArr[i2 + (-2)] == 0 && bArr[i4] == 1;
        zArr[1] = i3 <= 1 ? zArr[2] && bArr[i4] == 0 : bArr[i2 + (-2)] == 0 && bArr[i4] == 0;
        zArr[2] = bArr[i4] == 0;
        return i2;
    }

    public static void clearPrefixFlags(boolean[] zArr) {
        zArr[0] = false;
        zArr[1] = false;
        zArr[2] = false;
    }

    public static String getH265BaseLayerCodecsString(List<byte[]> list) {
        for (int i = 0; i < list.size(); i++) {
            byte[] bArr = list.get(i);
            int length = bArr.length;
            if (length > 3) {
                ImmutableList<Integer> immutableListFindNalUnitPositions = findNalUnitPositions(bArr);
                for (int i2 = 0; i2 < immutableListFindNalUnitPositions.size(); i2++) {
                    if (immutableListFindNalUnitPositions.get(i2).intValue() + 3 < length) {
                        ParsableNalUnitBitArray parsableNalUnitBitArray = new ParsableNalUnitBitArray(bArr, immutableListFindNalUnitPositions.get(i2).intValue() + 3, length);
                        H265NalHeader h265NalHeader = parseH265NalHeader(parsableNalUnitBitArray);
                        if (h265NalHeader.nalUnitType == 33 && h265NalHeader.layerId == 0) {
                            return createCodecStringFromH265SpsPalyoad(parsableNalUnitBitArray);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static ImmutableList<Integer> findNalUnitPositions(byte[] bArr) {
        boolean[] zArr = new boolean[3];
        ImmutableList.Builder builder = ImmutableList.builder();
        int i = 0;
        while (i < bArr.length) {
            int iFindNalUnit = findNalUnit(bArr, i, bArr.length, zArr);
            if (iFindNalUnit != bArr.length) {
                builder.add(Integer.valueOf(iFindNalUnit));
            }
            i = iFindNalUnit + 3;
        }
        return builder.build();
    }

    private static String createCodecStringFromH265SpsPalyoad(ParsableNalUnitBitArray parsableNalUnitBitArray) {
        parsableNalUnitBitArray.skipBits(4);
        int bits = parsableNalUnitBitArray.readBits(3);
        parsableNalUnitBitArray.skipBit();
        H265ProfileTierLevel h265ProfileTierLevel = parseH265ProfileTierLevel(parsableNalUnitBitArray, true, bits, null);
        return CodecSpecificDataUtil.buildHevcCodecString(h265ProfileTierLevel.generalProfileSpace, h265ProfileTierLevel.generalTierFlag, h265ProfileTierLevel.generalProfileIdc, h265ProfileTierLevel.generalProfileCompatibilityFlags, h265ProfileTierLevel.constraintBytes, h265ProfileTierLevel.generalLevelIdc);
    }

    private static int findNextUnescapeIndex(byte[] bArr, int i, int i2) {
        while (i < i2 - 2) {
            if (bArr[i] == 0 && bArr[i + 1] == 0 && bArr[i + 2] == 3) {
                return i;
            }
            i++;
        }
        return i2;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0 */
    /* JADX WARN: Type inference failed for: r1v1, types: [int] */
    /* JADX WARN: Type inference failed for: r1v3 */
    /* JADX WARN: Type inference failed for: r1v4 */
    /* JADX WARN: Type inference failed for: r9v1 */
    /* JADX WARN: Type inference failed for: r9v2, types: [int] */
    /* JADX WARN: Type inference failed for: r9v4 */
    /* JADX WARN: Type inference failed for: r9v5 */
    private static void skipH265HrdParameters(ParsableNalUnitBitArray parsableNalUnitBitArray, boolean z, int i) {
        ?? r9;
        ?? r1;
        boolean bit;
        boolean bit2;
        if (z) {
            boolean bit3 = parsableNalUnitBitArray.readBit();
            boolean bit4 = parsableNalUnitBitArray.readBit();
            if (bit3 || bit4) {
                bit = parsableNalUnitBitArray.readBit();
                if (bit) {
                    parsableNalUnitBitArray.skipBits(19);
                }
                parsableNalUnitBitArray.skipBits(8);
                if (bit) {
                    parsableNalUnitBitArray.skipBits(4);
                }
                parsableNalUnitBitArray.skipBits(15);
                r1 = bit4;
                r9 = bit3;
            } else {
                bit = false;
                r1 = bit4;
                r9 = bit3;
            }
        } else {
            r9 = 0;
            r1 = 0;
            bit = false;
        }
        for (int i2 = 0; i2 <= i; i2++) {
            boolean bit5 = parsableNalUnitBitArray.readBit();
            if (!bit5) {
                bit5 = parsableNalUnitBitArray.readBit();
            }
            if (bit5) {
                parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                bit2 = false;
            } else {
                bit2 = parsableNalUnitBitArray.readBit();
            }
            int unsignedExpGolombCodedInt = !bit2 ? parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() : 0;
            int i3 = r9 + r1;
            for (int i4 = 0; i4 < i3; i4++) {
                for (int i5 = 0; i5 <= unsignedExpGolombCodedInt; i5++) {
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    if (bit) {
                        parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                        parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    }
                    parsableNalUnitBitArray.skipBit();
                }
            }
        }
    }

    /* JADX WARN: Code duplicated, block: B:21:0x005e  */
    /* JADX WARN: Code duplicated, block: B:23:0x0064  */
    /* JADX WARN: Code duplicated, block: B:26:0x006c  */
    /* JADX WARN: Code duplicated, block: B:30:0x0076  */
    /* JADX WARN: Code duplicated, block: B:39:0x006e A[SYNTHETIC] */
    private static H265ProfileTierLevel parseH265ProfileTierLevel(ParsableNalUnitBitArray parsableNalUnitBitArray, boolean z, int i, H265ProfileTierLevel h265ProfileTierLevel) {
        int[] iArr;
        int i2;
        boolean z2;
        int i3;
        int i4;
        boolean bit;
        int bits;
        int i5;
        int i6;
        int[] iArr2 = new int[6];
        if (z) {
            int bits2 = parsableNalUnitBitArray.readBits(2);
            bit = parsableNalUnitBitArray.readBit();
            bits = parsableNalUnitBitArray.readBits(5);
            i5 = 0;
            for (int i7 = 0; i7 < 32; i7++) {
                if (parsableNalUnitBitArray.readBit()) {
                    i5 |= 1 << i7;
                }
            }
            for (int i8 = 0; i8 < 6; i8++) {
                iArr2[i8] = parsableNalUnitBitArray.readBits(8);
            }
            i2 = bits2;
        } else {
            if (h265ProfileTierLevel != null) {
                int i9 = h265ProfileTierLevel.generalProfileSpace;
                bit = h265ProfileTierLevel.generalTierFlag;
                bits = h265ProfileTierLevel.generalProfileIdc;
                i5 = h265ProfileTierLevel.generalProfileCompatibilityFlags;
                iArr2 = h265ProfileTierLevel.constraintBytes;
                i2 = i9;
            } else {
                iArr = iArr2;
                i2 = 0;
                z2 = false;
                i3 = 0;
                i4 = 0;
            }
            int bits3 = parsableNalUnitBitArray.readBits(8);
            i6 = 0;
            for (int i10 = 0; i10 < i; i10++) {
                if (parsableNalUnitBitArray.readBit()) {
                    i6 += 88;
                }
                if (parsableNalUnitBitArray.readBit()) {
                    i6 += 8;
                }
            }
            parsableNalUnitBitArray.skipBits(i6);
            if (i > 0) {
                parsableNalUnitBitArray.skipBits((8 - i) * 2);
            }
            return new H265ProfileTierLevel(i2, z2, i3, i4, iArr, bits3);
        }
        iArr = iArr2;
        z2 = bit;
        i3 = bits;
        i4 = i5;
        int bits4 = parsableNalUnitBitArray.readBits(8);
        i6 = 0;
        while (i10 < i) {
            if (parsableNalUnitBitArray.readBit()) {
                i6 += 88;
            }
            if (parsableNalUnitBitArray.readBit()) {
                i6 += 8;
            }
        }
        parsableNalUnitBitArray.skipBits(i6);
        if (i > 0) {
            parsableNalUnitBitArray.skipBits((8 - i) * 2);
        }
        return new H265ProfileTierLevel(i2, z2, i3, i4, iArr, bits4);
    }

    private static H265RepFormatsAndIndices parseH265RepFormatsAndIndices(ParsableNalUnitBitArray parsableNalUnitBitArray, int i) {
        int unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        int i2 = unsignedExpGolombCodedInt + 1;
        ImmutableList.Builder builderBuilderWithExpectedSize = ImmutableList.builderWithExpectedSize(i2);
        int[] iArr = new int[i];
        for (int i3 = 0; i3 < i2; i3++) {
            builderBuilderWithExpectedSize.add(parseH265RepFormat(parsableNalUnitBitArray));
        }
        int i4 = 1;
        if (i2 <= 1 || !parsableNalUnitBitArray.readBit()) {
            while (i4 < i) {
                iArr[i4] = Math.min(i4, unsignedExpGolombCodedInt);
                i4++;
            }
        } else {
            int iLog2 = DoubleMath.log2(i2, RoundingMode.CEILING);
            while (i4 < i) {
                iArr[i4] = parsableNalUnitBitArray.readBits(iLog2);
                i4++;
            }
        }
        return new H265RepFormatsAndIndices(builderBuilderWithExpectedSize.build(), iArr);
    }

    private static H265RepFormat parseH265RepFormat(ParsableNalUnitBitArray parsableNalUnitBitArray) {
        int i;
        int i2;
        int bits;
        int bits2 = parsableNalUnitBitArray.readBits(16);
        int bits3 = parsableNalUnitBitArray.readBits(16);
        if (parsableNalUnitBitArray.readBit()) {
            int bits4 = parsableNalUnitBitArray.readBits(2);
            if (bits4 == 3) {
                parsableNalUnitBitArray.skipBit();
            }
            int bits5 = parsableNalUnitBitArray.readBits(4);
            bits = parsableNalUnitBitArray.readBits(4);
            i2 = bits5;
            i = bits4;
        } else {
            i = 0;
            i2 = 0;
            bits = 0;
        }
        if (parsableNalUnitBitArray.readBit()) {
            int unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            int unsignedExpGolombCodedInt2 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            int unsignedExpGolombCodedInt3 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            int unsignedExpGolombCodedInt4 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            bits2 = applyConformanceWindowToWidth(bits2, i, unsignedExpGolombCodedInt, unsignedExpGolombCodedInt2);
            bits3 = applyConformanceWindowToHeight(bits3, i, unsignedExpGolombCodedInt3, unsignedExpGolombCodedInt4);
        }
        return new H265RepFormat(i, i2, bits, bits2, bits3);
    }

    private static void skipH265DpbSize(ParsableNalUnitBitArray parsableNalUnitBitArray, int i, int[] iArr, int[] iArr2, boolean[][] zArr) {
        boolean bit;
        for (int i2 = 1; i2 < i; i2++) {
            boolean bit2 = parsableNalUnitBitArray.readBit();
            int i3 = 0;
            while (i3 < iArr[i2]) {
                if (i3 <= 0 || !bit2) {
                    bit = i3 == 0;
                } else {
                    bit = parsableNalUnitBitArray.readBit();
                }
                if (bit) {
                    for (int i4 = 0; i4 < iArr2[i2]; i4++) {
                        if (zArr[i2][i4]) {
                            parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                        }
                    }
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                }
                i3++;
            }
        }
    }

    private static void skipToH265VuiPresentFlagAfterDpbSize(ParsableNalUnitBitArray parsableNalUnitBitArray, int i, boolean[][] zArr) {
        int unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 2;
        if (parsableNalUnitBitArray.readBit()) {
            parsableNalUnitBitArray.skipBits(unsignedExpGolombCodedInt);
        } else {
            for (int i2 = 1; i2 < i; i2++) {
                for (int i3 = 0; i3 < i2; i3++) {
                    if (zArr[i2][i3]) {
                        parsableNalUnitBitArray.skipBits(unsignedExpGolombCodedInt);
                    }
                }
            }
        }
        int unsignedExpGolombCodedInt2 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        for (int i4 = 1; i4 <= unsignedExpGolombCodedInt2; i4++) {
            parsableNalUnitBitArray.skipBits(8);
        }
    }

    private static H265VideoSignalInfosAndIndices parseH265VideoSignalInfosAndIndices(ParsableNalUnitBitArray parsableNalUnitBitArray, int i, int i2, int[] iArr) {
        if (!parsableNalUnitBitArray.readBit() ? parsableNalUnitBitArray.readBit() : true) {
            parsableNalUnitBitArray.skipBit();
        }
        boolean bit = parsableNalUnitBitArray.readBit();
        boolean bit2 = parsableNalUnitBitArray.readBit();
        if (bit || bit2) {
            for (int i3 = 0; i3 < i2; i3++) {
                for (int i4 = 0; i4 < iArr[i3]; i4++) {
                    boolean bit3 = bit ? parsableNalUnitBitArray.readBit() : false;
                    boolean bit4 = bit2 ? parsableNalUnitBitArray.readBit() : false;
                    if (bit3) {
                        parsableNalUnitBitArray.skipBits(32);
                    }
                    if (bit4) {
                        parsableNalUnitBitArray.skipBits(18);
                    }
                }
            }
        }
        boolean bit5 = parsableNalUnitBitArray.readBit();
        int bits = bit5 ? parsableNalUnitBitArray.readBits(4) + 1 : i;
        ImmutableList.Builder builderBuilderWithExpectedSize = ImmutableList.builderWithExpectedSize(bits);
        int[] iArr2 = new int[i];
        for (int i5 = 0; i5 < bits; i5++) {
            builderBuilderWithExpectedSize.add(parseH265VideoSignalInfo(parsableNalUnitBitArray));
        }
        if (bit5 && bits > 1) {
            for (int i6 = 0; i6 < i; i6++) {
                iArr2[i6] = parsableNalUnitBitArray.readBits(4);
            }
        }
        return new H265VideoSignalInfosAndIndices(builderBuilderWithExpectedSize.build(), iArr2);
    }

    private static H265VideoSignalInfo parseH265VideoSignalInfo(ParsableNalUnitBitArray parsableNalUnitBitArray) {
        parsableNalUnitBitArray.skipBits(3);
        int i = parsableNalUnitBitArray.readBit() ? 1 : 2;
        int iIsoColorPrimariesToColorSpace = ColorInfo.isoColorPrimariesToColorSpace(parsableNalUnitBitArray.readBits(8));
        int iIsoTransferCharacteristicsToColorTransfer = ColorInfo.isoTransferCharacteristicsToColorTransfer(parsableNalUnitBitArray.readBits(8));
        parsableNalUnitBitArray.skipBits(8);
        return new H265VideoSignalInfo(iIsoColorPrimariesToColorSpace, i, iIsoTransferCharacteristicsToColorTransfer);
    }

    private static void skipScalingList(ParsableNalUnitBitArray parsableNalUnitBitArray, int i) {
        int signedExpGolombCodedInt = 8;
        int i2 = 8;
        for (int i3 = 0; i3 < i; i3++) {
            if (signedExpGolombCodedInt != 0) {
                signedExpGolombCodedInt = ((parsableNalUnitBitArray.readSignedExpGolombCodedInt() + i2) + 256) % 256;
            }
            if (signedExpGolombCodedInt != 0) {
                i2 = signedExpGolombCodedInt;
            }
        }
    }

    private static void skipHrdParameters(ParsableNalUnitBitArray parsableNalUnitBitArray) {
        int unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1;
        parsableNalUnitBitArray.skipBits(8);
        for (int i = 0; i < unsignedExpGolombCodedInt; i++) {
            parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
            parsableNalUnitBitArray.skipBit();
        }
        parsableNalUnitBitArray.skipBits(20);
    }

    private static void skipH265ScalingList(ParsableNalUnitBitArray parsableNalUnitBitArray) {
        for (int i = 0; i < 4; i++) {
            int i2 = 0;
            while (i2 < 6) {
                int i3 = 1;
                if (!parsableNalUnitBitArray.readBit()) {
                    parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                } else {
                    int iMin = Math.min(64, 1 << ((i << 1) + 4));
                    if (i > 1) {
                        parsableNalUnitBitArray.readSignedExpGolombCodedInt();
                    }
                    for (int i4 = 0; i4 < iMin; i4++) {
                        parsableNalUnitBitArray.readSignedExpGolombCodedInt();
                    }
                }
                if (i == 3) {
                    i3 = 3;
                }
                i2 += i3;
            }
        }
    }

    private static void skipH265ShortTermReferencePictureSets(ParsableNalUnitBitArray parsableNalUnitBitArray) {
        int unsignedExpGolombCodedInt = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
        int[] iArr = new int[0];
        int[] iArrCopyOf = new int[0];
        int i = -1;
        int i2 = -1;
        for (int i3 = 0; i3 < unsignedExpGolombCodedInt; i3++) {
            if (i3 != 0 && parsableNalUnitBitArray.readBit()) {
                int i4 = i + i2;
                int unsignedExpGolombCodedInt2 = (1 - ((parsableNalUnitBitArray.readBit() ? 1 : 0) * 2)) * (parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1);
                int i5 = i4 + 1;
                boolean[] zArr = new boolean[i5];
                for (int i6 = 0; i6 <= i4; i6++) {
                    if (!parsableNalUnitBitArray.readBit()) {
                        zArr[i6] = parsableNalUnitBitArray.readBit();
                    } else {
                        zArr[i6] = true;
                    }
                }
                int[] iArr2 = new int[i5];
                int[] iArr3 = new int[i5];
                int i7 = 0;
                for (int i8 = i2 - 1; i8 >= 0; i8--) {
                    int i9 = iArrCopyOf[i8] + unsignedExpGolombCodedInt2;
                    if (i9 < 0 && zArr[i + i8]) {
                        iArr2[i7] = i9;
                        i7++;
                    }
                }
                if (unsignedExpGolombCodedInt2 < 0 && zArr[i4]) {
                    iArr2[i7] = unsignedExpGolombCodedInt2;
                    i7++;
                }
                for (int i10 = 0; i10 < i; i10++) {
                    int i11 = iArr[i10] + unsignedExpGolombCodedInt2;
                    if (i11 < 0 && zArr[i10]) {
                        iArr2[i7] = i11;
                        i7++;
                    }
                }
                int[] iArrCopyOf2 = Arrays.copyOf(iArr2, i7);
                int i12 = 0;
                for (int i13 = i - 1; i13 >= 0; i13--) {
                    int i14 = iArr[i13] + unsignedExpGolombCodedInt2;
                    if (i14 > 0 && zArr[i13]) {
                        iArr3[i12] = i14;
                        i12++;
                    }
                }
                if (unsignedExpGolombCodedInt2 > 0 && zArr[i4]) {
                    iArr3[i12] = unsignedExpGolombCodedInt2;
                    i12++;
                }
                for (int i15 = 0; i15 < i2; i15++) {
                    int i16 = iArrCopyOf[i15] + unsignedExpGolombCodedInt2;
                    if (i16 > 0 && zArr[i + i15]) {
                        iArr3[i12] = i16;
                        i12++;
                    }
                }
                iArrCopyOf = Arrays.copyOf(iArr3, i12);
                iArr = iArrCopyOf2;
                i = i7;
                i2 = i12;
            } else {
                int unsignedExpGolombCodedInt3 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                int unsignedExpGolombCodedInt4 = parsableNalUnitBitArray.readUnsignedExpGolombCodedInt();
                int[] iArr4 = new int[unsignedExpGolombCodedInt3];
                int i17 = 0;
                while (i17 < unsignedExpGolombCodedInt3) {
                    iArr4[i17] = (i17 > 0 ? iArr4[i17 - 1] : 0) - (parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1);
                    parsableNalUnitBitArray.skipBit();
                    i17++;
                }
                int[] iArr5 = new int[unsignedExpGolombCodedInt4];
                int i18 = 0;
                while (i18 < unsignedExpGolombCodedInt4) {
                    iArr5[i18] = (i18 > 0 ? iArr5[i18 - 1] : 0) + parsableNalUnitBitArray.readUnsignedExpGolombCodedInt() + 1;
                    parsableNalUnitBitArray.skipBit();
                    i18++;
                }
                i = unsignedExpGolombCodedInt3;
                iArr = iArr4;
                i2 = unsignedExpGolombCodedInt4;
                iArrCopyOf = iArr5;
            }
        }
    }

    private NalUnitUtil() {
    }
}
