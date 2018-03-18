package com.mrkostua.mathalarm.alarmSettings.optionSetRingtone

import android.content.Context
import com.mrkostua.mathalarm.injections.scope.FragmentScope
import dagger.Module
import dagger.Provides

/**
 * @author Kostiantyn Prysiazhnyi on 3/18/2018.
 */
@Module
public class OptionSetRingtoneModule {
    @FragmentScope
    @Provides
    public fun provideOptionSetRingtonePresenter(presenter: OptionSetRingtonePresenter): OptionSetRingtoneContract.Presenter = presenter

    @FragmentScope
    @Provides
    public fun provideMediaPlayerHelper(context: Context): MediaPlayerHelper = MediaPlayerHelper(context)
}