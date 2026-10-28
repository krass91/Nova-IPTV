package bg.novatv.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.DeviceInfo;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.text.CueGroup;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.SubtitleView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* JADX INFO: loaded from: classes.dex */
public class PlayerActivity extends Activity {
    static PlayerActivity testingCurrent;
    ProgressBar busy;
    List<Models.Channel> channels;
    int index;
    TextView info;
    TextView name;
    LinearLayout panel;
    Button pause;
    ExoPlayer player;
    TextView position;
    Models.Channel resolved;
    Future<?> resolving;
    FrameLayout root;
    SeekBar seek;
    TextView state;
    SubtitleView subtitles;
    SurfaceView surface;
    FrameLayout videoBox;
    int generation = 0;
    int retries = 0;
    int aspect = 0;
    boolean controls = true;
    boolean stopped = true;
    long resumePosition = 0;
    float videoRatio = 1.7777778f;
    Handler handler = new Handler();
    ExecutorService worker = Executors.newSingleThreadExecutor();
    final Runnable retry = new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda3
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.m241lambda$0$bgnovatvappPlayerActivity();
        }
    };
    final Runnable hide = new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda4
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.m242lambda$1$bgnovatvappPlayerActivity();
        }
    };
    final Runnable progress = new Runnable() { // from class: bg.novatv.app.PlayerActivity.1
        @Override // java.lang.Runnable
        public void run() {
            if (PlayerActivity.this.player != null && !PlayerActivity.this.channels.get(PlayerActivity.this.index).kind.equals("live")) {
                long duration = PlayerActivity.this.player.getDuration();
                long currentPosition = PlayerActivity.this.player.getCurrentPosition();
                if (duration > 0) {
                    PlayerActivity.this.position.setText(String.valueOf(PlayerActivity.duration(currentPosition)) + " / " + PlayerActivity.duration(duration));
                    PlayerActivity.this.seek.setProgress((int) Math.min(1000L, (currentPosition * 1000) / duration));
                }
            }
            PlayerActivity.this.handler.postDelayed(this, 1000L);
        }
    };

    Button b(String str, final Runnable runnable) {
        Button button = Ui.button(this, str, false);
        button.setOnClickListener(new View.OnClickListener() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda5
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
        testingCurrent = this;
        List<Models.Channel> list = MainActivity.Session.channels;
        this.channels = list;
        if (list == null || list.isEmpty() || MainActivity.Session.repo == null) {
            startActivity(new Intent(this, (Class<?>) MainActivity.class));
            finish();
            return;
        }
        int iMax = Math.max(0, Math.min(getIntent().getIntExtra("index", 0), this.channels.size() - 1));
        this.index = iMax;
        if (bundle != null) {
            this.index = Math.max(0, Math.min(bundle.getInt("index", iMax), this.channels.size() - 1));
            this.resumePosition = bundle.getLong("position", 0L);
        }
        getWindow().addFlags(128);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        build();
    }

    void build() {
        FrameLayout frameLayout = new FrameLayout(this);
        this.root = frameLayout;
        frameLayout.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        this.root.setFocusable(true);
        this.root.setFocusableInTouchMode(true);
        setContentView(this.root);
        FrameLayout frameLayout2 = new FrameLayout(this);
        this.videoBox = frameLayout2;
        this.root.addView(frameLayout2, new FrameLayout.LayoutParams(-1, -1));
        SurfaceView surfaceView = new SurfaceView(this);
        this.surface = surfaceView;
        this.videoBox.addView(surfaceView, new FrameLayout.LayoutParams(-1, -1, 17));
        SubtitleView subtitleView = new SubtitleView(this);
        this.subtitles = subtitleView;
        subtitleView.setApplyEmbeddedStyles(true);
        this.subtitles.setApplyEmbeddedFontSizes(true);
        this.subtitles.setPadding(d(24.0f), d(24.0f), d(24.0f), d(84.0f));
        this.root.addView(this.subtitles, new FrameLayout.LayoutParams(-1, -1));
        this.root.setOnClickListener(new View.OnClickListener() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m256lambda$3$bgnovatvappPlayerActivity(view);
            }
        });
        this.surface.setOnClickListener(new View.OnClickListener() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m257lambda$4$bgnovatvappPlayerActivity(view);
            }
        });
        LinearLayout linearLayoutVertical = Ui.vertical(this);
        linearLayoutVertical.setPadding(d(24.0f), d(22.0f), d(24.0f), d(16.0f));
        linearLayoutVertical.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{-435417062, 790554}));
        TextView textViewText = Ui.text(this, "", 24, -1117185);
        this.name = textViewText;
        textViewText.setTypeface(Typeface.DEFAULT, 1);
        this.info = Ui.text(this, "", 14, -6640955);
        linearLayoutVertical.addView(this.name);
        linearLayoutVertical.addView(this.info);
        this.root.addView(linearLayoutVertical, new FrameLayout.LayoutParams(-1, d(114.0f), 48));
        linearLayoutVertical.setTag("upper");
        LinearLayout linearLayoutVertical2 = Ui.vertical(this);
        linearLayoutVertical2.setGravity(17);
        ProgressBar progressBar = new ProgressBar(this);
        this.busy = progressBar;
        linearLayoutVertical2.addView(progressBar, Ui.lp(d(44.0f), d(44.0f)));
        TextView textViewText2 = Ui.text(this, "Свързване…", 15, -1117185);
        this.state = textViewText2;
        textViewText2.setGravity(17);
        linearLayoutVertical2.addView(this.state, Ui.margin(this, -1, -2, 8));
        this.root.addView(linearLayoutVertical2, new FrameLayout.LayoutParams(-1, -2, 17));
        LinearLayout linearLayoutVertical3 = Ui.vertical(this);
        this.panel = linearLayoutVertical3;
        linearLayoutVertical3.setPadding(d(20.0f), d(20.0f), d(20.0f), d(20.0f));
        this.panel.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{-234090470, -1291055078, 790554}));
        TextView textViewText3 = Ui.text(this, "", 13, -6640955);
        this.position = textViewText3;
        this.panel.addView(textViewText3);
        SeekBar seekBar = new SeekBar(this);
        this.seek = seekBar;
        seekBar.setMax(1000);
        this.seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: bg.novatv.app.PlayerActivity.2
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar2, int i, boolean z) {
                if (z && PlayerActivity.this.player != null && PlayerActivity.this.player.isCurrentMediaItemSeekable()) {
                    PlayerActivity.this.player.seekTo((PlayerActivity.this.player.getDuration() * ((long) i)) / 1000);
                }
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar2) {
                PlayerActivity.this.handler.removeCallbacks(PlayerActivity.this.hide);
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar2) {
                PlayerActivity.this.scheduleHide();
            }
        });
        this.panel.addView(this.seek, Ui.lp(-1, d(35.0f)));
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        LinearLayout linearLayoutHorizontal = Ui.horizontal(this);
        horizontalScrollView.addView(linearLayoutHorizontal);
        linearLayoutHorizontal.addView(b("‹ Канал", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda12
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m258lambda$5$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        Button buttonB = b("Ⅱ Пауза", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m259lambda$6$bgnovatvappPlayerActivity();
            }
        });
        this.pause = buttonB;
        linearLayoutHorizontal.addView(buttonB, Ui.margin(this, -2, d(46.0f), 3));
        linearLayoutHorizontal.addView(b("Канал ›", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda14
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m260lambda$7$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        linearLayoutHorizontal.addView(b("★ Любими", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda15
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m261lambda$8$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        linearLayoutHorizontal.addView(b("Програма", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda16
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m262lambda$9$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        linearLayoutHorizontal.addView(b("Звук", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda17
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m243lambda$10$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        linearLayoutHorizontal.addView(b("Субтитри", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda18
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m244lambda$11$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        linearLayoutHorizontal.addView(b("Картина", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda19
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m245lambda$12$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        linearLayoutHorizontal.addView(b("↻ Свържи", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m246lambda$13$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        linearLayoutHorizontal.addView(b("Назад", new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m247lambda$14$bgnovatvappPlayerActivity();
            }
        }), Ui.margin(this, -2, d(46.0f), 3));
        this.panel.addView(horizontalScrollView, Ui.lp(-1, d(54.0f)));
        this.root.addView(this.panel, new FrameLayout.LayoutParams(-1, -2, 80));
        this.root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda9
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return this.f$0.m248lambda$15$bgnovatvappPlayerActivity(view, windowInsets);
            }
        });
        this.videoBox.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda10
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                this.f$0.m249lambda$16$bgnovatvappPlayerActivity(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.root.requestApplyInsets();
        this.controls = true;
        this.pause.requestFocus();
        updateInfo();
    }

    /* JADX INFO: renamed from: lambda$3$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m256lambda$3$bgnovatvappPlayerActivity(View view) {
        toggleControls();
    }

    /* JADX INFO: renamed from: lambda$4$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m257lambda$4$bgnovatvappPlayerActivity(View view) {
        toggleControls();
    }

    /* JADX INFO: renamed from: lambda$10$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m243lambda$10$bgnovatvappPlayerActivity() {
        tracks(1);
    }

    /* JADX INFO: renamed from: lambda$11$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m244lambda$11$bgnovatvappPlayerActivity() {
        tracks(3);
    }

    /* JADX INFO: renamed from: lambda$12$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m245lambda$12$bgnovatvappPlayerActivity() {
        String str;
        this.aspect = (this.aspect + 1) % 3;
        resize();
        int i = this.aspect;
        if (i == 0) {
            str = "Картина: оригинални пропорции";
        } else {
            str = i == 1 ? "Картина: запълни екрана" : "Картина: разтегни";
        }
        Ui.toast(this, str);
        scheduleHide();
    }

    /* JADX INFO: renamed from: lambda$13$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m246lambda$13$bgnovatvappPlayerActivity() {
        this.retries = 0;
        m241lambda$0$bgnovatvappPlayerActivity();
    }

    /* JADX INFO: renamed from: lambda$14$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m247lambda$14$bgnovatvappPlayerActivity() {
        finish();
    }

    /* JADX INFO: renamed from: lambda$15$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ WindowInsets m248lambda$15$bgnovatvappPlayerActivity(View view, WindowInsets windowInsets) {
        this.panel.setPadding(d(20.0f) + windowInsets.getSystemWindowInsetLeft(), d(20.0f), d(20.0f) + windowInsets.getSystemWindowInsetRight(), d(20.0f) + windowInsets.getSystemWindowInsetBottom());
        return windowInsets;
    }

    /* JADX INFO: renamed from: lambda$16$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m249lambda$16$bgnovatvappPlayerActivity(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        resize();
    }

    /* JADX INFO: renamed from: lambda$5$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m258lambda$5$bgnovatvappPlayerActivity() {
        zap(-1);
    }

    /* JADX INFO: renamed from: lambda$6$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m259lambda$6$bgnovatvappPlayerActivity() {
        ExoPlayer exoPlayer = this.player;
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                this.player.pause();
            } else {
                this.player.play();
            }
            syncPause();
            scheduleHide();
        }
    }

    /* JADX INFO: renamed from: lambda$7$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m260lambda$7$bgnovatvappPlayerActivity() {
        zap(1);
    }

    void updateInfo() {
        String str;
        if (this.name == null) {
            return;
        }
        Models.Channel channel = this.channels.get(this.index);
        this.name.setText(channel.name);
        Models.Program programNow = MainActivity.Session.epg == null ? null : MainActivity.Session.epg.now(channel);
        TextView textView = this.info;
        StringBuilder sb = new StringBuilder(String.valueOf(this.index + 1));
        sb.append(" / ");
        sb.append(this.channels.size());
        sb.append("  •  ");
        sb.append(channel.group);
        if (programNow == null) {
            str = "";
        } else {
            str = "  •  " + programNow.title;
        }
        sb.append(str);
        textView.setText(sb.toString());
        boolean zEquals = channel.kind.equals("live");
        this.seek.setVisibility(zEquals ? 8 : 0);
        this.position.setText(zEquals ? "● НА ЖИВО  ·  ↑/↓ сменя канал  ·  OK показва контролите" : "Видео по заявка  ·  ←/→ превърта 10 секунди");
    }

    /* JADX INFO: renamed from: lambda$17$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m250lambda$17$bgnovatvappPlayerActivity(Models.Channel channel, final int i) {
        try {
            final Models.Channel channelResolve = MainActivity.Session.repo.resolve(channel);
            this.handler.post(new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda20
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m251lambda$18$bgnovatvappPlayerActivity(i, channelResolve);
                }
            });
        } catch (Exception e) {
            this.handler.post(new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda21
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m252lambda$19$bgnovatvappPlayerActivity(i, e);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$18$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m251lambda$18$bgnovatvappPlayerActivity(int i, Models.Channel channel) {
        if (i != this.generation || this.stopped || isFinishing()) {
            return;
        }
        this.resolved = channel;
        prepare(channel);
    }

    /* JADX INFO: renamed from: lambda$19$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m252lambda$19$bgnovatvappPlayerActivity(int i, Exception exc) {
        if (i != this.generation || this.stopped) {
            return;
        }
        fail(Net.friendly(exc));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX INFO: renamed from: start, reason: merged with bridge method [inline-methods] */
    public void m241lambda$0$bgnovatvappPlayerActivity() {
        if (this.stopped || isFinishing()) {
            return;
        }
        final int i = this.generation + 1;
        this.generation = i;
        this.handler.removeCallbacks(this.retry);
        Future<?> future = this.resolving;
        if (future != null) {
            future.cancel(true);
        }
        release();
        this.state.setText("Свързване…");
        this.busy.setVisibility(0);
        this.state.setVisibility(0);
        updateInfo();
        final Models.Channel channel = this.channels.get(this.index);
        this.resolving = this.worker.submit(new Runnable() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda22
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m250lambda$17$bgnovatvappPlayerActivity(channel, i);
            }
        });
    }

    void prepare(Models.Channel channel) {
        int i;
        try {
            int i2 = 20000;
            DefaultDataSource.Factory factory = new DefaultDataSource.Factory(this, new DefaultHttpDataSource.Factory().setUserAgent(MainActivity.Session.profile.agent).setConnectTimeoutMs(12000).setReadTimeoutMs(20000).setAllowCrossProtocolRedirects(true).setDefaultRequestProperties(channel.headers));
            int intExtra = getIntent().getIntExtra("buffer", 1);
            if (intExtra == 0) {
                i2 = 3000;
            } else if (intExtra != 2) {
                i2 = 8000;
            }
            if (intExtra == 0) {
                i = 10000;
            } else {
                i = intExtra == 2 ? 50000 : 25000;
            }
            ExoPlayer exoPlayerBuild = new ExoPlayer.Builder(this).setMediaSourceFactory(new DefaultMediaSourceFactory(factory)).setLoadControl(new DefaultLoadControl.Builder().setBufferDurationsMs(i2, i, intExtra == 0 ? 750 : 1500, intExtra == 0 ? 1500 : 3000).build()).setRenderersFactory(new DefaultRenderersFactory(this).setEnableDecoderFallback(true)).build();
            this.player = exoPlayerBuild;
            exoPlayerBuild.setVideoSurfaceView(this.surface);
            this.player.setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(3).build(), true);
            this.player.setHandleAudioBecomingNoisy(true);
            this.player.addListener(new Player.Listener() { // from class: bg.novatv.app.PlayerActivity.3
                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onAudioAttributesChanged(AudioAttributes audioAttributes) {
                    Player.Listener.CC.$default$onAudioAttributesChanged(this, audioAttributes);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onAudioSessionIdChanged(int i3) {
                    Player.Listener.CC.$default$onAudioSessionIdChanged(this, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onAvailableCommandsChanged(Player.Commands commands) {
                    Player.Listener.CC.$default$onAvailableCommandsChanged(this, commands);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onCues(List list) {
                    Player.Listener.CC.$default$onCues(this, list);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onDeviceInfoChanged(DeviceInfo deviceInfo) {
                    Player.Listener.CC.$default$onDeviceInfoChanged(this, deviceInfo);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onDeviceVolumeChanged(int i3, boolean z) {
                    Player.Listener.CC.$default$onDeviceVolumeChanged(this, i3, z);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onEvents(Player player, Player.Events events) {
                    Player.Listener.CC.$default$onEvents(this, player, events);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onIsLoadingChanged(boolean z) {
                    Player.Listener.CC.$default$onIsLoadingChanged(this, z);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onLoadingChanged(boolean z) {
                    Player.Listener.CC.$default$onLoadingChanged(this, z);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onMaxSeekToPreviousPositionChanged(long j) {
                    Player.Listener.CC.$default$onMaxSeekToPreviousPositionChanged(this, j);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onMediaItemTransition(MediaItem mediaItem, int i3) {
                    Player.Listener.CC.$default$onMediaItemTransition(this, mediaItem, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
                    Player.Listener.CC.$default$onMediaMetadataChanged(this, mediaMetadata);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onMetadata(Metadata metadata) {
                    Player.Listener.CC.$default$onMetadata(this, metadata);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onPlayWhenReadyChanged(boolean z, int i3) {
                    Player.Listener.CC.$default$onPlayWhenReadyChanged(this, z, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                    Player.Listener.CC.$default$onPlaybackParametersChanged(this, playbackParameters);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onPlaybackSuppressionReasonChanged(int i3) {
                    Player.Listener.CC.$default$onPlaybackSuppressionReasonChanged(this, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onPlayerErrorChanged(PlaybackException playbackException) {
                    Player.Listener.CC.$default$onPlayerErrorChanged(this, playbackException);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onPlayerStateChanged(boolean z, int i3) {
                    Player.Listener.CC.$default$onPlayerStateChanged(this, z, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onPlaylistMetadataChanged(MediaMetadata mediaMetadata) {
                    Player.Listener.CC.$default$onPlaylistMetadataChanged(this, mediaMetadata);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onPositionDiscontinuity(int i3) {
                    Player.Listener.CC.$default$onPositionDiscontinuity(this, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onPositionDiscontinuity(Player.PositionInfo positionInfo, Player.PositionInfo positionInfo2, int i3) {
                    Player.Listener.CC.$default$onPositionDiscontinuity(this, positionInfo, positionInfo2, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onRenderedFirstFrame() {
                    Player.Listener.CC.$default$onRenderedFirstFrame(this);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onRepeatModeChanged(int i3) {
                    Player.Listener.CC.$default$onRepeatModeChanged(this, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onSeekBackIncrementChanged(long j) {
                    Player.Listener.CC.$default$onSeekBackIncrementChanged(this, j);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onSeekForwardIncrementChanged(long j) {
                    Player.Listener.CC.$default$onSeekForwardIncrementChanged(this, j);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onShuffleModeEnabledChanged(boolean z) {
                    Player.Listener.CC.$default$onShuffleModeEnabledChanged(this, z);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onSkipSilenceEnabledChanged(boolean z) {
                    Player.Listener.CC.$default$onSkipSilenceEnabledChanged(this, z);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onSurfaceSizeChanged(int i3, int i4) {
                    Player.Listener.CC.$default$onSurfaceSizeChanged(this, i3, i4);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onTimelineChanged(Timeline timeline, int i3) {
                    Player.Listener.CC.$default$onTimelineChanged(this, timeline, i3);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onTrackSelectionParametersChanged(TrackSelectionParameters trackSelectionParameters) {
                    Player.Listener.CC.$default$onTrackSelectionParametersChanged(this, trackSelectionParameters);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onTracksChanged(Tracks tracks) {
                    Player.Listener.CC.$default$onTracksChanged(this, tracks);
                }

                @Override // androidx.media3.common.Player.Listener
                public /* synthetic */ void onVolumeChanged(float f) {
                    Player.Listener.CC.$default$onVolumeChanged(this, f);
                }

                @Override // androidx.media3.common.Player.Listener
                public void onCues(CueGroup cueGroup) {
                    PlayerActivity.this.subtitles.setCues(cueGroup.cues);
                }

                @Override // androidx.media3.common.Player.Listener
                public void onIsPlayingChanged(boolean z) {
                    PlayerActivity.this.syncPause();
                }

                @Override // androidx.media3.common.Player.Listener
                public void onPlaybackStateChanged(int i3) {
                    PlayerActivity.this.busy.setVisibility(i3 == 2 ? 0 : 8);
                    if (i3 == 3) {
                        PlayerActivity.this.state.setVisibility(8);
                        PlayerActivity.this.retries = 0;
                        PlayerActivity.this.scheduleHide();
                    }
                    if (i3 == 4) {
                        PlayerActivity.this.state.setText("Видеото приключи");
                        PlayerActivity.this.state.setVisibility(0);
                        PlayerActivity.this.showControls();
                    }
                    PlayerActivity.this.syncPause();
                }

                @Override // androidx.media3.common.Player.Listener
                public void onPlayerError(PlaybackException playbackException) {
                    PlayerActivity.this.fail("Неуспешно възпроизвеждане · " + playbackException.getErrorCodeName() + "\nПровери връзката, абонамента и поддържания кодек.");
                }

                @Override // androidx.media3.common.Player.Listener
                public void onVideoSizeChanged(VideoSize videoSize) {
                    if (videoSize.height > 0) {
                        PlayerActivity.this.videoRatio = (videoSize.width * videoSize.pixelWidthHeightRatio) / videoSize.height;
                        PlayerActivity.this.resize();
                    }
                }
            });
            MediaItem.Builder uri = new MediaItem.Builder().setUri(channel.url);
            if (!channel.mime.isEmpty()) {
                uri.setMimeType(channel.mime);
            }
            this.player.setMediaItem(uri.build());
            if (this.resumePosition > 0 && !channel.kind.equals("live")) {
                this.player.seekTo(this.resumePosition);
            }
            this.resumePosition = 0L;
            this.player.prepare();
            this.player.play();
            this.handler.removeCallbacks(this.progress);
            this.handler.post(this.progress);
        } catch (Exception unused) {
            fail("Плейърът не може да стартира на това устройство.");
        }
    }

    void fail(String str) {
        this.busy.setVisibility(8);
        this.state.setText(str);
        this.state.setVisibility(0);
        showControls();
        int i = this.retries;
        if (i < 2) {
            this.retries = i + 1;
            this.state.append("\nПовторен опит " + this.retries + " / 2…");
            this.handler.postDelayed(this.retry, (long) (this.retries * DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS));
        }
    }

    /* JADX INFO: renamed from: lambda$1$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m242lambda$1$bgnovatvappPlayerActivity() {
        ExoPlayer exoPlayer = this.player;
        if (exoPlayer == null || !exoPlayer.isPlaying()) {
            return;
        }
        this.controls = false;
        this.panel.setVisibility(8);
        View viewFindViewWithTag = this.root.findViewWithTag("upper");
        if (viewFindViewWithTag != null) {
            viewFindViewWithTag.setVisibility(8);
        }
        this.root.requestFocus();
    }

    void showControls() {
        this.controls = true;
        this.panel.setVisibility(0);
        View viewFindViewWithTag = this.root.findViewWithTag("upper");
        if (viewFindViewWithTag != null) {
            viewFindViewWithTag.setVisibility(0);
        }
        this.pause.requestFocus();
        scheduleHide();
    }

    void toggleControls() {
        if (!this.controls) {
            showControls();
            return;
        }
        this.controls = false;
        this.panel.setVisibility(8);
        View viewFindViewWithTag = this.root.findViewWithTag("upper");
        if (viewFindViewWithTag != null) {
            viewFindViewWithTag.setVisibility(8);
        }
        this.root.requestFocus();
    }

    void scheduleHide() {
        this.handler.removeCallbacks(this.hide);
        this.handler.postDelayed(this.hide, 6500L);
    }

    static String duration(long j) {
        long jMax = Math.max(0L, j) / 1000;
        return jMax >= 3600 ? String.format(Locale.US, "%d:%02d:%02d", Long.valueOf(jMax / 3600), Long.valueOf((jMax / 60) % 60), Long.valueOf(jMax % 60)) : String.format(Locale.US, "%02d:%02d", Long.valueOf(jMax / 60), Long.valueOf(jMax % 60));
    }

    void syncPause() {
        Button button = this.pause;
        if (button != null) {
            ExoPlayer exoPlayer = this.player;
            button.setText((exoPlayer == null || !exoPlayer.isPlaying()) ? "▶ Пусни" : "Ⅱ Пауза");
        }
    }

    void zap(int i) {
        if (this.channels.size() < 2) {
            return;
        }
        this.index = ((this.index + i) + this.channels.size()) % this.channels.size();
        this.resumePosition = 0L;
        this.retries = 0;
        this.subtitles.setCues(Collections.emptyList());
        m241lambda$0$bgnovatvappPlayerActivity();
        showControls();
    }

    void resize() {
        FrameLayout frameLayout = this.videoBox;
        if (frameLayout == null || this.surface == null) {
            return;
        }
        int width = frameLayout.getWidth();
        int height = this.videoBox.getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        int i = this.aspect;
        if (i != 2) {
            float f = this.videoRatio;
            if (f <= 0.0f) {
                f = 1.7777778f;
            }
            float f2 = width;
            float f3 = height;
            boolean z = f2 / f3 < f;
            if (i == 1) {
                z = !z;
            }
            if (z) {
                height = Math.round(f2 / f);
            } else {
                width = Math.round(f3 * f);
            }
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.surface.getLayoutParams();
        if (layoutParams.width == width && layoutParams.height == height) {
            return;
        }
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.gravity = 17;
        this.surface.setLayoutParams(layoutParams);
    }

    void tracks(final int i) {
        if (this.player == null) {
            return;
        }
        this.handler.removeCallbacks(this.hide);
        Tracks currentTracks = this.player.getCurrentTracks();
        final ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(i == 3 ? "Изключени" : "Автоматичен избор");
        arrayList.add(null);
        int size = 0;
        for (Tracks.Group group : currentTracks.getGroups()) {
            if (group.getType() == i) {
                for (int i2 = 0; i2 < group.length; i2++) {
                    if (group.isTrackSupported(i2)) {
                        Format trackFormat = group.getTrackFormat(i2);
                        String displayLanguage = (trackFormat.language == null || trackFormat.language.equals(C.LANGUAGE_UNDETERMINED)) ? "Без език" : new Locale(trackFormat.language).getDisplayLanguage(new Locale("bg"));
                        if (trackFormat.label != null) {
                            displayLanguage = String.valueOf(trackFormat.label) + " · " + displayLanguage;
                        }
                        if (i == 1 && trackFormat.channelCount > 0) {
                            displayLanguage = String.valueOf(displayLanguage) + " · " + trackFormat.channelCount + " ch";
                        }
                        arrayList2.add(displayLanguage);
                        arrayList.add(new TrackSelectionOverride(group.getMediaTrackGroup(), i2));
                        if (group.isTrackSelected(i2)) {
                            size = arrayList2.size() - 1;
                        }
                    }
                }
            }
        }
        if (arrayList2.size() != 1) {
            new AlertDialog.Builder(this).setTitle(i == 3 ? "Субтитри" : "Аудио писта").setSingleChoiceItems((CharSequence[]) arrayList2.toArray(new String[0]), size, new DialogInterface.OnClickListener() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda0
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i3) {
                    this.f$0.m253lambda$20$bgnovatvappPlayerActivity(i, arrayList, dialogInterface, i3);
                }
            }).setNegativeButton("Назад", new DialogInterface.OnClickListener() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda1
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i3) {
                    this.f$0.m254lambda$21$bgnovatvappPlayerActivity(dialogInterface, i3);
                }
            }).show();
        } else {
            Ui.toast(this, i == 3 ? "Няма субтитри в този поток." : "Няма допълнителни аудио писти.");
            scheduleHide();
        }
    }

    /* JADX INFO: renamed from: lambda$20$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m253lambda$20$bgnovatvappPlayerActivity(int i, List list, DialogInterface dialogInterface, int i2) {
        ExoPlayer exoPlayer = this.player;
        if (exoPlayer != null) {
            TrackSelectionParameters.Builder trackTypeDisabled = exoPlayer.getTrackSelectionParameters().buildUpon().clearOverridesOfType(i).setTrackTypeDisabled(i, i2 == 0 && i == 3);
            if (i2 > 0) {
                trackTypeDisabled.setOverrideForType((TrackSelectionOverride) list.get(i2));
            }
            this.player.setTrackSelectionParameters(trackTypeDisabled.build());
        }
        dialogInterface.dismiss();
        scheduleHide();
    }

    /* JADX INFO: renamed from: lambda$21$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m254lambda$21$bgnovatvappPlayerActivity(DialogInterface dialogInterface, int i) {
        scheduleHide();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX INFO: renamed from: favorite, reason: merged with bridge method [inline-methods] */
    public void m261lambda$8$bgnovatvappPlayerActivity() {
        try {
            Models.Channel channel = this.channels.get(this.index);
            MainActivity.Session.store.toggle(MainActivity.Session.profile, channel);
            Ui.toast(this, MainActivity.Session.store.favorite(MainActivity.Session.profile, channel) ? "Добавен в любими" : "Премахнат от любими");
        } catch (Exception unused) {
            Ui.toast(this, "Неуспешно запазване на любимите.");
        }
        scheduleHide();
    }

    /* JADX INFO: renamed from: lambda$22$bg-novatv-app-PlayerActivity, reason: not valid java name */
    /* synthetic */ void m255lambda$22$bgnovatvappPlayerActivity(DialogInterface dialogInterface, int i) {
        scheduleHide();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX INFO: renamed from: program, reason: merged with bridge method [inline-methods] */
    public void m262lambda$9$bgnovatvappPlayerActivity() {
        this.handler.removeCallbacks(this.hide);
        Models.Channel channel = this.channels.get(this.index);
        List<Models.Program> listEmptyList = MainActivity.Session.epg == null ? Collections.emptyList() : MainActivity.Session.epg.forChannel(channel);
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        int i = 0;
        for (Models.Program program : listEmptyList) {
            if (program.end > System.currentTimeMillis()) {
                int i2 = i + 1;
                if (i < 12) {
                    sb.append(simpleDateFormat.format(new Date(program.start)));
                    sb.append("  ");
                    sb.append(program.title);
                    sb.append("\n\n");
                }
                i = i2;
            }
        }
        new AlertDialog.Builder(this).setTitle(channel.name).setMessage(sb.length() == 0 ? "Няма налична XMLTV програма за този канал." : sb.toString()).setPositiveButton("Готово", new DialogInterface.OnClickListener() { // from class: bg.novatv.app.PlayerActivity$$ExternalSyntheticLambda2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i3) {
                this.f$0.m255lambda$22$bgnovatvappPlayerActivity(dialogInterface, i3);
            }
        }).show();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        ExoPlayer exoPlayer;
        if (i == 85) {
            ExoPlayer exoPlayer2 = this.player;
            if (exoPlayer2 != null) {
                if (exoPlayer2.isPlaying()) {
                    this.player.pause();
                } else {
                    this.player.play();
                }
            }
            showControls();
            return true;
        }
        if (i == 126) {
            ExoPlayer exoPlayer3 = this.player;
            if (exoPlayer3 != null) {
                exoPlayer3.play();
            }
            return true;
        }
        if (i == 127) {
            ExoPlayer exoPlayer4 = this.player;
            if (exoPlayer4 != null) {
                exoPlayer4.pause();
            }
            return true;
        }
        if (i == 166) {
            zap(1);
            return true;
        }
        if (i == 167) {
            zap(-1);
            return true;
        }
        if (this.controls) {
            scheduleHide();
        } else {
            if (i == 19) {
                zap(-1);
                return true;
            }
            if (i == 20) {
                zap(1);
                return true;
            }
            if (i == 23 || i == 66) {
                showControls();
                return true;
            }
            if ((i == 21 || i == 22) && (exoPlayer = this.player) != null && exoPlayer.isCurrentMediaItemSeekable()) {
                ExoPlayer exoPlayer5 = this.player;
                exoPlayer5.seekTo(Math.max(0L, exoPlayer5.getCurrentPosition() + ((long) (i == 22 ? 10000 : -10000))));
                return true;
            }
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        finish();
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        if (this.channels != null) {
            this.stopped = false;
            m241lambda$0$bgnovatvappPlayerActivity();
        }
    }

    @Override // android.app.Activity
    protected void onStop() {
        this.stopped = true;
        this.generation++;
        if (this.player != null && !this.channels.get(this.index).kind.equals("live")) {
            this.resumePosition = this.player.getCurrentPosition();
        }
        Future<?> future = this.resolving;
        if (future != null) {
            future.cancel(true);
        }
        this.handler.removeCallbacksAndMessages(null);
        release();
        super.onStop();
    }

    void release() {
        ExoPlayer exoPlayer = this.player;
        if (exoPlayer != null) {
            exoPlayer.release();
            this.player = null;
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        if (testingCurrent == this) {
            testingCurrent = null;
        }
        this.handler.removeCallbacksAndMessages(null);
        this.worker.shutdownNow();
        release();
        super.onDestroy();
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putInt("index", this.index);
        ExoPlayer exoPlayer = this.player;
        bundle.putLong("position", exoPlayer == null ? this.resumePosition : exoPlayer.getCurrentPosition());
        super.onSaveInstanceState(bundle);
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        resize();
        getWindow().getDecorView().setSystemUiVisibility(5894);
    }
}
