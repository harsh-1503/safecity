package com.grayseal.safecity.components

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.grayseal.safecity.R
import com.grayseal.safecity.ui.theme.Green
import kotlinx.coroutines.launch

enum class MultiFloatingState {
    Expanded,
    Collapsed
}

class MiniFabItem(
    val icon: ImageBitmap,
    val label: String,
    val identifier: String
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MultiFloatingActionButton(
    multiFloatingState: MultiFloatingState,
    sheetState: BottomSheetState,
    drawerState: DrawerState,
    onMultiFabStateChange: (MultiFloatingState) -> Unit,
    items: List<MiniFabItem>,
) {
    val transition = updateTransition(targetState = multiFloatingState, label = "transition")
    val rotate by transition.animateFloat(label = "rotate") {
        if (it == MultiFloatingState.Expanded) 315f else 0f
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFabVisible by remember { mutableStateOf(true) }

    // Remember the MutableInteractionSource here
    val interactionSource = remember { MutableInteractionSource() }

    val modifier = if (transition.currentState == MultiFloatingState.Expanded) {
        Modifier.padding(bottom = 250.dp)
    } else {
        Modifier.padding(bottom = 120.dp)
    }

    if (isFabVisible) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            if (transition.currentState == MultiFloatingState.Expanded) {
                items.forEach {
                    MiniFab(
                        item = it,
                        onMiniFabItemClick = { item ->
                            if (item.label == "Report") {
                                isFabVisible = false
                                scope.launch {
                                    if (sheetState.isCollapsed) {
                                        sheetState.expand()
                                    }
                                }
                            }
                            if (item.label == "Call Police") {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.CALL_PHONE
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    ActivityCompat.requestPermissions(
                                        context as Activity,
                                        arrayOf(android.Manifest.permission.CALL_PHONE),
                                        101
                                    )
                                } else {
                                    val intent =
                                        Intent(Intent.ACTION_CALL, Uri.parse("tel:+254759060329"))
                                    startActivity(context, intent, null)
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.size(5.dp))
                }
                Spacer(modifier = Modifier.size(5.dp))
            }
            FloatingActionButton(
                modifier = Modifier
                    .size(60.dp),
                onClick = {
                    onMultiFabStateChange(
                        if (transition.currentState == MultiFloatingState.Expanded) {
                            MultiFloatingState.Collapsed
                        } else {
                            MultiFloatingState.Expanded
                        }
                    )
                },
                shape = CircleShape,
                containerColor = Green,
                interactionSource = interactionSource // Use remembered interactionSource here
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add",
                    modifier = Modifier.rotate(rotate)
                )
            }
        }
    }

    LaunchedEffect(sheetState.isCollapsed, drawerState.isOpen) {
        isFabVisible = sheetState.isCollapsed && !drawerState.isOpen
    }
}


@Composable
fun MiniFab(
    item: MiniFabItem,
    showLabel: Boolean = true,
    onMiniFabItemClick: (MiniFabItem) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showLabel) {
            Text(
                text = item.label,
                fontSize = 16.sp,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
                // Uncomment for background effect if needed
                // modifier = Modifier.background(color = Color.White, shape = RoundedCornerShape(3.dp)).padding(horizontal = 5.dp)
            )
        }
        Spacer(modifier = Modifier.size(5.dp))
        Canvas(
            modifier = Modifier
                .size(60.dp)
                .clickable(
                    onClick = { onMiniFabItemClick.invoke(item) },
                    interactionSource = interactionSource,
                    indication = indication
                ),
            onDraw = {
                drawCircle(
                    color = Color.White,
                    radius = 50f
                )
                drawImage(
                    image = item.icon,
                    topLeft = Offset(
                        center.x - (item.icon.width / 2),
                        center.y - (item.icon.height / 2)
                    ),
                    colorFilter = ColorFilter.tint(color = Green)
                )
            }
        )
    }
}
