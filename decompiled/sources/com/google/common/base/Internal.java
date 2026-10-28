package com.google.common.base;

import java.time.Duration;

/* JADX INFO: loaded from: classes.dex */
@ElementTypesAreNonnullByDefault
final class Internal {
    static long toNanosSaturated(Duration duration) {
        try {
            return duration.toNanos();
        } catch (ArithmeticException unused) {
            return duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
    }

    private Internal() {
    }
}
