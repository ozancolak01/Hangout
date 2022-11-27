package com.example.hangout

class Events constructor(creatorID: String, title: String, category: String, location: String, participantNumber: Int, description: String, private: Boolean){
    private lateinit var creatorID: String
    private lateinit var title: String
    private lateinit var category: String
    private lateinit var location: String
    private lateinit var description: String

    private var participantNumber: Int = 0
    private var private: Boolean = false
}
