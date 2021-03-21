package com.catalystmedia.half_life

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_timer_complete_screen.*
import kotlinx.android.synthetic.main.welcome_msg_dialog.*
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
const val ONESIGNAL_APP_ID = "78c3ae41-3fde-4781-be2a-a43c9bb96c28"
class HomeActivity : AppCompatActivity(){
    private val CHANNEL_ID = "daily_notif"
    private val notificationId = 101
    private val user = FirebaseAuth.getInstance().currentUser
    private var todaysDate = ""
    private var currentday = 0
    private var currentTime = ""
    private var dayDifference = ""
    private var isFirst = false
    var daysRecorded: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        showLoader()
        readWelcomeMsg()
        userInfo()
        getTreeNumber()
        getTotalTime()
        setUI()
        checkGrowth()
        getCurrentDay()
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

//button onClick Listners
        btn_settings.setOnClickListener {
            val intent = Intent(this@HomeActivity, SettingsActivity ::class.java)
            startActivity(intent)
        }


        btn_history.setOnClickListener {
            val intent = Intent(this@HomeActivity, HistoryActivity::class.java)
            startActivity(intent)
//            sendNotification()
        }


        btn_start.setOnClickListener {
            val intent = Intent(this, MeditationActivity::class.java)
            intent.putExtra("currentDay", currentday)
            startActivity(intent)
        }


    }
    private fun setUI() {
        //for Tree Text
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("treeGrowth").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    tv_init.text = "Your tree has started to grow!\nHave a look a little time later"
                    ll_growth.visibility = View.VISIBLE
                }
                else if (!snapshot.exists()){
                    tv_init.text = "Complete a session\nstart growing a new tree!"
                    ll_growth.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    //loading Dialog
    private fun showLoader(){
        val loadDialog = Dialog(this)
        loadDialog.setContentView(R.layout.loading_dialog)
        loadDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loadDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            loadDialog.dismiss()
        }, 4000)

    }
    //welcome handler
    private fun readWelcomeMsg(){
        val msgRef = FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("welcome")
        msgRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val msg = snapshot.value.toString()
                    if(msg == "default") {
                        val defMsg = "You forgot to meditate for a day, your current tree withered away!\nGrow a new one now."
                        val iv_back = "dry"
                        showWelcomeDialog(defMsg, iv_back)
                    }
                    else if(msg == "first"){
                        val iv_back = "welcome"
                        val defMsg = "Welcome to Breatharmony\nClick on the Start Session Button for your first Session."
                        showWelcomeDialog(defMsg, iv_back)
                    }
                    else{
                        val iv_back = "default"
                        showWelcomeDialog(msg, iv_back)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }
    private fun dismissWelcomeMsg(){
        FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("welcome").removeValue()
    }
    private fun showWelcomeDialog(msg: String, iv_back: String) {
        val welcomeDialog = Dialog(this)
        welcomeDialog.setContentView(R.layout.welcome_msg_dialog)
        welcomeDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        welcomeDialog.welcome_msg.text = msg
        if(iv_back == "dry"){
            welcomeDialog.dialog_iv.setImageResource(R.drawable.ic_tree_ded)
        }
        if(iv_back == "welcome"){
            welcomeDialog.dialog_iv.setImageResource(R.drawable.ic_tree)
        }
        val dismissBtn = welcomeDialog.continue_ads_btn
        dismissBtn.setOnClickListener {
            welcomeDialog.dismiss()
            dismissWelcomeMsg()
        }
        welcomeDialog.show()
    }
    private fun getTreeNumber() {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treesGrown").addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val numberTrees = snapshot.value.toString().toInt()
                        if(numberTrees>1) {
                            tv_tree_number.text = "You have grown $numberTrees trees."
                        }
                        else if(numberTrees == 1) {
                            tv_tree_number.text = "You have grown $numberTrees tree."
                        }
                    }
                    else if (!snapshot.exists()){
                        tv_tree_number.text = "Go ahead start a session grow your first tree!"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }

    private fun getTotalTime(){
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("totalTimeMed").addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val timeMeditated = snapshot.value.toString().toInt()
                        if(timeMeditated>1) {
                            tv_totalTime.text =
                                "You have meditated for a total of\n$timeMeditated mins."
                        }
                        else if(timeMeditated == 1){
                            tv_totalTime.text =
                                "You have meditated for a total of\n$timeMeditated min."
                        }
                    }
                    else if (!snapshot.exists()){
                        tv_totalTime.text = "You have not started to meditate yet!"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })

    }
    //fetching user image and display name
    private fun userInfo(){
        val userName = user!!.displayName
        tv_salutaion.text = "Hi, $userName"
        Glide.with(this@HomeActivity).load(user.photoUrl).into(iv_home_profile)
    }
/*
tree functions
1) Check Growth Value from database
2) Find difference in days from current date
3) Proceed to find diffrence in time from current timee
*/
    private fun checkGrowth() {
        checkDate()
        val user = FirebaseAuth.getInstance().currentUser
        val treeRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(user!!.uid).child("treeGrowth")
        treeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val oldDate = dataSnapshot.value.toString()
                    val currentDate = todaysDate.toString()
                    val date1: Date
                    val date2: Date
                    val dates = SimpleDateFormat("dd/MM/yyyy")
                    date1 = dates.parse(oldDate)
                    date2 = dates.parse(currentDate)
                    val difference: Long = abs(date1.time - date2.time)
                    val differenceDates = difference / (24 * 60 * 60 * 1000)
                    dayDifference = differenceDates.toString()
                    checkGrowthTime(dayDifference)
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    //Date Check
    private fun checkDate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            todaysDate =  current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            todaysDate = formatter.format(date)
        }
    }
    //checking difference between current time and growth start time
    private fun checkGrowthTime(dayDifference: String) {
        checkTime()
        val user = FirebaseAuth.getInstance().currentUser
        val timeRef = FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treeGrowthTime")
        timeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val treeStartTime = dataSnapshot.value.toString()
                    val nowTime = currentTime.toString()
                    val date1: Date
                    val date2: Date
                    val dates = SimpleDateFormat("HH:mm")
                    date1 = dates.parse(treeStartTime)
                    date2 = dates.parse(nowTime)
                    val difference: Long = abs(date1.time - date2.time)
                    val hourDiff = (difference / (60 * 60 * 1000) % 24)
                    val hourInMin = hourDiff * 60
                    val minDiff = (difference / (60 * 1000)) % 60
                    val totalDiffMin = hourInMin + minDiff
                    val theFinalDiffrence: Double =
                        (((dayDifference.toDouble()) * 1440) + totalDiffMin).toDouble()
                    val growthDouble: Double = ((theFinalDiffrence / 4319) * 100)
                    val growthProgress = growthDouble.toInt()
                    Log.v("value", "$growthProgress")
                    updateProgress(growthProgress.toLong())
                    when {
                        growthProgress <= 10 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_1)
                            tv_init.visibility = View.VISIBLE

                        }
                        growthProgress <= 20 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_2)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress <= 30 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_3)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress <= 40 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_4)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress <= 50 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_5)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress <= 60 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_6)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress <= 70 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_7)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress <= 80 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_8)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress <= 90 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_9)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress < 100 -> {
                            iv_tree_img.setImageResource(R.drawable.ic_tree_10)
                            tv_init.visibility = View.GONE
                        }
                        growthProgress >=100 ->{
                            addTreeAndReset(growthProgress)
                        }
                    }
                }
                else{
                    iv_tree_img.setImageResource(R.drawable.ic_tree_1)
                    tv_init.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
   //sets the progress bar value of tree growth
    private fun updateProgress(growthProgress: Long) {
        val growthBar = growthProgress.toInt()
        tv_tree_growth.text = "$growthBar% Grown"
        progress_tree.animateProgress(2000, 0, growthBar)
    }
    private fun addTreeAndReset(growthProgress: Int) {
        Log.v("Reset Value", "$growthProgress + Progress Reseted")
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treesGrown").addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val previousTrees = snapshot.value.toString().toInt()
                        val newTrees = (previousTrees + 1)
                        addNewTrees(newTrees)
                    }
                    else {
                        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
                            .child("treesGrown").setValue("1")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
    private fun addNewTrees(newTrees: Int) {
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treesGrown").setValue(newTrees.toString()).addOnCompleteListener { task->
                resetTrees()
            }

    }
    private fun resetTrees() {
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treeGrowth").removeValue()
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treeGrowthTime").removeValue()
        updateProgress(0)
        iv_tree_img.setImageResource(R.drawable.ic_tree_1)
        tv_init.visibility = View.VISIBLE
        tv_init.text = "You just grew a tree, well done!"
    }
    private fun checkTime(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            currentTime = current.format(formatter)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("HH:mm")
            currentTime = formatter.format(date)
        }

    }
    private fun getCurrentDay() {
        checkDate()
        var dayRef = FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child(
            "startDate")
        dayRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val startDate = dataSnapshot.value.toString()
                    val nowDate = todaysDate.toString()
                    val date1: Date
                    val date2: Date
                    val dates = SimpleDateFormat("dd/MM/yyyy")
                    date1 = dates.parse(startDate)
                    date2 = dates.parse(nowDate)
                    val difference: Long = abs(date1.time - date2.time)
                    val differenceDates = difference / (24 * 60 * 60 * 1000)
                    currentday = differenceDates.toString().toInt()
                    val tvDays = differenceDates + 1
                    if ( tvDays < 10) {
                        tv_days.text = "Day 0$tvDays"
                    } else {
                        tv_days.text = "Day $tvDays"
                    }
                    checkIfTreeExists(currentday.toString())
                    getDaysDifference(currentday.toString())

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    private fun checkIfTreeExists(currentday: String) {
        FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid).child("treeGrowth")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val treeStart = snapshot.value.toString()
                        val todayDateFromMain = todaysDate.toString()
                        Log.v("todayDateFromMain", "$todayDateFromMain")
                        if (treeStart != todayDateFromMain) {
                            checkStreak(currentday)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }
    private fun checkStreak(currentday: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val dayBefore = currentday.toInt() - 1
        Log.v("yesterday", "$dayBefore")
        if(currentday.toInt() != 0) {
            FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("days")
                .child(dayBefore.toString()).child("goalCompleted")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val completedBool = snapshot.value.toString()
                            if (completedBool == "true") {
                                //do nothing
                            } else if (completedBool == "false") {
                                killTree()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }


    }
    private fun killTree() {
        addWelcomeMsg()
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treeGrowth").removeValue()
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid)
            .child("treeGrowthTime").removeValue()
        updateProgress(0)
        iv_tree_img.setImageResource(R.drawable.ic_tree_1)
        tv_init.visibility = View.VISIBLE
        tv_init.text = "Your tree just died, you did not meditate yesterday!"
    }
    private fun addWelcomeMsg() {
        FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("welcome").setValue("default")
    }
    private fun getDaysDifference(currentday: String) {
        Log.e("CURRENT_DAY", currentday)
        val currentdayInt = currentday.toInt()
        if(currentdayInt != 0) {
            FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("days")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            daysRecorded = snapshot.childrenCount.toString().toInt() - 1
                        } else if (!snapshot.exists()) {
                            daysRecorded = -1
                        }
                        val currentDayLong = currentday.toInt()
                        val missingDays: Int = currentDayLong - daysRecorded
                      if (missingDays > 1) {
                            addInbetweenDay(missingDays, daysRecorded)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
        }
    }
    private fun addInbetweenDay(missingDays: Int, daysRecorded: Int) {
        val dayToAdd = daysRecorded + 1
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("days").child(dayToAdd.toString()).child("goalCompleted").setValue(false)
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("days").child(dayToAdd.toString()).child("day").setValue(dayToAdd.toString())
        val isDayStillMissing = missingDays - 1
        if(isDayStillMissing > 1){
            getCurrentDay()
        }
    }
}