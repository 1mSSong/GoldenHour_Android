package com.gdsc.goldenhour.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import com.gdsc.goldenhour.DisasterModeActivity
import com.gdsc.goldenhour.network.RetrofitObject
import com.gdsc.goldenhour.network.model.Disaster
import com.gdsc.goldenhour.network.model.DisasterList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive() called")

        if (intent.action.equals(SMS_RECEIVED_ACTION) or
            intent.action.equals(CB_RECEIVED_ACTION)) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNotEmpty()) {
                val content = messages[0]?.messageBody.toString()
                Log.d(TAG, content)

                // todo: 재난과 관련된 문자인 경우에만, 체크리스트 화면 띄우기
                //getDisasterList()

                sendSmsContentToActivity(context, content)
            }
        }
    }

    private fun getDisasterList(): List<Disaster>? {
        var disasterList: List<Disaster>? = null

        RetrofitObject.networkService.getDisasterList()
            .enqueue(object : Callback<DisasterList> {
                override fun onResponse(
                    call: Call<DisasterList>,
                    response: Response<DisasterList>
                ) {
                    if (response.isSuccessful) {
                        disasterList = response.body()?.data
                    }
                }
                override fun onFailure(call: Call<DisasterList>, t: Throwable) {
                    Log.d("Retrofit", t.message.toString())
                    call.cancel()
                }
            })

        return disasterList
    }

    private fun sendSmsContentToActivity(context: Context, content: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && Settings.canDrawOverlays(context)) {
            val intent = Intent(context, DisasterModeActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            intent.putExtra("content", content)
            context.startActivity(intent)
        }
    }

    companion object {
        private const val SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED"
        private const val CB_RECEIVED_ACTION = "android.provider.Telephony.SMS_EMERGENCY_CB_RECEIVED"
        private const val TAG = "SMS RECEIVER"
    }
}