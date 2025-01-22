package com.example.capstoneproject.util

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel(){
    //Canvas Variable
    val paths = mutableMapOf<Int, MutableLiveData<List<DrawnPath>>>()
    val currentPaths = mutableMapOf<Int, List<DrawnPath>>()
    val drawingView = mutableMapOf<Int, DrawingView>()

    //Canvas property variable
    private val _brushSize = MutableLiveData<Float>()
    val brushSize : LiveData<Float> = _brushSize

    private val _brushColor = MutableLiveData<Int>()
    val brushColor : LiveData<Int> = _brushColor

    private val _clearCanvas = MutableLiveData<Boolean>()
    val clearCanvas : LiveData<Boolean> = _clearCanvas

    private val _undo = MutableLiveData<Boolean>()
    val undo : LiveData<Boolean> = _undo

    private val _redo = MutableLiveData<Boolean>()
    val redo : LiveData<Boolean> = _redo

    //Timer Variable
    private var _minutes = MutableLiveData<Long>()
    var minutes : LiveData<Long> = _minutes

    private var _seconds = MutableLiveData<Long>()
    var seconds : LiveData<Long> = _seconds

    lateinit var timer : CountDownTimer

    fun countDownTimer(){
        timer = object : CountDownTimer(300000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                _minutes.value = millisUntilFinished / 1000 / 60
                _seconds.value = (millisUntilFinished / 1000) % 60
            }

            override fun onFinish() {

            }

        }
    }

    fun updateBrushSize(newBrushSize : Float){
        _brushSize.value = newBrushSize
    }

    fun updateBrushColor(newBrushColor : Int){
        _brushColor.value = newBrushColor
    }

    fun updateClearCanvas(isClearCanvas : Boolean){
        _clearCanvas.value = isClearCanvas
    }

    fun updateUndo(isUndo : Boolean){
        _undo.value = isUndo
    }

    fun updateRedo(isRedo : Boolean){
        _redo.value = isRedo
    }
}