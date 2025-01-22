package com.example.gametest.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.MotionEvent
import com.example.gametest.game.Cell
import kotlin.math.min

class SudokuBoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    private var sqrtSize = 3
    private var size = 9
    private var cellSizePixels = 0F
    private var noteSizePixels = 0F
    private var selectedRow = -1
    private var selectedCol = -1
    private var highlighting = false
    private var grid: Array<Array<Cell>>? = null
    private var listener: OnTouchListener? = null

    private val thickLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#AD1457")
        strokeWidth = 6F
    }

    private val thinLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2F
    }

    private val selectedCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#4527A0")
    }

    private val conflictingCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#65646A")
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.MAGENTA
    }

    private val startingCellTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.MAGENTA
        typeface = Typeface.DEFAULT_BOLD
    }

    private val noteTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.MAGENTA
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val sizePixels = min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(sizePixels, sizePixels)
    }

    override fun onDraw(canvas: Canvas) {
        updateMeasurements(width)
        fillCells(canvas)
        drawLines(canvas)
        if (grid != null) drawText(canvas)
    }

    private fun updateMeasurements(width: Int) {
        cellSizePixels = (width / size).toFloat()
        noteSizePixels = cellSizePixels / sqrtSize.toFloat()
        noteTextPaint.textSize = cellSizePixels / sqrtSize.toFloat()
        textPaint.textSize = cellSizePixels / 1.5F
        startingCellTextPaint.textSize = cellSizePixels / 1.5F
    }

    private fun drawLines(canvas: Canvas) {
        for (i in 1 until size) {
            val paintToUse = when (i % sqrtSize) {
                0 -> thickLinePaint
                else -> thinLinePaint
            }

            canvas.drawLine(i * cellSizePixels, 0F, i * cellSizePixels, height.toFloat(), paintToUse)
            canvas.drawLine(0F, i * cellSizePixels, width.toFloat(), i * cellSizePixels, paintToUse)
        }
    }

    private fun fillCells(canvas: Canvas) {
        //if (selectedRow == -1 || selectedCol == -1) return
        for (row in 0..8) {
            for (col in 0..8) {
                if (grid!![row][col].highlight) {
                    fillCell(canvas, row, col, selectedCellPaint)
                    continue
                } else if (!highlighting) {
                    if (row == selectedRow && col == selectedCol) {
                        fillCell(canvas, row, col, selectedCellPaint)
                    } else if (row == selectedRow || col == selectedCol) {
                        fillCell(canvas, row, col, conflictingCellPaint)
                    } else if (selectedRow != -1 && selectedCol != -1 && (row / sqrtSize == selectedRow / sqrtSize && col / sqrtSize == selectedCol / sqrtSize)) {
                        fillCell(canvas, row, col, conflictingCellPaint)
                    }
                }
            }
        }
    }

    private fun fillCell(canvas: Canvas, row: Int, col: Int, paint: Paint) {
        canvas.drawRect(col * cellSizePixels, row * cellSizePixels, (col + 1) * cellSizePixels, (row + 1) * cellSizePixels, paint)
    }

    private fun drawText(canvas: Canvas) {
        for (row in 0..8) {
            for (col in 0..8) {
                val textBounds = Rect()

                if (grid!![row][col].value != 0) {
                    val valueString = grid!![row][col].value.toString()
                    val paintToUse = if (grid!![row][col].isStarting) startingCellTextPaint else textPaint
                    paintToUse.getTextBounds(valueString, 0, valueString.length, textBounds)
                    val textWidth = textPaint.measureText(valueString)
                    val textHeight = textBounds.height()

                    canvas.drawText(
                        valueString,
                        (col * cellSizePixels) + cellSizePixels / 2 - textWidth / 2,
                        (row * cellSizePixels) + cellSizePixels / 2 + textHeight / 2,
                        textPaint
                    )
                } else {
                    grid!![row][col].notes.forEach { note ->
                        val valueString = note.toString()
                        val rowInCell = (note - 1) / sqrtSize
                        val colInCell = (note - 1) % sqrtSize
                        noteTextPaint.getTextBounds(valueString, 0 , valueString.length, textBounds)
                        val textWidth = noteTextPaint.measureText(valueString)
                        val textHeight = textBounds.height()
                        canvas.drawText(
                            valueString,
                            (col * cellSizePixels) + (colInCell * noteSizePixels) + noteSizePixels / 2 - textWidth / 2,
                            (row * cellSizePixels) + (rowInCell * noteSizePixels) + noteSizePixels / 2 + textHeight / 2,
                            noteTextPaint
                        )
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleTouchEvent(event.x, event.y)
                true
            } else -> false
        }
    }

//    fun handleTouchEvent(x: Float, y: Float) {
//        selectedRow = (y / cellSizePixels).toInt()
//        selectedCol = (x / cellSizePixels).toInt()
//        invalidate()
//    }

    private fun handleTouchEvent(x: Float, y: Float) {
        val cell: Pair<Int, Int> = Pair((y / cellSizePixels).toInt(), (x / cellSizePixels).toInt())
        listener?.onCellTouched(cell)
    }

    fun registerListener(listener: OnTouchListener) {
        this.listener = listener
    }

    interface OnTouchListener {
        fun onCellTouched(cell: Pair<Int, Int>)
    }

    fun updateGridUI(grid: Array<Array<Cell>>) {
        this.grid = grid
        invalidate()
    }

    fun updateSelectedCellUI(cell: Pair<Int, Int>) {
        selectedRow = cell.first
        selectedCol = cell.second
        invalidate()
    }

    fun updateCellsUI(cell: Cell) {
        grid!![cell.row][cell.col] = cell
        invalidate()
    }

    fun updateHighlightingUI(highlighting: Boolean) {
        this.highlighting = highlighting
    }
}