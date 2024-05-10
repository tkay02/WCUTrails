package edu.wcu.cs.thomas_kay.gpskotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

/**
 * @author Thomas Kay
 * @version 5/9/2024
 *
 * The activity that prints out the score that the user received after completing a trail. Plans for
 * this activity is to rebuild it into a leaderboard.
 */

class Score : AppCompatActivity() {

    /**
     * Sets up activity and calculates the score to display for the user. Grabs the number of
     * seconds from an intent that was passed to the activity. Uses an enum to determine what
     * message that should be displayed for the user.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        var seconds = 0
        val bundle = intent.extras
        Toast.makeText(this, "${bundle != null}", Toast.LENGTH_SHORT).show()
        if(bundle != null) {
            seconds = bundle.getInt(SCORE)
            Toast.makeText(this, "Seconds: $seconds", Toast.LENGTH_LONG).show()
        }
        val score = MAX_SECONDS - seconds
        val txt1 = this.findViewById(R.id.score_num) as TextView
        val txt2 = this.findViewById(R.id.score_enum) as TextView
        var text = txt1.text.toString()
        text = "$text $score"
        txt1.text = text
        // Uses an enum to calculate and display score
        when {
            score >= Rating.AMAZING.score-> {txt2.text = Rating.AMAZING.scoreMessage}
            score >= Rating.GREAT.score -> {txt2.text = Rating.GREAT.scoreMessage}
            score >= Rating.GOOD.score -> {txt2.text = Rating.GOOD.scoreMessage}
            score >= Rating.OKAY.score -> {txt2.text = Rating.OKAY.scoreMessage}
            else -> {txt2.text = Rating.WORST.scoreMessage}
        }
    }
}

/**
 * Enum used to display an encouraging message (or not) based on a received score.
 *
 * @property score The score required for the message.
 * @property scoreMessage The message related to the score.
 */
enum class Rating(val score:Int, val scoreMessage:String) {
    // Best possible score
    AMAZING(10000, "Fastest person in the world!"),
    GREAT(8000, "Excellent job!"),
    GOOD(6000, "Nice running!"),
    OKAY(4000, "A little more practice can do the trick"),
    // Worst possible score
    WORST(1, "Better luck next time")

}