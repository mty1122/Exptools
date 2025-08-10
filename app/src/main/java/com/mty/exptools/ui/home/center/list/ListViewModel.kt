package com.mty.exptools.ui.home.center.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mty.exptools.enum.LightSource
import com.mty.exptools.enum.Pollutant
import com.mty.exptools.ui.home.center.list.item.ItemOtherUiState
import com.mty.exptools.ui.home.center.list.item.ItemPhotoUiState
import com.mty.exptools.ui.home.center.list.item.ItemStatus
import com.mty.exptools.ui.home.center.list.item.ItemSynUiState
import com.mty.exptools.ui.home.center.list.item.ItemTestUiState
import com.mty.exptools.ui.home.center.list.item.ItemUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ListViewModel @Inject constructor() : ViewModel() {

    private val _itemUiStateList = MutableStateFlow(fetchTestList())
    val itemUiStateList: StateFlow<List<ItemUiState>> = _itemUiStateList.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()
    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            delay(2000)
            _refreshing.value = false
        }
    }

    // 仅用于开发阶段
    private fun fetchTestList(): List<ItemUiState> {
        val uiState1 = ItemPhotoUiState(
            listItemId = 1,
            materialName = "钨酸铋-实验2-22",
            targetPollutant = Pollutant.TC,
            lightSource = LightSource.XENON_L,
            elapsedMinutes = 70,
            totalMinutes = 90,
            progress = 0.75f,
            rightTimes = 20,
            status = ItemStatus.STATUS_START
        )
        val uiState2 = ItemSynUiState(
            listItemId = 2,
            materialName = "钨酸铋-实验2-23",
            targetStep = "120℃ 24h 水热反应",
            targetDevice = "1号釜 小烘箱",
            nextStep = "60℃ 干燥 12小时",
            progress = 0.6f,
            rightTimes = 999,
            status = ItemStatus.STATUS_START
        )
        val uiState3 = ItemPhotoUiState(
            listItemId = 3,
            materialName = "钨酸铋-实验2-21",
            targetPollutant = Pollutant.TC,
            lightSource = LightSource.XENON_L,
            rightTimes = 1,
            status = ItemStatus.STATUS_COMPLETE
        )
        val uiState4 = ItemSynUiState(
            listItemId = 4,
            materialName = "钨酸铋-实验2-21",
            completeInfo = "120℃ 24h | 1号釜 小烘箱",
            rightTimes = 3,
            status = ItemStatus.STATUS_COMPLETE
        )
        val uiState5 = ItemTestUiState(
            listItemId = 5,
            materialName = "钨酸铋-实验2-19",
            testInfo = "XPS",
            testDate = "2025.03.22",
            rightTimes = 10,
            status = ItemStatus.STATUS_COMPLETE
        )
        val uiState6 = ItemOtherUiState(
            listItemId = 6,
            title = "某文章撰写",
            info = "引言部分",
            endDate = "2025.03.12",
            rightTimes = 20,
            status = ItemStatus.STATUS_COMPLETE
        )
        return listOf<ItemUiState>(uiState1, uiState2, uiState3, uiState4, uiState5, uiState6)
    }
}
