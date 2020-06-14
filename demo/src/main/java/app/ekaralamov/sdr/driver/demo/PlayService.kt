package app.ekaralamov.sdr.driver.demo

import androidx.core.content.ContextCompat
import androidx.media2.session.MediaSession
import androidx.media2.session.MediaSessionService
import androidx.media2.session.SessionCommand
import androidx.media2.session.SessionCommand.*
import androidx.media2.session.SessionCommandGroup

class PlayService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        if (mediaSession == null) {
            mediaSession = MediaSession.Builder(this, Player(this))
                .setSessionCallback(
                    ContextCompat.getMainExecutor(this),
                    object : MediaSession.SessionCallback() {
                        override fun onConnect(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo
                        ) = SessionCommandGroup.Builder()
                            .addCommand(SessionCommand(COMMAND_CODE_PLAYER_PREPARE))
                            .addCommand(SessionCommand(COMMAND_CODE_PLAYER_PLAY))
                            .addCommand(SessionCommand(COMMAND_CODE_PLAYER_PAUSE))
                            .build()
                    }
                )
                .build()
        }
        return mediaSession!!
    }

    companion object {

        private var mediaSession: MediaSession? = null
    }
}
