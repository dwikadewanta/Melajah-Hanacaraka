package com.example.capstoneproject.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstoneproject.R
import com.example.capstoneproject.adapter.StatistikAdapter
import com.example.capstoneproject.databinding.ActivityStatistikBinding
import com.example.capstoneproject.databinding.StatistikFragmentBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatistikActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatistikBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var user : FirebaseUser
    private lateinit var chart : LineChart
    private val recyclerView = mutableListOf<RecyclerView>()
    private val penganggeLists = List(6) { mutableListOf<Map<String, Int>?>() }
    private val penganggeListTotal =  List(6) { mutableMapOf<String, Int>() }
    private val hurufBenar = mutableMapOf<String?, Int?>()
    private val bulan = mutableListOf<Int>()
    private var indexEntries = listOf<String?>()
    private val top3ValuesPerDay = mutableListOf<List<Triple<Int, String, String>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_statistik)
        binding.statistikActivity = this
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        chart = binding.chart
        chart.setNoDataText("Loading Grafik")

        user = auth.currentUser!!

        setupRecyclerViews()
        fetchData()
    }

    private fun setupRecyclerViews(){
        recyclerView.addAll(
            listOf(
                binding.rvPengangge1, binding.rvPengangge2, binding.rvPengangge3,
                binding.rvPengangge4, binding.rvPengangge5, binding.rvPengangge6,
            )
        )

        recyclerView.forEach{
            it.layoutManager = GridLayoutManager(this, 3)
        }
    }

    private fun updateRecyclerViewAdapters(isUpdateAll : Boolean, position: Int = 0){
        penganggeLists.forEachIndexed {index, list ->
            var updateToAdapter = mapOf<String, Int>()
            if(isUpdateAll){
                val totalMap = penganggeListTotal[index]
                list.filterNotNull().forEach { map ->
                    map.forEach { (key, value) ->
                        totalMap[key] = (totalMap[key] ?: 0) + value
                    }
                }
                updateToAdapter = totalMap
            }else{
                updateToAdapter = list[position]!!
            }
            recyclerView[index].adapter = StatistikAdapter(this, updateToAdapter)
        }
    }

    private fun fetchData(){
        val databaseReference = database.reference.child(user.uid)
        val queryLatihan = databaseReference.child("Latihan").orderByKey().limitToLast(7)
        val queryKuis = databaseReference.child("Kuis").orderByValue().limitToLast(7)

        queryKuis.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                processKuisData(dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        queryLatihan.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                processLatihanData(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle kesalahan jika terjadi
                println("Error getting data : ${databaseError.toException()}")
            }
        })
    }

    private fun processLatihanData(dataSnapshot: DataSnapshot){
        var totalHurufBenar = 0
        val tanggalXAxis = mutableListOf<String?>()
        val xAxisTrueValue = mutableListOf("0f", "1f", "2f", "3f", "4f", "5f", "6f")
        var i = 0
        for(tanggalSnapshot in dataSnapshot.children){
            val huruf_benar = tanggalSnapshot.child("huruf_benar").getValue(Int::class.java)
            val penganggeData = listOf(
                tanggalSnapshot.child("Tanpa Pengangge").getValue(),
                tanggalSnapshot.child("Pengangge Ulu").getValue(),
                tanggalSnapshot.child("Pengangge Suku").getValue(),
                tanggalSnapshot.child("Pengangge Taleng").getValue(),
                tanggalSnapshot.child("Pengangge Taleng Tedong").getValue(),
                tanggalSnapshot.child("Pengangge Pepet").getValue(),
            )
            hurufBenar[xAxisTrueValue[i]] = huruf_benar

            if (huruf_benar != null) {
                totalHurufBenar += huruf_benar
            }
            bulan.add(tanggalSnapshot.key?.substring(5, 7)?.toInt() ?: 0)
            tanggalXAxis.add(tanggalSnapshot.key?.substring(8, 10))
            i++

            penganggeData.forEachIndexed { index, data ->
                penganggeLists[index].add(data as? Map<String, Int>)
            }
        }
        createChar(hurufBenar, tanggalXAxis)
        updateStatus(totalHurufBenar)
        updateRecyclerViewAdapters(true)
    }

    private fun processKuisData(dataSnapshot: DataSnapshot){
        // List to hold all values with their date and attempt
        val allValuesWithDetails = mutableListOf<Triple<Int, String, String>>()

        for (tanggalSnapshot in dataSnapshot.children) {
            val tanggal = tanggalSnapshot.key
            val valuesForDay = mutableListOf<Triple<Int, String, String>>()
            for (percobaan in tanggalSnapshot.children) {
                val kuisPercobaan = percobaan.getValue(Int::class.java)
                val percobaanKey = percobaan.key
                if (kuisPercobaan != null && tanggal != null && percobaanKey != null) {
                    val entry = Triple(kuisPercobaan, tanggal, percobaanKey)
                    valuesForDay.add(entry)
                    allValuesWithDetails.add(entry)
                }
            }
            // Sort values for the day in descending order and take top 3
            val top3ForDay = valuesForDay.sortedByDescending { it.first }.take(3)
            top3ValuesPerDay.add(top3ForDay)
        }

        // Sort by value in descending order and take top 3
        val top3ValuesWithDetails = allValuesWithDetails
            .sortedByDescending { it.first }
            .take(3)

        updateKuisUI(top3ValuesWithDetails, true)
    }

    private fun updateKuisUI(top3ValuesWithDetails : List<Triple<Int, String, String>>, isUpdateAll : Boolean){
        val textViewList = listOf(binding.tv1, binding.tv2, binding.tv3,
            binding.tv4, binding.tv5, binding.tv6)

        val params = textViewList[2].layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = 5
        textViewList[2].layoutParams = params

        textViewList.forEach{
            it.visibility = View.GONE
        }

        println(top3ValuesWithDetails)

        if(isUpdateAll){
            top3ValuesWithDetails.getOrNull(0)?.let{
                if(it.third == "no_records"){
                    textViewList[0].apply {
                        text = "Belum ada percobaan"
                        visibility = View.VISIBLE
                    }
                    textViewList[1].visibility = View.GONE
                }else{
                    textViewList[0].apply {
                        text = it.second
                        visibility = View.VISIBLE
                    }
                    textViewList[1].apply {
                        text = "- ${it.third} : ${it.first}"
                        visibility = View.VISIBLE
                    }
                }
            }

            top3ValuesWithDetails.getOrNull(1)?.let {
                if(it.third == "no_records"){
                    textViewList[2].visibility = View.GONE
                    textViewList[3].visibility = View.GONE
                }else{
                    textViewList[2].apply {
                        text = it.second
                        visibility = View.VISIBLE
                    }
                    textViewList[3].apply {
                        text = "- ${it.third} : ${it.first}"
                        visibility = View.VISIBLE
                    }
                }
            }

            top3ValuesWithDetails.getOrNull(2)?.let{
                if(it.third == "no_records"){
                    textViewList[4].visibility = View.GONE
                    textViewList[5].visibility = View.GONE
                }else{
                    textViewList[4].apply {
                        text = it.second
                        visibility = View.VISIBLE
                    }
                    textViewList[5].apply {
                        text = "- ${it.third} : ${it.first}"
                        visibility = View.VISIBLE
                    }
                }
            }
        }else{
            if(top3ValuesWithDetails[0].third == "no_records"){
                textViewList[0].apply {
                    text = "Belum ada percobaan"
                    visibility = View.VISIBLE
                }
            }else{
                when(top3ValuesWithDetails.size){
                    3 -> {
                        textViewList[0].apply {
                            text = "- ${top3ValuesWithDetails[0].third} : ${top3ValuesWithDetails[0].first}"
                            visibility = View.VISIBLE
                        }
                        textViewList[1].apply {
                            text = "- ${top3ValuesWithDetails[1].third} : ${top3ValuesWithDetails[1].first}"
                            visibility = View.VISIBLE
                        }
                        textViewList[2].apply {
                            text = "- ${top3ValuesWithDetails[2].third} : ${top3ValuesWithDetails[2].first}"
                            visibility = View.VISIBLE
                            val params = layoutParams as ViewGroup.MarginLayoutParams
                            params.topMargin = 0
                            layoutParams = params
                        }
                    }
                    2 -> {
                        textViewList[0].apply {
                            text = "- ${top3ValuesWithDetails[0].third} : ${top3ValuesWithDetails[0].first}"
                            visibility = View.VISIBLE
                        }
                        textViewList[1].apply {
                            text = "- ${top3ValuesWithDetails[1].third} : ${top3ValuesWithDetails[1].first}"
                            visibility = View.VISIBLE
                        }
                    }
                    1 -> {
                        textViewList[0].apply {
                            text = "- ${top3ValuesWithDetails[0].third} : ${top3ValuesWithDetails[0].first}"
                            visibility = View.VISIBLE
                        }
                    }
                }
            }

        }
    }

    private fun createChar(hurufBenar : Map<String?, Int?>, tanggalXAxis : List<String?>){
        val entries = hurufBenar.map { Entry(it.key?.toFloat() ?: 0f, it.value?.toFloat() ?: 0f) }
        indexEntries = hurufBenar.keys.toList()

        val yAxisLeft = chart.axisLeft
        val yAxisRight = chart.axisRight
        val xAxis = chart.xAxis
        val dataSet = LineDataSet(entries, "Label")

        yAxisLeft.apply {
            axisMinimum = 0f
            axisLineWidth = 2f
            axisLineColor = Color.BLACK
            textSize = 12f
        }

        yAxisRight.apply {
            setDrawLabels(false)
            setDrawGridLines(false)
        }

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = IndexAxisValueFormatter(tanggalXAxis)
            granularity = 1f
            textSize = 12f
            axisLineWidth = 3f
            axisLineColor = Color.BLACK
            setDrawLabels(true)
            setDrawGridLines(false)
        }

        dataSet.apply {
            color = resources.getColor(R.color.blue_eight)
            lineWidth = 4f
            valueTextSize = 14f
            circleHoleRadius = 4f
            circleRadius = 6f
            setCircleColor(resources.getColor(R.color.blue_eight))
            highLightColor = Color.RED
            highlightLineWidth = 2f; // Ketebalan garis highlight
            // Set up the data set as integer values
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString() // Convert float to integer
                }
            }
        }

        chart.apply {
            data = LineData(dataSet)
            isDragEnabled = false
            isScaleXEnabled = false
            isScaleYEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            invalidate()

            //To Do
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    e?.let {
                        when(e.x){
                            indexEntries[0]?.toFloat() -> {
                                updateRecyclerViewAdapters(false, 0)
                                val valuesPerDay = top3ValuesPerDay[0]
                                updateKuisUI(valuesPerDay, false)
                            }
                            indexEntries[1]?.toFloat() -> {
                                updateRecyclerViewAdapters(false, 1)
                                val valuesPerDay = top3ValuesPerDay[1]
                                updateKuisUI(valuesPerDay, false)
                            }
                            indexEntries[2]?.toFloat() -> {
                                updateRecyclerViewAdapters(false, 2)
                                val valuesPerDay = top3ValuesPerDay[2]
                                updateKuisUI(valuesPerDay, false)
                            }
                            indexEntries[3]?.toFloat() -> {
                                updateRecyclerViewAdapters(false, 3)
                                val valuesPerDay = top3ValuesPerDay[3]
                                updateKuisUI(valuesPerDay, false)
                            }
                            indexEntries[4]?.toFloat() -> {
                                updateRecyclerViewAdapters(false, 4)
                                val valuesPerDay = top3ValuesPerDay[4]
                                updateKuisUI(valuesPerDay, false)
                            }
                            indexEntries[5]?.toFloat() -> {
                                updateRecyclerViewAdapters(false, 5)
                                val valuesPerDay = top3ValuesPerDay[5]
                                updateKuisUI(valuesPerDay, false)
                            }
                            indexEntries[6]?.toFloat() -> {
                                updateRecyclerViewAdapters(false, 6)
                                val valuesPerDay = top3ValuesPerDay[6]
                                updateKuisUI(valuesPerDay, false)
                            }
                        }
                    }
                    chart.invalidate()
                }

                override fun onNothingSelected() {
                    chart.data?.dataSets?.forEach { dataSet ->
                        (dataSet as? LineDataSet)?.apply {
                            circleHoleColor = Color.WHITE // Warna titik ketika tidak terpilih
                            circleHoleRadius = 4f // Ukuran lubang pada titik ketika tidak terpilih
                        }
                    }
                    chart.invalidate() // Refresh tampilan grafik
                }
            })
        }
    }

    private fun updateStatus(totalHurufBenar : Int){
        val listStatus = listOf("Tidak Memuaskan", "Buruk", "Stabil", "Baik", "Memuaskan")
        val listDetailStatus = listOf("Hasil belajarmu kali ini belum memuaskan. Ayo, lebih semangat lagi dan jangan menyerah! Kamu pasti bisa lebih baik.",
            "Kamu bisa lebih baik dari ini! Mari kita coba latihan lagi dan terus belajar dengan giat.",
            "Kamu belajar dengan baik dan hasilmu stabil. Terus pertahankan dan coba untuk meningkatkan lebih lagi!",
            "Hasil belajarmu sangat baik! Kamu menunjukkan peningkatan yang bagus. Teruskan usahamu!",
            "Kamu luar biasa! Hasil belajarmu sangat memuaskan. Tetaplah semangat dan terus belajar.")

        if(totalHurufBenar <= 20){
            binding.tvStatus.text = listStatus[0]
            binding.tvStatus.setTextColor(resources.getColor(R.color.red_color))
            binding.tvStatusDetails.text = listDetailStatus[0]
        }else if (totalHurufBenar in 21..40){
            binding.tvStatus.text = listStatus[1]
            binding.tvStatus.setTextColor(resources.getColor(R.color.orange_color))
            binding.tvStatusDetails.text = listDetailStatus[1]
        }else if(totalHurufBenar in 41..60){
            binding.tvStatus.text = listStatus[2]
            binding.tvStatus.setTextColor(resources.getColor(R.color.yellow_color))
            binding.tvStatusDetails.text = listDetailStatus[2]
        }else if(totalHurufBenar in 61..80){
            binding.tvStatus.text = listStatus[3]
            binding.tvStatus.setTextColor(resources.getColor(R.color.green_color))
            binding.tvStatusDetails.text = listDetailStatus[3]
        }else{
            binding.tvStatus.text = listStatus[4]
            binding.tvStatus.setTextColor(resources.getColor(R.color.green_darken))
            binding.tvStatusDetails.text = listDetailStatus[4]
        }
    }

    //Boolean true if latihan arrow click, Boolean false if kuis arrow click
    private fun toggleVisibility(view : View, arrow: ImageView){
        if(view.visibility == View.GONE){
            view.visibility = View.VISIBLE
            arrow.rotation = 270f
        }else{
            view.visibility = View.GONE
            arrow.rotation = 180f
        }
    }

    fun pengangge1Click() = toggleVisibility(binding.rvPengangge1, binding.ivArrow1)
    fun pengangge2Click() = toggleVisibility(binding.rvPengangge2, binding.ivArrow2)
    fun pengangge3Click() = toggleVisibility(binding.rvPengangge3, binding.ivArrow3)
    fun pengangge4Click() = toggleVisibility(binding.rvPengangge4, binding.ivArrow4)
    fun pengangge5Click() = toggleVisibility(binding.rvPengangge5, binding.ivArrow5)
    fun pengangge6Click() = toggleVisibility(binding.rvPengangge6, binding.ivArrow6)
    fun kuisClick() = toggleVisibility(binding.llKuis, binding.ivArrow)
    fun btnBack() = finish()
}