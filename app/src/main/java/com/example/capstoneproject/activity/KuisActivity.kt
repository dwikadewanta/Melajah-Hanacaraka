package com.example.capstoneproject.activity

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.capstoneproject.R
import com.example.capstoneproject.databinding.ActivityKuisBinding
import com.example.capstoneproject.fragment.KuisNumberEightFragment
import com.example.capstoneproject.fragment.KuisNumberFiveFragment
import com.example.capstoneproject.fragment.KuisNumberFourFragment
import com.example.capstoneproject.fragment.KuisNumberNineFragment
import com.example.capstoneproject.fragment.KuisNumberOneFragment
import com.example.capstoneproject.fragment.KuisNumberSevenFragment
import com.example.capstoneproject.fragment.KuisNumberSixFragment
import com.example.capstoneproject.fragment.KuisNumberTenFragment
import com.example.capstoneproject.fragment.KuisNumberThreeFragment
import com.example.capstoneproject.fragment.KuisNumberTwoFragment
import com.example.capstoneproject.util.ApiClient
import com.example.capstoneproject.util.DataCallback2
import com.example.capstoneproject.util.DrawingView
import com.example.capstoneproject.util.DrawingViewModel
import com.example.capstoneproject.util.ListHanacarakaID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class KuisActivity : AppCompatActivity() {
    private lateinit var binding : ActivityKuisBinding
    private lateinit var viewModel: DrawingViewModel
    private lateinit var auth : FirebaseAuth
    private lateinit var apiClient : ApiClient
    private lateinit var listHanacarakaID : ListHanacarakaID
    private lateinit var ivHanacaraka : ImageView
    private lateinit var btnNext : ImageView
    private lateinit var btnPrev : ImageView
    private val positionHanacaraka : MutableList<Int> = mutableListOf()
    private val penganggeSuaraCompleted = mutableListOf<String>()
    private val hanacarakaLabelCompleted = mutableListOf<String>()
    private var fragmentPosition = 1
    private var progressValue = 20
    private var alreadyKuis = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_kuis)
        binding.kuisActivity = this
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[DrawingViewModel::class.java]
        apiClient = ApiClient()

        btnNext = binding.ivNextHanacaraka
        btnPrev = binding.ivPrevHanacaraka
        ivHanacaraka = binding.ivHanacaraka

        val tvTimerMinutes = findViewById<TextView>(R.id.tvTimerMinutes)
        val tvTimerSeconds = findViewById<TextView>(R.id.tvTimerSeconds)

        showStartPopup()

        replaceFragment(KuisNumberOneFragment())

        viewModel.minutes.observe(this){
            tvTimerMinutes.text = "$it" + " : "
        }

        viewModel.seconds.observe(this){
            val currentMinute = viewModel.minutes.value?.toInt()

            if(it < 10){
                tvTimerSeconds.text =  "0"+ "$it"
            }else{
                tvTimerSeconds.text = "$it"
            }

            if (currentMinute == 0 && it < 10){
                tvTimerSeconds.text =  "0"+ "$it"
                binding.llTime.backgroundTintList = ColorStateList.valueOf(Color.RED)
                if(it.toInt() == 0){
                    btnKumpulKuis()
                }
            }
        }

        listHanacarakaID = ListHanacarakaID()
        for(i in 0..9){
            val randomValue = Random.nextInt(0, 108)
            positionHanacaraka.add(randomValue)
        }

        for (i in positionHanacaraka){
        when(i%6){
            0 -> {
                penganggeSuaraCompleted.add("Tanpa Pengangge")
                hanacarakaLabelCompleted.add(listHanacarakaID.texts[i])
            }
            1 -> {
                penganggeSuaraCompleted.add("Pengangge Ulu")
                hanacarakaLabelCompleted.add(listHanacarakaID.texts[i])
            }
            2 -> {
                penganggeSuaraCompleted.add("Pengangge Suku")
                hanacarakaLabelCompleted.add(listHanacarakaID.texts[i])
            }
            3 -> {
                penganggeSuaraCompleted.add("Pengangge Taleng")
                hanacarakaLabelCompleted.add(listHanacarakaID.texts[i])
            }
            4 -> {
                penganggeSuaraCompleted.add("Pengangge Taleng Tedong")
                hanacarakaLabelCompleted.add(listHanacarakaID.texts[i])
            }
            5 -> {
                penganggeSuaraCompleted.add("Pengangge Pepet")
                hanacarakaLabelCompleted.add(listHanacarakaID.texts[i])
            }
        }
        }

        handleVisibility(2)
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[0]])
    }

    fun btnBrush(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sesuaikan Ukuran Kuas")

        val view = layoutInflater.inflate(R.layout.brush_popup, null)

        val seekBar = view.findViewById<SeekBar>(R.id.seekBarBrushSize)
        seekBar.progress = progressValue
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                println("progress in int: " + progress)
                progressValue = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        builder.setPositiveButton("Ok"){ dialog, _ ->
            viewModel.updateBrushSize(progressValue.toFloat())
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel"){ dialog, _ ->
            dialog.dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
    }

    fun btnClear(){
        viewModel.updateClearCanvas(true)
    }

    fun btnColor(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pilih Warna")

        val view = layoutInflater.inflate(R.layout.color_popup, null)

        val btnColorBlack = view.findViewById<ImageView>(R.id.btnColorBlack)
        val btnColorRed = view.findViewById<ImageView>(R.id.btnColorRed)
        val btnColorGreen = view.findViewById<ImageView>(R.id.btnColorGreen)
        val btnColorBlue = view.findViewById<ImageView>(R.id.btnColorBlue)
        val btnColorOrange = view.findViewById<ImageView>(R.id.btnColorOrange)
        val btnColorYellow = view.findViewById<ImageView>(R.id.btnColorYellow)
        val btnColorCyan = view.findViewById<ImageView>(R.id.btnColorCyan)
        val btnColorPink = view.findViewById<ImageView>(R.id.btnColorPink)
        val btnColorPurple = view.findViewById<ImageView>(R.id.btnColorPurple)
        val btnColorBrown = view.findViewById<ImageView>(R.id.btnColorBrown)
        val btnColorCream = view.findViewById<ImageView>(R.id.btnColorCream)
        val btnColorDarkGreen = view.findViewById<ImageView>(R.id.btnColorDarkGreen)

        builder.setNegativeButton("Cancel"){ dialog, _ ->
            dialog.dismiss()
        }

        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setLayout(100, 200)
        dialog.show()

        btnColorBlack.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.black))
            dialog.dismiss()
        }

        btnColorRed.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.red_color))
            dialog.dismiss()
        }

        btnColorGreen.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.green_color))
            dialog.dismiss()
        }

        btnColorBlue.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.blue_color))
            dialog.dismiss()
        }

        btnColorOrange.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.orange_color))
            dialog.dismiss()
        }

        btnColorYellow.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.yellow_color))
            dialog.dismiss()
        }

        btnColorCyan.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.cyan_color))
            dialog.dismiss()
        }

        btnColorPink.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.pink_color))
            dialog.dismiss()
        }

        btnColorPurple.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.purple_color))
            dialog.dismiss()
        }

        btnColorBrown.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.brown_color))
            dialog.dismiss()
        }

        btnColorCream.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.cream_color))
            dialog.dismiss()
        }

        btnColorDarkGreen.setOnClickListener {
            viewModel.updateBrushColor(getColor(R.color.dark_green_color))
            dialog.dismiss()
        }
    }

    fun btnUndo(){
        viewModel.updateUndo(true)
    }

    fun btnRedo(){
        viewModel.updateRedo(true)
    }

    fun btnNext(){
        fragmentPosition++
        when(fragmentPosition){
            2 -> {
                replaceFragment(KuisNumberTwoFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[1]])
                handleVisibility(1)
            }
            3 -> {
                replaceFragment(KuisNumberThreeFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[2]])
                handleVisibility(1)
            }
            4 -> {
                replaceFragment(KuisNumberFourFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[3]])
                handleVisibility(1)
            }
            5 -> {
                replaceFragment(KuisNumberFiveFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[4]])
                handleVisibility(1)
            }
            6 -> {
                replaceFragment(KuisNumberSixFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[5]])
                handleVisibility(1)
            }
            7 -> {
                replaceFragment(KuisNumberSevenFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[6]])
                handleVisibility(1)
            }
            8 -> {
                replaceFragment(KuisNumberEightFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[7]])
                handleVisibility(1)
            }
            9 -> {
                replaceFragment(KuisNumberNineFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[8]])
                handleVisibility(1)
            }
            10 -> {
                replaceFragment(KuisNumberTenFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[9]])
                handleVisibility(3)
            }
        }
    }

    fun btnPrev(){
        fragmentPosition--
        when(fragmentPosition){
            1 -> {
                replaceFragment(KuisNumberOneFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[0]])
                handleVisibility(2)
            }
            2 -> {
                replaceFragment(KuisNumberTwoFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[1]])
                handleVisibility(1)
            }
            3 -> {
                replaceFragment(KuisNumberThreeFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[2]])
                handleVisibility(1)
            }
            4 -> {
                replaceFragment(KuisNumberFourFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[3]])
                handleVisibility(1)
            }
            5 -> {
                replaceFragment(KuisNumberFiveFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[4]])
                handleVisibility(1)
            }
            6 -> {
                replaceFragment(KuisNumberSixFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[5]])
                handleVisibility(1)
            }
            7 -> {
                replaceFragment(KuisNumberSevenFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[6]])
                handleVisibility(1)
            }
            8 -> {
                replaceFragment(KuisNumberEightFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[7]])
                handleVisibility(1)
            }
            9 -> {
                replaceFragment(KuisNumberNineFragment())
                ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[8]])
                handleVisibility(1)
            }
        }
    }

    fun btnBack(){
        finish()
    }

    fun btnNumberOne(){
        replaceFragment(KuisNumberOneFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[0]])
        fragmentPosition = 1
        handleVisibility(2)
    }

    fun btnNumberTwo(){
        replaceFragment(KuisNumberTwoFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[1]])
        fragmentPosition = 2
        handleVisibility(1)
    }

    fun btnNumberThree(){
        replaceFragment(KuisNumberThreeFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[2]])
        fragmentPosition = 3
        handleVisibility(1)
    }

    fun btnNumberFour(){
        replaceFragment(KuisNumberFourFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[3]])
        fragmentPosition = 4
        handleVisibility(1)
    }

    fun btnNumberFive() {
        replaceFragment(KuisNumberFiveFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[4]])
        fragmentPosition = 5
        handleVisibility(1)
    }

    fun btnNumberSix(){
        replaceFragment(KuisNumberSixFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[5]])
        fragmentPosition = 6
        handleVisibility(1)
    }

    fun btnNumberSeven(){
        replaceFragment(KuisNumberSevenFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[6]])
        fragmentPosition = 7
        handleVisibility(1)
    }

    fun btnNumberEight(){
        replaceFragment(KuisNumberEightFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[7]])
        fragmentPosition = 8
        handleVisibility(1)
    }

    fun btnNumberNine(){
        replaceFragment(KuisNumberNineFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[8]])
        fragmentPosition = 9
        handleVisibility(1)
    }

    fun btnNumberTen(){
        replaceFragment(KuisNumberTenFragment())
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka[9]])
        fragmentPosition = 10
        handleVisibility(3)
    }

    fun btnKumpulKuis(){
        if(!alreadyKuis){
            viewModel.timer.cancel()
            buttonAdjust(true)
            val fragment = supportFragmentManager.findFragmentById(R.id.drawingViewFragment)
            fragment?.let {
                when(it.javaClass.simpleName){
                    "KuisNumberOneFragment" -> {
                        it as KuisNumberOneFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberTwoFragment" -> {
                        it as KuisNumberTwoFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberThreeFragment"-> {
                        it as KuisNumberThreeFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberFourFragment" -> {
                        it as KuisNumberFourFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberFiveFragment" -> {
                        it as KuisNumberFiveFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberSixFragment" -> {
                        it as KuisNumberSixFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberSevenFragment" -> {
                        it as KuisNumberSevenFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberEightFragment" -> {
                        it as KuisNumberEightFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberNineFragment" -> {
                        it as KuisNumberNineFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                    "KuisNumberTenFragment" -> {
                        it as KuisNumberTenFragment
                        viewModel.currentPaths[it.indexValue] = it.drawingView.paths
                        viewModel.drawingView[it.indexValue] = it.drawingView
                    }
                }
            }

            val drawingView = viewModel.drawingView
            val penganggeSuara = mutableListOf<String>()
            val hanacarakaLabel = mutableListOf<String>()

            drawingView.forEach{
                penganggeSuara.add(penganggeSuaraCompleted[it.key])
                hanacarakaLabel.add(hanacarakaLabelCompleted[it.key])
            }

            val userID = auth.currentUser?.uid

            uploadImageFirebase(drawingView, userID, penganggeSuara, hanacarakaLabel)
        }else{
            Toast.makeText(this, "Kembali ke menu!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageFirebase(drawingViews : MutableMap<Int, DrawingView>, userID : String?,
                                    penganggeSuara : List<String>, hanacarakaLabel : List<String>){

        val filenameList = mutableListOf<String>()
        var uploadSuccessCount = 0

        drawingViews.forEach{
            val bitmap = Bitmap.createBitmap(
                it.value.width,
                it.value.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            it.value.draw(canvas)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            val storage = Firebase.storage
            val storageRef = storage.reference
            val filename = "${System.currentTimeMillis()}.png"
            filenameList.add(filename)

            val imageRef = storageRef.child("${userID}/Kuis/$filename")

            val uploadTask = imageRef.putBytes(byteArray)
            println("Start uploading images")

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    uploadSuccessCount++
                    if (uploadSuccessCount == penganggeSuara.size) {
                        handleApi(userID, penganggeSuara, hanacarakaLabel, filenameList)
                        println("Success uploading images")
                    }
                }
            }.addOnFailureListener { e ->
                e.printStackTrace()
                buttonAdjust(false)
            }
        }
    }

    private fun handleResult(listResult : List<Boolean>){
        val drawnFragment = viewModel.drawingView.keys
        val result = mutableMapOf<Int, Boolean>()
        var listIter = 0

        drawnFragment.forEach {
            result[it] = listResult[listIter]
            listIter++
        }

        val listBtn = listOf(binding.btnOne, binding.btnTwo, binding.btnThree, binding.btnFour,
            binding.btnFive, binding.btnSix, binding.btnSeven, binding.btnEight, binding.btnNine,
            binding.btnTen)

        var trueCount = 0

        for(i in 0..9){
            val btn = listBtn[i]
            if(result.keys.contains(i)){
                val value = result[i]
                if(value == true){
                    btn.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    trueCount++
                }else{
                    btn.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }else{
                btn.backgroundTintList = ColorStateList.valueOf(Color.RED)
            }
        }

        val totalValue = trueCount * 10

        showResult(totalValue)
        alreadyKuis = true
    }

    private fun handleApi(userID : String? , penganggeSuara : List<String>, hanacarakaLabel : List<String>, imageFilename : List<String>){
        println("start sending api")
        apiClient.sendDataKuis(userID, penganggeSuara, hanacarakaLabel, imageFilename, object : DataCallback2{
            override fun onDataReceived(apiResult: List<Boolean>) {
                println("Result: $apiResult")
                buttonAdjust(false)
                handleResult(apiResult)
            }

            override fun onError(apiMessage: String) {
                println(apiMessage)
                Toast.makeText(this@KuisActivity, "Kumpul kembali", Toast.LENGTH_SHORT).show()
                buttonAdjust(false)
            }
        })
    }
    
    private fun showResult(totalValue : Int){
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.popup_kuis, null)

        val ivClose = view.findViewById<ImageView>(R.id.ivClose)
        val tvValue = view.findViewById<TextView>(R.id.tvValue)

        tvValue.text = "${totalValue}"

        builder.setView(view)
        val dialog = builder.create()

        ivClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showStartPopup(){
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.popup_start_kuis, null)

        val btnMulai = view.findViewById<Button>(R.id.btnMulai)
        val btnKembali = view.findViewById<Button>(R.id.btnKembali)

        builder.setView(view)
        val dialog = builder.create()

        btnMulai.setOnClickListener {
            dialog.dismiss()
            viewModel.countDownTimer()
            viewModel.timer.start()
        }

        btnKembali.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.drawingViewFragment, fragment)
        fragmentTransaction.commit()
    }

    private fun handleVisibility(state : Int){
        when(state){
            1 -> {
                btnPrev.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE
            }
            2 -> {
                btnPrev.visibility = View.GONE
                btnNext.visibility = View.VISIBLE
            }
            3 -> {
                btnPrev.visibility = View.VISIBLE
                btnNext.visibility = View.GONE
            }
        }
    }

    private fun buttonAdjust(isEnableButton : Boolean){
        val progressBar = binding.progressBar
        val btnKumpulKuis = binding.btnKumpulKuis

        if(isEnableButton){
            btnKumpulKuis.apply {
                isEnabled = false
                text = ""
            }
            progressBar.visibility = View.VISIBLE
        }else{
            btnKumpulKuis.apply {
                isEnabled = true
                text = "Kumpul Kuis"
            }
            progressBar.visibility = View.GONE
        }
    }
}