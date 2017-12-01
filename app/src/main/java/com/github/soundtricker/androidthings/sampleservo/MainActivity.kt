package com.github.soundtricker.androidthings.sampleservo

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View
import com.google.android.things.contrib.driver.pwmservo.Servo
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

private val TAG = MainActivity::class.java.simpleName

class MainActivity : Activity() {

    companion object {
        private val PIN_PWM = "PWM0"
        private val MIN_PULSE_MS = 0.6
        private val MAX_PULSE_MS = 2.35

    }

    private var mServo: Servo? = null
    private lateinit var mHandler: Handler
    private lateinit var mHandlerThread: HandlerThread
    private var mAngle = 90.0
    private var mSpeed = 5.0

    private val mRunnable = Runnable {
        if (mServo == null) {
            return@Runnable
        }
        while(mAngle != mServo!!.angle) {
            try {
                var targetAngle: Double
                if (mAngle > mServo!!.angle) {
                    targetAngle = mServo!!.angle + mSpeed
                    targetAngle = Math.min(targetAngle, mAngle)
                } else {
                    targetAngle = mServo!!.angle - mSpeed
                    targetAngle = Math.max(targetAngle, mAngle)
                }

                targetAngle = Math.min(targetAngle, mServo!!.maximumAngle)
                targetAngle = Math.max(targetAngle, mServo!!.minimumAngle)

                mServo!!.angle = targetAngle
                runOnUiThread {
                    currentAngle.text = targetAngle.toString()
                }
                Thread.sleep(1)
            } catch (e: IOException) {
                Log.e(TAG, "Error setting Servo angle")
                break
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            mServo = Servo(PIN_PWM)
            mServo!!.setAngleRange(0.0, 180.0)
            mServo!!.setPulseDurationRange(MIN_PULSE_MS, MAX_PULSE_MS)
            mServo!!.setEnabled(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Servo", e)
            return
        }
        mHandlerThread = HandlerThread("servo")
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper)
        currentAngle.text = "0.0"
        angleText.setText("0.0")
        speedText.setText("5.0")
        setAngle(0.0)
    }

    private fun setAngle(angle: Double) {
        mAngle = angle
        mHandler.post(mRunnable)
    }

    fun onClick(view: View) {
        mSpeed = speedText.text.toString().toDouble()
        setAngle(angleText.text.toString().toDouble())

    }

    override fun onDestroy() {
        super.onDestroy()
        mHandlerThread.quitSafely()
        mHandler.removeCallbacks(mRunnable)

        try {
            mServo?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Servo closing error", e)
        } finally {
            mServo = null
        }
    }
}
