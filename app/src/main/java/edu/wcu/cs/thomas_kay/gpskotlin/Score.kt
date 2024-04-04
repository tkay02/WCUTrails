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
            score >= Rating.AMAZING.score-> {txt2.text = Rating.AMAZING.scoreMessage}
            score >= 8000 -> {txt2.text = Rating.GREAT.scoreMessage}
            score >= 6000 -> {txt2.text = Rating.GOOD.scoreMessage}
            score >= 4000 -> {txt2.text = Rating.OKAY.scoreMessage}
            else -> {txt2.text = Rating.WORST.scoreMessage}
        }
    }
}

enum class Rating(val score:Int, val scoreMessage:String) {
    AMAZING(10000, "Fastest person in the world!"),
    GREAT(8000, "Excellent job!"),
    GOOD(6000, "Nice running!"),
    OKAY(4000, "A little more practice can do the trick"),
    WORST(1, "Better luck next time")

}