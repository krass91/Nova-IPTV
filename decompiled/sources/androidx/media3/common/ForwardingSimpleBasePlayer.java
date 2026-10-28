package androidx.media3.common;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import androidx.media3.common.text.CueGroup;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Objects;

/* JADX INFO: loaded from: classes.dex */
public class ForwardingSimpleBasePlayer extends SimpleBasePlayer {
    private ForwardingPositionSupplier currentPositionSupplier;
    private Metadata lastTimedMetadata;
    private int pendingDiscontinuityReason;
    private boolean pendingFirstFrameRendered;
    private long pendingPositionDiscontinuityNewPositionMs;
    private int playWhenReadyChangeReason;
    private final Player player;

    public ForwardingSimpleBasePlayer(final Player player) {
        super(player.getApplicationLooper());
        this.player = player;
        this.lastTimedMetadata = new Metadata(C.TIME_UNSET, new Metadata.Entry[0]);
        this.playWhenReadyChangeReason = 1;
        this.pendingDiscontinuityReason = 5;
        this.currentPositionSupplier = new ForwardingPositionSupplier(player);
        player.addListener(new Player.Listener() { // from class: androidx.media3.common.ForwardingSimpleBasePlayer.1
            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onAudioAttributesChanged(AudioAttributes audioAttributes) {
                Player.Listener.CC.$default$onAudioAttributesChanged(this, audioAttributes);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onAudioSessionIdChanged(int i) {
                Player.Listener.CC.$default$onAudioSessionIdChanged(this, i);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onAvailableCommandsChanged(Player.Commands commands) {
                Player.Listener.CC.$default$onAvailableCommandsChanged(this, commands);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onCues(CueGroup cueGroup) {
                Player.Listener.CC.$default$onCues(this, cueGroup);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onCues(List list) {
                Player.Listener.CC.$default$onCues(this, list);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onDeviceInfoChanged(DeviceInfo deviceInfo) {
                Player.Listener.CC.$default$onDeviceInfoChanged(this, deviceInfo);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onDeviceVolumeChanged(int i, boolean z) {
                Player.Listener.CC.$default$onDeviceVolumeChanged(this, i, z);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onIsLoadingChanged(boolean z) {
                Player.Listener.CC.$default$onIsLoadingChanged(this, z);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onIsPlayingChanged(boolean z) {
                Player.Listener.CC.$default$onIsPlayingChanged(this, z);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onLoadingChanged(boolean z) {
                Player.Listener.CC.$default$onLoadingChanged(this, z);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onMaxSeekToPreviousPositionChanged(long j) {
                Player.Listener.CC.$default$onMaxSeekToPreviousPositionChanged(this, j);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onMediaItemTransition(MediaItem mediaItem, int i) {
                Player.Listener.CC.$default$onMediaItemTransition(this, mediaItem, i);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
                Player.Listener.CC.$default$onMediaMetadataChanged(this, mediaMetadata);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Player.Listener.CC.$default$onPlaybackParametersChanged(this, playbackParameters);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onPlaybackStateChanged(int i) {
                Player.Listener.CC.$default$onPlaybackStateChanged(this, i);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onPlaybackSuppressionReasonChanged(int i) {
                Player.Listener.CC.$default$onPlaybackSuppressionReasonChanged(this, i);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onPlayerError(PlaybackException playbackException) {
                Player.Listener.CC.$default$onPlayerError(this, playbackException);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onPlayerErrorChanged(PlaybackException playbackException) {
                Player.Listener.CC.$default$onPlayerErrorChanged(this, playbackException);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onPlayerStateChanged(boolean z, int i) {
                Player.Listener.CC.$default$onPlayerStateChanged(this, z, i);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onPlaylistMetadataChanged(MediaMetadata mediaMetadata) {
                Player.Listener.CC.$default$onPlaylistMetadataChanged(this, mediaMetadata);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onPositionDiscontinuity(int i) {
                Player.Listener.CC.$default$onPositionDiscontinuity(this, i);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onRepeatModeChanged(int i) {
                Player.Listener.CC.$default$onRepeatModeChanged(this, i);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onSeekBackIncrementChanged(long j) {
                Player.Listener.CC.$default$onSeekBackIncrementChanged(this, j);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onSeekForwardIncrementChanged(long j) {
                Player.Listener.CC.$default$onSeekForwardIncrementChanged(this, j);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onShuffleModeEnabledChanged(boolean z) {
                Player.Listener.CC.$default$onShuffleModeEnabledChanged(this, z);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onSkipSilenceEnabledChanged(boolean z) {
                Player.Listener.CC.$default$onSkipSilenceEnabledChanged(this, z);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onSurfaceSizeChanged(int i, int i2) {
                Player.Listener.CC.$default$onSurfaceSizeChanged(this, i, i2);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onTimelineChanged(Timeline timeline, int i) {
                Player.Listener.CC.$default$onTimelineChanged(this, timeline, i);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onTrackSelectionParametersChanged(TrackSelectionParameters trackSelectionParameters) {
                Player.Listener.CC.$default$onTrackSelectionParametersChanged(this, trackSelectionParameters);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onTracksChanged(Tracks tracks) {
                Player.Listener.CC.$default$onTracksChanged(this, tracks);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onVideoSizeChanged(VideoSize videoSize) {
                Player.Listener.CC.$default$onVideoSizeChanged(this, videoSize);
            }

            @Override // androidx.media3.common.Player.Listener
            public /* synthetic */ void onVolumeChanged(float f) {
                Player.Listener.CC.$default$onVolumeChanged(this, f);
            }

            @Override // androidx.media3.common.Player.Listener
            public void onMetadata(Metadata metadata) {
                ForwardingSimpleBasePlayer.this.lastTimedMetadata = metadata;
            }

            @Override // androidx.media3.common.Player.Listener
            public void onPlayWhenReadyChanged(boolean z, int i) {
                ForwardingSimpleBasePlayer.this.playWhenReadyChangeReason = i;
            }

            @Override // androidx.media3.common.Player.Listener
            public void onPositionDiscontinuity(Player.PositionInfo positionInfo, Player.PositionInfo positionInfo2, int i) {
                ForwardingSimpleBasePlayer.this.pendingDiscontinuityReason = i;
                ForwardingSimpleBasePlayer.this.pendingPositionDiscontinuityNewPositionMs = positionInfo2.positionMs;
                ForwardingSimpleBasePlayer.this.currentPositionSupplier.setConstant(positionInfo.positionMs, positionInfo.contentPositionMs);
                ForwardingSimpleBasePlayer.this.currentPositionSupplier = new ForwardingPositionSupplier(player);
            }

            @Override // androidx.media3.common.Player.Listener
            public void onRenderedFirstFrame() {
                ForwardingSimpleBasePlayer.this.pendingFirstFrameRendered = true;
            }

            @Override // androidx.media3.common.Player.Listener
            public void onEvents(Player player2, Player.Events events) {
                ForwardingSimpleBasePlayer.this.invalidateState();
            }
        });
    }

    protected final Player getPlayer() {
        return this.player;
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected SimpleBasePlayer.State getState() {
        Tracks currentTracks;
        SimpleBasePlayer.State.Builder builder = new SimpleBasePlayer.State.Builder();
        final ForwardingPositionSupplier forwardingPositionSupplier = this.currentPositionSupplier;
        if (this.player.isCommandAvailable(16)) {
            Objects.requireNonNull(forwardingPositionSupplier);
            builder.setAdBufferedPositionMs(new SimpleBasePlayer.PositionSupplier() { // from class: androidx.media3.common.ForwardingSimpleBasePlayer$$ExternalSyntheticLambda0
                @Override // androidx.media3.common.SimpleBasePlayer.PositionSupplier
                public final long get() {
                    return forwardingPositionSupplier.getBufferedPositionMs();
                }
            });
            Objects.requireNonNull(forwardingPositionSupplier);
            builder.setAdPositionMs(new SimpleBasePlayer.PositionSupplier() { // from class: androidx.media3.common.ForwardingSimpleBasePlayer$$ExternalSyntheticLambda1
                @Override // androidx.media3.common.SimpleBasePlayer.PositionSupplier
                public final long get() {
                    return forwardingPositionSupplier.getCurrentPositionMs();
                }
            });
        }
        if (this.player.isCommandAvailable(21)) {
            builder.setAudioAttributes(this.player.getAudioAttributes());
        }
        builder.setAvailableCommands(this.player.getAvailableCommands());
        if (this.player.isCommandAvailable(16)) {
            Objects.requireNonNull(forwardingPositionSupplier);
            builder.setContentBufferedPositionMs(new SimpleBasePlayer.PositionSupplier() { // from class: androidx.media3.common.ForwardingSimpleBasePlayer$$ExternalSyntheticLambda2
                @Override // androidx.media3.common.SimpleBasePlayer.PositionSupplier
                public final long get() {
                    return forwardingPositionSupplier.getContentBufferedPositionMs();
                }
            });
            Objects.requireNonNull(forwardingPositionSupplier);
            builder.setContentPositionMs(new SimpleBasePlayer.PositionSupplier() { // from class: androidx.media3.common.ForwardingSimpleBasePlayer$$ExternalSyntheticLambda3
                @Override // androidx.media3.common.SimpleBasePlayer.PositionSupplier
                public final long get() {
                    return forwardingPositionSupplier.getContentPositionMs();
                }
            });
            if (this.player.isCommandAvailable(17)) {
                builder.setCurrentAd(this.player.getCurrentAdGroupIndex(), this.player.getCurrentAdIndexInAdGroup());
            }
        }
        if (this.player.isCommandAvailable(28)) {
            builder.setCurrentCues(this.player.getCurrentCues());
        }
        if (this.player.isCommandAvailable(17)) {
            builder.setCurrentMediaItemIndex(this.player.getCurrentMediaItemIndex());
        }
        builder.setDeviceInfo(this.player.getDeviceInfo());
        if (this.player.isCommandAvailable(23)) {
            builder.setDeviceVolume(this.player.getDeviceVolume());
            builder.setIsDeviceMuted(this.player.isDeviceMuted());
        }
        builder.setIsLoading(this.player.isLoading());
        builder.setMaxSeekToPreviousPositionMs(this.player.getMaxSeekToPreviousPosition());
        if (this.pendingFirstFrameRendered) {
            builder.setNewlyRenderedFirstFrame(true);
            this.pendingFirstFrameRendered = false;
        }
        builder.setPlaybackParameters(this.player.getPlaybackParameters());
        builder.setPlaybackState(this.player.getPlaybackState());
        builder.setPlaybackSuppressionReason(this.player.getPlaybackSuppressionReason());
        builder.setPlayerError(this.player.getPlayerError());
        if (this.player.isCommandAvailable(17)) {
            if (this.player.isCommandAvailable(30)) {
                currentTracks = this.player.getCurrentTracks();
            } else {
                currentTracks = Tracks.EMPTY;
            }
            builder.setPlaylist(this.player.getCurrentTimeline(), currentTracks, this.player.isCommandAvailable(18) ? this.player.getMediaMetadata() : null);
        }
        if (this.player.isCommandAvailable(18)) {
            builder.setPlaylistMetadata(this.player.getPlaylistMetadata());
        }
        builder.setPlayWhenReady(this.player.getPlayWhenReady(), this.playWhenReadyChangeReason);
        long j = this.pendingPositionDiscontinuityNewPositionMs;
        if (j != C.TIME_UNSET) {
            builder.setPositionDiscontinuity(this.pendingDiscontinuityReason, j);
            this.pendingPositionDiscontinuityNewPositionMs = C.TIME_UNSET;
        }
        builder.setRepeatMode(this.player.getRepeatMode());
        builder.setSeekBackIncrementMs(this.player.getSeekBackIncrement());
        builder.setSeekForwardIncrementMs(this.player.getSeekForwardIncrement());
        builder.setShuffleModeEnabled(this.player.getShuffleModeEnabled());
        builder.setSurfaceSize(this.player.getSurfaceSize());
        builder.setTimedMetadata(this.lastTimedMetadata);
        if (this.player.isCommandAvailable(16)) {
            Objects.requireNonNull(forwardingPositionSupplier);
            builder.setTotalBufferedDurationMs(new SimpleBasePlayer.PositionSupplier() { // from class: androidx.media3.common.ForwardingSimpleBasePlayer$$ExternalSyntheticLambda4
                @Override // androidx.media3.common.SimpleBasePlayer.PositionSupplier
                public final long get() {
                    return forwardingPositionSupplier.getTotalBufferedDurationMs();
                }
            });
        }
        builder.setTrackSelectionParameters(this.player.getTrackSelectionParameters());
        builder.setVideoSize(this.player.getVideoSize());
        if (this.player.isCommandAvailable(22)) {
            builder.setVolume(this.player.getVolume());
        }
        return builder.build();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetPlayWhenReady(boolean z) {
        this.player.setPlayWhenReady(z);
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handlePrepare() {
        this.player.prepare();
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleStop() {
        this.player.stop();
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleRelease() {
        this.player.release();
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetRepeatMode(int i) {
        this.player.setRepeatMode(i);
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetShuffleModeEnabled(boolean z) {
        this.player.setShuffleModeEnabled(z);
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetPlaybackParameters(PlaybackParameters playbackParameters) {
        this.player.setPlaybackParameters(playbackParameters);
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetTrackSelectionParameters(TrackSelectionParameters trackSelectionParameters) {
        this.player.setTrackSelectionParameters(trackSelectionParameters);
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetPlaylistMetadata(MediaMetadata mediaMetadata) {
        this.player.setPlaylistMetadata(mediaMetadata);
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetVolume(float f) {
        this.player.setVolume(f);
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetDeviceVolume(int i, int i2) {
        if (this.player.isCommandAvailable(33)) {
            this.player.setDeviceVolume(i, i2);
        } else {
            this.player.setDeviceVolume(i);
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleIncreaseDeviceVolume(int i) {
        if (this.player.isCommandAvailable(34)) {
            this.player.increaseDeviceVolume(i);
        } else {
            this.player.increaseDeviceVolume();
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleDecreaseDeviceVolume(int i) {
        if (this.player.isCommandAvailable(34)) {
            this.player.decreaseDeviceVolume(i);
        } else {
            this.player.decreaseDeviceVolume();
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetDeviceMuted(boolean z, int i) {
        if (this.player.isCommandAvailable(34)) {
            this.player.setDeviceMuted(z, i);
        } else {
            this.player.setDeviceMuted(z);
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetAudioAttributes(AudioAttributes audioAttributes, boolean z) {
        this.player.setAudioAttributes(audioAttributes, z);
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetVideoOutput(Object obj) {
        if (obj instanceof SurfaceView) {
            this.player.setVideoSurfaceView((SurfaceView) obj);
        } else if (obj instanceof TextureView) {
            this.player.setVideoTextureView((TextureView) obj);
        } else if (obj instanceof SurfaceHolder) {
            this.player.setVideoSurfaceHolder((SurfaceHolder) obj);
        } else if (obj instanceof Surface) {
            this.player.setVideoSurface((Surface) obj);
        } else {
            throw new IllegalStateException();
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleClearVideoOutput(Object obj) {
        if (obj instanceof SurfaceView) {
            this.player.clearVideoSurfaceView((SurfaceView) obj);
        } else if (obj instanceof TextureView) {
            this.player.clearVideoTextureView((TextureView) obj);
        } else if (obj instanceof SurfaceHolder) {
            this.player.clearVideoSurfaceHolder((SurfaceHolder) obj);
        } else if (obj instanceof Surface) {
            this.player.clearVideoSurface((Surface) obj);
        } else if (obj == null) {
            this.player.clearVideoSurface();
        } else {
            throw new IllegalStateException();
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSetMediaItems(List<MediaItem> list, int i, long j) {
        boolean z = list.size() == 1 && this.player.isCommandAvailable(31);
        if (i == -1) {
            if (z) {
                this.player.setMediaItem(list.get(0));
            } else {
                this.player.setMediaItems(list);
            }
        } else if (z) {
            this.player.setMediaItem(list.get(0), j);
        } else {
            this.player.setMediaItems(list, i, j);
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleAddMediaItems(int i, List<MediaItem> list) {
        if (list.size() == 1) {
            this.player.addMediaItem(i, list.get(0));
        } else {
            this.player.addMediaItems(i, list);
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleMoveMediaItems(int i, int i2, int i3) {
        if (i2 == i + 1) {
            this.player.moveMediaItem(i, i3);
        } else {
            this.player.moveMediaItems(i, i2, i3);
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleReplaceMediaItems(int i, int i2, List<MediaItem> list) {
        if (i2 == i + 1 && list.size() == 1) {
            this.player.replaceMediaItem(i, list.get(0));
        } else {
            this.player.replaceMediaItems(i, i2, list);
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleRemoveMediaItems(int i, int i2) {
        if (i2 == i + 1) {
            this.player.removeMediaItem(i);
        } else {
            this.player.removeMediaItems(i, i2);
        }
        return Futures.immediateVoidFuture();
    }

    @Override // androidx.media3.common.SimpleBasePlayer
    protected ListenableFuture<?> handleSeek(int i, long j, int i2) {
        switch (i2) {
            case 4:
                this.player.seekToDefaultPosition();
                break;
            case 5:
                this.player.seekTo(j);
                break;
            case 6:
                this.player.seekToPreviousMediaItem();
                break;
            case 7:
                this.player.seekToPrevious();
                break;
            case 8:
                this.player.seekToNextMediaItem();
                break;
            case 9:
                this.player.seekToNext();
                break;
            case 10:
                if (i != -1) {
                    this.player.seekTo(i, j);
                }
                break;
            case 11:
                this.player.seekBack();
                break;
            case 12:
                this.player.seekForward();
                break;
            default:
                throw new IllegalStateException();
        }
        return Futures.immediateVoidFuture();
    }

    /* JADX INFO: Access modifiers changed from: private */
    static final class ForwardingPositionSupplier {
        private final Player player;
        private long positionsMs = C.TIME_UNSET;
        private long contentPositionMs = C.TIME_UNSET;

        public ForwardingPositionSupplier(Player player) {
            this.player = player;
        }

        public void setConstant(long j, long j2) {
            this.positionsMs = j;
            this.contentPositionMs = j2;
        }

        public long getCurrentPositionMs() {
            long j = this.positionsMs;
            return j == C.TIME_UNSET ? this.player.getCurrentPosition() : j;
        }

        public long getBufferedPositionMs() {
            long j = this.positionsMs;
            return j == C.TIME_UNSET ? this.player.getBufferedPosition() : j;
        }

        public long getContentPositionMs() {
            long j = this.contentPositionMs;
            return j == C.TIME_UNSET ? this.player.getContentPosition() : j;
        }

        public long getContentBufferedPositionMs() {
            long j = this.contentPositionMs;
            return j == C.TIME_UNSET ? this.player.getContentBufferedPosition() : j;
        }

        public long getTotalBufferedDurationMs() {
            if (this.positionsMs == C.TIME_UNSET) {
                return this.player.getTotalBufferedDuration();
            }
            return 0L;
        }
    }
}
