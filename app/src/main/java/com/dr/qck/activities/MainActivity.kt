package com.dr.qck.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
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
import com.dr.qck.service.LifecycleService
import com.dr.qck.utils.Constants.IS_ENABLED
import com.dr.qck.utils.Constants.NOTIFICATIONS_ENABLED
import com.dr.qck.utils.Constants.NOTIF_PERMISSION_COUNT
import com.dr.qck.utils.Constants.PERMISSION_REQ_COUNT
import com.dr.qck.utils.Constants.THEME
import com.dr.qck.utils.ThemeType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


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
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // Check whether the initial data is ready.
                return if (applicationViewModel.userPreferences.value != null) {
                    Log.d("SplashScreen", "HasData")
                    // The content is ready. Start drawing.
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    true
                } else {
                    // The content isn't ready. Suspend.
                    false
                }
            }
        })
        initViews()
        observeData()
        // TODO
        initOnClick()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun observeData() {
        // TODO add theme condition
        applicationViewModel.userPreferences.observe(this) { prefs ->
            Log.d("Datastore", prefs.toString())
            permissionCount = prefs.permissionRequestCount
            notifPermissionCount = prefs.notificationPermissionCount
            Log.d("Noifff", checkNotificationPermission().toString())
            when {
                prefs.isEnabled && checkSmsPermission() -> {
                    binding.autoCopySwitch.isChecked = true
//                    binding.textView2.text = getString(R.string.auto_copy_enabled)
                    if (!isServiceRunning()) {
                        QckApplication.startService()
                    }
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
            if (!isThemeSwitched) {
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

    private fun changeTheme(theme: String) {
        isThemeSwitched = true
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
        binding.themeButton.setOnClickListener(this)
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
                    QckApplication.smsReceiver(binding.autoCopySwitch.isChecked)
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
                        changeTheme(ThemeType.DARK.name)
                    }

                    else -> {
                        changeTheme(ThemeType.LIGHT.name)
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

    private fun isServiceRunning(serviceClass: Class<*> = LifecycleService::class.java): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = manager.runningAppProcesses

        if (runningServices != null) {
            for (processInfo in runningServices) {
                if (processInfo.processName == serviceClass.name) {
                    return true
                }
            }
        }
        return false
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