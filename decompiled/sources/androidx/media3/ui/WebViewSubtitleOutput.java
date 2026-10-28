package androidx.media3.ui;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.FrameLayout;
import androidx.media3.common.text.Cue;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.Util;
import androidx.media3.extractor.text.ttml.TtmlNode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
final class WebViewSubtitleOutput extends FrameLayout implements SubtitleView.Output {
    private static final float CSS_LINE_HEIGHT = 1.2f;
    private static final String DEFAULT_BACKGROUND_CSS_CLASS = "default_bg";
    private float bottomPaddingFraction;
    private final CanvasSubtitleOutput canvasSubtitleOutput;
    private float defaultTextSize;
    private int defaultTextSizeType;
    private CaptionStyleCompat style;
    private List<Cue> textCues;
    private final WebView webView;

    private static int anchorTypeToTranslatePercent(int i) {
        if (i != 1) {
            return i != 2 ? 0 : -100;
        }
        return -50;
    }

    public WebViewSubtitleOutput(Context context) {
        this(context, null);
    }

    public WebViewSubtitleOutput(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.textCues = Collections.emptyList();
        this.style = CaptionStyleCompat.DEFAULT;
        this.defaultTextSize = 0.0533f;
        this.defaultTextSizeType = 0;
        this.bottomPaddingFraction = 0.08f;
        CanvasSubtitleOutput canvasSubtitleOutput = new CanvasSubtitleOutput(context, attributeSet);
        this.canvasSubtitleOutput = canvasSubtitleOutput;
        WebView webView = new WebView(context, attributeSet) { // from class: androidx.media3.ui.WebViewSubtitleOutput.1
            @Override // android.webkit.WebView, android.view.View
            public boolean onTouchEvent(MotionEvent motionEvent) {
                super.onTouchEvent(motionEvent);
                return false;
            }

            @Override // android.view.View
            public boolean performClick() {
                super.performClick();
                return false;
            }
        };
        this.webView = webView;
        webView.setBackgroundColor(0);
        addView(canvasSubtitleOutput);
        addView(webView);
    }

    @Override // androidx.media3.ui.SubtitleView.Output
    public void update(List<Cue> list, CaptionStyleCompat captionStyleCompat, float f, int i, float f2) {
        this.style = captionStyleCompat;
        this.defaultTextSize = f;
        this.defaultTextSizeType = i;
        this.bottomPaddingFraction = f2;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i2 = 0; i2 < list.size(); i2++) {
            Cue cue = list.get(i2);
            if (cue.bitmap != null) {
                arrayList.add(cue);
            } else {
                arrayList2.add(cue);
            }
        }
        if (!this.textCues.isEmpty() || !arrayList2.isEmpty()) {
            this.textCues = arrayList2;
            updateWebView();
        }
        this.canvasSubtitleOutput.update(arrayList, captionStyleCompat, f, i, f2);
        invalidate();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (!z || this.textCues.isEmpty()) {
            return;
        }
        updateWebView();
    }

    public void destroy() {
        this.webView.destroy();
    }

    /* JADX WARN: Code duplicated, block: B:25:0x0102  */
    /* JADX WARN: Code duplicated, block: B:26:0x0112  */
    /* JADX WARN: Code duplicated, block: B:29:0x012c  */
    /* JADX WARN: Code duplicated, block: B:30:0x012f  */
    /* JADX WARN: Code duplicated, block: B:33:0x0146  */
    /* JADX WARN: Code duplicated, block: B:35:0x0149 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:36:0x014b  */
    /* JADX WARN: Code duplicated, block: B:37:0x014e A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:39:0x0151 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:40:0x0153  */
    /* JADX WARN: Code duplicated, block: B:48:0x0169  */
    /* JADX WARN: Code duplicated, block: B:52:0x0191  */
    /* JADX WARN: Code duplicated, block: B:58:0x01ba  */
    /* JADX WARN: Code duplicated, block: B:62:0x022d  */
    /* JADX WARN: Code duplicated, block: B:63:0x024d  */
    private void updateWebView() {
        String invariant;
        int iAnchorTypeToTranslatePercent;
        int i;
        String invariant2;
        int i2;
        int i3;
        int i4;
        String str;
        String str2;
        String str3;
        String str4;
        int i5;
        SpannedToHtmlConverter.HtmlAndCss htmlAndCssConvert;
        Iterator it;
        SpannedToHtmlConverter.HtmlAndCss htmlAndCss;
        String str5;
        boolean z;
        WebViewSubtitleOutput webViewSubtitleOutput = this;
        StringBuilder sb = new StringBuilder();
        String cssRgba = HtmlUtils.toCssRgba(webViewSubtitleOutput.style.foregroundColor);
        String strConvertTextSizeToCss = webViewSubtitleOutput.convertTextSizeToCss(webViewSubtitleOutput.defaultTextSizeType, webViewSubtitleOutput.defaultTextSize);
        float f = CSS_LINE_HEIGHT;
        int i6 = 0;
        int i7 = 1;
        sb.append(Util.formatInvariant("<body><div style='-webkit-user-select:none;position:fixed;top:0;bottom:0;left:0;right:0;color:%s;font-size:%s;line-height:%.2f;text-shadow:%s;'>", cssRgba, strConvertTextSizeToCss, Float.valueOf(CSS_LINE_HEIGHT), convertCaptionStyleToCssTextShadow(webViewSubtitleOutput.style)));
        HashMap map = new HashMap();
        map.put(HtmlUtils.cssAllClassDescendantsSelector(DEFAULT_BACKGROUND_CSS_CLASS), Util.formatInvariant("background-color:%s;", HtmlUtils.toCssRgba(webViewSubtitleOutput.style.backgroundColor)));
        int i8 = 0;
        while (i8 < webViewSubtitleOutput.textCues.size()) {
            Cue cue = webViewSubtitleOutput.textCues.get(i8);
            float f2 = cue.position != -3.4028235E38f ? cue.position * 100.0f : 50.0f;
            int iAnchorTypeToTranslatePercent2 = anchorTypeToTranslatePercent(cue.positionAnchor);
            if (cue.line != -3.4028235E38f) {
                if (cue.lineType != i7) {
                    Object[] objArr = new Object[i7];
                    objArr[i6] = Float.valueOf(cue.line * 100.0f);
                    invariant = Util.formatInvariant("%.2f%%", objArr);
                    if (cue.verticalType == i7) {
                        iAnchorTypeToTranslatePercent = -anchorTypeToTranslatePercent(cue.lineAnchor);
                    } else {
                        iAnchorTypeToTranslatePercent = anchorTypeToTranslatePercent(cue.lineAnchor);
                    }
                } else if (cue.line >= 0.0f) {
                    Object[] objArr2 = new Object[i7];
                    objArr2[i6] = Float.valueOf(cue.line * f);
                    invariant = Util.formatInvariant("%.2fem", objArr2);
                    iAnchorTypeToTranslatePercent = i6;
                    i = iAnchorTypeToTranslatePercent;
                } else {
                    Object[] objArr3 = new Object[i7];
                    objArr3[i6] = Float.valueOf(((-cue.line) - 1.0f) * f);
                    invariant = Util.formatInvariant("%.2fem", objArr3);
                    i = i7;
                    iAnchorTypeToTranslatePercent = i6;
                }
                if (cue.size != -3.4028235E38f) {
                    Object[] objArr4 = new Object[i7];
                    objArr4[i6] = Float.valueOf(cue.size * 100.0f);
                    invariant2 = Util.formatInvariant("%.2f%%", objArr4);
                } else {
                    invariant2 = "fit-content";
                }
                String strConvertAlignmentToCss = convertAlignmentToCss(cue.textAlignment);
                String strConvertVerticalTypeToCss = convertVerticalTypeToCss(cue.verticalType);
                String strConvertTextSizeToCss2 = webViewSubtitleOutput.convertTextSizeToCss(cue.textSizeType, cue.textSize);
                if (cue.windowColorSet) {
                    i2 = cue.windowColor;
                } else {
                    i2 = webViewSubtitleOutput.style.windowColor;
                }
                String cssRgba2 = HtmlUtils.toCssRgba(i2);
                i3 = iAnchorTypeToTranslatePercent;
                i4 = cue.verticalType;
                str = TtmlNode.RIGHT;
                str2 = TtmlNode.LEFT;
                str3 = "top";
                if (i4 != 1) {
                    if (i != 0) {
                        str = TtmlNode.LEFT;
                    }
                    str2 = "top";
                    str3 = str;
                } else if (i4 != 2) {
                    if (i == 0) {
                        str = TtmlNode.LEFT;
                    }
                    str2 = "top";
                    str3 = str;
                } else if (i != 0) {
                    str3 = "bottom";
                }
                if (cue.verticalType != 2 || cue.verticalType == 1) {
                    str4 = "height";
                    i5 = i3;
                    i3 = iAnchorTypeToTranslatePercent2;
                } else {
                    str4 = "width";
                    i5 = iAnchorTypeToTranslatePercent2;
                }
                htmlAndCssConvert = SpannedToHtmlConverter.convert(cue.text, getContext().getResources().getDisplayMetrics().density);
                it = map.keySet().iterator();
                while (it.hasNext()) {
                    Iterator it2 = it;
                    String str6 = (String) it.next();
                    SpannedToHtmlConverter.HtmlAndCss htmlAndCss2 = htmlAndCssConvert;
                    str5 = (String) map.put(str6, (String) map.get(str6));
                    if (str5 != null || str5.equals(map.get(str6))) {
                        z = true;
                    } else {
                        z = false;
                    }
                    Assertions.checkState(z);
                    it = it2;
                    htmlAndCssConvert = htmlAndCss2;
                }
                htmlAndCss = htmlAndCssConvert;
                HashMap map2 = map;
                sb.append(Util.formatInvariant("<div style='position:absolute;z-index:%s;%s:%.2f%%;%s:%s;%s:%s;text-align:%s;writing-mode:%s;font-size:%s;background-color:%s;transform:translate(%s%%,%s%%)%s;'>", Integer.valueOf(i8), str2, Float.valueOf(f2), str3, invariant, str4, invariant2, strConvertAlignmentToCss, strConvertVerticalTypeToCss, strConvertTextSizeToCss2, cssRgba2, Integer.valueOf(i5), Integer.valueOf(i3), getBlockShearTransformFunction(cue)));
                sb.append(Util.formatInvariant("<span class='%s'>", DEFAULT_BACKGROUND_CSS_CLASS));
                if (cue.multiRowAlignment != null) {
                    sb.append(Util.formatInvariant("<span style='display:inline-block; text-align:%s;'>", convertAlignmentToCss(cue.multiRowAlignment)));
                    sb.append(htmlAndCss.html);
                    sb.append("</span>");
                } else {
                    sb.append(htmlAndCss.html);
                }
                sb.append("</span></div>");
                i8++;
                map = map2;
                i7 = 1;
                f = CSS_LINE_HEIGHT;
                i6 = 0;
                webViewSubtitleOutput = this;
            } else {
                Object[] objArr5 = new Object[i7];
                objArr5[i6] = Float.valueOf((1.0f - webViewSubtitleOutput.bottomPaddingFraction) * 100.0f);
                invariant = Util.formatInvariant("%.2f%%", objArr5);
                iAnchorTypeToTranslatePercent = -100;
            }
            i = i6;
            if (cue.size != -3.4028235E38f) {
                Object[] objArr6 = new Object[i7];
                objArr6[i6] = Float.valueOf(cue.size * 100.0f);
                invariant2 = Util.formatInvariant("%.2f%%", objArr6);
            } else {
                invariant2 = "fit-content";
            }
            String strConvertAlignmentToCss2 = convertAlignmentToCss(cue.textAlignment);
            String strConvertVerticalTypeToCss2 = convertVerticalTypeToCss(cue.verticalType);
            String strConvertTextSizeToCss3 = webViewSubtitleOutput.convertTextSizeToCss(cue.textSizeType, cue.textSize);
            if (cue.windowColorSet) {
                i2 = cue.windowColor;
            } else {
                i2 = webViewSubtitleOutput.style.windowColor;
            }
            String cssRgba3 = HtmlUtils.toCssRgba(i2);
            i3 = iAnchorTypeToTranslatePercent;
            i4 = cue.verticalType;
            str = TtmlNode.RIGHT;
            str2 = TtmlNode.LEFT;
            str3 = "top";
            if (i4 != 1) {
                if (i != 0) {
                    str = TtmlNode.LEFT;
                }
                str2 = "top";
                str3 = str;
            } else if (i4 != 2) {
                if (i == 0) {
                    str = TtmlNode.LEFT;
                }
                str2 = "top";
                str3 = str;
            } else if (i != 0) {
                str3 = "bottom";
            }
            if (cue.verticalType != 2) {
                str4 = "height";
                i5 = i3;
                i3 = iAnchorTypeToTranslatePercent2;
            } else {
                str4 = "height";
                i5 = i3;
                i3 = iAnchorTypeToTranslatePercent2;
            }
            htmlAndCssConvert = SpannedToHtmlConverter.convert(cue.text, getContext().getResources().getDisplayMetrics().density);
            it = map.keySet().iterator();
            while (it.hasNext()) {
                Iterator it3 = it;
                String str7 = (String) it.next();
                SpannedToHtmlConverter.HtmlAndCss htmlAndCss3 = htmlAndCssConvert;
                str5 = (String) map.put(str7, (String) map.get(str7));
                if (str5 != null) {
                    z = true;
                } else {
                    z = true;
                }
                Assertions.checkState(z);
                it = it3;
                htmlAndCssConvert = htmlAndCss3;
            }
            htmlAndCss = htmlAndCssConvert;
            HashMap map3 = map;
            sb.append(Util.formatInvariant("<div style='position:absolute;z-index:%s;%s:%.2f%%;%s:%s;%s:%s;text-align:%s;writing-mode:%s;font-size:%s;background-color:%s;transform:translate(%s%%,%s%%)%s;'>", Integer.valueOf(i8), str2, Float.valueOf(f2), str3, invariant, str4, invariant2, strConvertAlignmentToCss2, strConvertVerticalTypeToCss2, strConvertTextSizeToCss3, cssRgba3, Integer.valueOf(i5), Integer.valueOf(i3), getBlockShearTransformFunction(cue)));
            sb.append(Util.formatInvariant("<span class='%s'>", DEFAULT_BACKGROUND_CSS_CLASS));
            if (cue.multiRowAlignment != null) {
                sb.append(Util.formatInvariant("<span style='display:inline-block; text-align:%s;'>", convertAlignmentToCss(cue.multiRowAlignment)));
                sb.append(htmlAndCss.html);
                sb.append("</span>");
            } else {
                sb.append(htmlAndCss.html);
            }
            sb.append("</span></div>");
            i8++;
            map = map3;
            i7 = 1;
            f = CSS_LINE_HEIGHT;
            i6 = 0;
            webViewSubtitleOutput = this;
        }
        Map map4 = map;
        sb.append("</div></body></html>");
        StringBuilder sb2 = new StringBuilder("<html><head><style>");
        for (String str8 : map4.keySet()) {
            sb2.append(str8);
            sb2.append("{");
            sb2.append((String) map4.get(str8));
            sb2.append("}");
        }
        sb2.append("</style></head>");
        sb.insert(0, sb2.toString());
        this.webView.loadData(Base64.encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8), 1), "text/html", "base64");
    }

    private static String getBlockShearTransformFunction(Cue cue) {
        String str;
        if (cue.shearDegrees != 0.0f) {
            if (cue.verticalType == 2 || cue.verticalType == 1) {
                str = "skewY";
            } else {
                str = "skewX";
            }
            return Util.formatInvariant("%s(%.2fdeg)", str, Float.valueOf(cue.shearDegrees));
        }
        return "";
    }

    private String convertTextSizeToCss(int i, float f) {
        float fResolveTextSize = SubtitleViewUtils.resolveTextSize(i, f, getHeight(), (getHeight() - getPaddingTop()) - getPaddingBottom());
        return fResolveTextSize == -3.4028235E38f ? "unset" : Util.formatInvariant("%.2fpx", Float.valueOf(fResolveTextSize / getContext().getResources().getDisplayMetrics().density));
    }

    private static String convertCaptionStyleToCssTextShadow(CaptionStyleCompat captionStyleCompat) {
        int i = captionStyleCompat.edgeType;
        if (i == 1) {
            return Util.formatInvariant("1px 1px 0 %1$s, 1px -1px 0 %1$s, -1px 1px 0 %1$s, -1px -1px 0 %1$s", HtmlUtils.toCssRgba(captionStyleCompat.edgeColor));
        }
        if (i == 2) {
            return Util.formatInvariant("0.1em 0.12em 0.15em %s", HtmlUtils.toCssRgba(captionStyleCompat.edgeColor));
        }
        if (i == 3) {
            return Util.formatInvariant("0.06em 0.08em 0.15em %s", HtmlUtils.toCssRgba(captionStyleCompat.edgeColor));
        }
        if (i == 4) {
            return Util.formatInvariant("-0.05em -0.05em 0.15em %s", HtmlUtils.toCssRgba(captionStyleCompat.edgeColor));
        }
        return "unset";
    }

    private static String convertVerticalTypeToCss(int i) {
        if (i == 1) {
            return "vertical-rl";
        }
        if (i == 2) {
            return "vertical-lr";
        }
        return "horizontal-tb";
    }

    private static String convertAlignmentToCss(Layout.Alignment alignment) {
        if (alignment == null) {
            return TtmlNode.CENTER;
        }
        int i = AnonymousClass2.$SwitchMap$android$text$Layout$Alignment[alignment.ordinal()];
        if (i == 1) {
            return TtmlNode.START;
        }
        if (i != 2) {
            return TtmlNode.CENTER;
        }
        return TtmlNode.END;
    }

    /* JADX INFO: renamed from: androidx.media3.ui.WebViewSubtitleOutput$2, reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$android$text$Layout$Alignment;

        static {
            int[] iArr = new int[Layout.Alignment.values().length];
            $SwitchMap$android$text$Layout$Alignment = iArr;
            try {
                iArr[Layout.Alignment.ALIGN_NORMAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Layout.Alignment.ALIGN_OPPOSITE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Layout.Alignment.ALIGN_CENTER.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }
}
