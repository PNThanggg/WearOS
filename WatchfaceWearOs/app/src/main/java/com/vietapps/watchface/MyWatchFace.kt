package com.vietapps.watchface

import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema

///**
// * Updates rate in milliseconds for interactive mode. We update once a second to advance the
// * second hand.
// */
//private const val INTERACTIVE_UPDATE_RATE_MS = 1000
//
///**
// * Handler message id for updating the time periodically in interactive mode.
// */
//private const val MSG_UPDATE_TIME = 0
//
//private const val HOUR_STROKE_WIDTH = 5f
//private const val MINUTE_STROKE_WIDTH = 3f
//private const val SECOND_TICK_STROKE_WIDTH = 2f
//
//private const val CENTER_GAP_AND_CIRCLE_RADIUS = 4f
//
//private const val SHADOW_RADIUS = 6f

class MyWatchFace : WatchFaceService() {

    // Used by Watch Face APIs to construct user setting options and repository.
    override fun createUserStyleSchema(): UserStyleSchema =
        createUserStyleSchema(context = applicationContext)

    // Creates all complication user settings and adds them to the existing user settings
    // repository.
    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager = createComplicationSlotManager(
        context = applicationContext,
        currentUserStyleRepository = currentUserStyleRepository
    )

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        Log.d(TAG, "createWatchFace()")

        // Creates class that renders the watch face.
        val renderer = MyWatchFaceRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
//            complicationSlotsManager = complicationSlotsManager,
            // ẩn 2 cái slot tiện ích
            complicationSlotsManager = ComplicationSlotsManager(emptyList(), currentUserStyleRepository),
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.HARDWARE
        )

        // Creates the watch face.
        return WatchFace(
            watchFaceType = WatchFaceType.ANALOG,
            renderer = renderer
        )
    }

    companion object {
        const val TAG = "AnalogWatchFaceService"
    }
}