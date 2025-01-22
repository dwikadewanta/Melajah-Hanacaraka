package com.example.capstoneproject.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.VideoView

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null

    var paths = mutableListOf<DrawnPath>()
    private var undonePaths = mutableListOf<DrawnPath>()
    private var currentPath: DrawnPath? = null
    private var drawPaint: Paint = Paint()
    private var undoPerformed = false
    private var brushSize: Float = 20f
    private var color = Color.BLACK
//    private var videoView: VideoView? = null

    init {
        setupDrawing()
    }

//    fun setVideoView(videoView: VideoView) {
//        this.videoView = videoView
//    }


    private fun setupDrawing() {
        drawPaint.color = color
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = brushSize
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { drawnPath ->
            drawPaint.strokeWidth = drawnPath.brushSize
            drawPaint.color = drawnPath.color
            canvas.drawPath(drawnPath.path, drawPaint)
        }
        currentPath?.let {
            drawPaint.strokeWidth = it.brushSize
            drawPaint.color = it.color
            canvas.drawPath(it.path, drawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if(undoPerformed){
                    undonePaths.clear()
                    undoPerformed = false
                }
                currentPath = DrawnPath(Path(), brushSize, color)
                currentPath?.path?.moveTo(touchX, touchY)
//                videoView?.visibility = GONE
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath?.path?.lineTo(touchX, touchY)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPath?.let{
                    paths.add(it)
                    currentPath = null
                }
                invalidate()
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }

    // Tambahkan fungsi updatePaths untuk mengubah nilai paths
    fun updatePaths(newPaths: List<DrawnPath>) {
        paths.addAll(newPaths)
        invalidate()
    }

    fun undo(){
        if(paths.isNotEmpty()){
            undonePaths.add(paths.removeAt(paths.size - 1 ))
            undoPerformed = true
            invalidate()
        }
    }

    fun redo(){
        if(undonePaths.isNotEmpty() && undoPerformed){
            paths.add(undonePaths.removeAt(undonePaths.size - 1))
            invalidate()
        }
    }

    fun setColor(newColor: Int) {
        color = newColor
        drawPaint.color = color
    }

    fun setBrushSize(newSize: Float) {
        brushSize = newSize
        drawPaint.strokeWidth = brushSize
    }

    fun clearCanvas() {
        paths.clear()
        undonePaths.clear()
        currentPath = null
        drawCanvas?.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
        invalidate()
    }
}