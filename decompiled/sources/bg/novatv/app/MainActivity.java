package bg.novatv.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* JADX INFO: loaded from: classes.dex */
public class MainActivity extends Activity {
    ChannelAdapter adapter;
    LinearLayout body;
    Models.Catalog catalog;
    TextView clock;
    LinearLayout detail;
    Future<?> epgTask;
    boolean importingFile;
    ListView list;
    LogoLoader logos;
    LinearLayout main;
    Models.Catalog pendingCatalog;
    Models.Profile pendingFile;
    TextView pendingFileLabel;
    EditText pendingFileName;
    EditText pendingFileUrl;
    Models.Profile profile;
    Repository repo;
    LinearLayout root;
    EditText search;
    Models.Channel selected;
    TextView status;
    Store store;
    Future<?> task;
    TextView title;
    boolean wide;
    Epg epg = new Epg();
    String screen = "live";
    String category = "Всички";
    String query = "";
    boolean busy = false;
    String message = "";
    volatile int generation = 0;
    final ExecutorService workers = Executors.newFixedThreadPool(3);
    final Handler handler = new Handler();
    final Map<String, Models.Catalog> cache = new HashMap();
    List<Models.Channel> visible = new ArrayList();
    int scrollPosition = 0;
    final Runnable clockTick = new Runnable() { // from class: bg.novatv.app.MainActivity.1
        @Override // java.lang.Runnable
        public void run() {
            if (MainActivity.this.clock != null) {
                MainActivity.this.clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            }
            if (MainActivity.this.adapter != null) {
                MainActivity.this.adapter.notifyDataSetChanged();
            }
            MainActivity.this.handler.postDelayed(this, 60000L);
        }
    };

    static class Session {
        static List<Models.Channel> channels;
        static Epg epg;
        static Models.Profile profile;
        static Repository repo;
        static Store store;

        Session() {
        }
    }

    /* JADX INFO: renamed from: lambda$0$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m200lambda$0$bgnovatvappMainActivity(DialogInterface dialogInterface, int i) {
        finish();
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.logos = new LogoLoader(this);
        getWindow().setStatusBarColor(-15986662);
        getWindow().setNavigationBarColor(-15986662);
        try {
            Store store = new Store(this);
            this.store = store;
            Models.Profile profileCurrent = store.current();
            this.profile = profileCurrent;
            if (profileCurrent != null) {
                this.repo = new Repository(profileCurrent);
            }
            build();
            if (this.profile != null) {
                load(false);
            }
            this.handler.post(this.clockTick);
        } catch (Exception unused) {
            new AlertDialog.Builder(this).setTitle("Профилите не могат да се прочетат").setMessage("Запазените данни не могат да се отключат. Не са изтрити. Опитай отново или изчисти данните от настройките на Android.").setPositiveButton("Затвори", new DialogInterface.OnClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda30
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    this.f$0.m200lambda$0$bgnovatvappMainActivity(dialogInterface, i);
                }
            }).show();
        }
    }

    Button button(String str, boolean z, final Runnable runnable) {
        Button button = Ui.button(this, str, z);
        button.setOnClickListener(new View.OnClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda22
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

    void toast(String str) {
        Ui.toast(this, str);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v0 */
    /* JADX WARN: Type inference failed for: r4v1, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r4v8 */
    void build() {
        LinearLayout linearLayout;
        LinearLayout linearLayout2;
        LinearLayout.LayoutParams layoutParams;
        int i = 1;
        ?? r4 = 0;
        this.wide = getResources().getConfiguration().screenWidthDp >= 700;
        LinearLayout linearLayoutVertical = Ui.vertical(this);
        this.root = linearLayoutVertical;
        linearLayoutVertical.setBackgroundColor(-15986662);
        float f = 12.0f;
        this.root.setPadding(d(16.0f), d(12.0f), d(16.0f), d(12.0f));
        setContentView(this.root);
        this.root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return this.f$0.m211lambda$2$bgnovatvappMainActivity(view, windowInsets);
            }
        });
        this.root.requestApplyInsets();
        LinearLayout linearLayoutHorizontal = Ui.horizontal(this);
        TextView textViewText = Ui.text(this, "NOVA", 23, -1117185);
        textViewText.setTypeface(Typeface.DEFAULT, 1);
        linearLayoutHorizontal.addView(textViewText);
        linearLayoutHorizontal.addView(Ui.text(this, "  IPTV", 13, -10296117));
        linearLayoutHorizontal.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1.0f));
        Models.Profile profile = this.profile;
        Button button = button(profile == null ? "＋ Добави профил" : String.valueOf(profile.name) + "  ▾", false, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m222lambda$3$bgnovatvappMainActivity();
            }
        });
        button.setMaxWidth(d(this.wide ? 280 : 175));
        button.setSingleLine();
        linearLayoutHorizontal.addView(button, Ui.margin(this, -2, d(44.0f), 3));
        TextView textViewText2 = Ui.text(this, new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()), 16, -6640955);
        this.clock = textViewText2;
        if (this.wide) {
            Ui.pad(textViewText2, 10);
            linearLayoutHorizontal.addView(this.clock);
        }
        this.root.addView(linearLayoutHorizontal, Ui.lp(-1, d(58.0f)));
        LinearLayout linearLayoutHorizontal2 = this.wide ? Ui.horizontal(this) : Ui.vertical(this);
        this.body = linearLayoutHorizontal2;
        linearLayoutHorizontal2.setGravity(48);
        this.root.addView(this.body, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayoutVertical2 = this.wide ? Ui.vertical(this) : Ui.horizontal(this);
        linearLayoutVertical2.setPadding(0, d(12.0f), d(this.wide ? 12 : 0), 0);
        String[] strArr = {"live", "favorites", "vod", "series", "profiles", "settings"};
        String[] strArr2 = {"▣  Телевизия", "★  Любими", "▶  Филми", "▤  Сериали", "☷  Профили", "⚙  Настройки"};
        int i2 = 0;
        while (i2 < 6) {
            final String str = strArr[i2];
            Button button2 = button(strArr2[i2], this.screen.equals(str), new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m233lambda$4$bgnovatvappMainActivity(str);
                }
            });
            if (this.wide) {
                button2.setGravity(19);
            }
            linearLayoutVertical2.addView(button2, Ui.margin(this, this.wide ? -1 : -2, d(48.0f), 3));
            i2++;
            i = 1;
            r4 = 0;
            f = 12.0f;
        }
        if (this.wide) {
            this.body.addView(linearLayoutVertical2, Ui.lp(d(164.0f), -1));
        } else {
            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
            horizontalScrollView.setHorizontalScrollBarEnabled(r4);
            horizontalScrollView.addView(linearLayoutVertical2);
            this.body.addView(horizontalScrollView, Ui.lp(-1, d(70.0f)));
        }
        LinearLayout linearLayoutVertical3 = Ui.vertical(this);
        this.main = linearLayoutVertical3;
        linearLayoutVertical3.setPadding(d(8.0f), d(f), d(8.0f), r4);
        if (this.wide) {
            linearLayout = this.body;
            linearLayout2 = this.main;
            layoutParams = new LinearLayout.LayoutParams(r4, -1, 1.0f);
        } else {
            linearLayout = this.body;
            linearLayout2 = this.main;
            layoutParams = new LinearLayout.LayoutParams(-1, r4, 1.0f);
        }
        linearLayout.addView(linearLayout2, layoutParams);
        LinearLayout linearLayoutHorizontal3 = Ui.horizontal(this);
        TextView textViewText3 = Ui.text(this, screenName(), this.wide ? 25 : 23, -1117185);
        this.title = textViewText3;
        textViewText3.setTypeface(Typeface.DEFAULT, i);
        linearLayoutHorizontal3.addView(this.title, new LinearLayout.LayoutParams(r4, -2, 1.0f));
        if (this.profile != null && (this.screen.equals("live") || this.screen.equals("favorites"))) {
            linearLayoutHorizontal3.addView(button("EPG", r4, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda3
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m236lambda$5$bgnovatvappMainActivity();
                }
            }), Ui.margin(this, d(64.0f), d(44.0f), 3));
        }
        linearLayoutHorizontal3.addView(button("↻", r4, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m237lambda$6$bgnovatvappMainActivity();
            }
        }), Ui.lp(d(50.0f), d(44.0f)));
        this.main.addView(linearLayoutHorizontal3, Ui.lp(-1, d(52.0f)));
        TextView textViewText4 = Ui.text(this, "", 12, -6640955);
        this.status = textViewText4;
        this.main.addView(textViewText4, Ui.margin(this, -1, -2, 3));
        if (this.profile == null) {
            empty();
            return;
        }
        if ((this.screen.equals("vod") || this.screen.equals("series")) && !this.profile.type.equals("xtream")) {
            TextView textViewText5 = Ui.text(this, "Филми и сериали са достъпни с Xtream Codes профил.\n\nM3U и MAC/STB профилите са в раздел Телевизия.", 17, -6640955);
            Ui.pad(textViewText5, 20);
            this.main.addView(textViewText5);
            setStatus();
            return;
        }
        EditText editTextInput = Ui.input(this, "Търси канал, филм или сериал…", this.query, r4);
        this.search = editTextInput;
        editTextInput.setInputType(524289);
        this.search.setImeOptions(3);
        this.main.addView(this.search, Ui.margin(this, -1, d(48.0f), 3));
        this.search.addTextChangedListener(new TextWatcher() { // from class: bg.novatv.app.MainActivity.2
            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i3, int i4, int i5) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i3, int i4, int i5) {
                MainActivity.this.query = charSequence.toString();
                MainActivity.this.filter();
            }
        });
        this.search.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda5
            @Override // android.widget.TextView.OnEditorActionListener
            public final boolean onEditorAction(TextView textView, int i3, KeyEvent keyEvent) {
                return this.f$0.m238lambda$7$bgnovatvappMainActivity(textView, i3, keyEvent);
            }
        });
        HorizontalScrollView horizontalScrollView2 = new HorizontalScrollView(this);
        horizontalScrollView2.setHorizontalScrollBarEnabled(r4);
        LinearLayout linearLayoutHorizontal4 = Ui.horizontal(this);
        horizontalScrollView2.addView(linearLayoutHorizontal4);
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet();
        linkedHashSet.add("Всички");
        Models.Catalog catalog = this.catalog;
        if (catalog != null) {
            Iterator<Models.Channel> it = catalog.items.iterator();
            while (it.hasNext()) {
                linkedHashSet.add(it.next().group);
            }
        }
        if (!linkedHashSet.contains(this.category)) {
            this.category = "Всички";
        }
        for (final String str2 : linkedHashSet) {
            linearLayoutHorizontal4.addView(button(str2, str2.equals(this.category), new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m239lambda$8$bgnovatvappMainActivity(str2);
                }
            }), Ui.margin(this, -2, d(40.0f), 3));
        }
        this.main.addView(horizontalScrollView2, Ui.lp(-1, d(53.0f)));
        ListView listView = new ListView(this);
        this.list = listView;
        listView.setDivider(null);
        this.list.setDividerHeight(d(7.0f));
        this.list.setSelector(new ColorDrawable(r4));
        this.list.setCacheColorHint(-15986662);
        this.list.setItemsCanFocus(r4);
        ChannelAdapter channelAdapter = new ChannelAdapter();
        this.adapter = channelAdapter;
        this.list.setAdapter((ListAdapter) channelAdapter);
        this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda7
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i3, long j) {
                this.f$0.m240lambda$9$bgnovatvappMainActivity(adapterView, view, i3, j);
            }
        });
        this.list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda8
            @Override // android.widget.AdapterView.OnItemLongClickListener
            public final boolean onItemLongClick(AdapterView adapterView, View view, int i3, long j) {
                return this.f$0.m201lambda$10$bgnovatvappMainActivity(adapterView, view, i3, j);
            }
        });
        this.list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: bg.novatv.app.MainActivity.3
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> adapterView, View view, int i3, long j) {
                if (i3 < MainActivity.this.visible.size()) {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.selected = mainActivity.visible.get(i3);
                    MainActivity.this.updateDetail();
                }
            }
        });
        this.list.setOnKeyListener(new View.OnKeyListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda9
            @Override // android.view.View.OnKeyListener
            public final boolean onKey(View view, int i3, KeyEvent keyEvent) {
                return this.f$0.m202lambda$11$bgnovatvappMainActivity(view, i3, keyEvent);
            }
        });
        this.main.addView(this.list, new LinearLayout.LayoutParams(-1, r4, 1.0f));
        filter();
        this.list.setSelection(Math.min(this.scrollPosition, Math.max((int) r4, this.visible.size() - i)));
        if (!this.wide || getResources().getConfiguration().screenWidthDp < 1050) {
            this.detail = null;
        } else {
            LinearLayout linearLayoutVertical4 = Ui.vertical(this);
            this.detail = linearLayoutVertical4;
            linearLayoutVertical4.setBackground(Ui.shape(-15393748, r4, r4, d(14.0f)));
            Ui.pad(this.detail, 18);
            this.body.addView(this.detail, Ui.margin(this, d(278.0f), -1, 8));
            updateDetail();
        }
        setStatus();
        if (this.busy) {
            return;
        }
        this.list.requestFocus();
    }

    /* JADX INFO: renamed from: lambda$2$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ WindowInsets m211lambda$2$bgnovatvappMainActivity(View view, WindowInsets windowInsets) {
        this.root.setPadding(d(16.0f) + windowInsets.getSystemWindowInsetLeft(), d(12.0f) + windowInsets.getSystemWindowInsetTop(), d(16.0f) + windowInsets.getSystemWindowInsetRight(), d(12.0f) + windowInsets.getSystemWindowInsetBottom());
        return windowInsets;
    }

    /* JADX INFO: renamed from: lambda$4$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m233lambda$4$bgnovatvappMainActivity(String str) {
        if (str.equals("profiles")) {
            m222lambda$3$bgnovatvappMainActivity();
        } else if (str.equals("settings")) {
            settings();
        } else {
            switchScreen(str);
        }
    }

    /* JADX INFO: renamed from: lambda$6$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m237lambda$6$bgnovatvappMainActivity() {
        load(true);
    }

    /* JADX INFO: renamed from: lambda$7$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ boolean m238lambda$7$bgnovatvappMainActivity(TextView textView, int i, KeyEvent keyEvent) {
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.search.getWindowToken(), 0);
        ListView listView = this.list;
        if (listView == null) {
            return true;
        }
        listView.requestFocus();
        return true;
    }

    /* JADX INFO: renamed from: lambda$8$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m239lambda$8$bgnovatvappMainActivity(String str) {
        this.category = str;
        this.scrollPosition = 0;
        build();
    }

    /* JADX INFO: renamed from: lambda$10$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ boolean m201lambda$10$bgnovatvappMainActivity(AdapterView adapterView, View view, int i, long j) {
        showChannel(this.visible.get(i));
        return true;
    }

    /* JADX INFO: renamed from: lambda$11$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ boolean m202lambda$11$bgnovatvappMainActivity(View view, int i, KeyEvent keyEvent) {
        int selectedItemPosition;
        if (keyEvent.getAction() != 0) {
            return false;
        }
        if ((i != 82 && i != 22) || (selectedItemPosition = this.list.getSelectedItemPosition()) < 0 || selectedItemPosition >= this.visible.size()) {
            return false;
        }
        showChannel(this.visible.get(selectedItemPosition));
        return true;
    }

    /* JADX INFO: renamed from: lambda$9$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m240lambda$9$bgnovatvappMainActivity(AdapterView adapterView, View view, int i, long j) {
        m234lambda$40$bgnovatvappMainActivity(this.visible.get(i));
    }

    String screenName() {
        if (this.screen.equals("favorites")) {
            return "Твоите любими";
        }
        if (this.screen.equals("vod")) {
            return "Филми";
        }
        return this.screen.equals("series") ? "Сериали" : "Телевизия на живо";
    }

    void empty() {
        this.status.setText("Твоите канали. Твоят екран.");
        LinearLayout linearLayoutVertical = Ui.vertical(this);
        Ui.pad(linearLayoutVertical, this.wide ? 30 : 20);
        linearLayoutVertical.setBackground(Ui.shape(-15393748, 0, 0, d(16.0f)));
        TextView textViewText = Ui.text(this, "Всичко започва\nс твоя профил.", this.wide ? 34 : 27, -1117185);
        textViewText.setTypeface(Typeface.DEFAULT, 1);
        linearLayoutVertical.addView(textViewText);
        TextView textViewText2 = Ui.text(this, "Добави M3U листа, Xtream Codes или MAC/STB портал от твоя доставчик.\n\nУправлявай с дистанционно или докосване. Приложението не включва телевизионен абонамент.", 16, -6640955);
        textViewText2.setLineSpacing(d(5.0f), 1.0f);
        linearLayoutVertical.addView(textViewText2, Ui.margin(this, -1, -2, 8));
        Button button = button("＋ Добави профил", true, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda31
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m203lambda$12$bgnovatvappMainActivity();
            }
        });
        linearLayoutVertical.addView(button, Ui.margin(this, -1, d(50.0f), 5));
        linearLayoutVertical.addView(button("📂 M3U файл от устройството", false, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda32
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m204lambda$13$bgnovatvappMainActivity();
            }
        }), Ui.margin(this, -1, d(50.0f), 5));
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.addView(linearLayoutVertical);
        this.main.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        button.requestFocus();
    }

    /* JADX INFO: renamed from: lambda$12$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m203lambda$12$bgnovatvappMainActivity() {
        editProfile(null);
    }

    void setStatus() {
        String string;
        TextView textView = this.status;
        if (textView == null) {
            return;
        }
        if (this.busy) {
            string = "◌  Зареждане от доставчика…";
        } else if (this.message.isEmpty()) {
            String str = "";
            if (this.profile == null) {
                string = "";
            } else {
                StringBuilder sb = new StringBuilder(String.valueOf(this.visible.size()));
                sb.append(" заглавия  •  ");
                sb.append(typeName(this.profile.type));
                if (this.epg.count > 0) {
                    str = "  •  EPG " + this.epg.count;
                }
                sb.append(str);
                string = sb.toString();
            }
        } else {
            string = this.message;
        }
        textView.setText(string);
        this.status.setTextColor(this.message.isEmpty() ? -6640955 : -12911);
    }

    String typeName(String str) {
        if (str.equals("file")) {
            return "M3U файл";
        }
        if (str.equals("stb")) {
            return "MAC / STB";
        }
        return str.equals("xtream") ? "Xtream Codes" : "M3U";
    }

    void switchScreen(String str) {
        this.screen = str;
        this.category = "Всички";
        this.query = "";
        this.selected = null;
        this.scrollPosition = 0;
        if (str.equals("favorites")) {
            str = "live";
        }
        if (!this.cache.containsKey(str)) {
            this.catalog = null;
            build();
            load(false);
            return;
        }
        this.catalog = this.cache.get(str);
        build();
        if (str.equals("live") && this.epg.count == 0 && !this.catalog.epg.isEmpty()) {
            loadEpg(this.catalog.epg, this.generation);
        }
    }

    void filter() {
        this.visible.clear();
        Models.Catalog catalog = this.catalog;
        if (catalog != null) {
            for (Models.Channel channel : catalog.items) {
                if (!this.screen.equals("favorites") || this.store.favorite(this.profile, channel)) {
                    if (this.category.equals("Всички") || this.category.equals(channel.group)) {
                        String lowerCase = this.query.toLowerCase(Locale.ROOT);
                        if (lowerCase.isEmpty() || channel.name.toLowerCase(Locale.ROOT).contains(lowerCase) || channel.group.toLowerCase(Locale.ROOT).contains(lowerCase)) {
                            this.visible.add(channel);
                        }
                    }
                }
            }
        }
        ChannelAdapter channelAdapter = this.adapter;
        if (channelAdapter != null) {
            channelAdapter.notifyDataSetChanged();
        }
        setStatus();
    }

    /* JADX INFO: renamed from: lambda$14$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m205lambda$14$bgnovatvappMainActivity(boolean z, Models.Profile profile, final String str, final int i, Repository repository) {
        final Models.Catalog catalogLoad;
        try {
            if (this.catalog == null && !z) {
                try {
                    final Models.Catalog catalog = CatalogCache.read(this, this.store, profile, str);
                    if (catalog != null) {
                        this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda10
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f$0.m206lambda$15$bgnovatvappMainActivity(i, catalog);
                            }
                        });
                    }
                } catch (Exception unused) {
                }
            }
            if (profile.type.equals("file")) {
                if (profile.url.equals("local:" + profile.id)) {
                    catalogLoad = CatalogCache.read(this, this.store, profile, "source");
                    if (catalogLoad == null) {
                        throw new IOException("Избери M3U файла отново чрез Редактирай профил.");
                    }
                } else {
                    Throwable th = null;
                    try {
                        InputStream inputStreamOpenInputStream = getContentResolver().openInputStream(Uri.parse(profile.url));
                        try {
                            if (inputStreamOpenInputStream == null) {
                                throw new IOException("Файлът не е достъпен.");
                            }
                            Models.Catalog catalog2 = M3u.parse(Net.read(inputStreamOpenInputStream, 26214400), "");
                            if (inputStreamOpenInputStream != null) {
                                inputStreamOpenInputStream.close();
                            }
                            catalogLoad = catalog2;
                        } catch (Throwable th2) {
                            if (inputStreamOpenInputStream != null) {
                                inputStreamOpenInputStream.close();
                            }
                            throw th2;
                        }
                    } catch (Throwable th3) {
                        if (0 == 0) {
                            throw th3;
                        }
                        if (null == th3) {
                            throw null;
                        }
                        th.addSuppressed(th3);
                        throw null;
                    }
                }
                if (!profile.epg.isEmpty()) {
                    catalogLoad.epg = profile.epg;
                }
            } else {
                catalogLoad = repository.load(str);
            }
            if (i != this.generation || Thread.currentThread().isInterrupted()) {
                return;
            }
            try {
                CatalogCache.save(this, this.store, profile, str, catalogLoad);
            } catch (Exception unused2) {
            }
            this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda11
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m207lambda$16$bgnovatvappMainActivity(i, catalogLoad, str);
                }
            });
        } catch (Throwable th4) {
            this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda12
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m208lambda$17$bgnovatvappMainActivity(i, th4);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$15$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m206lambda$15$bgnovatvappMainActivity(int i, Models.Catalog catalog) {
        if (i != this.generation || isFinishing()) {
            return;
        }
        this.catalog = catalog;
        build();
    }

    /* JADX INFO: renamed from: lambda$16$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m207lambda$16$bgnovatvappMainActivity(int i, Models.Catalog catalog, String str) {
        if (i != this.generation || isFinishing()) {
            return;
        }
        this.busy = false;
        this.catalog = catalog;
        this.cache.put(str, catalog);
        this.message = catalog.items.isEmpty() ? "Този раздел няма съдържание." : "";
        build();
        if (!str.equals("live") || catalog.epg.isEmpty()) {
            return;
        }
        loadEpg(catalog.epg, i);
    }

    /* JADX INFO: renamed from: lambda$17$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m208lambda$17$bgnovatvappMainActivity(int i, Throwable th) {
        if (i != this.generation || isFinishing()) {
            return;
        }
        this.busy = false;
        this.message = Net.friendly(th);
        build();
        toast(this.message);
    }

    void load(final boolean z) {
        if (this.profile == null) {
            build();
            return;
        }
        final String str = this.screen.equals("favorites") ? "live" : this.screen;
        if (str.equals("live") || this.profile.type.equals("xtream")) {
            if (!z && this.cache.containsKey(str)) {
                this.catalog = this.cache.get(str);
                build();
                return;
            }
            Future<?> future = this.task;
            if (future != null) {
                future.cancel(true);
            }
            final int i = this.generation + 1;
            this.generation = i;
            final Models.Profile profile = this.profile;
            final Repository repository = this.repo;
            this.busy = true;
            this.message = "";
            setStatus();
            this.task = this.workers.submit(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda23
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m205lambda$14$bgnovatvappMainActivity(z, profile, str, i, repository);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$18$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m209lambda$18$bgnovatvappMainActivity(String str, final int i) {
        Throwable th = null;
        try {
            InputStream inputStreamOpen = Net.open(str, this.repo.headers());
            try {
                final Epg epg = Epg.parse(inputStreamOpen, System.currentTimeMillis());
                this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda27
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m210lambda$19$bgnovatvappMainActivity(i, epg);
                    }
                });
            } finally {
                if (inputStreamOpen != null) {
                    inputStreamOpen.close();
                }
            }
        } catch (Throwable th2) {
            if (0 == 0) {
                throw th2;
            }
            if (null != th2) {
                try {
                    th.addSuppressed(th2);
                } catch (Exception e) {
                    this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda28
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m212lambda$20$bgnovatvappMainActivity(i, e);
                        }
                    });
                    return;
                }
            }
            throw null;
        }
    }

    /* JADX INFO: renamed from: lambda$19$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m210lambda$19$bgnovatvappMainActivity(int i, Epg epg) {
        if (i != this.generation || isFinishing()) {
            return;
        }
        this.epg = epg;
        ChannelAdapter channelAdapter = this.adapter;
        if (channelAdapter != null) {
            channelAdapter.notifyDataSetChanged();
        }
        setStatus();
        updateDetail();
    }

    /* JADX INFO: renamed from: lambda$20$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m212lambda$20$bgnovatvappMainActivity(int i, Exception exc) {
        if (i != this.generation || isFinishing()) {
            return;
        }
        toast("EPG: " + Net.friendly(exc));
    }

    void loadEpg(final String str, final int i) {
        Future<?> future = this.epgTask;
        if (future != null) {
            future.cancel(true);
        }
        this.epgTask = this.workers.submit(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda41
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m209lambda$18$bgnovatvappMainActivity(str, i);
            }
        });
    }

    void activate(Models.Profile profile) throws Exception {
        this.store.active = profile.id;
        this.store.save();
        this.generation++;
        this.profile = profile;
        this.repo = new Repository(profile);
        this.selected = null;
        this.catalog = null;
        this.cache.clear();
        this.epg = new Epg();
        this.screen = "live";
        this.category = "Всички";
        this.query = "";
        this.message = "";
        build();
        load(false);
    }

    /* JADX INFO: renamed from: lambda$21$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m213lambda$21$bgnovatvappMainActivity(AlertDialog alertDialog, Models.Profile profile) {
        alertDialog.dismiss();
        try {
            activate(profile);
        } catch (Exception unused) {
            toast("Профилът не се запази.");
        }
    }

    /* JADX INFO: renamed from: lambda$22$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m214lambda$22$bgnovatvappMainActivity(final Models.Profile profile, final AlertDialog alertDialog) {
        new AlertDialog.Builder(this).setTitle(profile.name).setItems(new String[]{"Редактирай", "Изтрий"}, new DialogInterface.OnClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda21
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m215lambda$23$bgnovatvappMainActivity(alertDialog, profile, dialogInterface, i);
            }
        }).show();
    }

    /* JADX INFO: renamed from: lambda$23$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m215lambda$23$bgnovatvappMainActivity(AlertDialog alertDialog, final Models.Profile profile, DialogInterface dialogInterface, int i) {
        alertDialog.dismiss();
        if (i == 0) {
            editProfile(profile);
        } else {
            new AlertDialog.Builder(this).setTitle("Изтриване на профил?").setMessage(profile.name).setPositiveButton("Изтрий", new DialogInterface.OnClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda29
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface2, int i2) {
                    this.f$0.m216lambda$24$bgnovatvappMainActivity(profile, dialogInterface2, i2);
                }
            }).setNegativeButton("Отказ", (DialogInterface.OnClickListener) null).show();
        }
    }

    /* JADX INFO: renamed from: lambda$24$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m216lambda$24$bgnovatvappMainActivity(Models.Profile profile, DialogInterface dialogInterface, int i) {
        try {
            this.store.remove(profile);
            Models.Profile profileCurrent = this.store.current();
            if (profileCurrent != null) {
                activate(profileCurrent);
            } else {
                this.profile = null;
                this.repo = null;
                this.catalog = null;
                this.cache.clear();
                this.generation++;
                build();
            }
        } catch (Exception unused) {
            toast("Профилът не може да бъде изтрит.");
        }
    }

    /* JADX INFO: renamed from: lambda$25$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m217lambda$25$bgnovatvappMainActivity(AlertDialog alertDialog) {
        alertDialog.dismiss();
        editProfile(null);
    }

    /* JADX INFO: renamed from: lambda$26$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m218lambda$26$bgnovatvappMainActivity(AlertDialog alertDialog) {
        alertDialog.dismiss();
        m204lambda$13$bgnovatvappMainActivity();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX INFO: renamed from: profiles, reason: merged with bridge method [inline-methods] */
    public void m222lambda$3$bgnovatvappMainActivity() {
        LinearLayout linearLayoutVertical = Ui.vertical(this);
        Ui.pad(linearLayoutVertical, 16);
        final AlertDialog alertDialogCreate = new AlertDialog.Builder(this).setTitle("Твоите профили").setView(linearLayoutVertical).setNegativeButton("Затвори", (DialogInterface.OnClickListener) null).create();
        for (final Models.Profile profile : this.store.profiles) {
            LinearLayout linearLayoutHorizontal = Ui.horizontal(this);
            Button button = button((profile.id.equals(this.store.active) ? "● " : "") + profile.name + " · " + typeName(profile.type), profile.id.equals(this.store.active), new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda17
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m213lambda$21$bgnovatvappMainActivity(alertDialogCreate, profile);
                }
            });
            button.setSingleLine();
            linearLayoutHorizontal.addView(button, new LinearLayout.LayoutParams(0, d(50.0f), 1.0f));
            linearLayoutHorizontal.addView(button("⋮", false, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda18
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m214lambda$22$bgnovatvappMainActivity(profile, alertDialogCreate);
                }
            }), Ui.margin(this, d(48.0f), d(50.0f), 2));
            linearLayoutVertical.addView(linearLayoutHorizontal);
        }
        linearLayoutVertical.addView(button("＋ Нов профил", false, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda19
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m217lambda$25$bgnovatvappMainActivity(alertDialogCreate);
            }
        }), Ui.margin(this, -1, d(48.0f), 5));
        linearLayoutVertical.addView(button("📂 M3U файл от устройството", false, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda20
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m218lambda$26$bgnovatvappMainActivity(alertDialogCreate);
            }
        }), Ui.margin(this, -1, d(48.0f), 5));
        alertDialogCreate.show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX INFO: renamed from: editFileProfile, reason: merged with bridge method [inline-methods] */
    public void m204lambda$13$bgnovatvappMainActivity() {
        Models.Profile profile = new Models.Profile();
        profile.type = "file";
        editProfile(profile);
    }

    void editProfile(final Models.Profile profile) {
        String str;
        final MainActivity mainActivity = this;
        boolean z = profile == null || !mainActivity.store.profiles.contains(profile);
        Models.Profile profile2 = profile == null ? new Models.Profile() : profile;
        mainActivity.pendingFile = new Models.Profile();
        Object obj = null;
        mainActivity.pendingCatalog = null;
        mainActivity.importingFile = false;
        ScrollView scrollView = new ScrollView(mainActivity);
        LinearLayout linearLayoutVertical = Ui.vertical(this);
        Ui.pad(linearLayoutVertical, 16);
        scrollView.addView(linearLayoutVertical);
        Spinner spinner = new Spinner(mainActivity);
        final String[] strArr = {"m3u", "file", "xtream", "stb"};
        spinner.setAdapter((SpinnerAdapter) new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_dropdown_item, new String[]{"M3U адрес", "M3U файл", "Xtream Codes", "MAC / STB портал"}));
        int i = 0;
        int i2 = 0;
        while (i < 4) {
            Models.Profile profile3 = profile2;
            Object obj2 = obj;
            Spinner spinner2 = spinner;
            ScrollView scrollView2 = scrollView;
            if (strArr[i].equals(profile3.type)) {
                i2 = i;
            }
            i++;
            mainActivity = this;
            spinner = spinner2;
            scrollView = scrollView2;
            profile2 = profile3;
            obj = obj2;
        }
        linearLayoutVertical.addView(spinner, Ui.margin(mainActivity, -1, mainActivity.d(48.0f), 4));
        spinner.setSelection(i2);
        final EditText editTextField = mainActivity.field(linearLayoutVertical, "Име на профила", profile2.name, false);
        final EditText editTextField2 = mainActivity.field(linearLayoutVertical, "Адрес: https://server:port", profile2.url, false);
        final EditText editTextField3 = mainActivity.field(linearLayoutVertical, "Потребителско име", profile2.user, false);
        final EditText editTextField4 = mainActivity.field(linearLayoutVertical, "Парола", profile2.pass, true);
        final EditText editTextField5 = mainActivity.field(linearLayoutVertical, "MAC: 00:1A:79:12:34:56", profile2.mac, false);
        final EditText editTextField6 = mainActivity.field(linearLayoutVertical, "XMLTV адрес (по избор)", profile2.epg, false);
        final EditText editTextField7 = mainActivity.field(linearLayoutVertical, "User-Agent (по избор)", profile2.agent, false);
        final EditText editTextField8 = mainActivity.field(linearLayoutVertical, "STB сериен номер (по избор)", profile2.serial, false);
        final EditText editTextField9 = mainActivity.field(linearLayoutVertical, "STB Device ID (по избор)", profile2.device, false);
        editTextField2.setInputType(17);
        editTextField5.setInputType(524289);
        editTextField3.setInputType(524289);
        final TextView textViewText = Ui.text(mainActivity, "Ползвай данните, предоставени от твоя доставчик. Паролите се пазят криптирано на това устройство.", 12, -6640955);
        linearLayoutVertical.addView(textViewText, Ui.margin(mainActivity, -1, -2, 6));
        final Button button = mainActivity.button("Избери M3U файл…", false, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda24
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m219lambda$27$bgnovatvappMainActivity();
            }
        });
        linearLayoutVertical.addView(button, Ui.margin(mainActivity, -1, mainActivity.d(48.0f), 4));
        if (!profile2.type.equals("file") || profile2.url.isEmpty()) {
            str = "Няма избран файл. Поддържат се .m3u и .m3u8.";
        } else {
            str = "Запазена листа: " + profile2.name;
        }
        final TextView textViewText2 = Ui.text(mainActivity, str, 13, -10296117);
        linearLayoutVertical.addView(textViewText2, Ui.margin(mainActivity, -1, -2, 4));
        mainActivity.pendingFileName = editTextField;
        mainActivity.pendingFileUrl = editTextField2;
        mainActivity.pendingFileLabel = textViewText2;
        mainActivity.pendingFile.id = profile2.id;
        final Models.Profile profile4 = profile2;
        final Spinner spinner3 = spinner;
        ScrollView scrollView3 = scrollView;
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: bg.novatv.app.MainActivity.4
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> adapterView, View view, int i3, long j) {
                String str2;
                String str3 = strArr[i3];
                editTextField3.setVisibility(str3.equals("xtream") ? 0 : 8);
                editTextField4.setVisibility(str3.equals("xtream") ? 0 : 8);
                editTextField5.setVisibility(str3.equals("stb") ? 0 : 8);
                editTextField8.setVisibility(str3.equals("stb") ? 0 : 8);
                editTextField9.setVisibility(str3.equals("stb") ? 0 : 8);
                button.setVisibility(str3.equals("file") ? 0 : 8);
                editTextField2.setVisibility(str3.equals("file") ? 8 : 0);
                textViewText2.setVisibility(str3.equals("file") ? 0 : 8);
                TextView textView = textViewText;
                if (str3.equals("file")) {
                    str2 = "Избери листа от паметта, Downloads или USB. Запазва се криптирано копие. За обновяване избери файла отново. Каналите трябва да съдържат пълни интернет адреси.";
                } else {
                    str2 = str3.equals("stb") ? "Въведи регистрирания при доставчика MAC адрес. При нужда добави неговите сериен номер и Device ID. Поддържа се телевизия; някои портали изискват специфичен STB модел." : "Данните се пазят криптирано на устройството. M3U може да съдържа адрес за XMLTV програма.";
                }
                textView.setText(str2);
            }
        });
        final AlertDialog alertDialogCreate = new AlertDialog.Builder(mainActivity).setTitle(z ? "Добавяне на профил" : "Редактиране на профил").setView(scrollView3).setPositiveButton("Запази и свържи", (DialogInterface.OnClickListener) null).setNegativeButton("Отказ", (DialogInterface.OnClickListener) null).create();
        alertDialogCreate.setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda25
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                this.f$0.m220lambda$28$bgnovatvappMainActivity(dialogInterface);
            }
        });
        alertDialogCreate.show();
        if (mainActivity.wide) {
            alertDialogCreate.getWindow().setLayout(mainActivity.d(580.0f), -2);
        }
        Button button2 = alertDialogCreate.getButton(-1);
        final boolean z2 = z;
        button2.setOnClickListener(new View.OnClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda26
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m221lambda$29$bgnovatvappMainActivity(strArr, spinner3, editTextField2, editTextField, profile4, editTextField3, editTextField4, editTextField5, editTextField6, editTextField7, editTextField8, editTextField9, z2, profile, alertDialogCreate, view);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$27$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m219lambda$27$bgnovatvappMainActivity() {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.setType("*/*");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.addFlags(65);
        try {
            startActivityForResult(intent, 11);
        } catch (Exception unused) {
            toast("Няма приложение за избор на файл.");
        }
    }

    /* JADX INFO: renamed from: lambda$28$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m220lambda$28$bgnovatvappMainActivity(DialogInterface dialogInterface) {
        this.pendingFile = null;
        this.pendingCatalog = null;
        this.pendingFileName = null;
        this.pendingFileUrl = null;
        this.pendingFileLabel = null;
    }

    /* JADX INFO: renamed from: lambda$29$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m221lambda$29$bgnovatvappMainActivity(String[] strArr, Spinner spinner, EditText editText, EditText editText2, Models.Profile profile, EditText editText3, EditText editText4, EditText editText5, EditText editText6, EditText editText7, EditText editText8, EditText editText9, boolean z, Models.Profile profile2, AlertDialog alertDialog, View view) {
        Models.Catalog catalog;
        Models.Profile profile3;
        try {
            if (this.importingFile) {
                throw new IOException("Изчакай проверката на файла.");
            }
            String str = strArr[spinner.getSelectedItemPosition()];
            String strTrim = editText.getText().toString().trim();
            if (str.equals("file") && (profile3 = this.pendingFile) != null && !profile3.url.isEmpty()) {
                strTrim = this.pendingFile.url;
            }
            if (editText2.getText().toString().trim().isEmpty()) {
                throw new IOException("Въведи име на профила.");
            }
            if (!str.equals("file")) {
                Net.root(strTrim);
            } else if (!strTrim.startsWith("content://")) {
                if (!strTrim.equals("local:" + profile.id)) {
                    throw new IOException("Първо избери M3U файл.");
                }
            }
            if (str.equals("xtream") && (editText3.getText().toString().isEmpty() || editText4.getText().toString().isEmpty())) {
                throw new IOException("Въведи потребителско име и парола.");
            }
            if (str.equals("stb") && !editText5.getText().toString().trim().matches("(?i)[0-9a-f]{2}(:[0-9a-f]{2}){5}")) {
                throw new IOException("Въведи валиден MAC адрес.");
            }
            String strTrim2 = editText6.getText().toString().trim();
            if (!strTrim2.isEmpty()) {
                Net.root(strTrim2);
            }
            Models.Profile profile4 = new Models.Profile();
            profile4.id = profile.id;
            profile4.type = str;
            profile4.name = editText2.getText().toString().trim();
            profile4.url = strTrim;
            profile4.user = editText3.getText().toString();
            profile4.pass = editText4.getText().toString();
            profile4.mac = editText5.getText().toString().trim().toUpperCase(Locale.ROOT);
            profile4.epg = strTrim2;
            profile4.agent = editText7.getText().toString().trim();
            profile4.serial = editText8.getText().toString().trim();
            profile4.device = editText9.getText().toString().trim();
            if (profile4.agent.isEmpty()) {
                profile4.agent = "NovaIPTV/1.0";
            }
            if (str.equals("file") && (catalog = this.pendingCatalog) != null) {
                CatalogCache.save(this, this.store, profile4, "source", catalog);
            }
            CatalogCache.clear(this, profile4);
            if (z) {
                this.store.profiles.add(profile4);
            } else {
                this.store.profiles.set(this.store.profiles.indexOf(profile2), profile4);
            }
            activate(profile4);
            alertDialog.dismiss();
        } catch (Exception e) {
            toast(Net.friendly(e));
        }
    }

    EditText field(LinearLayout linearLayout, String str, String str2, boolean z) {
        EditText editTextInput = Ui.input(this, str, str2, z);
        linearLayout.addView(editTextInput, Ui.margin(this, -1, d(50.0f), 4));
        return editTextInput;
    }

    /* JADX WARN: Code duplicated, block: B:15:0x002d  */
    /* JADX WARN: Code duplicated, block: B:25:0x003c A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:66:0x003e A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:69:? A[RETURN, SYNTHETIC] */
    /* JADX INFO: renamed from: lambda$30$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m223lambda$30$bgnovatvappMainActivity(Uri uri, final Models.Profile profile) throws Throwable {
        String str;
        String string;
        Throwable th;
        Throwable th2 = null;
        try {
            Cursor cursorQuery = getContentResolver().query(uri, new String[]{"_display_name"}, null, null, null);
            if (cursorQuery != null) {
                try {
                    if (cursorQuery.moveToFirst()) {
                        string = cursorQuery.getString(0);
                    } else {
                        string = "Моята M3U листа";
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursorQuery != null) {
                        try {
                            cursorQuery.close();
                        } catch (Throwable th4) {
                            th = th4;
                            str = "Моята M3U листа";
                            if (th != null) {
                                throw th;
                            }
                            if (th != th) {
                                try {
                                    th.addSuppressed(th);
                                } catch (Exception unused) {
                                    string = str;
                                }
                            }
                            throw th;
                        }
                    }
                    throw th;
                }
            } else {
                string = "Моята M3U листа";
            }
            if (cursorQuery != null) {
                try {
                    cursorQuery.close();
                } catch (Throwable th5) {
                    th = th5;
                    str = string;
                    th = null;
                    if (th != null) {
                        throw th;
                    }
                    if (th != th) {
                        th.addSuppressed(th);
                    }
                    throw th;
                }
            }
            try {
                try {
                    InputStream inputStreamOpenInputStream = getContentResolver().openInputStream(uri);
                    try {
                        if (inputStreamOpenInputStream == null) {
                            throw new IOException("Файлът не е достъпен.");
                        }
                        final Models.Catalog catalog = M3u.parse(Net.read(inputStreamOpenInputStream, 26214400), "");
                        if (inputStreamOpenInputStream != null) {
                            inputStreamOpenInputStream.close();
                        }
                        final String str2 = string != null ? string : "Моята M3U листа";
                        this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda39
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f$0.m224lambda$31$bgnovatvappMainActivity(profile, catalog, str2);
                            }
                        });
                    } catch (Throwable th6) {
                        if (inputStreamOpenInputStream == null) {
                            throw th6;
                        }
                        inputStreamOpenInputStream.close();
                        throw th6;
                    }
                } catch (Exception e) {
                    this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda40
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m225lambda$32$bgnovatvappMainActivity(profile, e);
                        }
                    });
                }
            } catch (Throwable th7) {
                if (0 == 0) {
                    throw th7;
                }
                if (null != th7) {
                    th2.addSuppressed(th7);
                }
                throw null;
            }
        } catch (Throwable th8) {
            th = th8;
            str = "Моята M3U листа";
        }
    }

    /* JADX INFO: renamed from: lambda$31$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m224lambda$31$bgnovatvappMainActivity(Models.Profile profile, Models.Catalog catalog, String str) {
        if (this.pendingFile != profile || isFinishing()) {
            return;
        }
        this.pendingCatalog = catalog;
        profile.url = "local:" + profile.id;
        this.importingFile = false;
        this.pendingFileUrl.setText(profile.url);
        if (this.pendingFileName.getText().toString().trim().isEmpty()) {
            this.pendingFileName.setText(str.replaceFirst("(?i)\\.m3u8?$", ""));
        }
        this.pendingFileLabel.setText("✓ " + str + " • " + catalog.items.size() + " канала");
        toast("Листата е проверена. Натисни Запази и свържи.");
    }

    /* JADX INFO: renamed from: lambda$32$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m225lambda$32$bgnovatvappMainActivity(Models.Profile profile, Exception exc) {
        if (this.pendingFile != profile || isFinishing()) {
            return;
        }
        this.importingFile = false;
        this.pendingFileLabel.setText(this.pendingCatalog == null ? "Неуспешен импорт. Избери валидна M3U листа." : "Новият файл не е приет. Предишният избор е запазен.");
        toast(Net.friendly(exc));
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        final Uri data;
        super.onActivityResult(i, i2, intent);
        if (i != 11 || i2 != -1 || intent == null || this.pendingFile == null || (data = intent.getData()) == null) {
            return;
        }
        final Models.Profile profile = this.pendingFile;
        this.importingFile = true;
        TextView textView = this.pendingFileLabel;
        if (textView != null) {
            textView.setText("Проверка на M3U файла…");
        }
        this.workers.submit(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda38
            @Override // java.lang.Runnable
            public final void run() throws Throwable {
                this.f$0.m223lambda$30$bgnovatvappMainActivity(data, profile);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$33$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m226lambda$33$bgnovatvappMainActivity(RadioGroup radioGroup, int i) {
        getPreferences(0).edit().putInt("buffer", i - 300).apply();
    }

    void settings() {
        LinearLayout linearLayoutVertical = Ui.vertical(this);
        Ui.pad(linearLayoutVertical, 16);
        linearLayoutVertical.addView(Ui.text(this, "Nova IPTV 1.0.1\nHLS • DASH • MPEG-TS • MP4 • RTSP\n\nБуферът се прилага при следващото стартиране на плейъра. Кодеците зависят от устройството.\n\nВ плейъра: ↑/↓ канал, OK контроли, Назад връщане. В списъка: задържане или → за програма и любими.\n\nПрофилите и кешът са криптирани. Няма реклами или аналитично следене.", 14, -6640955), Ui.margin(this, -1, -2, 5));
        linearLayoutVertical.addView(Ui.text(this, "БУФЕР", 13, -10296117), Ui.margin(this, -1, -2, 8));
        RadioGroup radioGroup = new RadioGroup(this);
        String[] strArr = {"Бърз · 3–10 секунди", "Балансиран · 8–25 секунди", "Стабилен · 20–50 секунди"};
        int i = getPreferences(0).getInt("buffer", 1);
        int i2 = 0;
        while (i2 < 3) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(i2 + 300);
            radioButton.setText(strArr[i2]);
            radioButton.setTextColor(-1117185);
            radioButton.setTextSize(15.0f);
            radioButton.setPadding(d(8.0f), d(8.0f), d(8.0f), d(8.0f));
            radioGroup.addView(radioButton);
            radioButton.setChecked(i == i2);
            i2++;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda35
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public final void onCheckedChanged(RadioGroup radioGroup2, int i3) {
                this.f$0.m226lambda$33$bgnovatvappMainActivity(radioGroup2, i3);
            }
        });
        linearLayoutVertical.addView(radioGroup);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayoutVertical);
        new AlertDialog.Builder(this).setTitle("Настройки").setView(scrollView).setPositiveButton("Готово", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$34$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m227lambda$34$bgnovatvappMainActivity(Repository repository, final Models.Channel channel, final int i) {
        try {
            final List<Models.Channel> listEpisodes = repository.episodes(channel);
            this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda36
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m228lambda$35$bgnovatvappMainActivity(i, listEpisodes, channel);
                }
            });
        } catch (Exception e) {
            this.handler.post(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda37
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m230lambda$37$bgnovatvappMainActivity(e);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$35$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m228lambda$35$bgnovatvappMainActivity(int i, final List list, Models.Channel channel) {
        if (i != this.generation || isFinishing()) {
            return;
        }
        this.busy = false;
        setStatus();
        if (isFinishing()) {
            return;
        }
        if (list.isEmpty()) {
            toast("Няма епизоди за този сериал.");
            return;
        }
        int size = list.size();
        String[] strArr = new String[size];
        for (int i2 = 0; i2 < size; i2++) {
            strArr[i2] = String.valueOf(((Models.Channel) list.get(i2)).group) + " · " + ((Models.Channel) list.get(i2)).name;
        }
        new AlertDialog.Builder(this).setTitle(channel.name).setItems(strArr, new DialogInterface.OnClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda14
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i3) {
                this.f$0.m229lambda$36$bgnovatvappMainActivity(list, dialogInterface, i3);
            }
        }).setNegativeButton("Назад", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: renamed from: lambda$36$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m229lambda$36$bgnovatvappMainActivity(List list, DialogInterface dialogInterface, int i) {
        startPlayer(list, i);
    }

    /* JADX INFO: renamed from: lambda$37$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m230lambda$37$bgnovatvappMainActivity(Exception exc) {
        this.busy = false;
        setStatus();
        toast(Net.friendly(exc));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX INFO: renamed from: play, reason: merged with bridge method [inline-methods] */
    public void m234lambda$40$bgnovatvappMainActivity(final Models.Channel channel) {
        if (!channel.kind.equals("series")) {
            startPlayer(new ArrayList(this.visible), this.visible.indexOf(channel));
            return;
        }
        this.busy = true;
        setStatus();
        final int i = this.generation;
        final Repository repository = this.repo;
        this.workers.submit(new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m227lambda$34$bgnovatvappMainActivity(repository, channel, i);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX INFO: renamed from: guide, reason: merged with bridge method [inline-methods] */
    public void m236lambda$5$bgnovatvappMainActivity() {
        if (this.visible.isEmpty()) {
            toast("Първо зареди канали.");
            return;
        }
        Session.profile = this.profile;
        Session.repo = this.repo;
        Session.channels = new ArrayList(this.visible);
        Session.epg = this.epg;
        Session.store = this.store;
        Intent intent = new Intent(this, (Class<?>) GuideActivity.class);
        intent.putExtra("buffer", getPreferences(0).getInt("buffer", 1));
        startActivity(intent);
    }

    void startPlayer(List<Models.Channel> list, int i) {
        if (i < 0) {
            return;
        }
        Session.profile = this.profile;
        Session.repo = this.repo;
        Session.channels = list;
        Session.epg = this.epg;
        Session.store = this.store;
        Intent intent = new Intent(this, (Class<?>) PlayerActivity.class);
        intent.putExtra("index", i);
        intent.putExtra("buffer", getPreferences(0).getInt("buffer", 1));
        startActivity(intent);
    }

    /* JADX INFO: renamed from: lambda$38$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m231lambda$38$bgnovatvappMainActivity(Models.Channel channel, DialogInterface dialogInterface, int i) {
        m234lambda$40$bgnovatvappMainActivity(channel);
    }

    /* JADX INFO: renamed from: lambda$39$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m232lambda$39$bgnovatvappMainActivity(Models.Channel channel, DialogInterface dialogInterface, int i) {
        try {
            this.store.toggle(this.profile, channel);
            filter();
            updateDetail();
        } catch (Exception unused) {
            toast("Любимите не се запазиха.");
        }
    }

    void showChannel(final Models.Channel channel) {
        List<Models.Program> listForChannel = this.epg.forChannel(channel);
        StringBuilder sb = new StringBuilder(String.valueOf(channel.group) + "\n\n");
        int i = 0;
        for (Models.Program program : listForChannel) {
            if (program.end > System.currentTimeMillis()) {
                int i2 = i + 1;
                if (i < 8) {
                    sb.append(time(program.start));
                    sb.append(" – ");
                    sb.append(time(program.end));
                    sb.append("  ");
                    sb.append(program.title);
                    sb.append("\n");
                }
                i = i2;
            }
        }
        if (i == 0) {
            sb.append("Няма налична програма. Добави XMLTV адрес или провери tvg-id на канала.");
        }
        new AlertDialog.Builder(this).setTitle(channel.name).setMessage(sb.toString()).setPositiveButton("▶ Гледай", new DialogInterface.OnClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda15
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i3) {
                this.f$0.m231lambda$38$bgnovatvappMainActivity(channel, dialogInterface, i3);
            }
        }).setNeutralButton(this.store.favorite(this.profile, channel) ? "★ Премахни от любими" : "☆ В любими", new DialogInterface.OnClickListener() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda16
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i3) {
                this.f$0.m232lambda$39$bgnovatvappMainActivity(channel, dialogInterface, i3);
            }
        }).setNegativeButton("Назад", (DialogInterface.OnClickListener) null).show();
    }

    String time(long j) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(j));
    }

    class ChannelAdapter extends BaseAdapter {
        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return i;
        }

        ChannelAdapter() {
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return MainActivity.this.visible.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return MainActivity.this.visible.get(i);
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            LinearLayout linearLayoutHorizontal;
            TextView textViewText;
            TextView textViewText2;
            TextView textViewText3;
            ImageView imageView;
            String string;
            if (view == null) {
                linearLayoutHorizontal = Ui.horizontal(MainActivity.this);
                linearLayoutHorizontal.setPadding(MainActivity.this.d(15.0f), MainActivity.this.d(10.0f), MainActivity.this.d(15.0f), MainActivity.this.d(10.0f));
                linearLayoutHorizontal.setMinimumHeight(MainActivity.this.d(77.0f));
                textViewText3 = Ui.text(MainActivity.this, "", 13, -10296117);
                textViewText3.setGravity(17);
                linearLayoutHorizontal.addView(textViewText3, Ui.lp(MainActivity.this.d(38.0f), -1));
                imageView = new ImageView(MainActivity.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                MainActivity mainActivity = MainActivity.this;
                linearLayoutHorizontal.addView(imageView, Ui.margin(mainActivity, mainActivity.d(44.0f), MainActivity.this.d(44.0f), 5));
                LinearLayout linearLayoutVertical = Ui.vertical(MainActivity.this);
                textViewText = Ui.text(MainActivity.this, "", 16, -1117185);
                textViewText.setTypeface(Typeface.DEFAULT, 1);
                textViewText.setSingleLine();
                textViewText.setEllipsize(TextUtils.TruncateAt.END);
                textViewText2 = Ui.text(MainActivity.this, "", 12, -6640955);
                textViewText2.setSingleLine();
                textViewText2.setEllipsize(TextUtils.TruncateAt.END);
                linearLayoutVertical.addView(textViewText);
                linearLayoutVertical.addView(textViewText2, Ui.margin(MainActivity.this, -1, -2, 2));
                linearLayoutHorizontal.addView(linearLayoutVertical, new LinearLayout.LayoutParams(0, -2, 1.0f));
                linearLayoutHorizontal.setTag(new Object[]{textViewText3, textViewText, textViewText2, imageView});
                linearLayoutHorizontal.setBackground(Ui.focus(MainActivity.this, false));
            } else {
                linearLayoutHorizontal = (LinearLayout) view;
                Object[] objArr = (Object[]) linearLayoutHorizontal.getTag();
                TextView textView = (TextView) objArr[0];
                textViewText = (TextView) objArr[1];
                textViewText2 = (TextView) objArr[2];
                ImageView imageView2 = (ImageView) objArr[3];
                textViewText3 = textView;
                imageView = imageView2;
            }
            Models.Channel channel = MainActivity.this.visible.get(i);
            imageView.setVisibility(channel.logo.isEmpty() ? 8 : 0);
            MainActivity.this.logos.load(channel.logo, imageView);
            textViewText3.setText(MainActivity.this.store.favorite(MainActivity.this.profile, channel) ? "★" : String.format(Locale.US, "%02d", Integer.valueOf(i + 1)));
            textViewText.setText(channel.name);
            Models.Program programNow = MainActivity.this.epg.now(channel);
            if (programNow == null) {
                StringBuilder sb = new StringBuilder(String.valueOf(channel.group));
                sb.append(channel.kind.equals("series") ? " · Избери епизод" : "");
                string = sb.toString();
            } else {
                string = String.valueOf(MainActivity.this.time(programNow.start)) + " – " + MainActivity.this.time(programNow.end) + "  " + programNow.title;
            }
            textViewText2.setText(string);
            return linearLayoutHorizontal;
        }
    }

    /* JADX INFO: renamed from: lambda$41$bg-novatv-app-MainActivity, reason: not valid java name */
    /* synthetic */ void m235lambda$41$bgnovatvappMainActivity(Models.Channel channel) {
        try {
            this.store.toggle(this.profile, channel);
            filter();
            updateDetail();
        } catch (Exception unused) {
            toast("Неуспешно запазване.");
        }
    }

    void updateDetail() {
        LinearLayout linearLayout = this.detail;
        if (linearLayout == null) {
            return;
        }
        linearLayout.removeAllViews();
        final Models.Channel channel = this.selected;
        int i = 0;
        if (channel == null && !this.visible.isEmpty()) {
            channel = this.visible.get(0);
        }
        if (channel == null) {
            this.detail.addView(Ui.text(this, "Избери канал", 18, -6640955));
            return;
        }
        this.detail.addView(Ui.text(this, channel.kind.equals("live") ? "●  НА ЖИВО" : "▶  БИБЛИОТЕКА", 11, -10296117));
        TextView textViewText = Ui.text(this, channel.name, 23, -1117185);
        textViewText.setTypeface(Typeface.DEFAULT, 1);
        this.detail.addView(textViewText, Ui.margin(this, -1, -2, 8));
        this.detail.addView(Ui.text(this, channel.group, 13, -6640955));
        this.detail.addView(button("▶ Гледай", true, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda33
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m234lambda$40$bgnovatvappMainActivity(channel);
            }
        }), Ui.margin(this, -1, d(48.0f), 8));
        this.detail.addView(button(this.store.favorite(this.profile, channel) ? "★ В любими" : "☆ Добави в любими", false, new Runnable() { // from class: bg.novatv.app.MainActivity$$ExternalSyntheticLambda34
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m235lambda$41$bgnovatvappMainActivity(channel);
            }
        }), Ui.margin(this, -1, d(44.0f), 8));
        this.detail.addView(Ui.text(this, "ПРОГРАМА", 12, -10296117), Ui.margin(this, -1, -2, 8));
        for (Models.Program program : this.epg.forChannel(channel)) {
            if (program.end > System.currentTimeMillis()) {
                this.detail.addView(Ui.text(this, String.valueOf(time(program.start)) + "  " + program.title, 14, -1117185), Ui.margin(this, -1, -2, 6));
                i++;
                if (i == 6) {
                    break;
                }
            }
        }
        if (i == 0) {
            this.detail.addView(Ui.text(this, "Няма XMLTV програма\nза този канал.", 14, -6640955));
        }
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        ListView listView = this.list;
        if (listView != null) {
            this.scrollPosition = listView.getFirstVisiblePosition();
        }
        build();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (this.adapter != null) {
            filter();
            updateDetail();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        LogoLoader logoLoader = this.logos;
        if (logoLoader != null) {
            logoLoader.close();
        }
        this.generation++;
        Future<?> future = this.task;
        if (future != null) {
            future.cancel(true);
        }
        Future<?> future2 = this.epgTask;
        if (future2 != null) {
            future2.cancel(true);
        }
        this.workers.shutdownNow();
        this.handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
