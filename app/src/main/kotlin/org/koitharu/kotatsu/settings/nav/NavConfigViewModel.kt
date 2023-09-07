package org.koitharu.kotatsu.settings.nav

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.prefs.NavItem
import org.koitharu.kotatsu.core.ui.BaseViewModel
import org.koitharu.kotatsu.core.ui.util.ActivityRecreationHandle
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.main.ui.MainActivity
import org.koitharu.kotatsu.parsers.util.move
import org.koitharu.kotatsu.settings.nav.model.NavItemAddModel
import javax.inject.Inject

@HiltViewModel
class NavConfigViewModel @Inject constructor(
	private val settings: AppSettings,
	private val activityRecreationHandle: ActivityRecreationHandle,
) : BaseViewModel() {

	private val items = MutableStateFlow(settings.mainNavItems)

	val content: StateFlow<List<ListModel>> = items.map { snapshot ->
		if (snapshot.size < NavItem.entries.size) {
			snapshot + NavItemAddModel(snapshot.size < 5)
		} else {
			snapshot
		}
	}.stateIn(
		viewModelScope + Dispatchers.Default,
		SharingStarted.WhileSubscribed(5000),
		emptyList()
	)

	private var commitJob: Job? = null

	val availableItems
		get() = items.value.let { snapshot ->
			NavItem.entries.filterNot { x -> x in snapshot }
		}

	fun reorder(fromPos: Int, toPos: Int) {
		items.value = items.value.toMutableList().apply {
			move(fromPos, toPos)
			commit(this)
		}
	}

	fun addItem(item: NavItem) {
		items.value = items.value.plus(item).also {
			commit(it)
		}
	}

	fun removeItem(item: NavItem) {
		items.value = items.value.minus(item).also {
			commit(it)
		}
	}

	private fun commit(value: List<NavItem>) {
		val prevJob = commitJob
		commitJob = launchJob {
			prevJob?.cancelAndJoin()
			delay(500)
			settings.mainNavItems = value
			activityRecreationHandle.recreate(MainActivity::class.java)
		}
	}
}