package com.catalystmedia.half_life

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.dialog_reset.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_reset.setOnClickListener {
            showDialog()
        }

        btn_back_settings.setOnClickListener {
            finish()
        }
        btn_notif_settings.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.catalystmedia.iconpack.catalystux.applications.CandyBar")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.catalystmedia.iconpack.catalystux.applications.CandyBar")))
            }
        }

    }
    private fun showDialog() {
        val resetDialog = Dialog(this)
        resetDialog.setContentView(R.layout.dialog_reset)
        resetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        resetDialog.btn_accept.setOnClickListener {
            resetData()
        }
        resetDialog.btn_cancel.setOnClickListener {
            resetDialog.dismiss()
        }
        resetDialog.show()
    }

    private fun resetData() {
        val user = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(user!!.uid).removeValue()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}

