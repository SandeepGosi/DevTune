package com.devtune.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.devtune.ui.theme.DevTuneTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingRoute(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

    LaunchedEffect(isOnboardingCompleted) {
        if (isOnboardingCompleted) {
            onOnboardingComplete()
        }
    }

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onFinish = viewModel::completeOnboarding
        )
    }
}

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPage(page)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DevTuneTheme.spacing.screenPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator (Simple)
                Text(
                    text = "${pagerState.currentPage + 1} / 3",
                    style = MaterialTheme.typography.labelLarge
                )

                Row {
                    if (pagerState.currentPage > 0) {
                        TextButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }) {
                            Text("Back")
                        }
                    }

                    Button(onClick = {
                        scope.launch {
                            if (pagerState.currentPage < 2) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                onFinish()
                            }
                        }
                    }) {
                        Text(if (pagerState.currentPage == 2) "Get Started" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DevTuneTheme.spacing.huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (title, description) = when (page) {
            0 -> "Welcome to DevTune" to "Your modern Android companion for managing developer settings and system preferences with ease."
            1 -> "Safe & Transparent" to "DevTune respects Android's security model. We don't use root, hidden APIs, or bypass system restrictions."
            2 -> "Privacy First" to "All your data stays on your device. We are offline-first and committed to your privacy."
            else -> "" to ""
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(DevTuneTheme.spacing.medium))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
