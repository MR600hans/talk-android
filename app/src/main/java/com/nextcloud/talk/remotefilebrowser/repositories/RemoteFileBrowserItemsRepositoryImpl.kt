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

package com.nextcloud.talk.remotefilebrowser.repositories

import com.nextcloud.talk.components.filebrowser.webdav.ReadFilesystemOperation
import com.nextcloud.talk.remotefilebrowser.model.RemoteFileBrowserItem
import com.nextcloud.talk.utils.database.user.UserUtils
import io.reactivex.Observable
import okhttp3.OkHttpClient
import javax.inject.Inject

class RemoteFileBrowserItemsRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val userUtils: UserUtils
) : RemoteFileBrowserItemsRepository {

    override fun listFolder(parameters: RemoteFileBrowserItemsRepository.Parameters):
        Observable<List<RemoteFileBrowserItem>>? {
        val operation =
            ReadFilesystemOperation(
                okHttpClient,
                userUtils.currentUser,
                parameters.path,
                1
            )
        val davResponse = operation.readRemotePath()

        if (davResponse.getData() != null) {
            val objectList = davResponse.getData() as List<RemoteFileBrowserItem>
            return Observable.just(objectList)
        }

        return null
    }
}
