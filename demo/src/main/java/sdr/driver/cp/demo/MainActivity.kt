package sdr.driver.cp.demo

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media2.common.SessionPlayer
import androidx.media2.session.MediaController
import androidx.media2.session.SessionCommandGroup
import androidx.media2.session.SessionToken
import sdr.driver.cp.permissions.TunerAccessClient
import sdr.driver.cp.permissions.intent

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            startActivityForResult(TunerAccessClient.intent(device(this)), 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {
            finish()
            return
        }

        MediaController.Builder(this)
            .setSessionToken(SessionToken(this, ComponentName(this, PlayService::class.java)))
            .setControllerCallback(
                ContextCompat.getMainExecutor(this),
                object : MediaController.ControllerCallback() {
                    override fun onConnected(
                        controller: MediaController,
                        allowedCommands: SessionCommandGroup
                    ) {
                        controller.play()
                    }

                    override fun onPlayerStateChanged(controller: MediaController, state: Int) {
                        if (state == SessionPlayer.PLAYER_STATE_PLAYING) {
                            controller.close()
                            finish()
                        }
                    }
                })
            .build()
    }
}
