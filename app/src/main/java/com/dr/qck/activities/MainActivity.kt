package com.dr.qck.activities

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dr.qck.R
import com.dr.qck.application.ApplicationViewModel
import com.dr.qck.application.QckApplication
import com.dr.qck.application.QckApplication.Companion.isThemeSwitched
import com.dr.qck.databinding.ActivityMainBinding
import com.dr.qck.utils.AndroidUtils
import com.dr.qck.utils.Constants.IS_ENABLED
import com.dr.qck.utils.Constants.NOTIFICATIONS_ENABLED
import com.dr.qck.utils.Constants.NOTIF_PERMISSION_COUNT
import com.dr.qck.utils.Constants.PERMISSION_REQ_COUNT
import com.dr.qck.utils.Constants.THEME
import com.dr.qck.utils.ThemeType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnClickListener, CompoundButton.OnCheckedChangeListener {
    private lateinit var binding: ActivityMainBinding
    private var permissionCount = 0
    private var notifPermissionCount = 0
    private val applicationViewModel by viewModels<ApplicationViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (isThemeSwitched.first) {
            binding.themeSwitchImage.setImageBitmap(QckApplication.snapshot)
            val isDark = isThemeSwitched.second != ThemeType.LIGHT.name
            if (!isDark) {
                binding.mainLayout.alpha = 0F
                binding.mainLayout.elevation = 20F
            }
//            binding.themeSwitchImage.animate().alpha(0F).setDuration(200).start()
            CoroutineScope(Dispatchers.Main).launch {
                val w = resources.displayMetrics.widthPixels
                val h = resources.displayMetrics.heightPixels
                val pos = intArrayOf(0, 0)
                var finalRadius = sqrt(
                    ((w - pos[0]) * (w - pos[0]) + (h - pos[1]) * (h - pos[1])).toDouble()
                ).coerceAtLeast(
                    sqrt(
                        (pos[0] * pos[0] + (h - pos[1]) * (h - pos[1])).toDouble()
                    )
                ).toFloat()
                val finalRadius2 = sqrt(
                    ((w - pos[0]) * (w - pos[0]) + pos[1] * pos[1]).toDouble()
                ).coerceAtLeast(sqrt((pos[0] * pos[0] + pos[1] * pos[1]).toDouble())).toFloat()
                finalRadius = finalRadius.coerceAtLeast(finalRadius2)
                val anim = ViewAnimationUtils.createCircularReveal(
                    if (isDark) binding.themeSwitchImage else binding.mainLayout,
                    pos[0],
                    pos[1],
                    if (isDark) finalRadius else 0F,
                    if (isDark) 0F else finalRadius
                )
                anim.addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        binding.mainLayout.alpha = 1F
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        binding.themeSwitchImage.visibility = View.INVISIBLE
                        binding.themeSwitchImage.setImageDrawable(null)
                        QckApplication.snapshot = null
                        if (!isDark) binding.mainLayout.visibility = VISIBLE
                        binding.themeButton.setOnClickListener(this@MainActivity)
                    }
                })
                anim.setDuration(400)
                anim.start()
            }
        } else {
            setContentView(binding.root)
            binding.themeButton.setOnClickListener(this@MainActivity)
        }
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return if (isThemeSwitched.first) {
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    binding.mainLayout.visibility = VISIBLE
                    true
                } else {
                    false
                }
            }
        })
        initViews()
        observeData()
        initOnClick()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun observeData() {
        applicationViewModel.userPreferences.observe(this) { prefs ->
            Log.d("Datastore", prefs.toString())
            permissionCount = prefs.permissionRequestCount
            notifPermissionCount = prefs.notificationPermissionCount
            when {
                prefs.isEnabled && checkSmsPermission() -> {
                    binding.autoCopySwitch.isChecked = true
                }
            }
            binding.notificationSwitch.isChecked =
                prefs.notificationsEnabled && checkNotificationPermission()
            when (prefs.theme) {
                ThemeType.LIGHT.name -> {
                    binding.themeButton.setImageDrawable(getDrawable(R.drawable.night_ic))
                }

                else -> {
                    binding.themeButton.setImageDrawable(getDrawable(R.drawable.day_ic))
                }
            }
            if (!isThemeSwitched.first) {
                changeTheme(prefs.theme)
            }

        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (notifPermissionCount >= 2) {
                    // show popup
                    missingPermissionDialog()
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2
                    )
                    notifPermissionCount++
                    applicationViewModel.updateUserPreferences(
                        NOTIF_PERMISSION_COUNT, notifPermissionCount
                    )
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initViews() {
        applicationViewModel.getUserPreferences()
        binding.logoImageView.drawable.setTintList(null)
    }

    private fun changeTheme(theme: String, booted: Boolean = true) {
        if (!booted) {
            val bm = (AndroidUtils.viewToBitmap(binding.root, theme))
            QckApplication.snapshot = bm
        }
        isThemeSwitched = Pair(true, theme)
        when (theme) {
            ThemeType.DARK.name -> {
                applicationViewModel.updateUserPreferences(THEME, ThemeType.DARK.name)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            }

            else -> {
                applicationViewModel.updateUserPreferences(THEME, ThemeType.LIGHT.name)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            }
        }
    }

    private fun initOnClick() {
        binding.autoCopySwitch.setOnClickListener(this)
        binding.notificationSwitch.setOnClickListener(this)
        binding.autoCopySwitch.setOnCheckedChangeListener(this)
        binding.exceptionButton.setOnClickListener(this)
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (permissionCount >= 2) {
                // show popup
                missingPermissionDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS), 1
                )
                permissionCount++
                applicationViewModel.updateUserPreferences(
                    PERMISSION_REQ_COUNT, permissionCount
                )
            }
        }
    }

    private fun checkSmsPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED

    private fun missingPermissionDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle(getString(R.string.missing_permissions))
        dialog.setMessage(getString(R.string.permission_it_for_proper_functionality))
        dialog.setPositiveButton("Go to Settings") { _, _ ->
            navigateToSettings()
        }
        dialog.setNegativeButton("Cancel") { d, _ ->
            d.dismiss()
        }
        dialog.show()
    }

    private fun navigateToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty()) {
                    when (grantResults[0]) {
                        PackageManager.PERMISSION_DENIED -> {
                            Toast.makeText(
                                this, getString(R.string.permission_denied), Toast.LENGTH_SHORT
                            ).show()
                        }

                        PackageManager.PERMISSION_GRANTED -> {
                            binding.autoCopySwitch.performClick()
                        }
                    }
                }
            }

            2 -> {
                if (grantResults.isNotEmpty()) {
                    when (grantResults[0]) {
                        PackageManager.PERMISSION_DENIED -> {
                            Toast.makeText(
                                this, getString(R.string.permission_denied), Toast.LENGTH_SHORT
                            ).show()
                        }

                        PackageManager.PERMISSION_GRANTED -> {
                            binding.notificationSwitch.performClick()
                        }
                    }

                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.autoCopySwitch.id -> {
                if (checkSmsPermission()) {
                    applicationViewModel.updateUserPreferences(
                        IS_ENABLED, binding.autoCopySwitch.isChecked
                    )
                } else {
                    requestSmsPermission()
                    binding.autoCopySwitch.isChecked = false
                }
            }

            binding.notificationSwitch.id -> {
                if (checkNotificationPermission()) {
                    createNotificationChannel()
                    applicationViewModel.updateUserPreferences(
                        NOTIFICATIONS_ENABLED, binding.notificationSwitch.isChecked
                    )
                } else {
                    requestNotificationPermission()
                    binding.notificationSwitch.isChecked = false
                }
            }

            binding.themeButton.id -> {
                when (AppCompatDelegate.getDefaultNightMode()) {
                    AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> {
                        changeTheme(ThemeType.DARK.name, false)
                    }

                    else -> {
                        changeTheme(ThemeType.LIGHT.name, false)
                    }
                }

            }

            binding.exceptionButton.id -> {
                startActivity(Intent(this, ExceptionList::class.java))
            }
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.otp_copy_notifications)
        val descriptionText = getString(R.string.otp_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel("1.0", name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            binding.autoCopySwitch.id -> {
                binding.textView2.text =
                    if (isChecked) getString(R.string.auto_copy_enabled) else getString(R.string.auto_copy_disabled)
            }
        }
    }

}