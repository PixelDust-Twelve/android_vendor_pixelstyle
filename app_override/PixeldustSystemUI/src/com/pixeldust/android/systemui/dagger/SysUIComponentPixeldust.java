package com.pixeldust.android.systemui.dagger;

import com.android.systemui.dagger.DefaultComponentBinder;
import com.android.systemui.dagger.DependencyProvider;
import com.android.systemui.dagger.SystemUIBinder;
import com.android.systemui.dagger.SysUIComponent;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.SystemUIModule;

import com.pixeldust.android.systemui.columbus.ColumbusModule;
import com.pixeldust.android.systemui.elmyra.ElmyraModule;
import com.pixeldust.android.systemui.gamedashboard.GameDashboardModule;
import com.pixeldust.android.systemui.keyguard.KeyguardSliceProviderPixeldust;
import com.pixeldust.android.systemui.smartspace.KeyguardSmartspaceController;

import dagger.Subcomponent;

@SysUISingleton
@Subcomponent(modules = {
        ColumbusModule.class,
        DefaultComponentBinder.class,
        DependencyProvider.class,
        ElmyraModule.class,
        GameDashboardModule.class,
        SystemUIModule.class,
        SystemUIPixeldustBinder.class,
        SystemUIPixeldustDependencyProvider.class,
        SystemUIPixeldustModule.class})
public interface SysUIComponentPixeldust extends SysUIComponent {
    @SysUISingleton
    @Subcomponent.Builder
    interface Builder extends SysUIComponent.Builder {
        SysUIComponentPixeldust build();
    }

    /**
     * Member injection into the supplied argument.
     */
    void inject(KeyguardSliceProviderPixeldust keyguardSliceProvider);

    @SysUISingleton
    KeyguardSmartspaceController createKeyguardSmartspaceController();
}
