/*
 * Nextcloud Talk application
 *
 * @author Andy Scherzinger
 * @author Mario Danic
 * Copyright (C) 202 Andy Scherzinger <info@andy-scherzinger.de>
 * Copyright (C) 2017-2018 Mario Danic <mario@lovelyhq.com>
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

package com.nextcloud.talk.remotefilebrowser.model

import android.net.Uri
import android.os.Parcelable
import android.text.TextUtils
import at.bitfire.dav4jvm.Property
import at.bitfire.dav4jvm.Response
import at.bitfire.dav4jvm.property.DisplayName
import at.bitfire.dav4jvm.property.GetContentType
import at.bitfire.dav4jvm.property.GetLastModified
import at.bitfire.dav4jvm.property.ResourceType
import com.bluelinelabs.logansquare.annotation.JsonObject
import com.nextcloud.talk.components.filebrowser.models.properties.NCEncrypted
import com.nextcloud.talk.components.filebrowser.models.properties.NCPermission
import com.nextcloud.talk.components.filebrowser.models.properties.NCPreview
import com.nextcloud.talk.components.filebrowser.models.properties.OCFavorite
import com.nextcloud.talk.components.filebrowser.models.properties.OCId
import com.nextcloud.talk.components.filebrowser.models.properties.OCSize
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
@JsonObject
data class RemoteFileBrowserItem(
    var path: String? = null,
    var displayName: String? = null,
    var mimeType: String? = null,
    var modifiedTimestamp: Long = 0,
    var size: Long = 0,
    var isFile: Boolean = false,

    // Used for remote files
    var remoteId: String? = null,
    var hasPreview: Boolean = false,
    var isFavorite: Boolean = false,
    var isEncrypted: Boolean = false,
    var permissions: String? = null,
    var isAllowedToReShare: Boolean = false
) : Parcelable {
    // This constructor is added to work with the 'com.bluelinelabs.logansquare.annotation.JsonObject'
    constructor() : this(null, null, null, 0, 0, false, null, false, false, false, null, false)

    companion object {
        fun getModelFromResponse(response: Response, remotePath: String): RemoteFileBrowserItem {
            val remoteFileBrowserItem = RemoteFileBrowserItem()
            remoteFileBrowserItem.path = Uri.decode(remotePath)
            remoteFileBrowserItem.displayName = Uri.decode(File(remotePath).name)
            val properties = response.properties
            for (property in properties) {
                mapPropertyToBrowserFile(property, remoteFileBrowserItem)
            }
            if (remoteFileBrowserItem.permissions != null && remoteFileBrowserItem.permissions!!.contains("R")) {
                remoteFileBrowserItem.isAllowedToReShare = true
            }
            if (TextUtils.isEmpty(remoteFileBrowserItem.mimeType) && !remoteFileBrowserItem.isFile) {
                remoteFileBrowserItem.mimeType = "inode/directory"
            }

            return remoteFileBrowserItem
        }

        @Suppress("Detekt.ComplexMethod")
        private fun mapPropertyToBrowserFile(property: Property, remoteFileBrowserItem: RemoteFileBrowserItem) {
            when (property) {
                is OCId -> {
                    remoteFileBrowserItem.remoteId = property.ocId
                }
                is ResourceType -> {
                    remoteFileBrowserItem.isFile = !property.types.contains(ResourceType.COLLECTION)
                }
                is GetLastModified -> {
                    remoteFileBrowserItem.modifiedTimestamp = property.lastModified
                }
                is GetContentType -> {
                    remoteFileBrowserItem.mimeType = property.type
                }
                is OCSize -> {
                    remoteFileBrowserItem.size = property.ocSize
                }
                is NCPreview -> {
                    remoteFileBrowserItem.hasPreview = property.isNcPreview
                }
                is OCFavorite -> {
                    remoteFileBrowserItem.isFavorite = property.isOcFavorite
                }
                is DisplayName -> {
                    remoteFileBrowserItem.displayName = property.displayName
                }
                is NCEncrypted -> {
                    remoteFileBrowserItem.isEncrypted = property.isNcEncrypted
                }
                is NCPermission -> {
                    remoteFileBrowserItem.permissions = property.ncPermission
                }
            }
        }
    }
}
