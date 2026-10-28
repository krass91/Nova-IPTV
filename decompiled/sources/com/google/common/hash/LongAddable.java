package com.google.common.hash;

/* JADX INFO: loaded from: classes.dex */
@ElementTypesAreNonnullByDefault
interface LongAddable {
    void add(long x);

    void increment();

    long sum();
}
