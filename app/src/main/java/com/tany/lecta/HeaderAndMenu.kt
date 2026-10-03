package com.tany.lecta

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfilePicture(
    modifier: Modifier = Modifier,
    image: Painter? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF9AA0A6))
            .border(0.2.dp, lectaColors.text, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(
                painter = image,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize(0.72f),
                tint = Color.White
            )
        }
    }
}

@Composable
fun LectaHeader(
    profileImage: Painter? = null,
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(lectaColors.background)
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .clip(CircleShape)
                .clickable { onMenuClick() }
                .padding(8.dp),
            tint = lectaColors.text
        )

        ProfilePicture(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            image = profileImage,
            onClick = onProfileClick
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = lectaColors.accentDark
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = lectaColors.text
        )
    }
}

@Composable
fun SideMenu(
    userName: String,
    profileImage: Painter?,
    onClose: () -> Unit,
    onAccount: () -> Unit,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(
                lectaColors.background,
                RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAccount() }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePicture(
                modifier = Modifier.size(56.dp),
                image = profileImage,
                onClick = onAccount
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = userName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = lectaColors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(lectaColors.text.copy(alpha = 0.25f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        DrawerItem(Icons.Default.Person, "Account", onAccount)
        DrawerItem(Icons.Default.Info, "About the app", onAbout)
        DrawerItem(Icons.Default.Settings, "Settings", onSettings)
        DrawerItem(Icons.Default.Email, "Help & feedback", onHelp)

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Lecta v1.0",
            fontSize = 11.sp,
            color = lectaColors.text.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
