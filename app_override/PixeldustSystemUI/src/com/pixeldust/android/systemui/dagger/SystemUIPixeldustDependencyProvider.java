package com.pixeldust.android.systemui.dagger;

import static com.pixeldust.android.systemui.Dependency.*;

import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.IWallpaperManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.StatsManager;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IThermalService;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.UserManager;
import android.os.Vibrator;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.Nullable;

import com.android.internal.app.AssistUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.flags.SystemPropertiesHelper;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogBufferFactory;
import com.android.systemui.model.SysUiState;
import com.android.systemui.navigationbar.NavigationBarController;
import com.android.systemui.navigationbar.NavigationModeController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.settings.UserContextProvider;
import com.android.systemui.settings.UserTracker;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.flags.FeatureFlags;
import com.android.systemui.statusbar.LockscreenShadeTransitionController;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShadeWindowController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.commandline.CommandRegistry;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.UnlockedScreenOffAnimationController;
import com.android.systemui.statusbar.phone.panelstate.PanelExpansionStateManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.telephony.TelephonyListenerManager;
import com.android.systemui.theme.ThemeOverlayApplier;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import com.android.systemui.util.sensors.ProximitySensor;
import com.android.systemui.util.settings.SecureSettings;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import com.android.wm.shell.tasksurfacehelper.TaskSurfaceHelper;
import com.android.wm.shell.legacysplitscreen.LegacySplitScreen;

import com.google.android.systemui.LiveWallpaperScrimController;
import com.google.android.systemui.NotificationLockscreenUserManagerGoogle;
import com.google.android.systemui.autorotate.AutorotateDataService;
import com.google.android.systemui.autorotate.DataLogger;
import com.google.android.systemui.columbus.ColumbusServiceWrapper;
import com.google.android.systemui.dreamliner.DockObserver;
import com.google.android.systemui.dreamliner.DreamlinerUtils;
import com.google.android.systemui.elmyra.ServiceConfigurationGoogle;
import com.google.android.systemui.power.EnhancedEstimatesGoogleImpl;
import com.google.android.systemui.power.PowerNotificationWarningsGoogleImpl;
import com.google.android.systemui.reversecharging.ReverseChargingController;
import com.google.android.systemui.reversecharging.ReverseChargingViewController;
import com.google.android.systemui.reversecharging.ReverseWirelessCharger;
import com.google.android.systemui.smartspace.SmartSpaceController;
import com.google.android.systemui.statusbar.KeyguardIndicationControllerGoogle;
import com.google.android.systemui.statusbar.NotificationVoiceReplyManagerService;
import com.google.android.systemui.statusbar.notification.voicereplies.DebugNotificationVoiceReplyClient;
import com.google.android.systemui.statusbar.notification.voicereplies.NotificationVoiceReplyClient;
import com.google.android.systemui.statusbar.notification.voicereplies.NotificationVoiceReplyController;
import com.google.android.systemui.statusbar.notification.voicereplies.NotificationVoiceReplyLogger;
import com.google.android.systemui.statusbar.notification.voicereplies.NotificationVoiceReplyManager;
import com.google.android.systemui.statusbar.phone.WallpaperNotifier;

import com.pixeldust.android.systemui.PixeldustServices;
import com.pixeldust.android.systemui.log.dagger.NotifVoiceReplyLog;
import com.pixeldust.android.systemui.theme.ThemeOverlayControllerPixeldust;

import org.pixelexperience.systemui.assist.AssistManagerGoogle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.inject.Named;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;

@Module
public abstract class SystemUIPixeldustDependencyProvider {
    @Provides
    @SysUISingleton
    static NotificationLockscreenUserManagerGoogle provideNotificationLockscreenUserManagerGoogle(Context context, BroadcastDispatcher broadcastDispatcher, DevicePolicyManager devicePolicyManager, UserManager userManager, NotificationClickNotifier notificationClickNotifier, KeyguardManager keyguardManager, StatusBarStateController statusBarStateController, @Main Handler handler, DeviceProvisionedController deviceProvisionedController, KeyguardStateController keyguardStateController, Lazy<KeyguardBypassController> lazy, SmartSpaceController smartSpaceController, DumpManager dumpManager) {
        return new NotificationLockscreenUserManagerGoogle(context, broadcastDispatcher, devicePolicyManager, userManager, notificationClickNotifier, keyguardManager, statusBarStateController, handler, deviceProvisionedController, keyguardStateController, lazy, smartSpaceController, dumpManager);
    }

    @Provides
    @SysUISingleton
    static LiveWallpaperScrimController provideLiveWallpaperScrimController(LightBarController lightBarController, DozeParameters dozeParameters, AlarmManager alarmManager, KeyguardStateController keyguardStateController, DelayedWakeLock.Builder builder, Handler handler, @Nullable IWallpaperManager iWallpaperManager, LockscreenWallpaper lockscreenWallpaper, KeyguardUpdateMonitor keyguardUpdateMonitor, ConfigurationController configurationController, DockManager dockManager, @Main Executor executor, UnlockedScreenOffAnimationController unlockedScreenOffAnimationController, PanelExpansionStateManager panelExpansionStateManager) {
        return new LiveWallpaperScrimController(lightBarController, dozeParameters, alarmManager, keyguardStateController, builder, handler, iWallpaperManager, lockscreenWallpaper, keyguardUpdateMonitor, configurationController, dockManager, executor, unlockedScreenOffAnimationController, panelExpansionStateManager);
    }

    @Provides
    @SysUISingleton
    static AutorotateDataService provideAutorotateDataService(Context context, SensorManager sensorManager, DataLogger dataLogger, BroadcastDispatcher broadcastDispatcher, DeviceConfigProxy deviceConfigProxy, @Main DelayableExecutor delayableExecutor) {
        return new AutorotateDataService(context, sensorManager, dataLogger, broadcastDispatcher, deviceConfigProxy, delayableExecutor);
    }

    @Provides
    @SysUISingleton
    static DataLogger provideDataLogger(StatsManager statsManager) {
        return new DataLogger(statsManager);
    }

    @Provides
    @SysUISingleton
    static EnhancedEstimatesGoogleImpl provideEnhancedEstimatesGoogleImpl(Context context) {
        return new EnhancedEstimatesGoogleImpl(context);
    }

    @Provides
    @SysUISingleton
    static ThemeOverlayControllerPixeldust provideThemeOverlayControllerPixeldust(Context context, BroadcastDispatcher broadcastDispatcher, @Background Handler handler, @Main Executor executor, @Background Executor executorB, ThemeOverlayApplier themeOverlayApplier, SecureSettings secureSettings, WallpaperManager wallpaperManager, UserManager userManager, DeviceProvisionedController deviceProvisionedController, UserTracker userTracker, DumpManager dumpManager, FeatureFlags featureFlags, WakefulnessLifecycle wakefulnessLifecycle) {
        return new ThemeOverlayControllerPixeldust(context, broadcastDispatcher, handler, executor, executorB, themeOverlayApplier, secureSettings, wallpaperManager, userManager, deviceProvisionedController, userTracker, dumpManager, featureFlags, wakefulnessLifecycle);
    }

    @Provides
    @SysUISingleton
    static KeyguardIndicationControllerGoogle provideKeyguardIndicationControllerGoogle(Context context, WakeLock.Builder builder, KeyguardStateController keyguardStateController, StatusBarStateController statusBarStateController, KeyguardUpdateMonitor keyguardUpdateMonitor, DockManager dockManager, BroadcastDispatcher broadcastDispatcher, DevicePolicyManager devicePolicyManager, IBatteryStats iBatteryStats, UserManager userManager, TunerService tunerService, DeviceConfigProxy deviceConfigProxy, @Main DelayableExecutor delayableExecutor, FalsingManager falsingManager, LockPatternUtils lockPatternUtils, IActivityManager iActivityManager, KeyguardBypassController keyguardBypassController) {
        return new KeyguardIndicationControllerGoogle(context, builder, keyguardStateController, statusBarStateController, keyguardUpdateMonitor, dockManager, broadcastDispatcher, devicePolicyManager, iBatteryStats, userManager, tunerService, deviceConfigProxy, delayableExecutor, falsingManager, lockPatternUtils, iActivityManager, keyguardBypassController);
    }

    @Provides
    @SysUISingleton
    static NotificationVoiceReplyManagerService provideNotificationVoiceReplyManagerService(NotificationVoiceReplyManager.Initializer initializer, NotificationVoiceReplyLogger notificationVoiceReplyLogger) {
        return new NotificationVoiceReplyManagerService(initializer, notificationVoiceReplyLogger);
    }

    @Provides
    @SysUISingleton
    static WallpaperNotifier provideWallpaperNotifier(Context context, NotificationEntryManager notificationEntryManager, BroadcastDispatcher broadcastDispatcher) {
        return new WallpaperNotifier(context, notificationEntryManager, broadcastDispatcher);
    }

    @Provides
    @SysUISingleton
    static NotificationVoiceReplyManager.Initializer provideNotificationVoiceReplyController(NotificationEntryManager notificationEntryManager, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationRemoteInputManager notificationRemoteInputManager, LockscreenShadeTransitionController lockscreenShadeTransitionController, NotificationShadeWindowController notificationShadeWindowController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, StatusBar statusBar, SysuiStatusBarStateController sysuiStatusBarStateController, HeadsUpManager headsUpManager, PowerManager powerManager, Context context, NotificationVoiceReplyLogger notificationVoiceReplyLogger) {
        return new NotificationVoiceReplyController(notificationEntryManager, notificationLockscreenUserManager, notificationRemoteInputManager, lockscreenShadeTransitionController, notificationShadeWindowController, statusBarKeyguardViewManager, statusBar, sysuiStatusBarStateController, headsUpManager, powerManager, context, notificationVoiceReplyLogger);
    }

    @Provides
    @SysUISingleton
    static Optional<NotificationVoiceReplyClient> provideNotificationVoiceReplyClient(BroadcastDispatcher broadcastDispatcher, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationVoiceReplyManager.Initializer initializer) {
        return Optional.of(new DebugNotificationVoiceReplyClient(broadcastDispatcher, notificationLockscreenUserManager, initializer));
    }

    @Provides
    @SysUISingleton
    static NotificationVoiceReplyLogger provideNotificationVoiceReplyLogger(@NotifVoiceReplyLog LogBuffer logBuffer, UiEventLogger uiEventLogger) {
        return new NotificationVoiceReplyLogger(logBuffer, uiEventLogger);
    }

    @Provides
    @SysUISingleton
    static ReverseChargingController provideReverseChargingController(Context context, BroadcastDispatcher broadcastDispatcher, Optional<ReverseWirelessCharger> optional, AlarmManager alarmManager, Optional<UsbManager> optionalB, @Main Executor executor, @Background Executor executorB, BootCompleteCache bootCompleteCache, IThermalService iThermalService) {
        return new ReverseChargingController(context, broadcastDispatcher, optional, alarmManager, optionalB, executor, executorB, bootCompleteCache, iThermalService);
    }

    @Provides
    @SysUISingleton
    static Optional<ReverseChargingViewController> provideReverseChargingViewController(Context context, BatteryController batteryController, Lazy<StatusBar> lazy, StatusBarIconController statusBarIconController, BroadcastDispatcher broadcastDispatcher, @Main Executor executor, KeyguardIndicationControllerGoogle keyguardIndicationControllerGoogle) {
        if (batteryController.isReverseSupported()) {
            return Optional.of(new ReverseChargingViewController(context, batteryController, lazy, statusBarIconController, broadcastDispatcher, executor, keyguardIndicationControllerGoogle));
        }
        return Optional.empty();
    }

    @Provides
    @SysUISingleton
    static PowerNotificationWarningsGoogleImpl providePowerNotificationWarningsGoogleImpl(Context context, ActivityStarter activityStarter, BroadcastDispatcher broadcastDispatcher, UiEventLogger uiEventLogger) {
        return new PowerNotificationWarningsGoogleImpl(context, activityStarter, broadcastDispatcher, uiEventLogger);
    }

    @Provides
    @SysUISingleton
    static Optional<UsbManager> provideUsbManager(Context context) {
        return Optional.ofNullable(context.getSystemService(UsbManager.class));
    }

    @Provides
    @SysUISingleton
    static IThermalService provideIThermalService() {
        return IThermalService.Stub.asInterface(ServiceManager.getService("thermalservice"));
    }

    @Provides
    @SysUISingleton
    static Optional<ReverseWirelessCharger> provideReverseWirelessCharger(Context context) {
        return context.getResources().getBoolean(R.bool.config_wlc_support_enabled) ? Optional.of(new ReverseWirelessCharger(context)) : Optional.empty();
    }

    @Provides
    @SysUISingleton
    static DockObserver provideDockObserver(Context context, BroadcastDispatcher broadcastDispatcher, StatusBarStateController statusBarStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, ConfigurationController configurationController, @Main DelayableExecutor delayableExecutor) {
        return new DockObserver(context, DreamlinerUtils.getInstance(context), broadcastDispatcher, statusBarStateController, notificationInterruptStateProvider, configurationController, delayableExecutor);
    }

    @Provides
    @SysUISingleton
    @NotifVoiceReplyLog
    static LogBuffer provideNotifVoiceReplyLogBuffer(LogBufferFactory factory) {
        return factory.create("NotifVoiceReplyLog", 500);
    }
}
