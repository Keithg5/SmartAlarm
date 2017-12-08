package com.mrkostua.mathalarm.AlarmSettings

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageButton
import com.mrkostua.mathalarm.Alarms.MathAlarm.MainAlarmActivity
import com.mrkostua.mathalarm.ConstantValues
import com.mrkostua.mathalarm.R
import com.mrkostua.mathalarm.Tools.AllApiTools
import com.mrkostua.mathalarm.Tools.NotificationTools
import com.mrkostua.mathalarm.Tools.ShowLogs
import kotlinx.android.synthetic.main.activity_container_for_alarm_setttings.*

/**
 * @author Kostiantyn Prysiazhnyi on 01.12.2017.
 */

public class AlarmSettingsActivity : AppCompatActivity() {
    private val fragmentHelper: FragmentCreationHelper = FragmentCreationHelper(this)
    private val TAG = this.javaClass.simpleName
    private val notificationTools = NotificationTools(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_for_alarm_setttings)

        showChosenFragment()
                //todo download files from gmail and add here.
    }

    private fun showChosenFragment() {
         val indexOfFragmentToLoad = intent.getIntExtra(ConstantValues.INTENT_KEY_WHICH_FRAGMENT_TO_LOAD_FIRST, -1)
        if (indexOfFragmentToLoad != -1) {
            fragmentHelper.loadFragment((ConstantValues.alarmSettingsOptionsList[indexOfFragmentToLoad]))

        } else {
            fragmentHelper.loadFragment(ConstantValues.alarmSettingsOptionsList[0])
        }
    }

    private fun getCurrentFragmentIndex(): Int {
        return ConstantValues.alarmSettingsOptionsList.indexOf(fragmentHelper.getFragmentFormContainer())
    }

    private fun getLastFragmentIndex(): Int {
        return ConstantValues.alarmSettingsOptionsList.size - 1
    }

    private fun showNextPreviousFragment(isNextFragment: Boolean) {
        val currentFragmentIndex = getCurrentFragmentIndex()
        when (currentFragmentIndex) {
            -1, 0 -> {
                moveToNextFragment(currentFragmentIndex)

            }
            in 1 until getLastFragmentIndex() -> {
                if (isNextFragment) {
                    moveToNextFragment(currentFragmentIndex)


                } else {
                    moveToPreviousFragment(currentFragmentIndex)

                }

            }

            getLastFragmentIndex() -> {
                moveToPreviousFragment(currentFragmentIndex)
                //todo CHANGE next button look, clickable false

            }
            else -> {
                ShowLogs.log(TAG, " showNextPreviousFragment wrong index of currently showing fragment")
                startMainActivity()
            }
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainAlarmActivity::class.java))
    }

    private fun moveToNextFragment(currentFragmentIndex: Int) {
        fragmentHelper.loadFragment(ConstantValues.alarmSettingsOptionsList[currentFragmentIndex + 1])
        //todo maybe do some animations

    }

    private fun moveToPreviousFragment(currentFragmentIndex: Int) {
        fragmentHelper.loadFragment(ConstantValues.alarmSettingsOptionsList[currentFragmentIndex - 1])

    }

    private fun isButtonBlocked(whichButton: View): Boolean {
        return when (whichButton) {
            ibMoveBack -> getCurrentFragmentIndex() == 0
            ibMoveForward -> getCurrentFragmentIndex() == getLastFragmentIndex()
            else -> false
        }

    }

    private fun blockButtonAndShowMessage(whichButton: View) {
        whichButton.isClickable = false
        whichButton.isFocusable = false
        if (whichButton is ImageButton) {
            when (whichButton) {
                ibMoveBack -> whichButton.setImageDrawable(AllApiTools.getDrawable(resources, R.drawable.arrow_right_blocked))

                ibMoveForward -> whichButton.setImageDrawable(AllApiTools.getDrawable(resources, R.drawable.arrow_left_blocked))

                else -> ShowLogs.log(TAG, "blockButtonAndShowMessage wrong method argument.")
            }
        }
        notificationTools.showToastMessage(resources.getString(R.string.blockedButtonMessage))

    }

    fun ibMoveForwardClickListener(view: View) {
        if (isButtonBlocked(view)) {
            blockButtonAndShowMessage(view)
        } else {
            showNextPreviousFragment(true)

        }
    }

    fun ibMoveBackClickListener(view: View) {
        if (isButtonBlocked(view)) {
            blockButtonAndShowMessage(view)
        } else {
            showNextPreviousFragment(false)

        }

    }

    fun ibBackToMainActivityClickListener(view: View) {
        startMainActivity()
    }
}




