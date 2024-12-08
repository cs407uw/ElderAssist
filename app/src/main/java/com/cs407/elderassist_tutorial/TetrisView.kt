//package com.cs407.elderassist_tutorial
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.os.Handler
//import android.util.AttributeSet
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import java.util.*
//
//class TetrisView(context: Context, attrs: AttributeSet) : AppCompatActivity(context, attrs) {
//    private val paint = Paint()
//    private val gridSize = 10
//    private val cellSize = 100
//    private val rows = 20
//    private val cols = 10
//
//    private val block = mutableListOf<Pair<Int, Int>>()
//    private val handler = Handler()
//    private val random = Random()
//
//    init {
//        paint.color = Color.BLUE
//        spawnBlock()
//        handler.postDelayed(updateRunnable, 500)
//    }
//
//    private fun spawnBlock() {
//        block.clear()
//        val startX = random.nextInt(cols - 2)
//        block.add(Pair(startX, 0))
//        block.add(Pair(startX + 1, 0))
//        block.add(Pair(startX, 1))
//        block.add(Pair(startX + 1, 1))
//    }
//
//    private val updateRunnable = object : Runnable {
//        override fun run() {
//            moveBlockDown()
//            invalidate()
//            handler.postDelayed(this, 500)
//        }
//    }
//
//    private fun moveBlockDown() {
//        for (i in block.indices) {
//            block[i] = Pair(block[i].first, block[i].second + 1)
//        }
//    }
//
//    override fun onDraw(canvas: Int) {
//        super.onDraw(canvas)
//        for (cell in block) {
//            val left = cell.first * cellSize
//            val top = cell.second * cellSize
//            val right = left + cellSize
//            val bottom = top + cellSize
//            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
//        }
//    }
//}
