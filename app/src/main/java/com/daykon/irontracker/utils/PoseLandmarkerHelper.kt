package com.daykon.irontracker.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkerHelper(
    private var minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE,
    private var minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE,
    private var minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE,
    private var currentModel: Int = MODEL_POSE_LANDMARKER_FULL,
    private var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    // this listener is only used when running in RunningMode.LIVE_STREAM
    private val poseLandmarkerHelperListener: LandmarkerListener? = null
) {

  // For this example this needs to be a var so it can be reset on changes.
  // If the Pose Landmarker will not change, a lazy val would be preferable.
  private var poseLandmarker: PoseLandmarker? = null

  init {
    setupPoseLandmarker()
  }

  // Return running status of PoseLandmarkerHelper
  fun isClose(): Boolean {
    return poseLandmarker == null
  }

  // Initialize the Pose landmarker using current settings on the
  // thread that is using it. CPU can be used with Landmarker
  // that are created on the main thread and used on a background thread, but
  // the GPU delegate needs to be used on the thread that initialized the
  // Landmarker
  fun setupPoseLandmarker() {
    // Set general pose landmarker options
    val baseOptionBuilder = BaseOptions.builder()

    // Use the specified hardware for running the model. Default to CPU
    when (currentDelegate) {
      DELEGATE_CPU -> {
        baseOptionBuilder.setDelegate(Delegate.CPU)
      }

      DELEGATE_GPU -> {
        baseOptionBuilder.setDelegate(Delegate.GPU)
      }
    }

    val modelName =
        when (currentModel) {
          MODEL_POSE_LANDMARKER_FULL -> "pose_landmarker_full.task"
          MODEL_POSE_LANDMARKER_LITE -> "pose_landmarker_lite.task"
          MODEL_POSE_LANDMARKER_HEAVY -> "pose_landmarker_heavy.task"
          else -> "pose_landmarker_full.task"
        }

    baseOptionBuilder.setModelAssetPath(modelName)

    try {
      val baseOptions = baseOptionBuilder.build()
      // Create an option builder with base options and specific
      // options only use for Pose Landmarker.
      val optionsBuilder =
          PoseLandmarker.PoseLandmarkerOptions.builder()
              .setBaseOptions(baseOptions)
              .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
              .setMinTrackingConfidence(minPoseTrackingConfidence)
              .setMinPosePresenceConfidence(minPosePresenceConfidence)
              .setRunningMode(runningMode)

      val options = optionsBuilder.build()
      poseLandmarker =
          PoseLandmarker.createFromOptions(context, options)
    } catch (e: IllegalStateException) {
      poseLandmarkerHelperListener?.onError(
          "Pose Landmarker failed to initialize. See error logs for " +
              "details"
      )
      Log.e(
          TAG, "MediaPipe failed to load the task with error: " + e
          .message
      )
    } catch (e: RuntimeException) {
      // This occurs if the model being used does not support GPU
      poseLandmarkerHelperListener?.onError(
          "Pose Landmarker failed to initialize. See error logs for " +
              "details", GPU_ERROR
      )
      Log.e(
          TAG,
          "Image classifier failed to load model with error: " + e.message
      )
    }
  }

  // Accepts the URI for a video file loaded from the user's gallery and attempts to run
  // pose landmarker inference on the video. This process will evaluate every
  // frame in the video and attach the results to a bundle that will be
  // returned.


  // Accepted a Bitmap and runs pose landmarker inference on it to return
  // results back to the caller
  fun detectImage(image: Bitmap): ResultBundle? {
    if (runningMode != RunningMode.IMAGE) {
      throw IllegalArgumentException(
          "Attempting to call detectImage" +
              " while not using RunningMode.IMAGE"
      )
    }


    // Inference time is the difference between the system time at the
    // start and finish of the process
    val startTime = SystemClock.uptimeMillis()

    // Convert the input Bitmap object to an MPImage object to run inference
    val mpImage = BitmapImageBuilder(image).build()

    Log.d("TESTDEBUG", "AboutTo" + poseLandmarker.toString() + "aaaa" + mpImage.toString())

    // Run pose landmarker using MediaPipe Pose Landmarker API
    poseLandmarker?.detect(mpImage)?.also { landmarkResult ->
      val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
      return ResultBundle(
          listOf(landmarkResult),
          inferenceTimeMs,
          image.height,
          image.width
      )
    }

    // If poseLandmarker?.detect() returns null, this is likely an error. Returning null
    // to indicate this.
    poseLandmarkerHelperListener?.onError(
        "Pose Landmarker failed to detect."
    )

    return null
  }

  companion object {
    const val TAG = "PoseLandmarkerHelper"

    const val DELEGATE_CPU = 0
    const val DELEGATE_GPU = 1
    const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F
    const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F
    const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F
    const val OTHER_ERROR = 0
    const val GPU_ERROR = 1
    const val MODEL_POSE_LANDMARKER_FULL = 0
    const val MODEL_POSE_LANDMARKER_LITE = 1
    const val MODEL_POSE_LANDMARKER_HEAVY = 2
  }

  data class ResultBundle(
      val results: List<PoseLandmarkerResult>,
      val inferenceTime: Long,
      val inputImageHeight: Int,
      val inputImageWidth: Int,
  )

  interface LandmarkerListener {
    fun onError(error: String, errorCode: Int = OTHER_ERROR)
  }
}