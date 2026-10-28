package com.google.common.graph;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingSet;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
@ElementTypesAreNonnullByDefault
final class InvalidatableSet<E> extends ForwardingSet<E> {
    private final Set<E> delegate;
    private final Supplier<String> errorMessage;
    private final Supplier<Boolean> validator;

    public static final <E> InvalidatableSet<E> of(Set<E> delegate, Supplier<Boolean> validator, Supplier<String> errorMessage) {
        return new InvalidatableSet<>((Set) Preconditions.checkNotNull(delegate), (Supplier) Preconditions.checkNotNull(validator), (Supplier) Preconditions.checkNotNull(errorMessage));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.google.common.collect.ForwardingSet, com.google.common.collect.ForwardingCollection, com.google.common.collect.ForwardingObject
    public Set<E> delegate() {
        validate();
        return this.delegate;
    }

    private InvalidatableSet(Set<E> delegate, Supplier<Boolean> validator, Supplier<String> errorMessage) {
        this.delegate = delegate;
        this.validator = validator;
        this.errorMessage = errorMessage;
    }

    @Override // com.google.common.collect.ForwardingSet, java.util.Collection, java.util.Set
    public int hashCode() {
        return this.delegate.hashCode();
    }

    private void validate() {
        if (!this.validator.get().booleanValue()) {
            throw new IllegalStateException(this.errorMessage.get());
        }
    }
}
