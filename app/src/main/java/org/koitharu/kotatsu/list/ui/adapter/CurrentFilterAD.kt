package org.koitharu.kotatsu.list.ui.adapter

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.ui.widgets.ChipsView
import org.koitharu.kotatsu.core.model.MangaTag
import org.koitharu.kotatsu.list.ui.model.CurrentFilterModel
import org.koitharu.kotatsu.list.ui.model.ListModel

fun currentFilterAD(
	listener: MangaListListener,
) = adapterDelegate<CurrentFilterModel, ListModel>(R.layout.item_current_filter) {

	val chipGroup = itemView as ChipsView

	chipGroup.onChipCloseClickListener = ChipsView.OnChipCloseClickListener { chip, data ->
		listener.onTagRemoveClick(data as? MangaTag ?: return@OnChipCloseClickListener)
	}

	bind {
		chipGroup.setChips(item.chips)
	}
}