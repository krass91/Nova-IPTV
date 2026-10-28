package bg.novatv.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.media3.extractor.text.ttml.TtmlNode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class GuideActivity extends Activity {
    List<Models.Channel> channels;
    Epg epg;
    int labelWidth;
    ListView list;
    LinearLayout root;
    final long span = 7200000;
    long start;
    int timeline;

    Button b(String str, final Runnable runnable) {
        Button button = Ui.button(this, str, false);
        button.setOnClickListener(new View.OnClickListener() { // from class: bg.novatv.app.GuideActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                runnable.run();
            }
        });
        return button;
    }

    int d(float f) {
        return Ui.dp(this, f);
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.channels = MainActivity.Session.channels;
        Epg epg = MainActivity.Session.epg;
        this.epg = epg;
        if (this.channels == null || epg == null) {
            finish();
            return;
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        long j = jCurrentTimeMillis - (jCurrentTimeMillis % 1800000);
        this.start = j;
        if (bundle != null) {
            this.start = bundle.getLong(TtmlNode.START, j);
        }
        build();
    }

    void build() {
        LinearLayout linearLayoutVertical = Ui.vertical(this);
        this.root = linearLayoutVertical;
        linearLayoutVertical.setBackgroundColor(-15986662);
        this.root.setPadding(d(16.0f), d(16.0f), d(16.0f), d(16.0f));
        setContentView(this.root);
        this.root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: bg.novatv.app.GuideActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return this.f$0.m190lambda$1$bgnovatvappGuideActivity(view, windowInsets);
            }
        });
        this.root.requestApplyInsets();
        LinearLayout linearLayoutHorizontal = Ui.horizontal(this);
        TextView textViewText = Ui.text(this, "Телевизионна програма", 23, -1117185);
        boolean z = true;
        textViewText.setTypeface(Typeface.DEFAULT, 1);
        int i = 0;
        linearLayoutHorizontal.addView(textViewText, new LinearLayout.LayoutParams(0, -2, 1.0f));
        linearLayoutHorizontal.addView(b("Назад", new Runnable() { // from class: bg.novatv.app.GuideActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m191lambda$2$bgnovatvappGuideActivity();
            }
        }), Ui.margin(this, -2, d(44.0f), 3));
        int i2 = -1;
        this.root.addView(linearLayoutHorizontal, Ui.lp(-1, d(54.0f)));
        LinearLayout linearLayoutHorizontal2 = Ui.horizontal(this);
        linearLayoutHorizontal2.addView(b("‹ 1 час", new Runnable() { // from class: bg.novatv.app.GuideActivity$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m192lambda$3$bgnovatvappGuideActivity();
            }
        }), Ui.margin(this, -2, d(44.0f), 3));
        linearLayoutHorizontal2.addView(b("Сега", new Runnable() { // from class: bg.novatv.app.GuideActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m193lambda$4$bgnovatvappGuideActivity();
            }
        }), Ui.margin(this, -2, d(44.0f), 3));
        linearLayoutHorizontal2.addView(b("1 час ›", new Runnable() { // from class: bg.novatv.app.GuideActivity$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m194lambda$5$bgnovatvappGuideActivity();
            }
        }), Ui.margin(this, -2, d(44.0f), 3));
        TextView textViewText2 = Ui.text(this, new SimpleDateFormat("EEE, dd MMM · HH:mm", new Locale("bg")).format(new Date(this.start)), 13, -6640955);
        Ui.pad(textViewText2, 8);
        linearLayoutHorizontal2.addView(textViewText2);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        horizontalScrollView.addView(linearLayoutHorizontal2);
        this.root.addView(horizontalScrollView, Ui.lp(-1, d(55.0f)));
        int i3 = 4;
        this.root.addView(Ui.text(this, "Избери канал за гледане или предаване за подробности. Плъзни хоризонтално за часовете.", 12, -6640955), Ui.margin(this, -1, -2, 4));
        int i4 = getResources().getConfiguration().screenWidthDp;
        this.labelWidth = d(i4 + (-40) >= 700 ? 190 : 145);
        this.timeline = d(Math.max(720, i4 - 230));
        HorizontalScrollView horizontalScrollView2 = new HorizontalScrollView(this);
        horizontalScrollView2.setFillViewport(false);
        LinearLayout linearLayoutVertical2 = Ui.vertical(this);
        horizontalScrollView2.addView(linearLayoutVertical2, new FrameLayout.LayoutParams(this.labelWidth + this.timeline, -1));
        this.root.addView(horizontalScrollView2, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayoutHorizontal3 = Ui.horizontal(this);
        TextView textViewText3 = Ui.text(this, "КАНАЛ", 12, -10296117);
        Ui.pad(textViewText3, 12);
        linearLayoutHorizontal3.addView(textViewText3, Ui.lp(this.labelWidth, d(40.0f)));
        int i5 = 0;
        while (i5 < i3) {
            TextView textViewText4 = Ui.text(this, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(this.start + (((long) i5) * 1800000))), 13, -10296117);
            Ui.pad(textViewText4, 12);
            i3 = 4;
            linearLayoutHorizontal3.addView(textViewText4, Ui.lp(this.timeline / 4, d(40.0f)));
            i5++;
            z = true;
            i = 0;
            i2 = -1;
        }
        linearLayoutVertical2.addView(linearLayoutHorizontal3, Ui.lp(i2, d(42.0f)));
        ListView listView = new ListView(this);
        this.list = listView;
        listView.setDivider(null);
        this.list.setDividerHeight(d(6.0f));
        this.list.setItemsCanFocus(z);
        this.list.setSelector(new ColorDrawable(i));
        this.list.setAdapter((ListAdapter) new AnonymousClass1());
        linearLayoutVertical2.addView(this.list, new LinearLayout.LayoutParams(i2, i, 1.0f));
    }

    /* JADX INFO: renamed from: lambda$1$bg-novatv-app-GuideActivity, reason: not valid java name */
    /* synthetic */ WindowInsets m190lambda$1$bgnovatvappGuideActivity(View view, WindowInsets windowInsets) {
        this.root.setPadding(d(16.0f) + windowInsets.getSystemWindowInsetLeft(), d(16.0f) + windowInsets.getSystemWindowInsetTop(), d(16.0f) + windowInsets.getSystemWindowInsetRight(), d(16.0f) + windowInsets.getSystemWindowInsetBottom());
        return windowInsets;
    }

    /* JADX INFO: renamed from: lambda$2$bg-novatv-app-GuideActivity, reason: not valid java name */
    /* synthetic */ void m191lambda$2$bgnovatvappGuideActivity() {
        finish();
    }

    /* JADX INFO: renamed from: lambda$3$bg-novatv-app-GuideActivity, reason: not valid java name */
    /* synthetic */ void m192lambda$3$bgnovatvappGuideActivity() {
        this.start -= 3600000;
        build();
    }

    /* JADX INFO: renamed from: lambda$4$bg-novatv-app-GuideActivity, reason: not valid java name */
    /* synthetic */ void m193lambda$4$bgnovatvappGuideActivity() {
        long jCurrentTimeMillis = System.currentTimeMillis();
        this.start = jCurrentTimeMillis - (jCurrentTimeMillis % 1800000);
        build();
    }

    /* JADX INFO: renamed from: lambda$5$bg-novatv-app-GuideActivity, reason: not valid java name */
    /* synthetic */ void m194lambda$5$bgnovatvappGuideActivity() {
        this.start += 3600000;
        build();
    }

    /* JADX INFO: renamed from: bg.novatv.app.GuideActivity$1, reason: invalid class name */
    class AnonymousClass1 extends BaseAdapter {
        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        AnonymousClass1() {
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return GuideActivity.this.channels.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return GuideActivity.this.channels.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(final int i, View view, ViewGroup viewGroup) {
            boolean z;
            final Models.Channel channel = GuideActivity.this.channels.get(i);
            LinearLayout linearLayoutHorizontal = Ui.horizontal(GuideActivity.this);
            int i2 = 0;
            boolean z2 = true;
            Button buttonB = GuideActivity.this.b(String.format(Locale.US, "%02d  %s", Integer.valueOf(i + 1), channel.name), new Runnable() { // from class: bg.novatv.app.GuideActivity$1$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m196lambda$0$bgnovatvappGuideActivity$1(i);
                }
            });
            buttonB.setGravity(19);
            buttonB.setTextSize(14.0f);
            buttonB.setMaxLines(2);
            char c = 0;
            linearLayoutHorizontal.addView(buttonB, Ui.lp(GuideActivity.this.labelWidth, GuideActivity.this.d(76.0f)));
            FrameLayout frameLayout = new FrameLayout(GuideActivity.this);
            frameLayout.setBackground(Ui.shape(-15393748, 0, 0, GuideActivity.this.d(8.0f)));
            linearLayoutHorizontal.addView(frameLayout, Ui.lp(GuideActivity.this.timeline, GuideActivity.this.d(76.0f)));
            for (final Models.Program program : GuideActivity.this.epg.forChannel(channel)) {
                if (program.end <= GuideActivity.this.start || program.start >= GuideActivity.this.start + 7200000) {
                    z2 = z2;
                    c = c;
                    i2 = i2;
                } else {
                    long jMax = Math.max(GuideActivity.this.start, program.start);
                    int i3 = i2;
                    long jMin = Math.min(GuideActivity.this.start + 7200000, program.end);
                    int i4 = (int) (((jMax - GuideActivity.this.start) * ((long) GuideActivity.this.timeline)) / 7200000);
                    int iMax = Math.max(GuideActivity.this.d(10.0f), (int) (((jMin - jMax) * ((long) GuideActivity.this.timeline)) / 7200000));
                    Button buttonB2 = GuideActivity.this.b(String.valueOf(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(program.start))) + "\n" + program.title, new Runnable() { // from class: bg.novatv.app.GuideActivity$1$$ExternalSyntheticLambda1
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m197lambda$1$bgnovatvappGuideActivity$1(channel, program, i);
                        }
                    });
                    buttonB2.setGravity(19);
                    buttonB2.setTextSize(13.0f);
                    buttonB2.setMaxLines(3);
                    buttonB2.setPadding(GuideActivity.this.d(10.0f), GuideActivity.this.d(4.0f), GuideActivity.this.d(10.0f), GuideActivity.this.d(4.0f));
                    if (program.start > System.currentTimeMillis() || program.end <= System.currentTimeMillis()) {
                        z = true;
                    } else {
                        z = true;
                        buttonB2.setBackground(Ui.focus(GuideActivity.this, true));
                    }
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Math.max(GuideActivity.this.d(10.0f), iMax - GuideActivity.this.d(3.0f)), GuideActivity.this.d(76.0f));
                    layoutParams.leftMargin = i4;
                    frameLayout.addView(buttonB2, layoutParams);
                    z2 = z;
                    c = 0;
                    i2 = i3 + 1;
                }
            }
            if (i2 == 0) {
                TextView textViewText = Ui.text(GuideActivity.this, "Няма програма за този часови диапазон", 13, -6640955);
                Ui.pad(textViewText, 20);
                frameLayout.addView(textViewText);
            }
            return linearLayoutHorizontal;
        }

        /* JADX INFO: renamed from: lambda$0$bg-novatv-app-GuideActivity$1, reason: not valid java name */
        /* synthetic */ void m196lambda$0$bgnovatvappGuideActivity$1(int i) {
            GuideActivity.this.play(i);
        }

        /* JADX INFO: renamed from: lambda$1$bg-novatv-app-GuideActivity$1, reason: not valid java name */
        /* synthetic */ void m197lambda$1$bgnovatvappGuideActivity$1(Models.Channel channel, Models.Program program, int i) {
            GuideActivity.this.detail(channel, program, i);
        }
    }

    void detail(Models.Channel channel, Models.Program program, final int i) {
        String str = String.valueOf(new SimpleDateFormat("dd MMM · HH:mm", new Locale("bg")).format(new Date(program.start))) + " – " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(program.end));
        AlertDialog.Builder title = new AlertDialog.Builder(this).setTitle(program.title);
        StringBuilder sb = new StringBuilder(String.valueOf(channel.name));
        sb.append("\n");
        sb.append(str);
        sb.append("\n\n");
        sb.append(program.description == null ? "" : program.description);
        title.setMessage(sb.toString()).setPositiveButton("Гледай канала", new DialogInterface.OnClickListener() { // from class: bg.novatv.app.GuideActivity$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i2) {
                this.f$0.m195lambda$6$bgnovatvappGuideActivity(i, dialogInterface, i2);
            }
        }).setNegativeButton("Назад", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$6$bg-novatv-app-GuideActivity, reason: not valid java name */
    /* synthetic */ void m195lambda$6$bgnovatvappGuideActivity(int i, DialogInterface dialogInterface, int i2) {
        play(i);
    }

    void play(int i) {
        Intent intent = new Intent(this, (Class<?>) PlayerActivity.class);
        intent.putExtra("index", i);
        intent.putExtra("buffer", getIntent().getIntExtra("buffer", 1));
        startActivity(intent);
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        build();
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putLong(TtmlNode.START, this.start);
        super.onSaveInstanceState(bundle);
    }
}
