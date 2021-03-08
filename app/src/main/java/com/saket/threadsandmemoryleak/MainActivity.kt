package com.saket.threadsandmemoryleak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView

/*
What happens if :
Activity is recreated while a thread is running in the background?
Active threads cannot be cleaned by Garbage collector. So even though the containing
activity is dead, the thread keeps running. This leads to a memory leak.

Thread holds reference to UI elements while activity is recreated. Does it cause a memory leak?
Yes, even without holding UI reference, the running thread will cause a memory leak.

This is true for threads due to anonymous inner class (runnable). Anonymous inner class
holds an implicit reference to the containing activity. And it will also cause a memory leak
whenever activity is re-created.

There is another such example where we define a clicklistener for a singleton class in a given
activity and it also causes a memory leak when activity is re-created.

For more details refer -
https://www.lukaslechner.com/do-we-still-need-leakcanary-now-that-android-studio-3-6-has-memory-leak-detection/

The fix for threads is to handle threads as per the activity lifecycle. So you can stop the thread
in the onDestroy() callback of the activity.

 */
class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var txtTest: TextView
    var running = false
    var i = 0
    val myThread = Thread {
        while (running) {
            Thread.sleep(1000)
            txtTest.post { txtTest.setText(i.toString()) }
            Log.d(TAG, "strName: $i on ${Thread.currentThread().name}")
            i++
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtTest = findViewById(R.id.textView)
        val btnStartThread = findViewById<Button>(R.id.btnStart)
        btnStartThread.setOnClickListener {
            startThread()
        }
    }



    /*
    Observe that a simple thread will continue to run even when activity gets
    destroyed and re-created. However, does it hold implicit reference to the MainActivity?
    Probably the runnable defined inside the thread maybe holding implicit reference of activity.
     */
    fun startThread() {
        running = true
        myThread.start()
    }

    /*
    The good way to prevent memory leaks is to stop running threads in onDestroy
     */
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        if (myThread.isAlive) {
            running = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}