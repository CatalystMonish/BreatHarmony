package com.catalystmedia.half_life

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.*
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_timer_new.*
import kotlinx.android.synthetic.main.dialog_reset.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class TimerActivityNew : AppCompatActivity() {

    var nowSeconds: Long = 0

    enum class TimerState {
        Stopped, Paused, Running
    }

    //primary
    private lateinit var timer: CountDownTimer
    var SELECTED_TIME = "com.catalystmedia.selected_time"
    private var timerLengthSeconds: Long = 0
    private var timerState = TimerActivityNew.TimerState.Stopped
    private var secondsRemaining: Long = 0

    val messages = arrayOf("Your limitation—it’s only your imagination",
    "Perfect! Keep going",
    "Inhale the future, exhale the past!",
    "Difficult journeys often lead to beautiful destinations!",
    "You'll appreciate today's hard work tomorrow.",
    "Little things make big days.",
    "Push yourself, because no one else is going to do it for you.",
    "Wake up with determination. Go to bed with satisfaction.")

    var currentText = 0

    //secondary
    private var musicSelected = "None"
    private var backSelected = "None"
    private var mediaPlayer: MediaPlayer? = null
    private var dateFormat = ""
    private var treeFormat = ""
    private var isClicked: Boolean = true
    private var currentTimeTree = ""
    private var timeSelected = 0
    private var currentDay = 0
    private var isComplete = false
    private var shouldIntent = true


    override fun onCreate(savedInstanceState: Bundle?) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); if (getSupportActionBar() != null){
            getSupportActionBar()?.hide(); }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_new)
        val random = Random()
        val index = random.nextInt(messages.size)
        currentText = index
        nowSeconds = Calendar.getInstance().timeInMillis / 1000
        //getting selected time from pref
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val timerLengthMinutes = sharedPref.getInt(SELECTED_TIME, 2).toLong()
        secondsRemaining = timerLengthMinutes * 60L
        timerLengthSeconds = secondsRemaining
        //getting intent extras
        musicSelected = intent.getStringExtra("musicSelected").toString()
        backSelected = intent.getStringExtra("backSelected").toString()
        timeSelected = intent.getIntExtra("time", 0)
        currentDay = intent.getIntExtra("currentDayMed", 0)

        //textSwitcher
        ts_main.setFactory {
            val switcherTextView = TextView(this@TimerActivityNew)
            when (backSelected) {
                "None" ->{
                    switcherTextView.setTextColor(Color.parseColor("#000000"))
                }
                "Fire Works" -> {
                    switcherTextView.setTextColor(Color.parseColor("#000000"))
                }
                "Calm Night" -> {
                    switcherTextView.setTextColor(Color.parseColor("#FFFFFF"))
                }
                "Quiet Space" -> {
                    switcherTextView.setTextColor(Color.parseColor("#FFFFFF"))
                }
                "Passing Train" -> {
                    switcherTextView.setTextColor(Color.parseColor("#000000"))
                }
                else -> {
                    lottie_back_new.visibility = View.GONE
                }
            }
            switcherTextView.textSize = 20f
            switcherTextView.typeface =
                ResourcesCompat.getFont(this@TimerActivityNew, R.font.nexab);
            switcherTextView.gravity = Gravity.CENTER
            switcherTextView
        }
        ts_main.setOutAnimation(this, android.R.anim.slide_out_right);
        ts_main.setInAnimation(this, android.R.anim.slide_in_left);
        ts_main.setText(messages[currentText])



        //check todays date
        checkDate()





        //initTimer
        initTimer()
        startTimer()
        timerState =  TimerActivityNew.TimerState.Running
        updateButtons()
        startFunctions()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                val random = Random()
                val index = random.nextInt(messages.size)
                ts_main.setText(messages[index])
                handler.postDelayed(this, 10000)
            }
        }, 10000)

        btn_start_timer_new.setOnClickListener {
            startTimer()
            timerState =  TimerActivityNew.TimerState.Running
            updateButtons()
            startFunctions()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
        val random = Random()
        val index = random.nextInt(messages.size)
        ts_main.setText(messages[index])
                    handler.postDelayed(this, 10000)
                }
            }, 10000)

        }

        btn_pause_new.setOnClickListener {
            timer.cancel()
            timerState = TimerActivityNew.TimerState.Paused
            updateButtons()
            pauseFunctions()
            handler.removeCallbacksAndMessages(null);
        }
        mute_bg_new.setOnClickListener {
            muteFun()
        }
        back_from_timer_new.setOnClickListener {
            val resetDialog = Dialog(this)
            resetDialog.setContentView(R.layout.dialog_reset)
            resetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            resetDialog.tv_reset_timer.text = "Your are about to reset your Timer!"
            resetDialog.btn_accept.setOnClickListener {
                resetData()
            }
            resetDialog.btn_cancel.setOnClickListener {
                resetDialog.dismiss()
            }
            resetDialog.show()
        }

    }

    private fun initTimer() {
       updateCountdownUI()
        progress_time_new.max = timerLengthSeconds.toInt()

    }

    override fun onBackPressed() {
        val resetDialog = Dialog(this)
        resetDialog.setContentView(R.layout.dialog_reset)
        resetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        resetDialog.tv_reset_timer.text = "Your are about to reset your Timer!"
        resetDialog.btn_accept.setOnClickListener {
            resetData()
        }
        resetDialog.btn_cancel.setOnClickListener {
            resetDialog.dismiss()
        }
        resetDialog.show()
    }


    private fun resetData() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove("SELECTED_TIME")
        editor.apply()
        mediaPlayer?.stop()
        finish()
    }

    private fun startTimer(){
            timerState = TimerActivityNew.TimerState.Running
            timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
                override fun onFinish(){
                    isComplete = true
                    onTimerFinished()
                    val oldTimeToDB = timeSelected
                    addOldTimetoDB(oldTimeToDB)
                    addTotalTimetoDb(oldTimeToDB)

                }
                override fun onTick(millisUntilFinished: Long) {
                    secondsRemaining = millisUntilFinished / 1000
                    updateCountdownUI()
                }
            }.start()
        }

    override fun onResume() {
        super.onResume()
        if(isComplete){
            shouldIntent = false
            val intent = Intent(this, TimerCompleteScreen::class.java)
            intent.putExtra("timeCompleted", timeSelected.toString())
            intent.putExtra("currentDayTimer", currentDay.toString())
            startActivity(intent)

        }
    }

    private fun addTotalTimetoDb(oldTimeToDB: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("totalTimeMed").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val timeTotalOld = snapshot.value.toString().toInt()
                        timeTotalOldAdd(oldTimeToDB, timeTotalOld)
                    } else if (!snapshot.exists()) {
                        timeTotalNew(oldTimeToDB)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun timeTotalNew(oldTimeToDB: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("totalTimeMed").setValue(
            oldTimeToDB.toInt())
    }

    private fun timeTotalOldAdd(oldTimeToDB: Int, timeTotalOld: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        val timeToSave = oldTimeToDB + timeTotalOld
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("totalTimeMed").setValue(
            timeToSave.toInt())
    }

    private fun pauseFunctions() {
      mediaPlayer?.pause()
    }
    private fun muteFun() {
        if (isClicked) {
            mediaPlayer?.pause()
            mute_bg_new.setImageResource(R.drawable.ic_baseline_music_note_24)
            isClicked = false
        } else if (!isClicked) {
            mediaPlayer?.isLooping = true
            mute_bg_new.setImageResource(R.drawable.ic_music_off)
            isClicked = true
            mediaPlayer?.start();
            mediaPlayer?.isLooping = true
        }
    }



    private fun startFunctions() {
        lottie_back_new.playAnimation()
        lottie_back_new.visibility = View.VISIBLE
        playMusic(musicSelected)
        setBack(backSelected)
    }
    private fun playMusic(musicSelected: String) {
        if (musicSelected == "Rain") {
            mediaPlayer = MediaPlayer.create(this, R.raw.audio_rain)
            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.start()
                mediaPlayer?.isLooping = true
            }
        } else if (musicSelected == "Space") {
            mediaPlayer = MediaPlayer.create(this, R.raw.audio_sparkles)
            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.start()
                mediaPlayer?.isLooping = true
            }

        } else if (musicSelected == "None") {
            mediaPlayer?.stop()
        }
    }

    private fun setBack(backSelected: String) {
        if (backSelected == "Fire Works") {
            lottie_back_new.setAnimation(R.raw.ic_fire_works)
        } else if (backSelected == "Calm Night") {
            tv_timer_new.setTextColor(Color.parseColor("#FFFFFF"))
            lottie_back_new.setAnimation(R.raw.ic_night)
        } else if (backSelected == "Quiet Space") {
            tv_timer_new.setTextColor(Color.parseColor("#FFFFFF"))
            lottie_back_new.setAnimation(R.raw.ic_space_soccer)
        } else if (backSelected == "Passing Train") {
            lottie_back_new.setAnimation(R.raw.ic_train)
        } else {
            lottie_back_new.visibility = View.GONE
        }
    }

    private fun addOldTimetoDB(oldTimeToDB: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("days").child(currentDay.toString()).child("day").setValue(currentDay.toString())

        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("days").child(currentDay.toString()).child("date").setValue(dateFormat.toString())

        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("days").child(currentDay.toString()).child("goalCompleted").setValue(true)

        ifTimeExists(oldTimeToDB, currentDay)
    }

    private fun ifTimeExists(oldTimeToDB: Int, currentday: Int?) {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("days").child(currentday.toString()).child("timeMeditated").addListenerForSingleValueEvent(
                object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val pastMedTime = snapshot.value.toString()
                            if (pastMedTime == "") {
                                addTimeMeditated(oldTimeToDB, currentday)
                            } else {
                                addTimeMeditatedOld(oldTimeToDB, pastMedTime, currentday)
                            }
                        } else if (!snapshot.exists()) {
                            addTimeMeditated(oldTimeToDB, currentday)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

    }

    private fun addTimeMeditatedOld(oldTimeToDB: Int, pastMedTime: String, currentday: Int?) {
        val currentDayString = currentDay.toString()
        val pastTimeInt = pastMedTime.toInt()
        val newTimeMeditated = (oldTimeToDB + pastTimeInt)
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("days").child(currentday.toString()).child("timeMeditated").setValue(
                newTimeMeditated.toString()).addOnCompleteListener {
                Toast.makeText(this@TimerActivityNew, "Activity Recorded", Toast.LENGTH_SHORT).show()
                if(shouldIntent) {

                    val intent = Intent(this, TimerCompleteScreen::class.java)
                    intent.putExtra("timeCompleted", oldTimeToDB.toString())
                    intent.putExtra("currentDayTimer", currentDayString)
                    startActivity(intent)
                }
            }
    }


    private fun addTimeMeditated(oldTimeToDB: Int, currentday: Int?) {
        val user = FirebaseAuth.getInstance().currentUser
        val timeMeditatedSave = oldTimeToDB.toString()
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("days").child(currentday.toString()).child("timeMeditated").setValue(
                timeMeditatedSave.toString()).addOnCompleteListener {
                Toast.makeText(this@TimerActivityNew, "Activity Recorded", Toast.LENGTH_SHORT).show()
                if (shouldIntent) {
                    val intent = Intent(this, TimerCompleteScreen::class.java)
                    intent.putExtra("timeCompleted", oldTimeToDB.toString())
                    startActivity(intent)
                }
            }
    }

    private fun onTimerFinished() {
        Log.v("finished", timeSelected.toString())
        timerState = TimerActivityNew.TimerState.Stopped
        progress_time_new.progress = 0
        updateButtons()
        updateCountdownUI()
        mediaPlayer?.stop()
        treeEntry()
    }

    private fun treeEntry(){
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treeGrowth").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        checkDate()
                        changeGrowth()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })




    }

    private fun changeGrowth() {
        checkTime()
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treeGrowth").setValue(treeFormat.toString())

        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treeGrowthTime").setValue(currentTimeTree.toString())
    }

    private fun checkTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            currentTimeTree = current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("HH:mm")
            currentTimeTree = formatter.format(date)
        }

    }


    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        tv_timer_new.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"
        progress_time_new.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }


    private fun updateButtons() {
        when (timerState) {
            TimerActivityNew.TimerState.Running -> {
                btn_start_timer_new.visibility = View.GONE
                btn_pause_new.visibility = View.VISIBLE
            }

            TimerActivityNew.TimerState.Paused -> {
                btn_start_timer_new.visibility = View.VISIBLE
                btn_pause_new.visibility = View.GONE
            }
        }
    }


    private fun checkDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            dateFormat = current.format(formatter)
            treeFormat = current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            dateFormat = formatter.format(date)
            treeFormat = formatter.format(date)
        }
    }
}