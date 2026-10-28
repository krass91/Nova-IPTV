package androidx.media3.exoplayer;

import android.content.Context;
import android.media.MediaRoute2Info;
import android.media.MediaRouter2;
import android.media.RouteDiscoveryPreference;
import android.os.Handler;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.Util;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.concurrent.Executor;

/* JADX INFO: loaded from: classes.dex */
final class DefaultSuitableOutputChecker implements SuitableOutputChecker {
    private static final RouteDiscoveryPreference EMPTY_DISCOVERY_PREFERENCE = new RouteDiscoveryPreference.Builder(ImmutableList.of(), false).build();
    private MediaRouter2.ControllerCallback controllerCallback;
    private final Executor executor;
    private boolean isPreviousSelectedOutputSuitableForPlayback;
    private final MediaRouter2.RouteCallback routeCallback = new MediaRouter2.RouteCallback() { // from class: androidx.media3.exoplayer.DefaultSuitableOutputChecker.1
    };
    private final MediaRouter2 router;

    public DefaultSuitableOutputChecker(Context context, final Handler handler) {
        this.router = MediaRouter2.getInstance(context);
        this.executor = new Executor() { // from class: androidx.media3.exoplayer.DefaultSuitableOutputChecker.2
            @Override // java.util.concurrent.Executor
            public void execute(Runnable runnable) {
                Util.postOrRun(handler, runnable);
            }
        };
    }

    @Override // androidx.media3.exoplayer.SuitableOutputChecker
    public void enable(final SuitableOutputChecker.Callback callback) {
        this.router.registerRouteCallback(this.executor, this.routeCallback, EMPTY_DISCOVERY_PREFERENCE);
        MediaRouter2.ControllerCallback controllerCallback = new MediaRouter2.ControllerCallback() { // from class: androidx.media3.exoplayer.DefaultSuitableOutputChecker.3
            @Override // android.media.MediaRouter2.ControllerCallback
            public void onControllerUpdated(MediaRouter2.RoutingController routingController) {
                boolean zIsSelectedOutputSuitableForPlayback = DefaultSuitableOutputChecker.this.isSelectedOutputSuitableForPlayback();
                if (DefaultSuitableOutputChecker.this.isPreviousSelectedOutputSuitableForPlayback != zIsSelectedOutputSuitableForPlayback) {
                    DefaultSuitableOutputChecker.this.isPreviousSelectedOutputSuitableForPlayback = zIsSelectedOutputSuitableForPlayback;
                    callback.onSelectedOutputSuitabilityChanged(zIsSelectedOutputSuitableForPlayback);
                }
            }
        };
        this.controllerCallback = controllerCallback;
        this.router.registerControllerCallback(this.executor, controllerCallback);
        this.isPreviousSelectedOutputSuitableForPlayback = isSelectedOutputSuitableForPlayback();
    }

    @Override // androidx.media3.exoplayer.SuitableOutputChecker
    public void disable() {
        Assertions.checkStateNotNull(this.controllerCallback, "SuitableOutputChecker is not enabled");
        this.router.unregisterControllerCallback(this.controllerCallback);
        this.controllerCallback = null;
        this.router.unregisterRouteCallback(this.routeCallback);
    }

    @Override // androidx.media3.exoplayer.SuitableOutputChecker
    public boolean isSelectedOutputSuitableForPlayback() {
        Assertions.checkStateNotNull(this.controllerCallback, "SuitableOutputChecker is not enabled");
        int transferReason = this.router.getSystemController().getRoutingSessionInfo().getTransferReason();
        boolean zWasTransferInitiatedBySelf = this.router.getSystemController().wasTransferInitiatedBySelf();
        Iterator<MediaRoute2Info> it = this.router.getSystemController().getSelectedRoutes().iterator();
        while (it.hasNext()) {
            if (isRouteSuitableForMediaPlayback(it.next(), transferReason, zWasTransferInitiatedBySelf)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRouteSuitableForMediaPlayback(MediaRoute2Info mediaRoute2Info, int i, boolean z) {
        int suitabilityStatus = mediaRoute2Info.getSuitabilityStatus();
        if (suitabilityStatus == 1) {
            return (i == 1 || i == 2) && z;
        }
        return suitabilityStatus == 0;
    }
}
