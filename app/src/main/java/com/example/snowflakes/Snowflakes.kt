package com.example.snowflakes

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.os.SystemClock
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

@Suppress("DEPRECATION") // AsyncTask is required by the assignment.
class Snowflakes(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val density = resources.displayMetrics.density
    private var frame = emptyList<Dot>()
    private var active = false
    private var task: AnimationTask? = null

    fun start() {
        active = true
        startTask()
    }

    fun stop() {
        active = false
        cancelTask()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cancelTask()
        frame = emptyList()
        startTask()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(12, 24, 44))
        for (dot in frame) {
            paint.color = dot.color
            canvas.drawCircle(dot.x, dot.y, dot.radius, paint)
        }
    }

    private fun startTask() {
        if (active && task == null && width > 0 && height > 0) {
            val animation = AnimationTask(width.toFloat(), height.toFloat(), density)
            task = animation
            animation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    private fun cancelTask() {
        task?.cancel(true)
        task = null
    }

    private data class Dot(val x: Float, val y: Float, val radius: Float, val color: Int)

    private class Flake(fieldWidth: Float, fieldHeight: Float, density: Float) {
        val centerX = Random.nextFloat() * fieldWidth
        val radius = (1f + Random.nextFloat() * 3f) * density
        val amplitude = (8f + Random.nextFloat() * 18f) * density
        val frequency = 0.7f + Random.nextFloat() * 1.4f
        val speed = (70f + Random.nextFloat() * 70f) * density
        val shade = Random.nextInt(150, 236)
        val color = Color.rgb(shade, shade + 15, 255)
        var y = Random.nextFloat() * fieldHeight
        var phase = Random.nextFloat() * 2f * Math.PI.toFloat()
    }

    private inner class AnimationTask(
        private val fieldWidth: Float,
        private val fieldHeight: Float,
        private val density: Float,
    ) : AsyncTask<Unit, List<Dot>, Unit>() {
        override fun doInBackground(vararg params: Unit) {
            val flakes = List(100) { Flake(fieldWidth, fieldHeight, density) }
            var lastFrameTime = SystemClock.uptimeMillis()
            while (!isCancelled) {
                val now = SystemClock.uptimeMillis()
                val seconds = (now - lastFrameTime).coerceAtMost(50L) / 1000f
                lastFrameTime = now
                val dots = flakes.map { flake ->
                    val depth = (flake.y / fieldHeight).coerceIn(0f, 1f)
                    flake.y += flake.speed * (1f - 0.75f * depth) * seconds
                    if (flake.y > fieldHeight + flake.radius) {
                        flake.y = -flake.radius
                    }
                    flake.phase += flake.frequency * seconds
                    val x = flake.centerX + flake.amplitude * sin(flake.phase)
                    Dot(x, flake.y, flake.radius, flake.color)
                }
                publishProgress(dots)
                try {
                    Thread.sleep(16L)
                } catch (_: InterruptedException) {
                    return
                }
            }
        }

        override fun onProgressUpdate(vararg values: List<Dot>) {
            if (task === this) {
                frame = values[0]
                postInvalidateOnAnimation()
            }
        }
    }
}
