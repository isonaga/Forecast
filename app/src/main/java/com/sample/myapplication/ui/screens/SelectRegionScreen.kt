package com.sample.myapplication.ui.screens

import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.sample.myapplication.R
import kotlinx.coroutines.launch

// 地域を選択する画面
object SelectRegionScreen {
    // 地域
    sealed class Region(val cityName: String, val resId: Int) {
        object TOKYO : Region("Tokyo", R.string.region_tokyo)
        object HYOGO : Region("Hyogo", R.string.region_hyogo)
        object OITA : Region("Oita", R.string.region_oita)
        object HOKKAIDO : Region("Hokkaido", R.string.region_hokkaido)

        // 現在位置
        data class Location(
            val latitude: Double,
            val longitude: Double,
        ) : Region("Location", R.string.region_location)
    }

    // 固定の地域一覧
    val regions = listOf(
        Region.TOKYO,
        Region.HYOGO,
        Region.OITA,
        Region.HOKKAIDO,
    )

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        onSelectRegion: (Region) -> Unit,
    ) {
        val snackBarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        val locationPermissionState = rememberPermissionState(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        var permissionRequested by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val fusedLocationClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }

        val scrollBehavior =
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        val findLocation = {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    onSelectRegion(
                        Region.Location(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                        )
                    )
                } else {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(context.getString(R.string.region_location_failed))
                    }
                }
            }
        }

        LaunchedEffect(locationPermissionState.status) {
            if (permissionRequested && locationPermissionState.status.isGranted) {
                findLocation()
            }
        }

        Scaffold(
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.select_region_title))
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick =
                        @RequiresPermission(
                            allOf = [
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            ]
                        ) {
                            // 現在位置を取得して画面遷移
                            if (locationPermissionState.status.isGranted) {
                                findLocation()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                                permissionRequested = true
                            }
                        },
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                // 固定の地域をボタンで表示
                items(
                    items = regions,
                    key = { region -> region.cityName },
                ) { region ->
                    Button(
                        modifier = modifier,
                        onClick = {
                            onSelectRegion(region)
                        }
                    ) {
                        Text(text = stringResource(region.resId))
                    }
                }
            }
        }
    }
}
