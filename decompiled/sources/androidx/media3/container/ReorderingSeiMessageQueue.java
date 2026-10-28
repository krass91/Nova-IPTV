package androidx.media3.container;

import androidx.media3.common.C;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.ParsableByteArray;
import androidx.media3.common.util.Util;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/* JADX INFO: loaded from: classes.dex */
public final class ReorderingSeiMessageQueue {
    private SampleSeiMessages lastQueuedMessage;
    private final SeiConsumer seiConsumer;
    private final ArrayDeque<ParsableByteArray> unusedParsableByteArrays = new ArrayDeque<>();
    private final ArrayDeque<SampleSeiMessages> unusedSampleSeiMessages = new ArrayDeque<>();
    private final PriorityQueue<SampleSeiMessages> pendingSeiMessages = new PriorityQueue<>();
    private int reorderingQueueSize = -1;

    public interface SeiConsumer {
        void consume(long j, ParsableByteArray parsableByteArray);
    }

    public ReorderingSeiMessageQueue(SeiConsumer seiConsumer) {
        this.seiConsumer = seiConsumer;
    }

    public void setMaxSize(int i) {
        Assertions.checkState(i >= 0);
        this.reorderingQueueSize = i;
        flushQueueDownToSize(i);
    }

    public int getMaxSize() {
        return this.reorderingQueueSize;
    }

    public void add(long j, ParsableByteArray parsableByteArray) {
        int i = this.reorderingQueueSize;
        if (i == 0 || (i != -1 && this.pendingSeiMessages.size() >= this.reorderingQueueSize && j < ((SampleSeiMessages) Util.castNonNull(this.pendingSeiMessages.peek())).presentationTimeUs)) {
            this.seiConsumer.consume(j, parsableByteArray);
            return;
        }
        ParsableByteArray parsableByteArrayCopy = copy(parsableByteArray);
        SampleSeiMessages sampleSeiMessages = this.lastQueuedMessage;
        if (sampleSeiMessages != null && j == sampleSeiMessages.presentationTimeUs) {
            this.lastQueuedMessage.nalBuffers.add(parsableByteArrayCopy);
            return;
        }
        SampleSeiMessages sampleSeiMessages2 = this.unusedSampleSeiMessages.isEmpty() ? new SampleSeiMessages() : this.unusedSampleSeiMessages.pop();
        sampleSeiMessages2.init(j, parsableByteArrayCopy);
        this.pendingSeiMessages.add(sampleSeiMessages2);
        this.lastQueuedMessage = sampleSeiMessages2;
        int i2 = this.reorderingQueueSize;
        if (i2 != -1) {
            flushQueueDownToSize(i2);
        }
    }

    private ParsableByteArray copy(ParsableByteArray parsableByteArray) {
        ParsableByteArray parsableByteArrayPop;
        if (this.unusedParsableByteArrays.isEmpty()) {
            parsableByteArrayPop = new ParsableByteArray();
        } else {
            parsableByteArrayPop = this.unusedParsableByteArrays.pop();
        }
        parsableByteArrayPop.reset(parsableByteArray.bytesLeft());
        System.arraycopy(parsableByteArray.getData(), parsableByteArray.getPosition(), parsableByteArrayPop.getData(), 0, parsableByteArrayPop.bytesLeft());
        return parsableByteArrayPop;
    }

    public void flush() {
        flushQueueDownToSize(0);
    }

    private void flushQueueDownToSize(int i) {
        while (this.pendingSeiMessages.size() > i) {
            SampleSeiMessages sampleSeiMessages = (SampleSeiMessages) Util.castNonNull(this.pendingSeiMessages.poll());
            for (int i2 = 0; i2 < sampleSeiMessages.nalBuffers.size(); i2++) {
                this.seiConsumer.consume(sampleSeiMessages.presentationTimeUs, sampleSeiMessages.nalBuffers.get(i2));
                this.unusedParsableByteArrays.push(sampleSeiMessages.nalBuffers.get(i2));
            }
            sampleSeiMessages.nalBuffers.clear();
            SampleSeiMessages sampleSeiMessages2 = this.lastQueuedMessage;
            if (sampleSeiMessages2 != null && sampleSeiMessages2.presentationTimeUs == sampleSeiMessages.presentationTimeUs) {
                this.lastQueuedMessage = null;
            }
            this.unusedSampleSeiMessages.push(sampleSeiMessages);
        }
    }

    private static final class SampleSeiMessages implements Comparable<SampleSeiMessages> {
        public long presentationTimeUs = C.TIME_UNSET;
        public final List<ParsableByteArray> nalBuffers = new ArrayList();

        public void init(long j, ParsableByteArray parsableByteArray) {
            Assertions.checkArgument(j != C.TIME_UNSET);
            Assertions.checkState(this.nalBuffers.isEmpty());
            this.presentationTimeUs = j;
            this.nalBuffers.add(parsableByteArray);
        }

        @Override // java.lang.Comparable
        public int compareTo(SampleSeiMessages sampleSeiMessages) {
            return Long.compare(this.presentationTimeUs, sampleSeiMessages.presentationTimeUs);
        }
    }
}
