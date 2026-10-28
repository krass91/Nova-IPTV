package androidx.media3.common.util;

/* JADX INFO: loaded from: classes.dex */
public final class GlRect {
    public int bottom;
    public int left;
    public int right;
    public int top;

    public GlRect(int i, int i2) {
        this(0, 0, i, i2);
    }

    public GlRect(int i, int i2, int i3, int i4) {
        Assertions.checkArgument(i <= i3 && i2 <= i4);
        this.left = i;
        this.bottom = i2;
        this.right = i3;
        this.top = i4;
    }
}
