package com.catalystmedia.half_life

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import com.akexorcist.snaptimepicker.SnapTimePickerDialog
import com.akexorcist.snaptimepicker.TimeRange
import com.akexorcist.snaptimepicker.TimeValue
import kotlinx.android.synthetic.main.activity_meditation.*
import java.util.*

class MeditationActivity : AppCompatActivity() {

    //TODO: get recMin from Phase
    private val SELECTED_TIME = "com.catalystmedia.selected_time"
    private var recMin = 2
    private var recHr = 0
    private var selectedTime = 2
    private var selectedMusic = "None"
    private var selectedBack = "None"
    private var currentDay = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meditation)
        currentDay = intent.getIntExtra("currentDay", 0)
        val datasetMusic: List<String> = LinkedList(Arrays.asList("None", "Rain", "Space"))
        music_spinner.attachDataSource(datasetMusic)

        music_spinner.setOnSpinnerItemSelectedListener { parent, view, position, id ->
            selectedMusic = parent.getItemAtPosition(position).toString()
        }

 val datasetBackground: List<String> = LinkedList(Arrays.asList("None", "Fire Works", "Calm Night", "Quiet Space", "Passing Train"  ))
        theme_spinner.attachDataSource(datasetBackground)

        theme_spinner.setOnSpinnerItemSelectedListener { parent, view, position, id ->
            selectedBack = parent.getItemAtPosition(position).toString()
        }

            btn_back_med.setOnClickListener {
                val intent = Intent(this@MeditationActivity, HomeActivity::class.java)
                startActivity(intent)
        }

        ib_chng_time.setOnClickListener {
          showTimePop()
        }
        tv_timer.setOnClickListener {
            showTimePop()
        }

        btn_begin.setOnClickListener {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putInt(SELECTED_TIME, selectedTime)
            editor.apply()
            val intent = Intent(this, TimerActivityNew::class.java)
            intent.putExtra("timeUserSelected", selectedTime)
            intent.putExtra("musicSelected", selectedMusic )
            intent.putExtra("backSelected", selectedBack)
            intent.putExtra("time",selectedTime)
            intent.putExtra("currentDayMed", currentDay)
            startActivity(intent)
        }
    }

    private fun showTimePop() {
        SnapTimePickerDialog.Builder().apply {
            setTitle(R.string.title)
            setThemeColor(R.color.dark_blue_app)
            setTitleColor(R.color.colorWhite)
            setPreselectedTime(TimeValue(recHr, recMin))
            setSelectableTimeRange(TimeRange(TimeValue(0, 1), TimeValue(10, 0)))
        }.build().apply{
            setListener { hour, minute ->
                var selectedHour = hour*60
                selectedTime = (selectedHour+minute)
                setTimeText(selectedTime)
            }
        }.show(supportFragmentManager, "TimePicked")
    }

    override fun onBackPressed() {
        val intent = Intent(this@MeditationActivity, HomeActivity::class.java)
        startActivity(intent)
    }


    private fun setTimeText(selectedTime: Int) {
            tv_timer.text = "$selectedTime"
        }

}