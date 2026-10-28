package com.google.common.io;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import javax.annotation.CheckForNull;

/* JADX INFO: loaded from: classes.dex */
@ElementTypesAreNonnullByDefault
public final class Closer implements Closeable {
    private static final Suppressor SUPPRESSING_SUPPRESSOR = new Suppressor() { // from class: com.google.common.io.Closer$$ExternalSyntheticLambda0
        @Override // com.google.common.io.Closer.Suppressor
        public final void suppress(Closeable closeable, Throwable th, Throwable th2) {
            Closer.lambda$static$0(closeable, th, th2);
        }
    };
    private final Deque<Closeable> stack = new ArrayDeque(4);
    final Suppressor suppressor;

    @CheckForNull
    private Throwable thrown;

    interface Suppressor {
        void suppress(Closeable closeable, Throwable thrown, Throwable suppressed);
    }

    public static Closer create() {
        return new Closer(SUPPRESSING_SUPPRESSOR);
    }

    Closer(Suppressor suppressor) {
        this.suppressor = (Suppressor) Preconditions.checkNotNull(suppressor);
    }

    @ParametricNullness
    public <C extends Closeable> C register(@ParametricNullness C closeable) {
        if (closeable != null) {
            this.stack.addFirst(closeable);
        }
        return closeable;
    }

    public RuntimeException rethrow(Throwable e) throws Throwable {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.throwIfInstanceOf(e, IOException.class);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    public <X extends Exception> RuntimeException rethrow(Throwable e, Class<X> declaredType) throws Exception {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.throwIfInstanceOf(e, IOException.class);
        Throwables.throwIfInstanceOf(e, declaredType);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    public <X1 extends Exception, X2 extends Exception> RuntimeException rethrow(Throwable e, Class<X1> declaredType1, Class<X2> declaredType2) throws Exception {
        Preconditions.checkNotNull(e);
        this.thrown = e;
        Throwables.throwIfInstanceOf(e, IOException.class);
        Throwables.throwIfInstanceOf(e, declaredType1);
        Throwables.throwIfInstanceOf(e, declaredType2);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws Throwable {
        Throwable th = this.thrown;
        while (!this.stack.isEmpty()) {
            Closeable closeableRemoveFirst = this.stack.removeFirst();
            try {
                closeableRemoveFirst.close();
            } catch (Throwable th2) {
                if (th == null) {
                    th = th2;
                } else {
                    this.suppressor.suppress(closeableRemoveFirst, th, th2);
                }
            }
        }
        if (this.thrown != null || th == null) {
            return;
        }
        Throwables.throwIfInstanceOf(th, IOException.class);
        Throwables.throwIfUnchecked(th);
        throw new AssertionError(th);
    }

    static /* synthetic */ void lambda$static$0(Closeable closeable, Throwable th, Throwable th2) {
        if (th == th2) {
            return;
        }
        try {
            th.addSuppressed(th2);
        } catch (Throwable unused) {
            Closeables.logger.log(Level.WARNING, "Suppressing exception thrown when closing " + closeable, th2);
        }
    }
}
