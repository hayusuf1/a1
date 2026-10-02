package com.example.rapidrecall

import android.R
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rapidrecall.ui.theme.RapidRecallTheme
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import kotlin.compareTo
import kotlin.concurrent.timer
import kotlin.time.Duration.Companion.milliseconds

val sessionModel = SessionModel()

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

                    MyNavigation(modifier = Modifier)


        }
    }
}


//This is the navigation controller. It manages navigation between the
// different screens back-and-forth. This function creates a navController
// and a NavHost which starts at the StartScreen and is able to called
// when navigating to or from game screen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyNavigation(modifier: Modifier){
    val navController = rememberNavController()

    NavHost (navController=navController, startDestination = "start")
    {
        composable("start"){

            StartScreen(
                modifier ,
                navController
                )
        }
        composable("game"){
            GameScreen(modifier, navController)
        }

    }
}


//The start screen is the first thing you see when you open the app
// and it contains, the start game button, game summary and updated logs,
// retrieved from SessionModel class and AttemptLogs data class respectively.
@Composable
fun StartScreen(modifier: Modifier = Modifier, navController: NavController) {


    val attempts = sessionModel.attempts

    val correctAttempts =
        sessionModel.correctAttempts

    val accuracyPercentage =
       sessionModel.overallAccuracy

    Column(

    ) {

            Row( modifier = modifier.padding(100.dp)) {
                Button(

                    onClick = {
                        navController.navigate("game")
                    },
                    modifier = modifier
                        .width(150.dp)
                        .height(70.dp)

                ) { Text(
                   text =  "Start",
                    fontSize = 20.sp

                ) }
            }



        Row(
            modifier = modifier.padding(vertical = 50.dp)
        ) {
            Text( text = "Total attempts: $attempts", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = modifier.weight(0.5f))
            Text("Total correct attempts: $correctAttempts" , color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = modifier.weight(0.5f))
            Text("Overall accuracy %: $accuracyPercentage" , color = Color.Black, fontWeight = FontWeight.Bold)

        }

        Spacer(modifier = modifier.weight(1f))

        Row(

        ) {
            Text( text = "Sequence Length", color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = modifier.weight(0.5f))
            Text("User Input" , color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = modifier.weight(0.5f))
            Text("Target Sequence" , color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = modifier.weight(0.5f))
            Text("Verdict" , color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = modifier.weight(0.5f))
            Text("Timestamp" , color = Color.Black, fontWeight = FontWeight.Bold)

        }


        LazyColumn(
            modifier = Modifier.fillMaxSize()) {
            itemsIndexed(sessionModel.attemptLogs) { index, attempt ->
                AttemptLog(
                    modifier = Modifier,
                    attempt = attempt
                )

                if (index < sessionModel.attemptLogs.lastIndex) {
                    HorizontalDivider()
                }
            }
        }

            Row() { }

        }


    }




// This is just the rows in starting screen that show attempt logs throughout
// each session.
@Composable
fun AttemptLog(
    modifier: Modifier,
    attempt: AttemptLogs
){



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)


    ) {
        Text(
            text = attempt.sequenceLength.toString(),
            fontSize = 20.sp,
            modifier = modifier.weight(1f)
        )
        Spacer(modifier = modifier.weight(0.5f))
        Text(
            text = attempt.userInput,
            fontSize = 20.sp,
            modifier = modifier.weight(1f)
        )
        Spacer(modifier = modifier.weight(0.5f))
        Text(
            text = attempt.targetSequence,
            fontSize = 20.sp,
            modifier = modifier.weight(1f)
        )

        Text(
            text = attempt.verdict,
            fontSize = 20.sp,
            modifier = modifier.weight(1f)
        )

        Text(
            text = attempt.timeStamp,
            fontSize = 15.sp,
            modifier = modifier.weight(1f)
        )
    }
}


//this is the game screen, and it handles the most important functionalities
// First it has sequence length choosing functionality, which allows the
// user to choose between (1-10) length, then randomly generates a sequence
// and shows each character to the user for one second, then shows the
// user guessing input field. The user then is able to guess and see
// if they're correct and the correct answer compared to theirs.The whole data
// is stored into the SessionModel class and AttemptLogs data class.
// the user can go back to the starting screen any time.
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GameScreen(modifier: Modifier,navController: NavController){

    var sequenceLength  by remember { mutableStateOf("") }
    var userGuess by remember { mutableStateOf("") }
    var showSequence by remember { mutableStateOf(false) }

    val guessPool = arrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z',

        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z',

        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    var randomSequence by remember { mutableStateOf("") }

    var verdict by remember { mutableStateOf("") }

    val timeStamp by remember { mutableStateOf(LocalDateTime.now()) }

    val context = LocalContext.current

    var showInputField by remember { mutableStateOf(false) }

    var timer by remember { mutableIntStateOf(2) }

    Column(

    ) {

        Row( modifier = modifier.padding(50.dp)) {

            Button(
                onClick = {

                    navController.popBackStack()

                },
                modifier = modifier
                    .width(100.dp)
                    .height(50.dp)

            ) {Text("<-") }

        }

        Row(

        ) {

            OutlinedTextField(
                modifier = modifier.weight(0.11f),
                value = sequenceLength,
                onValueChange = {sequenceLength = it},
                label = {Text("Choose Sequence length")},

            )

            Button(
                onClick = {

                    if(sequenceLength.toInt() in 1..10) {


                        randomSequence = ""
                        for (i in 1..sequenceLength.toInt()) {

                            randomSequence += guessPool.random()

                        }

                        showSequence = true

                    }

                    else if(sequenceLength.toInt()<1){
                        Toast.makeText(context, "Please a choose sequence length", Toast.LENGTH_LONG).show()
                    }

                    else if(sequenceLength.toInt()>10){
                        Toast.makeText(context, "Please choose a length less than 10", Toast.LENGTH_LONG).show()
                    }


                }
            ) { Text("Set Sequence") }

        }

        Spacer(modifier = modifier.width(8.dp))

        if (showSequence){
            Row(

                ) {

                var currentIndex by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    for (i in randomSequence.indices){
                        currentIndex = i
                        delay(1000.milliseconds)
                    }

                    showSequence = false
                    showInputField = true


                }

                Text(text = randomSequence[currentIndex].toString(),
                    modifier = modifier.padding(vertical = 50.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                    )



            }
        }


        Row() {

            if (showInputField){


            OutlinedTextField(
                value = userGuess,
                onValueChange = {userGuess=it},
                label = {Text("Guess sequence")},
                modifier = modifier.weight(1f)
            )



            Spacer(modifier = modifier.width(8.dp))

            Button(
                onClick = {

                    sessionModel.attempts++

                    if (userGuess == randomSequence){
                        sessionModel.correctAttempts++
                        verdict = "Correct!"

                        Toast.makeText(context, "Correct guess!", Toast.LENGTH_LONG).show()

                    }
                    else {
                        Toast.makeText(context, "Incorrect guess!", Toast.LENGTH_LONG).show()
                        verdict = "Incorrect!"
                    }


                    sessionModel.overallAccuracy = (sessionModel.correctAttempts.toDouble()/sessionModel.attempts.toDouble())*100
                    showInputField = false

                    sessionModel.addAttemptLog(
                        attempt = AttemptLogs(
                            sequenceLength.toInt(),
                            userGuess,
                            targetSequence = randomSequence,
                            verdict = verdict,
                            timeStamp = timeStamp.toString()
                        )
                    )

                    userGuess = ""





                }
            ) { Text("Play") }



        }
        }

        Row(
            modifier = modifier.padding(20.dp)
        ) {
            Text(text="Correct sequence:$randomSequence", fontWeight = FontWeight.Bold)
            Text(text="Your sequence: $userGuess", fontWeight = FontWeight.Bold)
        }
    }


}