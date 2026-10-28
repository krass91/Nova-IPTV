package androidx.media3.exoplayer;

import androidx.media3.common.Format;
import androidx.media3.common.Timeline;
import androidx.media3.common.util.Clock;
import androidx.media3.exoplayer.analytics.PlayerId;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.SampleStream;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class ForwardingRenderer implements Renderer {
    private final Renderer renderer;

    public ForwardingRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override // androidx.media3.exoplayer.PlayerMessage.Target
    public void handleMessage(int i, Object obj) throws ExoPlaybackException {
        this.renderer.handleMessage(i, obj);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public String getName() {
        return this.renderer.getName();
    }

    @Override // androidx.media3.exoplayer.Renderer, androidx.media3.exoplayer.RendererCapabilities
    public int getTrackType() {
        return this.renderer.getTrackType();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public RendererCapabilities getCapabilities() {
        return this.renderer.getCapabilities();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void init(int i, PlayerId playerId, Clock clock) {
        this.renderer.init(i, playerId, clock);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public MediaClock getMediaClock() {
        return this.renderer.getMediaClock();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public int getState() {
        return this.renderer.getState();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void enable(RendererConfiguration rendererConfiguration, Format[] formatArr, SampleStream sampleStream, long j, boolean z, boolean z2, long j2, long j3, MediaSource.MediaPeriodId mediaPeriodId) throws ExoPlaybackException {
        this.renderer.enable(rendererConfiguration, formatArr, sampleStream, j, z, z2, j2, j3, mediaPeriodId);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void start() throws ExoPlaybackException {
        this.renderer.start();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void replaceStream(Format[] formatArr, SampleStream sampleStream, long j, long j2, MediaSource.MediaPeriodId mediaPeriodId) throws ExoPlaybackException {
        this.renderer.replaceStream(formatArr, sampleStream, j, j2, mediaPeriodId);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public SampleStream getStream() {
        return this.renderer.getStream();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public boolean hasReadStreamToEnd() {
        return this.renderer.hasReadStreamToEnd();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public long getReadingPositionUs() {
        return this.renderer.getReadingPositionUs();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public long getDurationToProgressUs(long j, long j2) {
        return this.renderer.getDurationToProgressUs(j, j2);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void setCurrentStreamFinal() {
        this.renderer.setCurrentStreamFinal();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public boolean isCurrentStreamFinal() {
        return this.renderer.isCurrentStreamFinal();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void maybeThrowStreamError() throws IOException {
        this.renderer.maybeThrowStreamError();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void resetPosition(long j) throws ExoPlaybackException {
        this.renderer.resetPosition(j);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void setPlaybackSpeed(float f, float f2) throws ExoPlaybackException {
        this.renderer.setPlaybackSpeed(f, f2);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void enableMayRenderStartOfStream() {
        this.renderer.enableMayRenderStartOfStream();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void setTimeline(Timeline timeline) {
        this.renderer.setTimeline(timeline);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void render(long j, long j2) throws ExoPlaybackException {
        this.renderer.render(j, j2);
    }

    @Override // androidx.media3.exoplayer.Renderer
    public boolean isReady() {
        return this.renderer.isReady();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public boolean isEnded() {
        return this.renderer.isEnded();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void stop() {
        this.renderer.stop();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void disable() {
        this.renderer.disable();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void reset() {
        this.renderer.reset();
    }

    @Override // androidx.media3.exoplayer.Renderer
    public void release() {
        this.renderer.release();
    }
}
