package com.tany.lecta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tany.lecta.ui.theme.LectaBackground
import com.tany.lecta.ui.theme.LectaTaskBox
import com.tany.lecta.ui.theme.LectaTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            LectaTheme {
                LectaHome()
            }
        }
    }
}


@Composable
fun TaskBox() {
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

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

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
            FloatingActionButton(
                onClick = {
                    // Add task logic will go here
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                shape = CircleShape,
                containerColor = Color(0xFF8A4A25)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task",
                    tint = Color.White
                )
            }
        }
    }
}