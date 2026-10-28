package bg.novatv.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.media3.extractor.ts.TsExtractor;

/* JADX INFO: loaded from: classes.dex */
final class Ui {
    static final int ACCENT = -10296117;
    static final int BG = -15986662;
    static final int CARD = -14866631;
    static final int MUTED = -6640955;
    static final int PANEL = -15393748;
    static final int TEXT = -1117185;

    Ui() {
    }

    static int dp(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    static GradientDrawable shape(int i, int i2, int i3, float f) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(f);
        if (i3 > 0) {
            gradientDrawable.setStroke(i3, i2);
        }
        return gradientDrawable;
    }

    static StateListDrawable focus(Context context, boolean z) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        int[] iArr = {android.R.attr.state_focused};
        int iDp = dp(context, 2.0f);
        float fDp = dp(context, 10.0f);
        int i = ACCENT;
        stateListDrawable.addState(iArr, shape(-14072229, ACCENT, iDp, fDp));
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, shape(-14072229, ACCENT, dp(context, 2.0f), dp(context, 10.0f)));
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, shape(-13542807, ACCENT, dp(context, 2.0f), dp(context, 10.0f)));
        int[] iArr2 = new int[0];
        int i2 = z ? -14664377 : CARD;
        if (!z) {
            i = -14142133;
        }
        stateListDrawable.addState(iArr2, shape(i2, i, dp(context, 1.0f), dp(context, 10.0f)));
        return stateListDrawable;
    }

    static TextView text(Context context, String str, int i, int i2) {
        TextView textView = new TextView(context);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(i2);
        textView.setFontFeatureSettings("kern");
        return textView;
    }

    static Button button(Context context, String str, boolean z) {
        Button button = new Button(context);
        button.setText(str);
        button.setAllCaps(false);
        button.setTextSize(14.0f);
        button.setTextColor(TEXT);
        button.setMinHeight(dp(context, 44.0f));
        button.setMinimumHeight(dp(context, 44.0f));
        button.setPadding(dp(context, 14.0f), dp(context, 7.0f), dp(context, 14.0f), dp(context, 7.0f));
        button.setBackground(focus(context, z));
        button.setFocusable(true);
        button.setTypeface(Typeface.DEFAULT, 1);
        return button;
    }

    static LinearLayout vertical(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        return linearLayout;
    }

    static LinearLayout horizontal(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        return linearLayout;
    }

    static void pad(View view, int i) {
        int iDp = dp(view.getContext(), i);
        view.setPadding(iDp, iDp, iDp, iDp);
    }

    static LinearLayout.LayoutParams lp(int i, int i2) {
        return new LinearLayout.LayoutParams(i, i2);
    }

    static LinearLayout.LayoutParams margin(Context context, int i, int i2, int i3) {
        LinearLayout.LayoutParams layoutParamsLp = lp(i, i2);
        int iDp = dp(context, i3);
        layoutParamsLp.setMargins(iDp, iDp, iDp, iDp);
        return layoutParamsLp;
    }

    static EditText input(Context context, String str, String str2, boolean z) {
        EditText editText = new EditText(context);
        editText.setSingleLine(true);
        editText.setTextSize(16.0f);
        editText.setTextColor(TEXT);
        editText.setHintTextColor(MUTED);
        editText.setHint(str);
        editText.setText(str2);
        editText.setPadding(dp(context, 14.0f), dp(context, 8.0f), dp(context, 14.0f), dp(context, 8.0f));
        editText.setBackground(focus(context, false));
        editText.setInputType(z ? TsExtractor.TS_STREAM_TYPE_AC3 : 1);
        return editText;
    }

    static void toast(Context context, String str) {
        Toast.makeText(context, str, 1).show();
    }
}
