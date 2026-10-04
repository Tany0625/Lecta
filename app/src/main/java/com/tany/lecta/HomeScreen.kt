package com.tany.lecta

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    var taskToEdit by remember { mutableStateOf<LectaTask?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    BackHandler(enabled = menuOpen) { menuOpen = false }
    val sortedTasks = tasks.sortedWith(
        compareBy<LectaTask> { it.done }
            .thenBy { it.priority.ordinal }
            .thenBy { it.startDate }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lectaColors.background)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {
                LectaHeader(
                    profileImage = profilePainter,
                    onProfileClick = {
                        fabExpanded = false
                        accountOpen = true
                    },
                    onMenuClick = { menuOpen = true }
                )

                GreetingSection(
                    tasks = tasks,
                    userName = userName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp)
                )

                HomeBody(
                    noticeExpanded = noticeExpanded,
                    tasks = tasks,
                    sortedTasks = sortedTasks,
                    notices = sortedNotices,
                    listState = listState,
                    onViewAll = {
                        fabExpanded = false
                        noticeExpanded = true
                    },
                    onToggle = { task ->
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
                    onEdit = { task -> taskToEdit = task },
                    onDelete = { task -> tasks.removeAll { it.id == task.id } },
                    onAddNotice = { showNoticeDialog = true },
                    onDeleteNotice = { notice -> noticeToDelete = notice },
                    onCloseNotices = { noticeExpanded = false },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = !noticeExpanded,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(250))
            ) {
                AddMenu(
                    expanded = fabExpanded,
                    onToggle = { fabExpanded = !fabExpanded },
                    onDismiss = { fabExpanded = false },
                    onManual = {
                        fabExpanded = false
                        showAddDialog = true
                    },
                    onAi = {
                        fabExpanded = false
                        showAiDialog = true
                    }
                )
            }

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

            taskToEdit?.let { target ->
                AddTaskDialog(
                    initial = target,
                    onDismiss = { taskToEdit = null },
                    onConfirm = { title, priority, start, end ->
                        val index = tasks.indexOfFirst { it.id == target.id }
                        if (index >= 0) {
                            tasks[index] = tasks[index].copy(
                                title = title,
                                priority = priority,
                                startDate = start,
                                endDate = end
                            )
                        }
                        taskToEdit = null
                    }
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
