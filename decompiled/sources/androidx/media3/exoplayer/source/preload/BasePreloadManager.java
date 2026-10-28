package androidx.media3.exoplayer.source.preload;

import android.os.Handler;
import android.os.Looper;
import androidx.media3.common.C;
import androidx.media3.common.FlagSet;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.Clock;
import androidx.media3.common.util.ListenerSet;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.source.MediaSource;
import com.google.common.base.Supplier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

/* JADX INFO: loaded from: classes.dex */
public abstract class BasePreloadManager<T> {
    private final Handler applicationHandler;
    private final ListenerSet<Listener> listeners;
    private final Object lock = new Object();
    private final Map<MediaItem, BasePreloadManager<T>.MediaSourceHolder> mediaItemMediaSourceHolderMap;
    private final MediaSource.Factory mediaSourceFactory;
    protected final Comparator<T> rankingDataComparator;
    private final PriorityQueue<BasePreloadManager<T>.MediaSourceHolder> sourceHolderPriorityQueue;
    private final TargetPreloadStatusControl<T> targetPreloadStatusControl;
    private TargetPreloadStatusControl.PreloadStatus targetPreloadStatusOfCurrentPreloadingSource;

    public interface Listener {
        void onCompleted(MediaItem mediaItem);

        void onError(PreloadException preloadException);
    }

    static /* synthetic */ void lambda$new$0(Listener listener, FlagSet flagSet) {
    }

    protected abstract void clearSourceInternal(MediaSource mediaSource);

    protected MediaSource createMediaSourceForPreloading(MediaSource mediaSource) {
        return mediaSource;
    }

    protected abstract void preloadSourceInternal(MediaSource mediaSource, long j);

    protected void releaseInternal() {
    }

    protected abstract void releaseSourceInternal(MediaSource mediaSource);

    protected boolean shouldStartPreloadingNextSource() {
        return true;
    }

    protected static abstract class BuilderBase<T> {
        protected Supplier<MediaSource.Factory> mediaSourceFactorySupplier;
        protected final Comparator<T> rankingDataComparator;
        protected final TargetPreloadStatusControl<T> targetPreloadStatusControl;

        public abstract BasePreloadManager<T> build();

        public BuilderBase(Comparator<T> comparator, TargetPreloadStatusControl<T> targetPreloadStatusControl, Supplier<MediaSource.Factory> supplier) {
            this.rankingDataComparator = comparator;
            this.targetPreloadStatusControl = targetPreloadStatusControl;
            this.mediaSourceFactorySupplier = supplier;
        }
    }

    protected BasePreloadManager(Comparator<T> comparator, TargetPreloadStatusControl<T> targetPreloadStatusControl, MediaSource.Factory factory) {
        Handler handlerCreateHandlerForCurrentOrMainLooper = Util.createHandlerForCurrentOrMainLooper();
        this.applicationHandler = handlerCreateHandlerForCurrentOrMainLooper;
        this.rankingDataComparator = comparator;
        this.targetPreloadStatusControl = targetPreloadStatusControl;
        this.mediaSourceFactory = factory;
        this.listeners = new ListenerSet<>(handlerCreateHandlerForCurrentOrMainLooper.getLooper(), Clock.DEFAULT, new ListenerSet.IterationFinishedEvent() { // from class: androidx.media3.exoplayer.source.preload.BasePreloadManager$$ExternalSyntheticLambda5
            @Override // androidx.media3.common.util.ListenerSet.IterationFinishedEvent
            public final void invoke(Object obj, FlagSet flagSet) {
                BasePreloadManager.lambda$new$0((BasePreloadManager.Listener) obj, flagSet);
            }
        });
        this.mediaItemMediaSourceHolderMap = new HashMap();
        this.sourceHolderPriorityQueue = new PriorityQueue<>();
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        verifyApplicationThread();
        this.listeners.remove(listener);
    }

    public void clearListeners() {
        verifyApplicationThread();
        this.listeners.clear();
    }

    public final int getSourceCount() {
        return this.mediaItemMediaSourceHolderMap.size();
    }

    public final void add(MediaItem mediaItem, T t) {
        add(this.mediaSourceFactory.createMediaSource(mediaItem), t);
    }

    public final void add(MediaSource mediaSource, T t) {
        MediaSource mediaSourceCreateMediaSourceForPreloading = createMediaSourceForPreloading(mediaSource);
        this.mediaItemMediaSourceHolderMap.put(mediaSourceCreateMediaSourceForPreloading.getMediaItem(), new MediaSourceHolder(this, mediaSourceCreateMediaSourceForPreloading, t));
    }

    public final void invalidate() {
        synchronized (this.lock) {
            this.sourceHolderPriorityQueue.clear();
            this.sourceHolderPriorityQueue.addAll(this.mediaItemMediaSourceHolderMap.values());
            while (!this.sourceHolderPriorityQueue.isEmpty() && !maybeStartPreloadNextSource()) {
                this.sourceHolderPriorityQueue.poll();
            }
        }
    }

    public final MediaSource getMediaSource(MediaItem mediaItem) {
        if (this.mediaItemMediaSourceHolderMap.containsKey(mediaItem)) {
            return this.mediaItemMediaSourceHolderMap.get(mediaItem).mediaSource;
        }
        return null;
    }

    public final boolean remove(MediaItem mediaItem) {
        if (!this.mediaItemMediaSourceHolderMap.containsKey(mediaItem)) {
            return false;
        }
        MediaSource mediaSource = this.mediaItemMediaSourceHolderMap.get(mediaItem).mediaSource;
        this.mediaItemMediaSourceHolderMap.remove(mediaItem);
        releaseSourceInternal(mediaSource);
        return true;
    }

    public final boolean remove(MediaSource mediaSource) {
        MediaItem mediaItem = mediaSource.getMediaItem();
        if (!this.mediaItemMediaSourceHolderMap.containsKey(mediaItem) || mediaSource != this.mediaItemMediaSourceHolderMap.get(mediaItem).mediaSource) {
            return false;
        }
        this.mediaItemMediaSourceHolderMap.remove(mediaItem);
        releaseSourceInternal(mediaSource);
        return true;
    }

    public final void reset() {
        Iterator<BasePreloadManager<T>.MediaSourceHolder> it = this.mediaItemMediaSourceHolderMap.values().iterator();
        while (it.hasNext()) {
            releaseSourceInternal(it.next().mediaSource);
        }
        this.mediaItemMediaSourceHolderMap.clear();
        synchronized (this.lock) {
            this.sourceHolderPriorityQueue.clear();
            this.targetPreloadStatusOfCurrentPreloadingSource = null;
        }
    }

    public final void release() {
        reset();
        releaseInternal();
        clearListeners();
    }

    protected final void onPreloadCompleted(final MediaSource mediaSource) {
        synchronized (this.lock) {
            if (isPreloading(mediaSource)) {
                this.applicationHandler.post(new Runnable() { // from class: androidx.media3.exoplayer.source.preload.BasePreloadManager$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m134xf9589f29(mediaSource);
                    }
                });
            }
        }
    }

    /* JADX INFO: renamed from: lambda$onPreloadCompleted$2$androidx-media3-exoplayer-source-preload-BasePreloadManager, reason: not valid java name */
    /* synthetic */ void m134xf9589f29(final MediaSource mediaSource) {
        this.listeners.sendEvent(-1, new ListenerSet.Event() { // from class: androidx.media3.exoplayer.source.preload.BasePreloadManager$$ExternalSyntheticLambda0
            @Override // androidx.media3.common.util.ListenerSet.Event
            public final void invoke(Object obj) {
                ((BasePreloadManager.Listener) obj).onCompleted(mediaSource.getMediaItem());
            }
        });
        m136x7039cf91(mediaSource);
    }

    protected final void onPreloadError(final PreloadException preloadException, final MediaSource mediaSource) {
        synchronized (this.lock) {
            if (isPreloading(mediaSource)) {
                this.applicationHandler.post(new Runnable() { // from class: androidx.media3.exoplayer.source.preload.BasePreloadManager$$ExternalSyntheticLambda4
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m135x773570a8(preloadException, mediaSource);
                    }
                });
            }
        }
    }

    /* JADX INFO: renamed from: lambda$onPreloadError$4$androidx-media3-exoplayer-source-preload-BasePreloadManager, reason: not valid java name */
    /* synthetic */ void m135x773570a8(final PreloadException preloadException, MediaSource mediaSource) {
        this.listeners.sendEvent(-1, new ListenerSet.Event() { // from class: androidx.media3.exoplayer.source.preload.BasePreloadManager$$ExternalSyntheticLambda1
            @Override // androidx.media3.common.util.ListenerSet.Event
            public final void invoke(Object obj) {
                ((BasePreloadManager.Listener) obj).onError(preloadException);
            }
        });
        m136x7039cf91(mediaSource);
    }

    protected final void onPreloadSkipped(final MediaSource mediaSource) {
        synchronized (this.lock) {
            if (isPreloading(mediaSource)) {
                this.applicationHandler.post(new Runnable() { // from class: androidx.media3.exoplayer.source.preload.BasePreloadManager$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m136x7039cf91(mediaSource);
                    }
                });
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: maybeAdvanceToNextSource, reason: merged with bridge method [inline-methods] */
    public void m136x7039cf91(MediaSource mediaSource) {
        synchronized (this.lock) {
            if (isPreloading(mediaSource)) {
                do {
                    this.sourceHolderPriorityQueue.poll();
                    if (this.sourceHolderPriorityQueue.isEmpty()) {
                        break;
                    }
                } while (!maybeStartPreloadNextSource());
            }
        }
    }

    private boolean isPreloading(MediaSource mediaSource) {
        return !this.sourceHolderPriorityQueue.isEmpty() && ((MediaSourceHolder) Assertions.checkNotNull(this.sourceHolderPriorityQueue.peek())).mediaSource == mediaSource;
    }

    protected final TargetPreloadStatusControl.PreloadStatus getTargetPreloadStatus(MediaSource mediaSource) {
        synchronized (this.lock) {
            if (!isPreloading(mediaSource)) {
                return null;
            }
            return this.targetPreloadStatusOfCurrentPreloadingSource;
        }
    }

    private boolean maybeStartPreloadNextSource() {
        if (!shouldStartPreloadingNextSource()) {
            return false;
        }
        MediaSourceHolder mediaSourceHolder = (MediaSourceHolder) Assertions.checkNotNull(this.sourceHolderPriorityQueue.peek());
        TargetPreloadStatusControl.PreloadStatus targetPreloadStatus = this.targetPreloadStatusControl.getTargetPreloadStatus(mediaSourceHolder.rankingData);
        this.targetPreloadStatusOfCurrentPreloadingSource = targetPreloadStatus;
        if (targetPreloadStatus != null) {
            preloadSourceInternal(mediaSourceHolder.mediaSource, mediaSourceHolder.startPositionUs);
            return true;
        }
        clearSourceInternal(mediaSourceHolder.mediaSource);
        return false;
    }

    private void verifyApplicationThread() {
        if (Looper.myLooper() != this.applicationHandler.getLooper()) {
            throw new IllegalStateException("Preload manager is accessed on the wrong thread.");
        }
    }

    private final class MediaSourceHolder implements Comparable<BasePreloadManager<T>.MediaSourceHolder> {
        public final MediaSource mediaSource;
        public final T rankingData;
        public final long startPositionUs;

        public MediaSourceHolder(BasePreloadManager basePreloadManager, MediaSource mediaSource, T t) {
            this(mediaSource, t, C.TIME_UNSET);
        }

        public MediaSourceHolder(MediaSource mediaSource, T t, long j) {
            this.mediaSource = mediaSource;
            this.rankingData = t;
            this.startPositionUs = j;
        }

        @Override // java.lang.Comparable
        public int compareTo(BasePreloadManager<T>.MediaSourceHolder mediaSourceHolder) {
            return BasePreloadManager.this.rankingDataComparator.compare(this.rankingData, mediaSourceHolder.rankingData);
        }
    }
}
