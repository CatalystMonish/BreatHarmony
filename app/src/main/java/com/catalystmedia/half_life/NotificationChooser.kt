package com.catalystmedia.half_life

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.akexorcist.snaptimepicker.SnapTimePickerDialog
import com.akexorcist.snaptimepicker.TimeRange
import com.akexorcist.snaptimepicker.TimeValue
import kotlinx.android.synthetic.main.activity_meditation.*
import kotlinx.android.synthetic.main.activity_notification_chooser.*

class NotificationChooser : AppCompatActivity() {

    private var isTime:String = "am"
    private var hourSelected:Int = 7
    private var minuteSelected:Int = 0
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_chooser)

        btn_chng_time.setOnClickListener {
        chooseTime()
        }
        notif_time.setOnClickListener {
             chooseTime()
        }

        am_btn.setBackgroundResource(R.drawable.btn_round_selected)
        pm_btn.setBackgroundResource(R.drawable.btn_round_un)
        am_btn.setTextColor(Color.parseColor("#FFFFFF"))
        pm_btn.setTextColor(Color.parseColor("#000000"))

        am_btn.setOnClickListener {
            am_btn.setTextColor(Color.parseColor("#FFFFFF"))
            pm_btn.setTextColor(Color.parseColor("#000000"))
            am_btn.setBackgroundResource(R.drawable.btn_round_selected)
            pm_btn.setBackgroundResource(R.drawable.btn_round_un)
            isTime = "am"
        }
        pm_btn.setOnClickListener {
            am_btn.setTextColor(Color.parseColor("#000000"))
            pm_btn.setTextColor(Color.parseColor("#FFFFFF"))
            am_btn.setBackgroundResource(R.drawable.btn_round_un)
            pm_btn.setBackgroundResource(R.drawable.btn_round_selected)
            isTime = "pm"
        }
        btn_forward_time.setOnClickListener {
            forwardFun()
        }
    }

    private fun chooseTime() {
        SnapTimePickerDialog.Builder().apply {
            setTitle(R.string.title)
            setThemeColor(R.color.dark_blue_app)
            setTitleColor(R.color.colorWhite)
            setPreselectedTime(TimeValue(7, 0))
            setSelectableTimeRange(TimeRange(TimeValue(0, 0), TimeValue(12, 0)))
        }.build().apply{
            setListener { hour, minute ->
                hourSelected = hour
                minuteSelected = minute
                setTimeText(hour, minute)
            }
        }.show(supportFragmentManager, "TimePicked")
    }

    private fun setTimeText(hour: Int, minute: Int) {
        if(minute >= 10) {
            notif_time.text = "$hour:$minute"
        }
        else if (minute < 10){
            notif_time.text = "$hour:0$minute"
        }

    }

    private fun forwardFun() {
        if(isTime == "am"){
            Toast.makeText(this@NotificationChooser, "The selected time is$hourSelected : $minuteSelected am",Toast.LENGTH_SHORT).show()
        }
        else if(isTime == "pm"){
            Toast.makeText(this@NotificationChooser, "The selected time is$hourSelected : $minuteSelected pm",Toast.LENGTH_SHORT).show()
        }
    }
}