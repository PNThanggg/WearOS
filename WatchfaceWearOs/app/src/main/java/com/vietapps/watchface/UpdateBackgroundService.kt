package com.vietapps.watchface

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ExecutionException
import kotlin.math.abs

class UpdateBackgroundService : WearableListenerService() {

    companion object {
        private const val TAG = "UpdateBackgroundService"
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)

        dataEventBuffer.filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/background_image_change_wf" }
            .forEach { event ->
                val bitmap: Bitmap? = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap.getAsset("image")
                    .let { asset ->
                        if (asset != null) {
                            Log.e(TAG, "$asset")
                            loadBitmapFromAsset(asset)
                        } else {
                            Log.e(TAG, "Asset null")
                            null
                        }
                    }

                if (bitmap != null) {
                    MyWatchFaceRenderer.backgroundWFCurrent = bitmap
                    MyWatchFaceRenderer.needUpdate = true
                }
            }

//        for (event in dataEventBuffer) {
//            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/background_image_change_wf") {
//                try {
//                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
//                    val profileAsset = dataMapItem.dataMap.getAsset("image")
//                    val bitmap = loadBitmapFromAsset(profileAsset)
//                    MyWatchFaceRenderer.backgroundWFCurrent = bitmap
//                    MyWatchFaceRenderer.needUpdate = true
//                } catch (e: ExecutionException) {
//                    e.printStackTrace()
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//            }
//        }
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun loadBitmapFromAsset(asset: Asset): Bitmap? {
        val assetInputStream: InputStream? =
            Tasks.await(Wearable.getDataClient(this).getFdForAsset(asset))
                ?.inputStream

        return assetInputStream?.let {
            val originBitmap = BitmapFactory.decodeStream(assetInputStream)
            val originBitmapCrop: Bitmap

            val constToCrop = originBitmap.height - originBitmap.width
            originBitmapCrop = if (constToCrop > 0) {
                Bitmap.createBitmap(
                    originBitmap,
                    0,
                    constToCrop / 2,
                    originBitmap.width,
                    originBitmap.height - constToCrop
                )
            } else if (constToCrop < 0) {
                Bitmap.createBitmap(
                    originBitmap,
                    abs(constToCrop / 2),
                    0,
                    originBitmap.width - abs(constToCrop),
                    originBitmap.height
                )
            } else {
                originBitmap
            }
            val scale =
                MyWatchFaceRenderer.backgroundWidth.toFloat() / originBitmapCrop.width.toFloat()
            val resultBitmap = Bitmap.createScaledBitmap(
                originBitmapCrop,
                (originBitmapCrop.width * scale).toInt(),
                (originBitmapCrop.height * scale).toInt(),
                true
            )
            saveBitmapIntoDevice(resultBitmap)

            return resultBitmap
        }?.run {
            Log.e(TAG, "Requested an unknown Asset.")
            null
        }
    }

    private fun saveBitmapIntoDevice(bitmap: Bitmap) {
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(outputMediaFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private val outputMediaFile: File?
        get() {
            val mediaStorageDir = File(
                getExternalFilesDir(null)
                    .toString() + "/Android/data/"
                        + applicationContext.packageName
                        + "/Files"
            )
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }
            val mediaFile: File
            val mImageName = "vietappsWF" + ".png"
            mediaFile = File(mediaStorageDir.path + File.separator + mImageName)
            return mediaFile
        }
}

/// ------------------------------------------------------------------ ///

//package com.vietapps.watchface
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import com.google.android.gms.tasks.Tasks
//import com.google.android.gms.wearable.*
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.util.concurrent.ExecutionException
//import kotlin.math.abs
//
//class UpdateBackgroundService : WearableListenerService() {
//    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
//        super.onDataChanged(dataEventBuffer)
//        for (event in dataEventBuffer) {
//            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/background_image_change_wf") {
//                try {
//                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
//                    val profileAsset = dataMapItem.dataMap.getAsset("image")
//                    val bitmap = loadBitmapFromAsset(profileAsset)
//                    MyWatchFaceRenderer.backgroundWFCurrent = bitmap
//                    MyWatchFaceRenderer.needUpdate = true
//                } catch (e: ExecutionException) {
//                    e.printStackTrace()
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
//
//    @Throws(ExecutionException::class, InterruptedException::class)
//    fun loadBitmapFromAsset(asset: Asset?): Bitmap {
//        requireNotNull(asset) { "Asset must be non-null" }
//        val assetInputStream = Tasks.await(Wearable.getDataClient(this).getFdForAsset(asset))
//            .inputStream
//        val originBitmap = BitmapFactory.decodeStream(assetInputStream)
//        val originBitmapCrop: Bitmap
//        val constToCrop = originBitmap.height - originBitmap.width
//        originBitmapCrop = if (constToCrop > 0) {
//            Bitmap.createBitmap(
//                originBitmap,
//                0,
//                constToCrop / 2,
//                originBitmap.width,
//                originBitmap.height - constToCrop
//            )
//        } else if (constToCrop < 0) {
//            Bitmap.createBitmap(
//                originBitmap,
//                Math.abs(constToCrop / 2),
//                0,
//                originBitmap.width - abs(constToCrop),
//                originBitmap.height
//            )
//        } else {
//            originBitmap
//        }
//        val scale = MyWatchFaceRenderer.backgroundWidth.toFloat() / originBitmapCrop.width.toFloat()
//        val resultBitmap = Bitmap.createScaledBitmap(
//            originBitmapCrop,
//            (originBitmapCrop.width * scale).toInt(),
//            (originBitmapCrop.height * scale).toInt(),
//            true
//        )
//        saveBitmapIntoDevice(resultBitmap)
//        return resultBitmap
//    }
//
//    private fun saveBitmapIntoDevice(bitmap: Bitmap) {
//        var out: FileOutputStream? = null
//        try {
//            out = FileOutputStream(outputMediaFile)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            try {
//                out?.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private val outputMediaFile: File?
//        get() {
//            val mediaStorageDir = File(
//                getExternalFilesDir(null)
//                    .toString() + "/Android/data/"
//                        + applicationContext.packageName
//                        + "/Files"
//            )
//            if (!mediaStorageDir.exists()) {
//                if (!mediaStorageDir.mkdirs()) {
//                    return null
//                }
//            }
//            val mediaFile: File
//            val mImageName = "vietappsWF" + ".png"
//            mediaFile = File(mediaStorageDir.path + File.separator + mImageName)
//            return mediaFile
//        }
//}