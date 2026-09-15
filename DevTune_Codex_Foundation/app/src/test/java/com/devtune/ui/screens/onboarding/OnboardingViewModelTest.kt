package com.devtune.ui.screens.onboarding

import com.devtune.domain.usecase.CompleteOnboardingUseCase
import com.devtune.domain.usecase.GetOnboardingStatusUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase = mock()
    private val completeOnboardingUseCase: CompleteOnboardingUseCase = mock()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(getOnboardingStatusUseCase()).thenReturn(MutableStateFlow(false))
        viewModel = OnboardingViewModel(getOnboardingStatusUseCase, completeOnboardingUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is not completed`() = runTest {
        assertEquals(false, viewModel.isOnboardingCompleted.first())
    }

    @Test
    fun `completeOnboarding calls use case`() = runTest {
        viewModel.completeOnboarding()
        verify(completeOnboardingUseCase).invoke()
    }
}
