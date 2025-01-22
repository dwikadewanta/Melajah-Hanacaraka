package com.example.capstoneproject.util

class FirebaseDataClass {
    data class Latihan(
        val huruf_benar: Int = 0
    )

    data class LatihanTanpaPengangge(
        val ha : Int = 0, val na : Int = 0, val ca : Int = 0,
        val ra : Int = 0, val ka : Int = 0, val da : Int = 0,
        val ta : Int = 0, val sa : Int = 0, val wa : Int = 0,
        val la : Int = 0, val ma : Int = 0, val ga : Int = 0,
        val ba : Int = 0, val nga : Int = 0, val pa : Int = 0,
        val ja : Int = 0, val ya : Int = 0, val nya : Int = 0,
    )

    data class LatihanPenganggeUlu(
        val hi : Int = 0, val ni : Int = 0, val ci : Int = 0,
        val ri : Int = 0, val ki : Int = 0, val di : Int = 0,
        val ti : Int = 0, val si : Int = 0, val wi : Int = 0,
        val li : Int = 0, val mi : Int = 0, val gi : Int = 0,
        val bi : Int = 0, val ngi : Int = 0, val pi : Int = 0,
        val ji : Int = 0, val yi : Int = 0, val nyi : Int = 0,
    )

    data class LatihanPenganggeSuku(
        val hu : Int = 0, val nu : Int = 0, val cu : Int = 0,
        val ru : Int = 0, val ku : Int = 0, val du : Int = 0,
        val tu : Int = 0, val su : Int = 0, val wu : Int = 0,
        val lu : Int = 0, val mu : Int = 0, val gu : Int = 0,
        val bu : Int = 0, val ngu : Int = 0, val pu : Int = 0,
        val ju : Int = 0, val yu : Int = 0, val nyu : Int = 0,
    )

    data class LatihanPenganggeTaleng(
        val hé : Int = 0, val né : Int = 0, val cé : Int = 0,
        val ré : Int = 0, val ké : Int = 0, val dé : Int = 0,
        val té : Int = 0, val sé : Int = 0, val wé : Int = 0,
        val lé : Int = 0, val mé : Int = 0, val gé : Int = 0,
        val bé : Int = 0, val ngé : Int = 0, val pé : Int = 0,
        val jé : Int = 0, val yé : Int = 0, val nyé : Int = 0,
    )

    data class LatihanPenganggeTalengTedong(
        val ho : Int = 0, val no : Int = 0, val co : Int = 0,
        val ro : Int = 0, val ko : Int = 0, val `do` : Int = 0,
        val to : Int = 0, val so : Int = 0, val wo : Int = 0,
        val lo : Int = 0, val mo : Int = 0, val go : Int = 0,
        val bo : Int = 0, val ngo : Int = 0, val po : Int = 0,
        val jo : Int = 0, val yo : Int = 0, val nyo : Int = 0,
    )

    data class LatihanPenganggePepet(
        val he : Int = 0, val ne : Int = 0, val ce : Int = 0,
        val re : Int = 0, val ke : Int = 0, val de : Int = 0,
        val te : Int = 0, val se : Int = 0, val we : Int = 0,
        val le : Int = 0, val me : Int = 0, val ge : Int = 0,
        val be : Int = 0, val nge : Int = 0, val pe : Int = 0,
        val je : Int = 0, val ye : Int = 0, val nye : Int = 0,
    )

    data class Kuis(
        val no_records : Int = 0
    )

    data class User (
        val username: String,
        val email: String,
    )
}