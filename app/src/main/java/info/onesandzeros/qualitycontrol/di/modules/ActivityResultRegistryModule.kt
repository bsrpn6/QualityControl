package info.onesandzeros.qualitycontrol.di.modules

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object ActivityResultRegistryModule {
    @Provides
    fun provideActivityResultRegistry(fragment: Fragment): ActivityResultRegistry {
        return fragment.requireActivity().activityResultRegistry
    }
}