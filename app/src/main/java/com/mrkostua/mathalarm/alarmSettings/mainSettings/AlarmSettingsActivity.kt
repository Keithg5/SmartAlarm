package com.mrkostua.mathalarm.alarmSettings.mainSettings

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.mrkostua.mathalarm.R
import com.mrkostua.mathalarm.alarmSettings.FragmentCreationHelper
import com.mrkostua.mathalarm.tools.AlarmTools
import com.mrkostua.mathalarm.tools.ConstantValues
import com.mrkostua.mathalarm.tools.NotificationTools
import com.mrkostua.mathalarm.tools.ShowLogs
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_container_for_alarm_setttings.*
import javax.inject.Inject

/**
 * @author Kostiantyn Prysiazhnyi on 01.12.2017.
 */

/**
 * Consider adding schedule intent to Alarm Service which will fully secure user of waking up.
1. Check the battery level every 2 h (depends on last results and if ti was plugged in ).
2. Check if the alarm turn on and scheduled intent.
3. Check if Alarm volume is turn off (or very low).

In all this cases inform user by mail or etc.
Also there can 2-3 level of argent (importance of this alarm) (in some case app will automatically changed Settings or start Playing music to attract user attention to low battery level).

 */
class AlarmSettingsActivity : DaggerAppCompatActivity(), AlarmSettingsContract.View {
    private val TAG = this.javaClass.simpleName

    @Inject
    public lateinit var notificationsTools: NotificationTools
    @Inject
    public lateinit var presenter: AlarmSettingsContract.Presenter
    @Inject
    public lateinit var fragmentCreationHelper: FragmentCreationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_for_alarm_setttings)
        presenter.showChosenFragment(savedInstanceState?.getInt(ConstantValues.INTENT_KEY_WHICH_FRAGMENT_TO_LOAD_FIRST,
                0) ?: 0)

    }

    override fun onResume() {
        ShowLogs.log(TAG, "onResume")
        super.onResume()

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(ConstantValues.INTENT_KEY_WHICH_FRAGMENT_TO_LOAD_FIRST,
                getCurrentFragmentIndex())
    }

    override fun onBackPressed() {
        moveToMainActivity()
    }

    override fun moveToMainActivity() {
        AlarmTools.startMainActivity(this)
        finish()
    }

    override fun blockInitialButton(loadedFragmentName: AlarmSettingsNames) {
        when (loadedFragmentName.getKeyValue()) {
            AlarmTools.getLastFragmentIndex() -> blockImageButton(ibMoveForward)
            0 -> blockImageButton(ibMoveBack)

        }
    }

    override fun getCurrentFragmentIndex(): Int {
        return ConstantValues.alarmSettingsOptionsList.indexOf(fragmentCreationHelper.getFragmentFormContainer())
    }

    override fun moveToNextFragment(currentFragmentIndex: Int) {
        fragmentCreationHelper.loadFragment(ConstantValues.alarmSettingsOptionsList[currentFragmentIndex + 1])

    }

    override fun moveToPreviousFragment(currentFragmentIndex: Int) {
        fragmentCreationHelper.loadFragment(ConstantValues.alarmSettingsOptionsList[currentFragmentIndex - 1])

    }

    override fun loadChosenFragment(fragmentName: AlarmSettingsNames) {
        fragmentCreationHelper.loadFragment(ConstantValues.alarmSettingsOptionsList[fragmentName.getKeyValue()])

    }

    fun ibMoveForwardClickListener(view: View) {
        if (!presenter.isLastSettingsFragment()) {
            presenter.showNextPreviousFragment(true)
            if (presenter.isLastSettingsFragment(1)) {
                blockImageButton(view)

            } else {
                unblockImageButton(ibMoveBack)

            }

        } else {
            notificationsTools.showToastMessage(resources.getString(R.string.blockedButtonMessage))

        }
    }

    fun ibMoveBackClickListener(view: View) {
        if (!presenter.isFirstSettingsFragment()) {
            presenter.showNextPreviousFragment(false)
            if (presenter.isFirstSettingsFragment(1)) {
                blockImageButton(view)

            } else {
                unblockImageButton(ibMoveForward)

            }

        } else {
            notificationsTools.showToastMessage(resources.getString(R.string.blockedButtonMessage))

        }
    }

    fun ibBackToMainActivityClickListener(view: View) {
        moveToMainActivity()
    }

    private fun blockImageButton(whichButton: View) {
        whichButton.isClickable = false
        whichButton.isFocusable = false
        if (whichButton is ImageButton) {
            when (whichButton) {
                ibMoveBack -> whichButton.setImageResource(R.drawable.arrow_left_blocked)
                ibMoveForward -> whichButton.setImageResource(R.drawable.arrow_right_blocked)

                else -> ShowLogs.log(TAG, "blockImageButton wrong method argument.")

            }
        }

    }

    private fun unblockImageButton(whichButton: View) {
        whichButton.isClickable = true
        whichButton.isFocusable = true
        if (whichButton is ImageButton) {
            when (whichButton) {
                ibMoveBack -> whichButton.setImageResource(R.drawable.arrow_left)
                ibMoveForward -> whichButton.setImageResource(R.drawable.arrow_right)

                else -> ShowLogs.log(TAG, "unblockImageButton wrong method argument.")

            }
        }
    }

}





