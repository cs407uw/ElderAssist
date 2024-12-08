package com.cs407.elderassist_tutorial

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat

class TetrisView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val rows = 10
    private val cols = 10
    private var cellSize = 0
    private val grid = Array(rows) { IntArray(cols) { 0 } }
    private val random = java.util.Random()
    private val handler = Handler()

    private val backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.tetris_background)

    private val shapes = arrayOf(
        arrayOf(intArrayOf(1, 1, 1, 1)),
        arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 1, 0)),
        arrayOf(intArrayOf(1, 1), intArrayOf(1, 1)),
        arrayOf(intArrayOf(0, 1, 1), intArrayOf(1, 1, 0)),
        arrayOf(intArrayOf(1, 1, 0), intArrayOf(0, 1, 1)),
        arrayOf(intArrayOf(1, 1, 1), intArrayOf(1, 0, 0)),
        arrayOf(intArrayOf(1, 1, 1), intArrayOf(0, 0, 1))
    )

    private val colors = arrayOf(
        Color.CYAN, Color.MAGENTA, Color.YELLOW,
        Color.GREEN, Color.RED, Color.BLUE, Color.parseColor("#FFA500")
    )

    private var currentShape = shapes[random.nextInt(shapes.size)]
    private var currentColor = colors[random.nextInt(colors.size)]
    private var currentX = cols / 2 - 1
    private var currentY = 0
    private var blockCount = 0
    private var score = 0

    private var gameOverListener: ((Int) -> Unit)? = null

    private val updateRunnable = object : Runnable {
        override fun run() {
            moveBlockDown()
            invalidate()
            handler.postDelayed(this, 500)
        }
    }

    init {
        handler.postDelayed(updateRunnable, 500)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    fun setGameOverListener(listener: (Int) -> Unit) {
        gameOverListener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = width / cols
    }

    private fun moveBlockDown() {
        if (!canMove(currentShape, currentX, currentY + 1)) {
            mergeShape()
            clearFullRows()
            checkGameOverCondition()
            spawnNewBlock()
        } else {
            currentY += 1
        }
    }

    private fun moveBlockLeft() {
        if (canMove(currentShape, currentX - 1, currentY)) {
            currentX -= 1
            invalidate()
        }
    }

    private fun moveBlockRight() {
        if (canMove(currentShape, currentX + 1, currentY)) {
            currentX += 1
            invalidate()
        }
    }

    private fun canMove(shape: Array<IntArray>, x: Int, y: Int): Boolean {
        for (i in shape.indices) {
            for (j in shape[i].indices) {
                if (shape[i][j] != 0) {
                    val newX = x + j
                    val newY = y + i
                    if (newX < 0 || newX >= cols || newY >= rows || grid[newY][newX] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun mergeShape() {
        for (i in currentShape.indices) {
            for (j in currentShape[i].indices) {
                if (currentShape[i][j] != 0) {
                    grid[currentY + i][currentX + j] = currentColor
                }
            }
        }
    }

    private fun clearFullRows() {
        for (i in grid.indices) {
            if (grid[i].all { it != 0 }) {
                for (k in i downTo 1) {
                    grid[k] = grid[k - 1].copyOf()
                }
                grid[0] = IntArray(cols) { 0 }
                score += 10
            }
        }
    }

    private fun checkGameOverCondition() {
        if (currentY * cellSize < spToPx(24)) {
            handler.removeCallbacks(updateRunnable)
            showGameOver()
        }
    }

    private fun spawnNewBlock() {
        if (blockCount >= 15) {
            handler.removeCallbacks(updateRunnable)
            showGameOver()
            return
        }

        currentShape = shapes[random.nextInt(shapes.size)]
        currentColor = colors[random.nextInt(colors.size)]
        currentX = cols / 2 - 1
        currentY = 0
        blockCount++

        if (!canMove(currentShape, currentX, currentY)) {
            handler.removeCallbacks(updateRunnable)
            showGameOver()
        }
    }

    private fun showGameOver() {
        gameOverListener?.invoke(score)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        drawGrid(canvas)
        drawCurrentShape(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawBitmap(backgroundBitmap, null, Rect(0, 0, width, height), null)
    }

    private fun drawGrid(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        for (i in grid.indices) {
            for (j in grid[i].indices) {
                if (grid[i][j] != 0) {
                    paint.color = grid[i][j]
                    canvas.drawRect(
                        j * cellSize.toFloat(),
                        i * cellSize.toFloat(),
                        (j + 1) * cellSize.toFloat(),
                        (i + 1) * cellSize.toFloat(),
                        paint
                    )
                }
            }
        }
    }

    private fun drawCurrentShape(canvas: Canvas) {
        paint.color = currentColor
        for (i in currentShape.indices) {
            for (j in currentShape[i].indices) {
                if (currentShape[i][j] != 0) {
                    canvas.drawRect(
                        (currentX + j) * cellSize.toFloat(),
                        (currentY + i) * cellSize.toFloat(),
                        (currentX + j + 1) * cellSize.toFloat(),
                        (currentY + i + 1) * cellSize.toFloat(),
                        paint
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                moveBlockLeft()
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                moveBlockRight()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun spToPx(sp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics
        ).toInt()
    }
}
