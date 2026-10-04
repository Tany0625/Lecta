package com.tany.lecta

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LectaHome() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf(ProfileStore.loadName(context)) }
    var profileImage by remember { mutableStateOf(ProfileStore.loadImage(context)) }
    val profilePainter = remember(profileImage) { profileImage?.let { BitmapPainter(it) } }
    var accountOpen by remember { mutableStateOf(false) }
    var aboutOpen by remember { mutableStateOf(false) }
    var helpOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) { ProfileStore.saveImage(context, uri) }
                if (saved != null) profileImage = saved
            }
        }
    }

    val tasks = remember {
        mutableStateListOf<LectaTask>().apply { addAll(TaskStore.load(context)) }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { tasks.toList() }.collect {
            TaskStore.save(context, it)
            ReminderScheduler.sync(context, it)
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) ReminderScheduler.sync(context, tasks.toList())
    }

    LaunchedEffect(Unit) {
        ReminderScheduler.ensureChannel(context)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch("android.permission.POST_NOTIFICATIONS")
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            tasks.removeAll { TaskStore.isExpired(it, now) }
            delay(60_000L)
        }
    }

    val notices = remember {
        mutableStateListOf<LectaNotice>().apply { addAll(NoticeStore.load(context)) }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { notices.toList() }.collect { NoticeStore.save(context, it) }
    }

    val todayDate = LocalDate.now()
    val sortedNotices = notices.sortedWith(
        compareBy<LectaNotice> { it.date.isBefore(todayDate) }
            .thenBy { if (it.date.isBefore(todayDate)) -it.date.toEpochDay() else it.date.toEpochDay() }
    )

    var noticeExpanded by remember { mutableStateOf(false) }
    var showNoticeDialog by remember { mutableStateOf(false) }
    var noticeToDelete by remember { mutableStateOf<LectaNotice?>(null) }

    BackHandler(enabled = noticeExpanded) { noticeExpanded = false }
    BackHandler(enabled = accountOpen) { accountOpen = false }
    BackHandler(enabled = aboutOpen) { aboutOpen = false }
    BackHandler(enabled = helpOpen) { helpOpen = false }
    BackHandler(enabled = settingsOpen) { settingsOpen = false }

    val listState = rememberLazyListState()

    var fabExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = menuOpen) { menuOpen = false }
    val plusRotation by animateFloatAsState(
        targetValue = if (fabExpanded) 90f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "plusRotation"
    )

    val sortedTasks = tasks.sortedWith(
        compareBy<LectaTask> { it.done }
            .thenBy { it.priority.ordinal }
            .thenBy { it.startDate }
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(lectaColors.background)
    ) {
        val noticeBoardBottomGap = 10.dp

        val taskAreaTop = (maxHeight / 2) + 15.dp
        val boxWidth = (maxWidth - 32.dp - 10.dp) / 2
        val boxHeight = boxWidth * (152f / 184f)
        val noticeTop = taskAreaTop - 30.dp - boxHeight - noticeBoardBottomGap

        val greetingHeight = maxOf(noticeTop - 52.dp - 8.dp, 0.dp)

        Box(modifier = Modifier.fillMaxSize()) {

            GreetingSection(
                tasks = tasks,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 52.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(greetingHeight),
                userName = userName,
                showSummary = greetingHeight >= 80.dp
            )

            AnimatedVisibility(
                visible = !noticeExpanded,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(250))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = noticeTop),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NoticeBoard(
                        notices = sortedNotices,
                        onViewAll = {
                            fabExpanded = false
                            noticeExpanded = true
                        },
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
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = taskAreaTop - 30.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(this@BoxWithConstraints.maxHeight - taskAreaTop)
                        .align(Alignment.TopStart)
                        .offset(y = taskAreaTop)
                        .clipToBounds()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 5.dp
                        ),
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
                                onToggle = {
                                    val firstIndex = listState.firstVisibleItemIndex
                                    val firstOffset = listState.firstVisibleItemScrollOffset

                                    val index = tasks.indexOfFirst { it.id == task.id }
                                    if (index >= 0) {
                                        val current = tasks[index]
                                        tasks[index] = current.copy(
                                            done = !current.done,
                                            completedAt = if (current.done) null else System.currentTimeMillis()
                                        )
                                    }

                                    listState.requestScrollToItem(firstIndex, firstOffset)
                                },
                                onDelete = { tasks.removeAll { it.id == task.id } },
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

                if (fabExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { fabExpanded = false }
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    AnimatedVisibility(
                        visible = fabExpanded,
                        enter = fadeIn(tween(200, delayMillis = 60)) +
                                scaleIn(tween(200, delayMillis = 60)) +
                                slideInVertically(tween(250, delayMillis = 60)) { it / 2 },
                        exit = fadeOut(tween(150)) +
                                scaleOut(tween(150)) +
                                slideOutVertically(tween(200)) { it / 2 }
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                showAddDialog = true
                            },
                            modifier = Modifier.padding(bottom = 12.dp),
                            shape = CircleShape,
                            containerColor = lectaColors.card,
                            contentColor = lectaColors.accentDark
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Add task manually",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = fabExpanded,
                        enter = fadeIn(tween(200)) +
                                scaleIn(tween(200)) +
                                slideInVertically(tween(250)) { it / 2 },
                        exit = fadeOut(tween(150, delayMillis = 40)) +
                                scaleOut(tween(150, delayMillis = 40)) +
                                slideOutVertically(tween(200, delayMillis = 40)) { it / 2 }
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                fabExpanded = false
                                showAiDialog = true
                            },
                            modifier = Modifier.padding(bottom = 12.dp),
                            shape = CircleShape,
                            containerColor = lectaColors.card,
                            contentColor = lectaColors.accentDark
                        ) {
                            Text(
                                text = "AI",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        shape = CircleShape,
                        containerColor = lectaColors.accent
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add task",
                            modifier = Modifier.rotate(plusRotation),
                            tint = Color.White
                        )
                    }
                }
                }
            }

            AnimatedVisibility(
                visible = noticeExpanded,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = noticeTop),
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 10 },
                exit = fadeOut(tween(200))
            ) {
                NoticeBoardFull(
                    notices = sortedNotices,
                    onAdd = { showNoticeDialog = true },
                    onDelete = { notice -> noticeToDelete = notice },
                    onClose = { noticeExpanded = false },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(this@BoxWithConstraints.maxHeight - noticeTop - 16.dp)
                )
            }

            LectaHeader(
                profileImage = profilePainter,
                onProfileClick = {
                    fabExpanded = false
                    accountOpen = true
                },
                onMenuClick = { menuOpen = true }
            )

            AnimatedVisibility(
                visible = accountOpen,
                modifier = Modifier.fillMaxSize(),
                enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(300)),
                exit = slideOutHorizontally(tween(250, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250))
            ) {
                AccountScreen(
                    userName = userName,
                    profileImage = profilePainter,
                    hasCustomPhoto = profileImage != null,
                    pendingCount = tasks.count { !it.done },
                    doneCount = tasks.count { it.done },
                    noticeCount = notices.size,
                    onNameSave = { entered ->
                        val finalName = if (entered.isBlank()) ProfileStore.DEFAULT_NAME else entered
                        userName = finalName
                        ProfileStore.saveName(context, finalName)
                    },
                    onUpload = { photoLauncher.launch(arrayOf("image/*")) },
                    onRemovePhoto = {
                        ProfileStore.deleteImage(context)
                        profileImage = null
                    },
                    onBack = { accountOpen = false }
                )
            }

            AnimatedVisibility(
                visible = aboutOpen,
                modifier = Modifier.fillMaxSize(),
                enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(300)),
                exit = slideOutHorizontally(tween(250, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250))
            ) {
                AboutScreen(onBack = { aboutOpen = false })
            }

            AnimatedVisibility(
                visible = helpOpen,
                modifier = Modifier.fillMaxSize(),
                enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(300)),
                exit = slideOutHorizontally(tween(250, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250))
            ) {
                HelpScreen(onBack = { helpOpen = false })
            }

            AnimatedVisibility(
                visible = settingsOpen,
                modifier = Modifier.fillMaxSize(),
                enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(300)),
                exit = slideOutHorizontally(tween(250, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250))
            ) {
                SettingsScreen(
                    taskCount = tasks.size,
                    noticeCount = notices.size,
                    onClearData = {
                        tasks.clear()
                        notices.clear()
                    },
                    onBack = { settingsOpen = false }
                )
            }

            AnimatedVisibility(
                visible = menuOpen,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { menuOpen = false }
                )
            }

            AnimatedVisibility(
                visible = menuOpen,
                modifier = Modifier.align(Alignment.CenterStart),
                enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it },
                exit = slideOutHorizontally(tween(250, easing = FastOutSlowInEasing)) { -it }
            ) {
                SideMenu(
                    userName = userName,
                    profileImage = profilePainter,
                    onClose = { menuOpen = false },
                    onAccount = {
                        menuOpen = false
                        accountOpen = true
                    },
                    onAbout = {
                        menuOpen = false
                        aboutOpen = true
                    },
                    onHelp = {
                        menuOpen = false
                        helpOpen = true
                    },
                    onSettings = {
                        menuOpen = false
                        settingsOpen = true
                    }
                )
            }

            noticeToDelete?.let { target ->
                ConfirmDeleteDialog(
                    title = "Delete notice?",
                    question = "Are you sure you want to delete this notice?",
                    preview = target.text,
                    onConfirm = {
                        notices.removeAll { it.id == target.id }
                        noticeToDelete = null
                    },
                    onDismiss = { noticeToDelete = null }
                )
            }

            if (showNoticeDialog) {
                AddNoticeDialog(
                    onDismiss = { showNoticeDialog = false },
                    onConfirm = { text, date ->
                        val nextId = (notices.maxOfOrNull { it.id } ?: 0) + 1
                        notices.add(LectaNotice(id = nextId, text = text, date = date))
                        showNoticeDialog = false
                    }
                )
            }

            if (showAiDialog) {
                AiTaskDialog(
                    onDismiss = { showAiDialog = false },
                    onConfirm = { _, _ -> showAiDialog = false }
                )
            }

            if (showAddDialog) {
                AddTaskDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { title, priority, start, end ->
                        val nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                        tasks.add(
                            LectaTask(
                                id = nextId,
                                title = title,
                                source = "Manual",
                                priority = priority,
                                startDate = start,
                                endDate = end,
                                confidence = 0,
                                extracted = "",
                                manual = true
                            )
                        )
                        showAddDialog = false
                    }
                )
            }
        }
    }
}
