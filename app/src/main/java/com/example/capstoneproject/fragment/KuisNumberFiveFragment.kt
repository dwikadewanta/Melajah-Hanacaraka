package com.example.capstoneproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.capstoneproject.R
import com.example.capstoneproject.util.DrawingView
import com.example.capstoneproject.util.DrawingViewModel
import com.example.capstoneproject.util.DrawnPath

class KuisNumberFiveFragment : Fragment() {
    lateinit var drawingView: DrawingView
    private lateinit var viewModel: DrawingViewModel
    var indexValue = 4

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.kuis_number_five_fragment, container, false)

        viewModel = ViewModelProvider(requireActivity())[DrawingViewModel::class.java]
        drawingView = rootView.findViewById(R.id.drawingView)

        if(!viewModel.paths.containsKey(indexValue)){
            viewModel.paths[indexValue] = MutableLiveData<List<DrawnPath>>()
        }else{
            viewModel.paths[indexValue]?.observe(viewLifecycleOwner){ paths ->
                paths?.let {
                    drawingView.updatePaths(it)
                }

            }
        }

        viewModel.brushSize.observe(viewLifecycleOwner) {brushSize ->
            brushSize?.let {
                drawingView.setBrushSize(it)
            }
        }

        viewModel.brushColor.observe(viewLifecycleOwner) {brushColor ->
            brushColor?.let {
                drawingView.setColor(it)
            }
        }

        viewModel.clearCanvas.observe(viewLifecycleOwner) {clearCanvas ->
            if(clearCanvas){
                drawingView.clearCanvas()
                viewModel.updateClearCanvas(false)
            }
        }

        viewModel.undo.observe(viewLifecycleOwner){undo ->
            if(undo){
                drawingView.undo()
                viewModel.updateUndo(false)
            }
        }

        viewModel.redo.observe(viewLifecycleOwner){undo ->
            if(undo){
                drawingView.redo()
                viewModel.updateRedo(false)
            }
        }

        return rootView
    }

    override fun onPause() {
        super.onPause()
        viewModel.currentPaths[indexValue] = drawingView.paths
        viewModel.drawingView[indexValue] = drawingView
    }

    override fun onResume() {
        super.onResume()
        viewModel.currentPaths[indexValue]?.let {
            drawingView.updatePaths(it)
        }
    }
}