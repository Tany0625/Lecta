package com.tany.lecta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TasksPanel(
    tasks: List<LectaTask>,
    sortedTasks: List<LectaTask>,
    notices: List<LectaNotice>,
    listState: LazyListState,
    onViewAll: () -> Unit,
    onToggle: (LectaTask) -> Unit,
    onEdit: (LectaTask) -> Unit,
    onDelete: (LectaTask) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NoticeBoard(
                notices = notices,
                onViewAll = onViewAll,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(184f / 152f)
            )

            CalendarBox(
                tasks = tasks,
                sundayStart = AppSettings.sundayStart,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(184f / 152f)
            )
        }

        Text(
            text = "Upcoming Tasks",
            color = lectaColors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 8.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (sortedTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No task left",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = lectaColors.text.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            items(sortedTasks, key = { it.id }) { task ->
                TaskBox(
                    task = task,
                    onToggle = { onToggle(task) },
                    onEdit = { onEdit(task) },
                    onDelete = { onDelete(task) },
                    modifier = Modifier.animateItem(
                        placementSpec = spring(
                            dampingRatio = 0.65f,
                            stiffness = Spring.StiffnessLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun HomeBody(
    noticeExpanded: Boolean,
    tasks: List<LectaTask>,
    sortedTasks: List<LectaTask>,
    notices: List<LectaNotice>,
    listState: LazyListState,
    onViewAll: () -> Unit,
    onToggle: (LectaTask) -> Unit,
    onEdit: (LectaTask) -> Unit,
    onDelete: (LectaTask) -> Unit,
    onAddNotice: () -> Unit,
    onDeleteNotice: (LectaNotice) -> Unit,
    onCloseNotices: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !noticeExpanded,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(250))
        ) {
            TasksPanel(
                tasks = tasks,
                sortedTasks = sortedTasks,
                notices = notices,
                listState = listState,
                onViewAll = onViewAll,
                onToggle = onToggle,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }

        AnimatedVisibility(
            visible = noticeExpanded,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 10 },
            exit = fadeOut(tween(200))
        ) {
            NoticeBoardFull(
                notices = notices,
                onAdd = onAddNotice,
                onDelete = onDeleteNotice,
                onClose = onCloseNotices,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
}
