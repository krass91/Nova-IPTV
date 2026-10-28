package com.google.common.util.concurrent;

import javax.annotation.CheckForNull;

/* JADX INFO: loaded from: classes.dex */
@ElementTypesAreNonnullByDefault
public class ExecutionError extends Error {
    private static final long serialVersionUID = 0;

    @Deprecated
    protected ExecutionError() {
    }

    @Deprecated
    protected ExecutionError(@CheckForNull String message) {
        super(message);
    }

    public ExecutionError(@CheckForNull String message, @CheckForNull Error cause) {
        super(message, cause);
    }

    public ExecutionError(@CheckForNull Error cause) {
        super(cause);
    }
}
