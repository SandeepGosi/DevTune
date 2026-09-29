package com.devtune.ui.screens.home

import com.devtune.domain.model.*
import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.TemporarySessionRepository
import com.devtune.domain.repository.UserPreferencesRepository
import com.devtune.domain.usecase.*
import com.devtune.platform.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val getSystemStateUseCase: GetSystemStateUseCase = mock()
    private val getShizukuStateUseCase: GetShizukuStateUseCase = mock()
    private val getDisplayDensityInfoUseCase: GetDisplayDensityInfoUseCase = mock()
    private val animationRepository: AnimationRepository = mock()
    private val temporarySessionRepository: TemporarySessionRepository = mock()
    private val startTemporarySessionUseCase: StartTemporarySessionUseCase = mock()
    private val restoreTemporarySessionUseCase: RestoreTemporarySessionUseCase = mock()
    private val timeProvider: TimeProvider = mock()
    private val getCapabilityStateUseCase: GetCapabilityStateUseCase = mock()
    private val openDeveloperOptionsUseCase: OpenDeveloperOptionsUseCase = mock()
    private val openDeviceInfoSettingsUseCase: OpenDeviceInfoSettingsUseCase = mock()
    private val openDisplaySettingsUseCase: OpenDisplaySettingsUseCase = mock()
    private val requestShizukuPermissionUseCase: RequestShizukuPermissionUseCase = mock()
    private val verifyAnimationWriteUseCase: VerifyAnimationWriteUseCase = mock()
    private val setAnimationScalesUseCase: SetAnimationScalesUseCase = mock()
    private val userPreferencesRepository: UserPreferencesRepository = mock()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        whenever(getSystemStateUseCase()).thenReturn(flowOf(SystemState(isDeveloperOptionsEnabled = true, displayDensityDpi = 420)))
        whenever(getShizukuStateUseCase()).thenReturn(flowOf(ShizukuState.Authorized(ShizukuBackend.ADB)))
        whenever(getDisplayDensityInfoUseCase()).thenReturn(
            flowOf(
                DisplayDensityInfo(
                    currentDensityDpi = 420,
                    widthPx = 1080,
                    heightPx = 2424,
                    configurationDensityDpi = 420,
                    displayMetricsDensityDpi = 420,
                    smallestWidthDpFromConfig = 411
                )
            )
        )
        whenever(animationRepository.getAnimationScales()).thenReturn(flowOf(AnimationScales(1.0f, 1.0f, 1.0f)))
        whenever(temporarySessionRepository.activeSession).thenReturn(flowOf(null))
        whenever(getCapabilityStateUseCase(any())).thenReturn(CapabilityState.AVAILABLE)
        whenever(timeProvider.currentTimeMillis()).thenReturn(1000000L)

        viewModel = HomeViewModel(
            getSystemStateUseCase,
            getShizukuStateUseCase,
            getDisplayDensityInfoUseCase,
            animationRepository,
            temporarySessionRepository,
            startTemporarySessionUseCase,
            restoreTemporarySessionUseCase,
            timeProvider,
            getCapabilityStateUseCase,
            openDeveloperOptionsUseCase,
            openDeviceInfoSettingsUseCase,
            openDisplaySettingsUseCase,
            requestShizukuPermissionUseCase,
            verifyAnimationWriteUseCase,
            setAnimationScalesUseCase,
            userPreferencesRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `snapToStep snaps continuous floats to 0_25x increments`() {
        assertEquals(0.0f, HomeViewModel.snapToStep(0.1f), 0.001f)
        assertEquals(0.25f, HomeViewModel.snapToStep(0.2f), 0.001f)
        assertEquals(0.50f, HomeViewModel.snapToStep(0.53f), 0.001f)
        assertEquals(0.75f, HomeViewModel.snapToStep(0.7f), 0.001f)
        assertEquals(1.00f, HomeViewModel.snapToStep(1.1f), 0.001f)
        assertEquals(2.00f, HomeViewModel.snapToStep(2.5f), 0.001f)
    }

    @Test
    fun `formatRemainingTime formats milliseconds to MM_SS`() {
        assertEquals("00:00", HomeViewModel.formatRemainingTime(0L))
        assertEquals("05:00", HomeViewModel.formatRemainingTime(300000L))
        assertEquals("29:42", HomeViewModel.formatRemainingTime(1782000L))
    }

    @Test
    fun `pending scale change sets local pending state without immediate system write`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        testScheduler.advanceUntilIdle()

        viewModel.onPendingWindowScaleChange(0.5f)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0.5f, state.effectiveWindowScale, 0.001f)
        assertTrue(state.hasPendingChanges)

        verifyNoInteractions(setAnimationScalesUseCase)
    }

    @Test
    fun `start temporary session invokes startTemporarySessionUseCase`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        testScheduler.advanceUntilIdle()

        val createdSession = TemporaryAnimationSession(
            sessionId = "sess-123",
            startedAtEpochMillis = 1000000L,
            expiresAtEpochMillis = 1000000L + 30 * 60 * 1000L,
            originalScales = AnimationScales.DEFAULT,
            targetScales = AnimationScales.FAST,
            status = SessionStatus.ACTIVE
        )
        whenever(startTemporarySessionUseCase(any(), any())).thenReturn(Result.success(createdSession))

        viewModel.onSelectProfilePreset(AnimationScales.FAST)
        viewModel.onSelectDuration(TemporaryDuration.THIRTY_MINUTES)
        viewModel.onStartTemporarySession()
        testScheduler.advanceUntilIdle()

        verify(startTemporarySessionUseCase).invoke(AnimationScales.FAST, TemporaryDuration.THIRTY_MINUTES)
        assertNotNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun `restore temporary session invokes restoreTemporarySessionUseCase`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        testScheduler.advanceUntilIdle()

        whenever(restoreTemporarySessionUseCase()).thenReturn(Result.success(Unit))

        viewModel.onRestoreTemporarySession()
        testScheduler.advanceUntilIdle()

        verify(restoreTemporarySessionUseCase).invoke()
        assertNotNull(viewModel.uiState.value.userMessage)
    }
}
