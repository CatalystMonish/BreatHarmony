package com.catalystmedia.half_life

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_timer_complete_screen.*
import kotlinx.android.synthetic.main.dialog_mood.*

class TimerCompleteScreen : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var rewardedAd: RewardedAd
    private var timeComplete = "NA"
    private var currentDay = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_complete_screen)

        mediaPlayer = MediaPlayer.create(this@TimerCompleteScreen, R.raw.ic_complete)
        mediaPlayer?.setOnPreparedListener {
            mediaPlayer?.start()
        }

        rewardedAd = RewardedAd(this, "ca-app-pub-3940256099942544/5224354917")
        val adLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
            }
            override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            showDialog()

        }, 3000)

        Handler(Looper.getMainLooper()).postDelayed({
            ll_btns.visibility = View.VISIBLE
            add_shown.visibility = View.VISIBLE

        }, 4000)

        currentDay = intent.getStringExtra("currentDayTimer").toString()
        timeComplete = intent.getStringExtra("timeCompleted").toString()
        setText(timeComplete)

        btn_finish.setOnClickListener {
            showAdFinish()
        }
        btn_history_end.setOnClickListener{
            showAdHistory()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        //doNothing or showDialog()
    }

    private fun showAdFinish() {
        if (rewardedAd.isLoaded) {
            val activityContext: Activity = this@TimerCompleteScreen
            val adCallback = object: RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    // Ad opened.
                }
                override fun onRewardedAdClosed() {

                }
                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
               homeActivity()
                }
                override fun onRewardedAdFailedToShow(adError: AdError) {
               homeActivity()
                }
            }
            rewardedAd.show(activityContext, adCallback)
        }
        else {
           Toast.makeText(this@TimerCompleteScreen, "No ads were ready to be shown!", Toast.LENGTH_SHORT).show()
            homeActivity()
        }
    }

    private fun homeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)

    }


    private fun showAdHistory() {
        if (rewardedAd.isLoaded) {
            val activityContext: Activity = this@TimerCompleteScreen
            val adCallback = object: RewardedAdCallback() {
                override fun onRewardedAdOpened() {
                    // Ad opened.
                }
                override fun onRewardedAdClosed() {

                }
                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                   historyActivity()
                }
                override fun onRewardedAdFailedToShow(adError: AdError) {
                    historyActivity()
                }
            }
            rewardedAd.show(activityContext, adCallback)
        }
        else {
            Toast.makeText(this@TimerCompleteScreen, "No ads were ready to be shown!", Toast.LENGTH_SHORT).show()
            historyActivity()
        }
    }

    private fun historyActivity() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)

    }

    private  fun showDialog(){
    val moodDialog = Dialog(this)
        moodDialog.setContentView(R.layout.dialog_mood)
        moodDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        moodDialog.mood_happy.setOnClickListener {
            val mood = "1"
            setMood(mood)
            moodDialog.dismiss()
        }
        moodDialog.mood_loved.setOnClickListener {
            val mood = "2"
            setMood(mood)
            moodDialog.dismiss()
        }

        moodDialog.mood_kiss.setOnClickListener {
            val mood = "3"
            setMood(mood)
            moodDialog.dismiss()
        }

        moodDialog.mood_sleepy.setOnClickListener {
            val mood = "4"
            setMood(mood)
            moodDialog.dismiss()
        }
        moodDialog.mood_sad.setOnClickListener {
            val mood = "5"
            setMood(mood)
            moodDialog.dismiss()
        }
        moodDialog.show()
}


    private fun setMood(mood: String) {
        val user = FirebaseAuth.getInstance().currentUser
                FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
                        .child("days").child(currentDay.toString()).child("mood").setValue(mood.toString()).addOnCompleteListener {
                            Toast.makeText(this@TimerCompleteScreen, "Mood Set", Toast.LENGTH_SHORT).show()
                        }
    }


    private fun setText(timeComplete: String) {
        tv_oldTIme.text = "$timeComplete\nminutes"
    }

}