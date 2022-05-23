/*
 * Nextcloud Talk application
 *
 * @author Andy Scherzinger
 * Copyright (C) 202 Andy Scherzinger <info@andy-scherzinger.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextcloud.talk.remotefilebrowser.adapters

import android.view.View
import com.facebook.drawee.view.SimpleDraweeView
import com.nextcloud.talk.databinding.RvItemBrowserFileBinding
import com.nextcloud.talk.remotefilebrowser.model.RemoteFileBrowserItem
import com.nextcloud.talk.utils.DateUtils

class RemoteFileBrowserItemsListViewHolder(
    override val binding: RvItemBrowserFileBinding,
    mimeTypeSelectionFilter: String?,
    onItemClicked: (Int) -> Unit
) : RemoteFileBrowserItemsViewHolder(binding, mimeTypeSelectionFilter) {

    override val fileIcon: SimpleDraweeView
        get() = binding.fileIcon

    init {
        itemView.setOnClickListener {
            onItemClicked(bindingAdapterPosition)
        }
    }

    override fun onBind(item: RemoteFileBrowserItem) {

        super.onBind(item)

        binding.fileModifiedInfo.text = DateUtils.getLocalDateTimeStringFromTimestamp(
            item.modifiedTimestamp * ONE_SECOND_IN_MILLIS
        )

        calculateSelectability(item)
    }

    private fun calculateSelectability(item: RemoteFileBrowserItem) {
        if (item.isFile &&
            mimeTypeSelectionFilter != null &&
            item.mimeType?.startsWith(mimeTypeSelectionFilter) == true
        ) {
            binding.selectFileCheckbox.visibility = View.VISIBLE
        } else {
            binding.selectFileCheckbox.visibility = View.GONE
        }
    }

    companion object {
        private const val ONE_SECOND_IN_MILLIS = 1000
    }
}
