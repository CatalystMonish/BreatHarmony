package com.catalystmedia.half_life

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_introduction.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class IntroductionActivity : AppCompatActivity() {
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)

        //declare views
        space2.visibility = View.GONE
        ll_1.visibility = View.VISIBLE
        tv_bottom_text.visibility = View.VISIBLE
        btn_next.visibility = View.VISIBLE

        btn_next.setOnClickListener {
            count++
            checkCount()
        }

        btn_begin.setOnClickListener {
            initializeDaysDB()
        }

        }

    private fun initializeDaysDB() {
        addWelcomeMsg()
               addDate()
               Toast.makeText(this, "Welcome Onboard!", Toast.LENGTH_SHORT).show()
               val intent = Intent(this, HomeActivity::class.java)
               intent.putExtra("isFirst", true)
               startActivity(intent)
               finish()
           }



    private fun addDate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            var myDate =  current.format(formatter)
            Toast.makeText(this@IntroductionActivity, myDate, Toast.LENGTH_LONG).show()
            checkIfStartExists(myDate)
        } else {
            var date = Date()
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            val myDate= formatter.format(date)
            Toast.makeText(this@IntroductionActivity, myDate, Toast.LENGTH_LONG).show()
            checkIfStartExists(myDate)
        }

    }

    private fun checkIfStartExists(myDate: String?) {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid.toString()).child("startDate").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    if (myDate != null) {
                        addStartDate(myDate)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun addWelcomeMsg(){
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid).child("welcome").setValue("first")
    }

    private fun addStartDate(myDate: String){
        val user = FirebaseAuth.getInstance().currentUser
     FirebaseDatabase.getInstance().reference.child("Users").child(user!!.uid.toString()).child("startDate").setValue(myDate)
    }

    private fun checkCount() {
        if(count == 0){
            center_lottie.visibility = View.VISIBLE
            center_image.visibility = View.GONE
            ll_1.visibility = View.VISIBLE
            tv_bottom_text.visibility = View.VISIBLE
            tv_bottom_text_2.visibility = View.GONE
        }
        if(count == 1){
            tv_bottom_text_2.visibility = View.GONE
            center_lottie.visibility = View.GONE
            center_image.visibility = View.VISIBLE
//            iv_image.setImageResource(R.drawable.meditation)
            tv_bottom_text.text = "We help you practise Mindfullness through meditation!"
        }
        if(count == 2){
            btn_begin.visibility = View.VISIBLE
            space2.visibility = View.VISIBLE
            ll_1.visibility = View.GONE
            tv_firstStep.visibility = View.VISIBLE
            tv_bottom_text.visibility = View.GONE
            tv_bottom_text_2.visibility = View.GONE
            btn_next.visibility = View.GONE
            center_lottie.visibility = View.GONE
            center_image.visibility = View.GONE
        }
    }
}