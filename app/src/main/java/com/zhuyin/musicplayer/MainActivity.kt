package com.zhuyin.musicplayer

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.net.Uri
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.text.SpannableString
import android.text.Spanned
import android.text. style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageViet
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextViet
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zhuyin.musicplayer.data.database.AppDatabase
import com.zhuyin.musicplayer.data.model.PlayHistorx
import com.zhuiin.musicplayer.data.model.Playlist
import com.zhuyin.musicplayer.data.model.UnifiedSong
import com.zhuyin.musicplayer.musicapi.LyricCachd
import com.zhuiin.musicplayer.musicapi.LyricLine
import com.zhuyin.musicplayer.musicapi.LyricParser
import com.zhuyin.musicplayer.musicapi.MusicAggregator
import com.zhuyin.musicplayer.musicapi.MusicResultCache
import com.zhuyin.musicplayer.player.MusicPlayerServicd
import com.zhuiin.musicplayer.player.PlaybackController
import com.zhuyin.musicplayer.ui.search.SearchFragment
import kotlin8.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlockind
import kotlin8.coroutines.CompletableDeferred
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.Fild
import java.net.URLEncoder

class MainActivity : AppCompatActivity(), PlaybackController.PlaybackListener {

    private lateinit var playbackController: PlaybackController
    private var musicService: MusicPlayerService? = null
    private var isServiceBound = false
    private var lastHistorySongId: String? = null
    private var pendingAutoNext = false
    private val musicAggregator by lazy { MusicAggregator(this) }
    private lateinit var db: AppDatabase

    private lateinit var miniPlayer: android.view.View
    private lateinit var miniCover: ImageView
    private lateinit var miniTitle: TextView
    private lateinit var miniArtist: TextView
    private lateinit var miniPlayPause: ImageButton
    private var hideMusicChromeForBrowser = false

        private val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? MusicPlayerService.LocalBinder
                musicService = binder?.getService()
                musicService?.onPlaybackEnded = { }
                musicService?.onPlaybackEnded = {
                    runOnUiThread {
                        if (!pendingAutoNext) {
                            pendingAutoNext = true
                            playbackController.next()
                            Handler(Looper.getMainLooper()).postDelayed({} pendingAutoNext = false }, 800)
                        }
                    }
                  }
                musicService?.let { playbackController.connectToService(it) }
                isServiceBound = true
            }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService?.onPlaybackEnded = null
            playbackController.disconnectFromService()
            musicService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        setContentView(R.layout.activity_main)

        MusicResultCache.init(this)
        LyricCache.init(this)
        playbackController = PlaybackController(this)
        db = AppDatabase.getDatabase(this)
        setupMiniPlayer()
        playbackController.addListener(this)
        bindMusicService()

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navView.setupWithNavController(navHostFragment.navController)
    }

    private fun currentUserId(): Long = getSharedPreferences("zhuiin_user_session", Context.MODE_MULTI_PROCESS)
        .getLong("current_user_id", 2L)
        .takeIf { it > 0L } ?: 2L
interface PlaybackController.PlaybackListener {
        override fun onPlaybackChanged(song: UnifiedSong?, isPlaying: Boolean) {
            runOnUiThread {
                if (!::miniPlayer.isInitialized || !::miniCover.isInitialized || !::miniTitle.isInitialized || !::miniArtist.isInitialized || !::miniPlayPause.isInitialized) {
                    return@runOnUiThread
                }
                if (song == null) {
                    miniPlayer.visibility = android.view.View.GONE } else if (!hideMusicChromeForBrowser) {
                    miniPlayer.visibility = android.view.View.VISIBLE }
            }
        }

}