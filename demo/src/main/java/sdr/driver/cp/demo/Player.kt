package sdr.driver.cp.demo

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.media.AudioAttributesCompat
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import sdr.driver.cp.opening.TunerContentUri
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class Player(private val context: Context) : SessionPlayer() {

    private var state = PLAYER_STATE_IDLE
        set(value) {
            field = value
            callbacks.forEach {
                it.second!!.execute {
                    it.first!!.onPlayerStateChanged(this, value)
                }
            }
        }

    private var commandsFD: ParcelFileDescriptor? = null
    private var dataFD: ParcelFileDescriptor? = null

    override fun getCurrentMediaItem(): MediaItem? = null

    override fun getDuration(): Long {
        TODO("Not yet implemented")
    }

    override fun setMediaItem(item: MediaItem): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun updatePlaylistMetadata(metadata: MediaMetadata?): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun setPlaybackSpeed(playbackSpeed: Float): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun getCurrentPosition() = UNKNOWN_TIME

    override fun getPlaylist(): MutableList<MediaItem> {
        TODO("Not yet implemented")
    }

    override fun play(): ListenableFuture<PlayerResult> {
        if (state == PLAYER_STATE_PLAYING)
            return noFuture

        val uri = TunerContentUri.build(device(context), context)
        commandsFD = context.contentResolver.openFileDescriptor(uri, "w")
        dataFD = context.contentResolver.openFileDescriptor(uri, "r")

        fm(commandsFD = commandsFD!!.fd, dataFD = dataFD!!.fd)

        state = PLAYER_STATE_PLAYING
        return noFuture
    }

    override fun skipToPreviousPlaylistItem(): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun getShuffleMode() = SHUFFLE_MODE_NONE

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun getRepeatMode() = REPEAT_MODE_NONE

    override fun getPlayerState() = state

    override fun setPlaylist(
        list: MutableList<MediaItem>,
        metadata: MediaMetadata?
    ): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun getPlaybackSpeed() = 1f

    override fun setShuffleMode(shuffleMode: Int): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun skipToNextPlaylistItem(): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun getBufferedPosition() = UNKNOWN_TIME

    override fun replacePlaylistItem(index: Int, item: MediaItem): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun getNextMediaItemIndex() = INVALID_ITEM_INDEX

    override fun addPlaylistItem(index: Int, item: MediaItem): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun seekTo(position: Long): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun getBufferingState() = BUFFERING_STATE_UNKNOWN

    override fun removePlaylistItem(index: Int): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun setRepeatMode(repeatMode: Int): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun skipToPlaylistItem(index: Int): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun prepare(): ListenableFuture<PlayerResult> {
        state = PLAYER_STATE_PAUSED
        return noFuture
    }

    override fun pause(): ListenableFuture<PlayerResult> {
        dataFD!!.close()
        dataFD = null
        commandsFD!!.close()
        commandsFD = null

        state = PLAYER_STATE_PAUSED
        return noFuture
    }

    override fun getPlaylistMetadata(): MediaMetadata? {
        TODO("Not yet implemented")
    }

    override fun getPreviousMediaItemIndex() = INVALID_ITEM_INDEX

    override fun setAudioAttributes(attributes: AudioAttributesCompat): ListenableFuture<PlayerResult> {
        TODO("Not yet implemented")
    }

    override fun getAudioAttributes(): AudioAttributesCompat? = null

    override fun getCurrentMediaItemIndex() = INVALID_ITEM_INDEX

    private external fun fm(commandsFD: Int, dataFD: Int)

    companion object {

        init {
            System.loadLibrary("demo")
        }

        private val noFuture = object : ListenableFuture<PlayerResult> {

            private val result = PlayerResult(1, null)

            override fun addListener(listener: Runnable, executor: Executor) {
                executor.execute(listener)
            }

            override fun isDone() = true

            override fun get() = result

            override fun get(p0: Long, p1: TimeUnit) = result

            override fun cancel(p0: Boolean) = true

            override fun isCancelled() = false
        }
    }
}
