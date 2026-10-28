package androidx.media3.common.audio;

import androidx.media3.common.C;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.LongArray;
import androidx.media3.common.util.LongArrayQueue;
import androidx.media3.common.util.SpeedProviderUtil;
import androidx.media3.common.util.TimestampConsumer;
import androidx.media3.common.util.Util;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

/* JADX INFO: loaded from: classes.dex */
public final class SpeedChangingAudioProcessor extends BaseAudioProcessor {
    private long bytesRead;
    private float currentSpeed;
    private boolean endOfStreamQueuedToSonic;
    private LongArray inputSegmentStartTimesUs;
    private long lastProcessedInputTimeUs;
    private long lastSpeedAdjustedInputTimeUs;
    private long lastSpeedAdjustedOutputTimeUs;
    private final Object lock;
    private LongArray outputSegmentStartTimesUs;
    private final LongArrayQueue pendingCallbackInputTimesUs;
    private final Queue<TimestampConsumer> pendingCallbacks;
    private final SynchronizedSonicAudioProcessor sonicAudioProcessor;
    private long speedAdjustedTimeAsyncInputTimeUs;
    private final SpeedProvider speedProvider;

    private static double divide(long j, long j2) {
        return j / j2;
    }

    public SpeedChangingAudioProcessor(SpeedProvider speedProvider) {
        this.speedProvider = speedProvider;
        Object obj = new Object();
        this.lock = obj;
        this.sonicAudioProcessor = new SynchronizedSonicAudioProcessor(obj);
        this.pendingCallbackInputTimesUs = new LongArrayQueue();
        this.pendingCallbacks = new ArrayDeque();
        this.speedAdjustedTimeAsyncInputTimeUs = C.TIME_UNSET;
        resetState();
    }

    @Override // androidx.media3.common.audio.BaseAudioProcessor, androidx.media3.common.audio.AudioProcessor
    public long getDurationAfterProcessorApplied(long j) {
        return SpeedProviderUtil.getDurationAfterSpeedProviderApplied(this.speedProvider, j);
    }

    @Override // androidx.media3.common.audio.BaseAudioProcessor
    public AudioProcessor.AudioFormat onConfigure(AudioProcessor.AudioFormat audioFormat) throws AudioProcessor.UnhandledAudioFormatException {
        return this.sonicAudioProcessor.configure(audioFormat);
    }

    @Override // androidx.media3.common.audio.AudioProcessor
    public void queueInput(ByteBuffer byteBuffer) {
        int iScaleLargeTimestamp;
        long jScaleLargeTimestamp = Util.scaleLargeTimestamp(this.bytesRead, 1000000L, ((long) this.inputAudioFormat.bytesPerFrame) * ((long) this.inputAudioFormat.sampleRate));
        float speed = this.speedProvider.getSpeed(jScaleLargeTimestamp);
        long nextSpeedChangeTimeUs = this.speedProvider.getNextSpeedChangeTimeUs(jScaleLargeTimestamp);
        if (getSampleRateAlignedTimestamp(nextSpeedChangeTimeUs, this.inputAudioFormat.sampleRate) == jScaleLargeTimestamp) {
            long jSampleCountToDurationUs = Util.sampleCountToDurationUs(1L, this.inputAudioFormat.sampleRate) + jScaleLargeTimestamp;
            float speed2 = this.speedProvider.getSpeed(jSampleCountToDurationUs);
            long nextSpeedChangeTimeUs2 = this.speedProvider.getNextSpeedChangeTimeUs(jSampleCountToDurationUs);
            speed = speed2;
            nextSpeedChangeTimeUs = nextSpeedChangeTimeUs2;
        }
        updateSpeed(speed, jScaleLargeTimestamp);
        int iLimit = byteBuffer.limit();
        if (nextSpeedChangeTimeUs != C.TIME_UNSET) {
            iScaleLargeTimestamp = (int) Util.scaleLargeTimestamp(nextSpeedChangeTimeUs - jScaleLargeTimestamp, ((long) this.inputAudioFormat.sampleRate) * ((long) this.inputAudioFormat.bytesPerFrame), 1000000L);
            int i = this.inputAudioFormat.bytesPerFrame - (iScaleLargeTimestamp % this.inputAudioFormat.bytesPerFrame);
            if (i != this.inputAudioFormat.bytesPerFrame) {
                iScaleLargeTimestamp += i;
            }
            byteBuffer.limit(Math.min(iLimit, byteBuffer.position() + iScaleLargeTimestamp));
        } else {
            iScaleLargeTimestamp = -1;
        }
        long jPosition = byteBuffer.position();
        if (isUsingSonic()) {
            this.sonicAudioProcessor.queueInput(byteBuffer);
            if (iScaleLargeTimestamp != -1 && ((long) byteBuffer.position()) - jPosition == iScaleLargeTimestamp) {
                this.sonicAudioProcessor.queueEndOfStream();
                this.endOfStreamQueuedToSonic = true;
            }
        } else {
            ByteBuffer byteBufferReplaceOutputBuffer = replaceOutputBuffer(byteBuffer.remaining());
            if (byteBuffer.hasRemaining()) {
                byteBufferReplaceOutputBuffer.put(byteBuffer);
            }
            byteBufferReplaceOutputBuffer.flip();
        }
        this.bytesRead += ((long) byteBuffer.position()) - jPosition;
        updateLastProcessedInputTime();
        byteBuffer.limit(iLimit);
    }

    @Override // androidx.media3.common.audio.BaseAudioProcessor
    protected void onQueueEndOfStream() {
        if (this.endOfStreamQueuedToSonic) {
            return;
        }
        this.sonicAudioProcessor.queueEndOfStream();
        this.endOfStreamQueuedToSonic = true;
    }

    @Override // androidx.media3.common.audio.BaseAudioProcessor, androidx.media3.common.audio.AudioProcessor
    public ByteBuffer getOutput() {
        ByteBuffer output = isUsingSonic() ? this.sonicAudioProcessor.getOutput() : super.getOutput();
        processPendingCallbacks();
        return output;
    }

    @Override // androidx.media3.common.audio.BaseAudioProcessor, androidx.media3.common.audio.AudioProcessor
    public boolean isEnded() {
        return super.isEnded() && this.sonicAudioProcessor.isEnded();
    }

    @Override // androidx.media3.common.audio.BaseAudioProcessor
    protected void onFlush() {
        resetState();
        this.sonicAudioProcessor.flush();
    }

    @Override // androidx.media3.common.audio.BaseAudioProcessor
    protected void onReset() {
        resetState();
        this.sonicAudioProcessor.reset();
    }

    public void getSpeedAdjustedTimeAsync(long j, TimestampConsumer timestampConsumer) {
        synchronized (this.lock) {
            Assertions.checkArgument(this.speedAdjustedTimeAsyncInputTimeUs < j);
            this.speedAdjustedTimeAsyncInputTimeUs = j;
            if ((j <= this.lastProcessedInputTimeUs && this.pendingCallbackInputTimesUs.isEmpty()) || isEnded()) {
                timestampConsumer.onTimestamp(calculateSpeedAdjustedTime(j));
            } else {
                this.pendingCallbackInputTimesUs.add(j);
                this.pendingCallbacks.add(timestampConsumer);
            }
        }
    }

    public long getMediaDurationUs(long j) {
        long jRound;
        long j2;
        synchronized (this.lock) {
            int size = this.outputSegmentStartTimesUs.size() - 1;
            while (size > 0 && this.outputSegmentStartTimesUs.get(size) > j) {
                size--;
            }
            long j3 = j - this.outputSegmentStartTimesUs.get(size);
            if (size == this.outputSegmentStartTimesUs.size() - 1) {
                jRound = getMediaDurationUsAtCurrentSpeed(j3);
            } else {
                int i = size + 1;
                jRound = Math.round(j3 * divide(this.inputSegmentStartTimesUs.get(i) - this.inputSegmentStartTimesUs.get(size), this.outputSegmentStartTimesUs.get(i) - this.outputSegmentStartTimesUs.get(size)));
            }
            j2 = this.inputSegmentStartTimesUs.get(size) + jRound;
        }
        return j2;
    }

    private long calculateSpeedAdjustedTime(long j) {
        long jRound;
        int size = this.inputSegmentStartTimesUs.size() - 1;
        while (size > 0 && this.inputSegmentStartTimesUs.get(size) > j) {
            size--;
        }
        if (size == this.inputSegmentStartTimesUs.size() - 1) {
            if (this.lastSpeedAdjustedInputTimeUs < this.inputSegmentStartTimesUs.get(size)) {
                this.lastSpeedAdjustedInputTimeUs = this.inputSegmentStartTimesUs.get(size);
                this.lastSpeedAdjustedOutputTimeUs = this.outputSegmentStartTimesUs.get(size);
            }
            jRound = getPlayoutDurationUsAtCurrentSpeed(j - this.lastSpeedAdjustedInputTimeUs);
        } else {
            int i = size + 1;
            jRound = Math.round((j - this.lastSpeedAdjustedInputTimeUs) * divide(this.outputSegmentStartTimesUs.get(i) - this.outputSegmentStartTimesUs.get(size), this.inputSegmentStartTimesUs.get(i) - this.inputSegmentStartTimesUs.get(size)));
        }
        this.lastSpeedAdjustedInputTimeUs = j;
        long j2 = this.lastSpeedAdjustedOutputTimeUs + jRound;
        this.lastSpeedAdjustedOutputTimeUs = j2;
        return j2;
    }

    private void processPendingCallbacks() {
        synchronized (this.lock) {
            while (!this.pendingCallbacks.isEmpty() && (this.pendingCallbackInputTimesUs.element() <= this.lastProcessedInputTimeUs || isEnded())) {
                this.pendingCallbacks.remove().onTimestamp(calculateSpeedAdjustedTime(this.pendingCallbackInputTimesUs.remove()));
            }
        }
    }

    private void updateSpeed(float f, long j) {
        synchronized (this.lock) {
            if (f != this.currentSpeed) {
                updateSpeedChangeArrays(j);
                this.currentSpeed = f;
                if (isUsingSonic()) {
                    this.sonicAudioProcessor.setSpeed(f);
                    this.sonicAudioProcessor.setPitch(f);
                }
                this.sonicAudioProcessor.flush();
                this.endOfStreamQueuedToSonic = false;
                super.getOutput();
            }
        }
    }

    private void updateSpeedChangeArrays(long j) {
        LongArray longArray = this.outputSegmentStartTimesUs;
        long j2 = longArray.get(longArray.size() - 1);
        LongArray longArray2 = this.inputSegmentStartTimesUs;
        long j3 = j - longArray2.get(longArray2.size() - 1);
        this.inputSegmentStartTimesUs.add(j);
        this.outputSegmentStartTimesUs.add(j2 + getPlayoutDurationUsAtCurrentSpeed(j3));
    }

    private long getPlayoutDurationUsAtCurrentSpeed(long j) {
        return isUsingSonic() ? this.sonicAudioProcessor.getPlayoutDuration(j) : j;
    }

    private long getMediaDurationUsAtCurrentSpeed(long j) {
        return isUsingSonic() ? this.sonicAudioProcessor.getMediaDuration(j) : j;
    }

    private void updateLastProcessedInputTime() {
        synchronized (this.lock) {
            if (isUsingSonic()) {
                long jScaleLargeTimestamp = Util.scaleLargeTimestamp(this.sonicAudioProcessor.getProcessedInputBytes(), 1000000L, ((long) this.inputAudioFormat.bytesPerFrame) * ((long) this.inputAudioFormat.sampleRate));
                LongArray longArray = this.inputSegmentStartTimesUs;
                this.lastProcessedInputTimeUs = longArray.get(longArray.size() - 1) + jScaleLargeTimestamp;
            } else {
                this.lastProcessedInputTimeUs = Util.scaleLargeTimestamp(this.bytesRead, 1000000L, ((long) this.inputAudioFormat.bytesPerFrame) * ((long) this.inputAudioFormat.sampleRate));
            }
        }
    }

    private boolean isUsingSonic() {
        boolean z;
        synchronized (this.lock) {
            z = this.currentSpeed != 1.0f;
        }
        return z;
    }

    @EnsuresNonNull({"inputSegmentStartTimesUs", "outputSegmentStartTimesUs"})
    @RequiresNonNull({"lock"})
    private void resetState() {
        synchronized (this.lock) {
            this.inputSegmentStartTimesUs = new LongArray();
            this.outputSegmentStartTimesUs = new LongArray();
            this.inputSegmentStartTimesUs.add(0L);
            this.outputSegmentStartTimesUs.add(0L);
            this.lastProcessedInputTimeUs = 0L;
            this.lastSpeedAdjustedInputTimeUs = 0L;
            this.lastSpeedAdjustedOutputTimeUs = 0L;
            this.currentSpeed = 1.0f;
        }
        this.bytesRead = 0L;
        this.endOfStreamQueuedToSonic = false;
    }

    private static long getSampleRateAlignedTimestamp(long j, int i) {
        long j2 = i;
        return Util.scaleLargeTimestamp(Util.scaleLargeTimestamp(j, j2, 1000000L), 1000000L, j2);
    }
}
