package androidx.media3.exoplayer.offline;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PriorityTaskManager;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.RunnableFutureTask;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.CacheWriter;
import androidx.media3.exoplayer.dash.offline.DashDownloader$$ExternalSyntheticLambda0;
import java.io.IOException;
import java.util.concurrent.Executor;

/* JADX INFO: loaded from: classes.dex */
public final class ProgressiveDownloader implements Downloader {
    private final CacheWriter cacheWriter;
    private final CacheDataSource dataSource;
    private final DataSpec dataSpec;
    private volatile RunnableFutureTask<Void, IOException> downloadRunnable;
    private final Executor executor;
    private volatile boolean isCanceled;
    private final PriorityTaskManager priorityTaskManager;
    private Downloader.ProgressListener progressListener;

    public ProgressiveDownloader(MediaItem mediaItem, CacheDataSource.Factory factory) {
        this(mediaItem, factory, new DashDownloader$$ExternalSyntheticLambda0());
    }

    public ProgressiveDownloader(MediaItem mediaItem, CacheDataSource.Factory factory, Executor executor) {
        this.executor = (Executor) Assertions.checkNotNull(executor);
        Assertions.checkNotNull(mediaItem.localConfiguration);
        DataSpec dataSpecBuild = new DataSpec.Builder().setUri(mediaItem.localConfiguration.uri).setKey(mediaItem.localConfiguration.customCacheKey).setFlags(4).build();
        this.dataSpec = dataSpecBuild;
        CacheDataSource cacheDataSourceCreateDataSourceForDownloading = factory.createDataSourceForDownloading();
        this.dataSource = cacheDataSourceCreateDataSourceForDownloading;
        this.cacheWriter = new CacheWriter(cacheDataSourceCreateDataSourceForDownloading, dataSpecBuild, null, new CacheWriter.ProgressListener() { // from class: androidx.media3.exoplayer.offline.ProgressiveDownloader$$ExternalSyntheticLambda0
            @Override // androidx.media3.datasource.cache.CacheWriter.ProgressListener
            public final void onProgress(long j, long j2, long j3) {
                this.f$0.onProgress(j, j2, j3);
            }
        });
        this.priorityTaskManager = factory.getUpstreamPriorityTaskManager();
    }

    /* JADX WARN: Bottom block not found for handler: all -> 0x0049 */
    @Override // androidx.media3.exoplayer.offline.Downloader
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void download(androidx.media3.exoplayer.offline.Downloader.ProgressListener r4) throws java.lang.InterruptedException, java.io.IOException {
        /*
            r3 = this;
            r3.progressListener = r4
            androidx.media3.common.PriorityTaskManager r4 = r3.priorityTaskManager
            r0 = -4000(0xfffffffffffff060, float:NaN)
            if (r4 == 0) goto Lb
            r4.add(r0)
        Lb:
            r4 = 0
        Lc:
            if (r4 != 0) goto L5d
            boolean r1 = r3.isCanceled     // Catch: java.lang.Throwable -> L49
            if (r1 != 0) goto L5d
            androidx.media3.exoplayer.offline.ProgressiveDownloader$1 r1 = new androidx.media3.exoplayer.offline.ProgressiveDownloader$1     // Catch: java.lang.Throwable -> L49
            r1.<init>()     // Catch: java.lang.Throwable -> L49
            r3.downloadRunnable = r1     // Catch: java.lang.Throwable -> L49
            androidx.media3.common.PriorityTaskManager r1 = r3.priorityTaskManager     // Catch: java.lang.Throwable -> L49
            if (r1 == 0) goto L20
            r1.proceed(r0)     // Catch: java.lang.Throwable -> L49
        L20:
            java.util.concurrent.Executor r1 = r3.executor     // Catch: java.lang.Throwable -> L49
            androidx.media3.common.util.RunnableFutureTask<java.lang.Void, java.io.IOException> r2 = r3.downloadRunnable     // Catch: java.lang.Throwable -> L49
            r1.execute(r2)     // Catch: java.lang.Throwable -> L49
            androidx.media3.common.util.RunnableFutureTask<java.lang.Void, java.io.IOException> r1 = r3.downloadRunnable     // Catch: java.util.concurrent.ExecutionException -> L2e java.lang.Throwable -> L49
            r1.get()     // Catch: java.util.concurrent.ExecutionException -> L2e java.lang.Throwable -> L49
            r4 = 1
            goto Lc
        L2e:
            r1 = move-exception
            java.lang.Throwable r1 = r1.getCause()     // Catch: java.lang.Throwable -> L49
            java.lang.Object r1 = androidx.media3.common.util.Assertions.checkNotNull(r1)     // Catch: java.lang.Throwable -> L49
            java.lang.Throwable r1 = (java.lang.Throwable) r1     // Catch: java.lang.Throwable -> L49
            boolean r2 = r1 instanceof androidx.media3.common.PriorityTaskManager.PriorityTooLowException     // Catch: java.lang.Throwable -> L49
            if (r2 == 0) goto L3e
            goto Lc
        L3e:
            boolean r2 = r1 instanceof java.io.IOException     // Catch: java.lang.Throwable -> L49
            if (r2 != 0) goto L46
            androidx.media3.common.util.Util.sneakyThrow(r1)     // Catch: java.lang.Throwable -> L49
            goto Lc
        L46:
            java.io.IOException r1 = (java.io.IOException) r1     // Catch: java.lang.Throwable -> L49
            throw r1     // Catch: java.lang.Throwable -> L49
        L49:
            r4 = move-exception
            androidx.media3.common.util.RunnableFutureTask<java.lang.Void, java.io.IOException> r1 = r3.downloadRunnable
            java.lang.Object r1 = androidx.media3.common.util.Assertions.checkNotNull(r1)
            androidx.media3.common.util.RunnableFutureTask r1 = (androidx.media3.common.util.RunnableFutureTask) r1
            r1.blockUntilFinished()
            androidx.media3.common.PriorityTaskManager r1 = r3.priorityTaskManager
            if (r1 == 0) goto L5c
            r1.remove(r0)
        L5c:
            throw r4
        L5d:
            androidx.media3.common.util.RunnableFutureTask<java.lang.Void, java.io.IOException> r4 = r3.downloadRunnable
            java.lang.Object r4 = androidx.media3.common.util.Assertions.checkNotNull(r4)
            androidx.media3.common.util.RunnableFutureTask r4 = (androidx.media3.common.util.RunnableFutureTask) r4
            r4.blockUntilFinished()
            androidx.media3.common.PriorityTaskManager r4 = r3.priorityTaskManager
            if (r4 == 0) goto L6f
            r4.remove(r0)
        L6f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.media3.exoplayer.offline.ProgressiveDownloader.download(androidx.media3.exoplayer.offline.Downloader$ProgressListener):void");
    }

    @Override // androidx.media3.exoplayer.offline.Downloader
    public void cancel() {
        this.isCanceled = true;
        RunnableFutureTask<Void, IOException> runnableFutureTask = this.downloadRunnable;
        if (runnableFutureTask != null) {
            runnableFutureTask.cancel(true);
        }
    }

    @Override // androidx.media3.exoplayer.offline.Downloader
    public void remove() {
        this.dataSource.getCache().removeResource(this.dataSource.getCacheKeyFactory().buildCacheKey(this.dataSpec));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onProgress(long j, long j2, long j3) {
        Downloader.ProgressListener progressListener = this.progressListener;
        if (progressListener == null) {
            return;
        }
        progressListener.onProgress(j, j2, (j == -1 || j == 0) ? -1.0f : (j2 * 100.0f) / j);
    }
}
