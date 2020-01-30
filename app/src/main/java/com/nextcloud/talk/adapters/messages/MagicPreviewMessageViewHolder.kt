/*
 * Nextcloud Talk application
 *
 * @author Mario Danic
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

package com.nextcloud.talk.adapters.messages

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.view.View
import androidx.emoji.widget.EmojiTextView
import butterknife.BindView
import butterknife.ButterKnife
import coil.api.load
import coil.transform.CircleCropTransformation
import com.nextcloud.talk.R.*
import com.nextcloud.talk.components.filebrowser.models.BrowserFile
import com.nextcloud.talk.components.filebrowser.models.DavResponse
import com.nextcloud.talk.components.filebrowser.webdav.ReadFilesystemOperation
import com.nextcloud.talk.models.json.chat.ChatMessage
import com.nextcloud.talk.models.json.chat.ChatMessage.MessageType.*
import com.nextcloud.talk.newarch.local.models.UserNgEntity
import com.nextcloud.talk.utils.AccountUtils.canWeOpenFilesApp
import com.nextcloud.talk.utils.DisplayUtils.setClickableString
import com.nextcloud.talk.utils.DrawableUtils.getDrawableResourceIdForMimeType
import com.nextcloud.talk.utils.bundle.BundleKeys.KEY_ACCOUNT
import com.nextcloud.talk.utils.bundle.BundleKeys.KEY_FILE_ID
import com.stfalcon.chatkit.messages.MessageHolders.IncomingImageMessageViewHolder
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.koin.core.KoinComponent
import org.koin.core.inject

class MagicPreviewMessageViewHolder(itemView: View?) : IncomingImageMessageViewHolder<ChatMessage>(
        itemView
), KoinComponent {
    @JvmField
    @BindView(id.messageText)
    var messageText: EmojiTextView? = null
    val context: Context by inject()
    val okHttpClient: OkHttpClient by inject()

    @SuppressLint("SetTextI18n")
    override fun onBind(message: ChatMessage) {
        super.onBind(message)
        if (userAvatar != null) {
            if (message.grouped || message.oneToOneConversation) {
                if (message.oneToOneConversation) {
                    userAvatar.visibility = View.GONE
                } else {
                    userAvatar.visibility = View.INVISIBLE
                }
            } else {
                userAvatar.visibility = View.VISIBLE
                if ("bots" == message.actorType && "changelog" == message.actorId) {
                    val layers =
                            arrayOfNulls<Drawable?>(2)
                    layers[0] = context.getDrawable(drawable.ic_launcher_background)
                    layers[1] = context.getDrawable(drawable.ic_launcher_foreground)
                    val layerDrawable =
                            LayerDrawable(layers)
                    userAvatar.load(layerDrawable) {
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }

        if (message.messageType == SINGLE_NC_ATTACHMENT_MESSAGE) {
            // it's a preview for a Nextcloud share

            messageText!!.text = message.selectedIndividualHashMap!!["name"]
            setClickableString(
                    message.selectedIndividualHashMap!!["name"]!!,
                    message.selectedIndividualHashMap!!["link"]!!, messageText!!
            )

            if (message.selectedIndividualHashMap!!.containsKey("mimetype")) {
                if (message.imageUrl == "no-preview") {
                    image.load(getDrawableResourceIdForMimeType(message.selectedIndividualHashMap!!["mimetype"]))
                }
            } else {
                fetchFileInformation(
                        "/" + message.selectedIndividualHashMap!!["path"],
                        message.activeUser
                )
            }

            image.setOnClickListener { v: View? ->
                val accountString =
                        message.activeUser!!.username + "@" + message.activeUser!!
                                .baseUrl
                                .replace("https://", "")
                                .replace("http://", "")
                if (canWeOpenFilesApp(context, accountString)) {
                    val filesAppIntent =
                            Intent(Intent.ACTION_VIEW, null)
                    val componentName = ComponentName(
                            context.getString(string.nc_import_accounts_from),
                            "com.owncloud.android.ui.activity.FileDisplayActivity"
                    )
                    filesAppIntent.component = componentName
                    filesAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    filesAppIntent.setPackage(
                            context.getString(string.nc_import_accounts_from)
                    )
                    filesAppIntent.putExtra(
                            KEY_ACCOUNT, accountString
                    )
                    filesAppIntent.putExtra(
                            KEY_FILE_ID,
                            message.selectedIndividualHashMap!!["id"]
                    )
                    context.startActivity(filesAppIntent)
                } else {
                    val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(message.selectedIndividualHashMap!!["link"])
                    )
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(browserIntent)
                }
            }
        } else if (message.messageType == SINGLE_LINK_GIPHY_MESSAGE) {
            messageText!!.text = "GIPHY"
            setClickableString(
                    "GIPHY", "https://giphy.com", messageText!!
            )
        } else if (message.messageType == SINGLE_LINK_TENOR_MESSAGE) {
            messageText!!.text = "Tenor"
            setClickableString(
                    "Tenor", "https://tenor.com", messageText!!
            )
        } else {
            if (message.messageType == SINGLE_LINK_IMAGE_MESSAGE) {
                image.setOnClickListener { v: View? ->
                    val browserIntent = Intent(
                            Intent.ACTION_VIEW, Uri.parse(message.imageUrl)
                    )
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(browserIntent)
                }
            } else {
                image.setOnClickListener(null)
            }
            messageText!!.text = ""
        }
    }

    private fun fetchFileInformation(
            url: String,
            activeUser: UserNgEntity?
    ) {
        Single.fromCallable {
            ReadFilesystemOperation(
                    okHttpClient, activeUser, url, 0
            )
        }
                .observeOn(Schedulers.io())
                .subscribe(object : SingleObserver<ReadFilesystemOperation?> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onSuccess(readFilesystemOperation: ReadFilesystemOperation) {
                        val davResponse: DavResponse =
                                readFilesystemOperation.readRemotePath()
                        if (davResponse.data != null) {
                            val browserFileList =
                                    davResponse.data as List<BrowserFile>
                            if (browserFileList.isNotEmpty()) {
                                image.load(getDrawableResourceIdForMimeType(browserFileList[0].mimeType))
                            }
                        }
                    }

                    override fun onError(e: Throwable) {}
                })
    }

    override fun getPayloadForImageLoader(message: ChatMessage): Any {
        val map = HashMap<String, Any>()
        // used for setting a placeholder
        if (message.selectedIndividualHashMap!!.containsKey("mimetype")) {
            map["mimetype"] = message.selectedIndividualHashMap!!["mimetype"]!!
        }

        map["hasPreview"] = message.selectedIndividualHashMap!!.getOrDefault("has-preview", false)

        return ImageLoaderPayload(map)
    }

    init {
        ButterKnife.bind(this, itemView!!)
    }
}