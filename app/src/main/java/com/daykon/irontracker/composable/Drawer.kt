package com.daykon.irontracker.composable

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.daykon.irontracker.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerItem(text: String,
               icon:  @Composable () -> Unit,
               onClick: () -> Unit,
               drawerState: DrawerState,
               isSelected: Boolean = false,
               ) {
    val scope = rememberCoroutineScope()

    NavigationDrawerItem(
        icon = icon,
        label = { Text(text) },
        selected = isSelected,
        onClick = {
            scope.launch { drawerState.close()
            onClick()
            }
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@ExperimentalMaterial3Api
@Composable
fun Drawer (isSelected: Int, drawerState: DrawerState,
            navController: NavController,
            content: @Composable () -> Unit) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                DrawerItem(text = "Home",
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    onClick = {  },
                    drawerState = drawerState,
                    isSelected = isSelected == 0)
                Spacer(Modifier.height(12.dp))
                DrawerItem(text = "Progress Pics",
                    icon = { Icon(painterResource(id = R.drawable.photo_camera_black_24dp),
                        contentDescription = null) },
                    onClick = { Log.d("TESTDEBUG", "progress")
                        navController.navigate("progress") },
                    drawerState = drawerState,
                    isSelected = isSelected == 0)
                Spacer(Modifier.height(12.dp))
                DrawerItem(text = "Weight",
                    icon = { Icon(painterResource(id = R.drawable.monitor_weight_black_24dp),
                        contentDescription = null) },
                    onClick = {  },
                    drawerState = drawerState,
                    isSelected = isSelected == 0)

            }
        },
        content = content
    )
}