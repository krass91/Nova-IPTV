package com.google.common.util.concurrent.internal;

/* JADX INFO: loaded from: classes.dex */
public abstract class InternalFutureFailureAccess {
    protected abstract Throwable tryInternalFastPathGetFailure();

    protected InternalFutureFailureAccess() {
    }
}
