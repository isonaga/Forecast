package com.sample.myapplication.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.sample.myapplication.R
import com.sample.myapplication.domain.model.ForecastItem
import com.sample.myapplication.ui.MainUiState
import com.sample.myapplication.ui.screens.SelectRegionScreen.Region
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ForecastScreen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        modifier: Modifier = Modifier,
        navController: NavController = rememberNavController(),
        uiState: MainUiState,
    ) {
        val context = LocalContext.current
        val snackBarHostState = remember { SnackbarHostState() }
        val scrollBehavior =
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

        LaunchedEffect(Unit) {
            uiState.refresh()
        }

        // 状態に合わせてSnackBar表示
        LaunchedEffect(uiState.status) {
            val message = when (uiState.status) {
                MainUiState.Status.LOADING -> context.getString(R.string.forecast_loading)
                MainUiState.Status.SUCCESS -> context.getString(R.string.forecast_success)
                MainUiState.Status.ERROR -> context.getString(R.string.forecast_error)
                else -> ""
            }
            snackBarHostState.showSnackbar(message)
        }

        Scaffold(
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                R.string.forecast_title,
                                stringResource(uiState.region.resId)
                            ),
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
            },
        ) { innerPadding ->
            val pullRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                modifier = Modifier.padding(innerPadding),
                state = pullRefreshState,
                isRefreshing = uiState.status == MainUiState.Status.LOADING,
                onRefresh = {
                    uiState.refresh()
                },
            ) {
                val list = uiState.currentForecast?.list ?: emptyList()

                val sdf = SimpleDateFormat(
                    stringResource(R.string.forecast_date_format),
                    Locale.getDefault()
                )

                // 日付でグループ分け
                val dateList = list.groupBy { sdf.format(Date(it.dt * 1000)) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                ) {
                    itemsIndexed(
                        items = dateList.keys.toList(),
                        key = { i, _ -> "item_${i}" }
                    ) { i, date ->
                        Column {
                            Text(
                                text = date,
                            )
                            LazyRow {
                                itemsIndexed(
                                    items = dateList[date]?.toList() ?: emptyList(),
                                    key = { j, _ -> "weather_${i}_$j" }
                                ) { _, item ->
                                    item.Content()
                                }
                            }
                        }
                    }
                }

                if (uiState.status == MainUiState.Status.ERROR) {
                    Button(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = {
                            uiState.refresh()
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.forecast_reload),
                        )
                    }
                }
            }
        }
    }

    // 予報の表示
    @Composable
    private fun ForecastItem.Content() {
        Column(
            modifier = Modifier
                .width(120.dp)
                .border(1.dp, Color.Gray)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = SimpleDateFormat.getTimeInstance(
                    SimpleDateFormat.SHORT
                ).format(dt * 1000),
            )
            Row {
                weather.forEach { weather ->
                    Column {
                        AsyncImage(
                            modifier = Modifier.size(40.dp),
                            model = ImageRequest
                                .Builder(LocalContext.current)
                                .data("https://openweathermap.org/img/wn/${weather.icon}@2x.png")
                                .build(),
                            contentDescription = weather.description,
                        )
                        Text(text = weather.description)
                    }
                }
            }
            Text(
                text = stringResource(
                    R.string.forecast_temp,
                    main.temp
                ),
            )
        }
    }
}
