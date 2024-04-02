package edu.wcu.cs.thomas_kay.gpskotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Score : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        var seconds = 0
        val bundle = intent.extras
        if(bundle != null) {
            seconds = bundle.getInt(SCORE)
        }
        val score = MAX_SECONDS - seconds
        val txt1 = this.findViewById(R.id.score_num) as TextView
        val txt2 = this.findViewById(R.id.score_enum) as TextView
        var text = txt1.text.toString()
        text = "$text $score"
        txt1.text = text
        when {
            score >= 10000 -> {txt2.text = "Fastest person in the world!"}
            score >= 8000 -> {txt2.text = "Excellent job!"}
            score >= 6000 -> {txt2.text = "Nice running!"}
            score >= 4000 -> {txt2.text = "A little more practice can do the trick"}
            else -> {txt2.text = "Better luck next time"}
        }
    }
}

enum class Rating(score_value:Int, score_message:String) {
    AMAZING(10000, "Fastest person in the world!"),
    GREAT(8000, "Excellent job!"),
    GOOD(6000, "Nice running!"),
    OKAY(4000, "A little more practice can do the trick")

}