package com.tany.lecta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tany.lecta.ui.theme.LectaBackground
import com.tany.lecta.ui.theme.LectaTaskBox
import com.tany.lecta.ui.theme.LectaTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.layout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.max
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            LectaTheme {
                LectaHome()
            }
        }

    }
}

fun addBreakPoints(text: String): String {
    return text.replace(
        Regex("""([\-_/\.])"""),
        "$1\u200B"
    )
}

@Composable
fun TaskBox() {

    var check by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = LectaTaskBox,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 0.2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(14.dp)
            )
    ){
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            //Sec1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        top = 12.dp,
                        start = 8.dp,
                        end = 8.dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.calender),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = "25/06/06\nto\n03/09/06",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 4.dp)
                            .padding(horizontal = 2.dp),
                        fontSize = 9.sp,
                        lineHeight = 9.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
            //Div1
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFD3D3D3))
            )
            //Sec2 main one
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(
                        horizontal = 10.dp,
                        vertical = 12.dp
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Task: ")
                            }

                            append(
                                addBreakPoints(
                                    "Pre Medical test for semester - 1 for 20 credits"
                                )
                            )
                        },
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("Priority: ")
                                }

                                append("Critical")
                            },
                            fontSize = 13.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .background(
                                    color = Color(0xFFFF2C2C),
                                    shape = CircleShape
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Source: ")
                            }

                            append(
                                addBreakPoints(
                                    "Semester-wise-exam.pdf"
                                )
                            )
                        },
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        maxLines = 3
                    )
                }
            }
            //Div2
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFD3D3D3))
            )

            // Sec3
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // the upper text including confidence and all
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Confidence",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            lineHeight = 11.sp,
                            maxLines = 1
                        )

                        Text(
                            text = "93%",
                            fontSize = 11.sp,
                            lineHeight = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Checkbox's code
                    Checkbox(
                        checked = check,
                        onCheckedChange = { check = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.Green,
                            checkmarkColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // last bottom text
                    Text(
                        text = "Extracted 3hrs ago",
                        color = Color.Black.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        lineHeight = 10.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LectaHeader(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(LectaBackground)
    ){
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            tint = Color.Black
        )
    }
}


@Composable
fun LectaHome() {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(LectaBackground)
    ) {

        val taskStart = (maxHeight / 2) + 15.dp

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
            LectaHeader()
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