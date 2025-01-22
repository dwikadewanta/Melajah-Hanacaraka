package com.example.capstoneproject.activity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.capstoneproject.R
import com.example.capstoneproject.databinding.ActivityMenulisBinding
import com.example.capstoneproject.util.ApiClient
import com.example.capstoneproject.util.DataCallback
import com.example.capstoneproject.util.DrawingView
import com.example.capstoneproject.util.ListHanacarakaID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class MenulisActivity : AppCompatActivity() {
    private lateinit var drawingView: DrawingView
    private lateinit var binding : ActivityMenulisBinding
    private lateinit var listHanacarakaID : ListHanacarakaID
    private lateinit var mediaPlayer : MediaPlayer
    private lateinit var auth : FirebaseAuth
    private var progressValue = 20
    private var positionHanacaraka = 0
    private var percobaan = 0
    private var alreadyCorrect = false

    private lateinit var apiClient : ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menulis)
        binding.mainActivity3 = this
        supportActionBar?.hide()

        positionHanacaraka = intent.getIntExtra("position", 0)
        listHanacarakaID = ListHanacarakaID()
        drawingView = binding.drawingView

        val ivHanacaraka = binding.ivHanacaraka
        ivHanacaraka.setImageResource(listHanacarakaID.images[positionHanacaraka])

        auth = FirebaseAuth.getInstance()
        apiClient = ApiClient()
        mediaPlayer = MediaPlayer()
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
                progressValue = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        builder.setPositiveButton("Ok"){ dialog, _ ->
            drawingView.setBrushSize(progressValue.toFloat())
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
        drawingView.clearCanvas()
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
            drawingView.setColor(getColor(R.color.black))
            dialog.dismiss()
        }

        btnColorRed.setOnClickListener {
            drawingView.setColor(getColor(R.color.red_color))
            dialog.dismiss()
        }

        btnColorGreen.setOnClickListener {
            drawingView.setColor(getColor(R.color.green_color))
            dialog.dismiss()
        }

        btnColorBlue.setOnClickListener {
            drawingView.setColor(getColor(R.color.blue_color))
            dialog.dismiss()
        }

        btnColorOrange.setOnClickListener {
            drawingView.setColor(getColor(R.color.orange_color))
            dialog.dismiss()
        }

        btnColorYellow.setOnClickListener {
            drawingView.setColor(getColor(R.color.yellow_color))
            dialog.dismiss()
        }

        btnColorCyan.setOnClickListener {
            drawingView.setColor(getColor(R.color.cyan_color))
            dialog.dismiss()
        }

        btnColorPink.setOnClickListener {
            drawingView.setColor(getColor(R.color.pink_color))
            dialog.dismiss()
        }

        btnColorPurple.setOnClickListener {
            drawingView.setColor(getColor(R.color.purple_color))
            dialog.dismiss()
        }

        btnColorBrown.setOnClickListener {
            drawingView.setColor(getColor(R.color.brown_color))
            dialog.dismiss()
        }

        btnColorCream.setOnClickListener {
            drawingView.setColor(getColor(R.color.cream_color))
            dialog.dismiss()
        }

        btnColorDarkGreen.setOnClickListener {
            drawingView.setColor(getColor(R.color.dark_green_color))
            dialog.dismiss()
        }
    }

    fun btnUndo(){
        drawingView.undo()
    }

    fun btnRedo(){
        drawingView.redo()
    }

    fun btnBack(){
        finish()
    }

    fun btnCekAksara(){
        if(!alreadyCorrect){
            buttonAdjust(true)
            var penganggeSuara = "Tanpa Pengangge"
            val hanacarakaLabel = listHanacarakaID.texts[positionHanacaraka]
            when(positionHanacaraka%6){
                0 -> penganggeSuara = "Tanpa Pengangge"
                1 -> penganggeSuara = "Pengangge Ulu"
                2 -> penganggeSuara = "Pengangge Suku"
                3 -> penganggeSuara = "Pengangge Taleng"
                4 -> penganggeSuara = "Pengangge Taleng Tedong"
                5 -> penganggeSuara = "Pengangge Pepet"
            }
            val userID = auth.currentUser?.uid

            uploadImageFirebase(drawingView, userID, penganggeSuara, hanacarakaLabel)
        }else{
            Toast.makeText(this, "Coba huruf yang lain", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResult(result : Boolean, probability : Float, penganggeSuara: String, hanacarakaLabel: String){
        if(result){
            val toast = Toast(this)
            toast.view = layoutInflater.inflate(R.layout.custom_toast_correct, null)
            toast.show()
            alreadyCorrect = true

            playSound()
            binding.tvTingkatKemiripan.visibility = View.VISIBLE
            binding.nilaiPersentase.visibility = View.VISIBLE
            binding.nilaiPersentase.text = "${String.format("%.2f", probability)} %"
        }else{
            val toast = Toast(this)
            toast.view = layoutInflater.inflate(R.layout.custom_toast_wrong, null)
            toast.show()

            percobaan++

            if(percobaan%3 == 0){
                val builder = AlertDialog.Builder(this)
                val view = layoutInflater.inflate(R.layout.animation_popup, null)

                val videoViewAnimation = view.findViewById<VideoView>(R.id.vvAnimation)
                val ivClose = view.findViewById<ImageView>(R.id.ivClose)
                val ivImageThumbnail = view.findViewById<ImageView>(R.id.ivAnimation)
                val tvHanacarakaLabel = view.findViewById<TextView>(R.id.tvHanacarakaLabel)
                //Ganti ke animation di database

                val videoRef = FirebaseStorage.getInstance().getReference("animations/${penganggeSuara}/${hanacarakaLabel}.mp4")

                videoRef.downloadUrl.addOnSuccessListener {
                    val videoUrl = it.toString()
                    tvHanacarakaLabel.text = hanacarakaLabel
                    Glide.with(this)
                        .load(videoUrl)
                        .thumbnail(0.1f)
                        .into(ivImageThumbnail)

                    val mediaController = MediaController(this)
                    mediaController.setAnchorView(videoViewAnimation)
                    videoViewAnimation.setMediaController(mediaController)
                    videoViewAnimation.setVideoURI(Uri.parse(videoUrl));
                    videoViewAnimation.requestFocus();
                    videoViewAnimation.start();

                    videoViewAnimation.setOnCompletionListener {
                        videoViewAnimation.start()
                    }

                    builder.setView(view)
                    val dialog = builder.create()

                    ivClose.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.show()

                }.addOnFailureListener {
                    Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }
        println(percobaan)
        buttonAdjust(false)
    }

    private fun handleApi(userID : String? , penganggeSuara : String, hanacarakaLabel : String, imageFilename : String){
        apiClient.sendDataMenulis(userID, penganggeSuara, hanacarakaLabel, imageFilename, object : DataCallback{
            override fun onDataReceived(apiResult: Boolean, apiProbability: Float) {
                println("Result: $apiResult")
                println("Probability: $apiProbability")
                handleResult(apiResult, apiProbability, penganggeSuara, hanacarakaLabel)
                buttonAdjust(false)
            }

            override fun onError(apiMessage: String) {
                println(apiMessage)
                Toast.makeText(this@MenulisActivity, "Cek kembali", Toast.LENGTH_SHORT).show()
                buttonAdjust(false)

            }
        })
    }

    private fun uploadImageFirebase(drawingView: DrawingView,
                                    userID : String? ,
                                    penganggeSuara : String,
                                    hanacarakaLabel : String){

        val bitmap = Bitmap.createBitmap(
            drawingView.width,
            drawingView.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawingView.draw(canvas)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val storage = Firebase.storage
        val storageRef = storage.reference
        val filename = "${System.currentTimeMillis()}.png"
        val imageRef = storageRef.child("${userID}/Latihan/$filename")

        val uploadTask = imageRef.putBytes(byteArray)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener {
                handleApi(userID, penganggeSuara, hanacarakaLabel, filename)
            }
        }.addOnFailureListener { e ->
            e.printStackTrace()
            buttonAdjust(false)
        }
    }

    private fun playSound(){
        // Release any existing MediaPlayer instance
        mediaPlayer.release()

        // Initialize a new MediaPlayer instance
        mediaPlayer = MediaPlayer.create(this, listHanacarakaID.sounds[positionHanacaraka])

        // Start playing the sound
        mediaPlayer.start()
    }

    private fun buttonAdjust(isEnableButton : Boolean){
        val progressBar = binding.progressBar
        val btnCekAksara = binding.btnCekAksara

        if(isEnableButton){
            btnCekAksara.apply {
                isEnabled = false
                text = ""
            }
            progressBar.visibility = View.VISIBLE
        }else{
            btnCekAksara.apply {
                isEnabled = true
                text = "Cek Aksara"
            }
            progressBar.visibility = View.GONE
        }
    }
}