package androidx.media3.datasource;

import android.net.Uri;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.Util;
import com.google.common.collect.Sets;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public class FileDescriptorDataSource extends BaseDataSource {
    private static final Set<FileDescriptor> inUseFileDescriptors = Sets.newConcurrentHashSet();
    private long bytesRemaining;
    private final FileDescriptor fileDescriptor;
    private FileInputStream inputStream;
    private final long length;
    private final long offset;
    private boolean opened;
    private Uri uri;

    public FileDescriptorDataSource(FileDescriptor fileDescriptor, long j, long j2) {
        super(false);
        this.fileDescriptor = (FileDescriptor) Assertions.checkNotNull(fileDescriptor);
        this.offset = j;
        this.length = j2;
    }

    @Override // androidx.media3.datasource.DataSource
    public long open(DataSpec dataSpec) throws DataSourceException {
        try {
            this.uri = dataSpec.uri;
            transferInitializing(dataSpec);
            if (!inUseFileDescriptors.add(this.fileDescriptor)) {
                throw new DataSourceException(new IllegalStateException("Attempted to re-use an already in-use file descriptor"), -2);
            }
            if (this.length != -1 && dataSpec.position > this.length) {
                throw new DataSourceException(2008);
            }
            seekFileDescriptor(this.fileDescriptor, this.offset + dataSpec.position);
            FileInputStream fileInputStream = new FileInputStream(this.fileDescriptor);
            this.inputStream = fileInputStream;
            long j = this.length;
            if (j == -1) {
                FileChannel channel = fileInputStream.getChannel();
                long size = channel.size();
                if (size == 0) {
                    this.bytesRemaining = -1L;
                } else {
                    long jPosition = size - channel.position();
                    this.bytesRemaining = jPosition;
                    if (jPosition < 0) {
                        throw new DataSourceException(2008);
                    }
                }
            } else {
                long j2 = j - dataSpec.position;
                this.bytesRemaining = j2;
                if (j2 < 0) {
                    throw new DataSourceException(2008);
                }
            }
            if (dataSpec.length != -1) {
                long j3 = this.bytesRemaining;
                this.bytesRemaining = j3 == -1 ? dataSpec.length : Math.min(j3, dataSpec.length);
            }
            this.opened = true;
            transferStarted(dataSpec);
            return dataSpec.length != -1 ? dataSpec.length : this.bytesRemaining;
        } catch (DataSourceException e) {
            throw e;
        } catch (IOException e2) {
            throw new DataSourceException(e2, e2 instanceof FileNotFoundException ? PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND : 2000);
        }
    }

    @Override // androidx.media3.common.DataReader
    public int read(byte[] bArr, int i, int i2) throws DataSourceException {
        if (i2 == 0) {
            return 0;
        }
        long j = this.bytesRemaining;
        if (j == 0) {
            return -1;
        }
        if (j != -1) {
            i2 = (int) Math.min(j, i2);
        }
        try {
            int i3 = ((FileInputStream) Util.castNonNull(this.inputStream)).read(bArr, i, i2);
            if (i3 == -1) {
                return -1;
            }
            long j2 = this.bytesRemaining;
            if (j2 != -1) {
                this.bytesRemaining = j2 - ((long) i3);
            }
            bytesTransferred(i3);
            return i3;
        } catch (IOException e) {
            throw new DataSourceException(e, 2000);
        }
    }

    @Override // androidx.media3.datasource.DataSource
    public Uri getUri() {
        return this.uri;
    }

    /* JADX WARN: Bottom block not found for handler: all -> 0x001e */
    @Override // androidx.media3.datasource.DataSource
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void close() throws androidx.media3.datasource.DataSourceException {
        /*
            r5 = this;
            r0 = 0
            r5.uri = r0
            java.util.Set<java.io.FileDescriptor> r1 = androidx.media3.datasource.FileDescriptorDataSource.inUseFileDescriptors
            java.io.FileDescriptor r2 = r5.fileDescriptor
            r1.remove(r2)
            r1 = 0
            java.io.FileInputStream r2 = r5.inputStream     // Catch: java.lang.Throwable -> L1e java.io.IOException -> L20
            if (r2 == 0) goto L12
            r2.close()     // Catch: java.lang.Throwable -> L1e java.io.IOException -> L20
        L12:
            r5.inputStream = r0
            boolean r0 = r5.opened
            if (r0 == 0) goto L1d
            r5.opened = r1
            r5.transferEnded()
        L1d:
            return
        L1e:
            r2 = move-exception
            goto L29
        L20:
            r2 = move-exception
            androidx.media3.datasource.DataSourceException r3 = new androidx.media3.datasource.DataSourceException     // Catch: java.lang.Throwable -> L1e
            r4 = 2000(0x7d0, float:2.803E-42)
            r3.<init>(r2, r4)     // Catch: java.lang.Throwable -> L1e
            throw r3     // Catch: java.lang.Throwable -> L1e
        L29:
            r5.inputStream = r0
            boolean r0 = r5.opened
            if (r0 == 0) goto L34
            r5.opened = r1
            r5.transferEnded()
        L34:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.media3.datasource.FileDescriptorDataSource.close():void");
    }

    private static void seekFileDescriptor(FileDescriptor fileDescriptor, long j) throws DataSourceException {
        try {
            Os.lseek(fileDescriptor, j, OsConstants.SEEK_SET);
        } catch (ErrnoException e) {
            throw new DataSourceException(e, 2000);
        }
    }
}
