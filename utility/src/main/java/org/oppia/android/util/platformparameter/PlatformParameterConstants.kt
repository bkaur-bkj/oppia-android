package org.oppia.android.util.platformparameter

import javax.inject.Qualifier

/**
 * This file contains all the constants that are associated with individual Platform Parameters.
 * These constants are:
 *  - Qualifier Annotation
 *  - Platform Parameter Name
 *  - Platform Parameter Default Value
 */

@Qualifier
annotation class SplashScreenWelcomeMsg

const val SPLASH_SCREEN_WELCOME_MSG = "splash_screen_welcome_msg"
const val SPLASH_SCREEN_WELCOME_MSG_DEFAULT_VALUE = false
const val SPLASH_SCREEN_WELCOME_MSG_SERVER_VALUE = true

@Qualifier
annotation class SyncUpWorkerTimePeriod

const val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS = "sync_up_worker_time_period"
const val SYNC_UP_WORKER_TIME_PERIOD_IN_HOURS_DEFAULT_VALUE = 12
