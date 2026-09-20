package com.tany.lecta
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import com.tany.lecta.ui.theme.LectaBackground
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.tany.lecta.ui.theme.LectaTaskBox
import com.tany.lecta.ui.theme.LectaTheme

class MainActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContent {
            LectaTheme {
                LectaHome()
            }
        }

    }

}

@Composable
fun TaskBox(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(242.dp)
            .background(
                color = LectaTaskBox,
                shape = RoundedCornerShape(9.dp)
            )
            .border(
                width = 0.2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(9.dp)
            )
    )
}

@Composable
fun LectaHome() {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(LectaBackground)
    ) {

        val taskStart = (maxHeight / 2) - 30.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 5.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item {
                Spacer(
                    modifier = Modifier.height(taskStart)
                )
            }

            // Tasks
            item {
                TaskBox()
            }

            item {
                TaskBox()
            }

            item {
                TaskBox()
            }
        }
    }
}