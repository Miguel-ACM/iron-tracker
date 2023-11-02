package com.daykon.irontracker.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat



import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.daykon.irontracker.R
import com.daykon.irontracker.db.Database
import com.daykon.irontracker.db.ProgressPic
import com.daykon.irontracker.utils.CropTransformation
import com.daykon.irontracker.utils.PadTransformation
import com.daykon.irontracker.utils.PoseLandmarkerHelper
import com.daykon.irontracker.utils.RotateTransformation
import com.daykon.irontracker.viewModels.ProgressPicViewModel
import com.daykon.irontracker.viewModels.events.ProgressPicEvent
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CameraView(
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // 1
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()

    // 2
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    // 3
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        IconButton(
            modifier = Modifier.padding(25.dp),
            onClick = {
                takePhoto(
                    filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                    imageCapture = imageCapture,
                    outputDirectory = outputDirectory,
                    executor = executor,
                    onImageCaptured = onImageCaptured,
                    onError = onError
                )
            },
            content = {
                Icon(
                    painterResource(id = R.drawable.photo_camera_black_24dp),
                    contentDescription = "Take picture",
                    tint = Color.White,
                    modifier = Modifier
                        //.padding(200.dp)
                        .border(1.dp, Color.White, CircleShape)
                        //.fillMaxSize()

                )
            }
        )
    }
}

private fun takePhoto(
    filenameFormat: String,
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {

    val photoFile = File(
        outputDirectory,
        SimpleDateFormat(filenameFormat, Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(outputOptions, executor, object: ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
            Log.e("kilo", "Take photo error:", exception)
            onError(exception)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val savedUri = Uri.fromFile(photoFile)
            onImageCaptured(savedUri)
        }
    })
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}


@Composable
@ExperimentalMaterial3Api
fun CameraScreen(db: Database,
                 navController: NavController) {
    val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    val progressPicViewModel = ProgressPicViewModel(db.progressPicDao)
    val onEvent = progressPicViewModel::onEvent
    val state = progressPicViewModel.state.collectAsState()
    val context = LocalContext.current
    val poseLandmarkerHelper = PoseLandmarkerHelper(
        context = context,
        runningMode = RunningMode.IMAGE
    )

    var isShowingCamera by remember { mutableStateOf(false) }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {
        }
    }

    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) -> {
            // Override the default behaviour of back press: we wan't to go
            // to the progress pic screen if we try to go back when the
            // camera is open
            val backPressedDispatcher: OnBackPressedDispatcher? = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            val backCallback = remember {
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (isShowingCamera) {
                            isShowingCamera = false
                        } else {
                            navController.popBackStack()
                        }

                    }
                }
            }
            DisposableEffect(key1 = backPressedDispatcher) {
                backPressedDispatcher?.addCallback(backCallback)

                onDispose {
                    backCallback.remove()
                }
            }

            if (isShowingCamera) {
                CameraView(
                    outputDirectory = context.filesDir,
                    executor = cameraExecutor,
                    onImageCaptured = { uri ->
                        if (poseLandmarkerHelper.isClose())
                        {
                            poseLandmarkerHelper.setupPoseLandmarker()
                        }
                        val detectionRes = poseLandmarkerHelper.detectImage(Glide.with(context).asBitmap().load(uri).submit().get())
                        if (detectionRes != null &&
                            detectionRes.results.isNotEmpty() &&
                            detectionRes.results[0].landmarks().isNotEmpty())
                        {
                            val landmarks = detectionRes.results[0].landmarks()[0]
                            val nose = mutableListOf(landmarks[0].x(),
                                landmarks[0].y(),
                                landmarks[0].z())
                            val hip = mutableListOf((landmarks[23].x() + landmarks[24].x()) / 2,
                                (landmarks[23].y() + landmarks[24].y()) / 2,
                                (landmarks[23].z() + landmarks[24].z()) / 2)

                            val angle = -(atan2(hip[1]-nose[1], hip[0]-nose[0]) * 180 / PI - 90f)
                            val angleRad = angle * PI / 180

                            var image = Glide.with(context).asBitmap().load(uri)

                            val newW = detectionRes.inputImageWidth * abs(cos(angleRad)) +
                                    detectionRes.inputImageHeight * abs(sin(angleRad))
                            val newH = detectionRes.inputImageWidth * abs(sin(angleRad)) +
                                    detectionRes.inputImageHeight * abs(cos(angleRad))

                            Log.d("TESTDEBUG", "oldsize ${detectionRes.inputImageWidth},${detectionRes.inputImageHeight}")
                            Log.d("TESTDEBUG", "newsize $newW,$newH")

                            val transformList = mutableListOf<BitmapTransformation>()

                            //landmarks.forEach() {landmark ->
                            //    transformList.add(DrawPointTransformation(
                            //        listOf((landmark.x() * detectionRes.inputImageWidth),
                            //            (landmark.y() * detectionRes.inputImageHeight)),
                            //        _size=5f, _color =0xffffff))
                            //}

                            transformList.add(RotateTransformation(
                                angle.toFloat()
                            ))



                            val noseRot = listOf((nose[0] - 0.5) * cos(angleRad) -
                                    (nose[1] - 0.5) * sin(angleRad) + 0.5,// + ((newW - detectionRes.inputImageWidth) ) / newW / 2,//
                                (nose[0] - 0.5) * sin(angleRad) +
                                        (nose[1] - 0.5) * cos(angleRad) + 0.5 //+ ((newH - detectionRes.inputImageHeight)) / newH / 2
                            )

                            val hipRot = listOf((hip[0] - 0.5) * cos(angleRad) -
                                    (hip[1] - 0.5) * sin(angleRad) + 0.5,
                                (hip[0] - 0.5) * sin(angleRad) +
                                        (hip[1] - 0.5) * cos(angleRad) + 0.5
                            )

                            val distanceNoseHip = hipRot[1] - noseRot[1]
                            var topPointNorm = noseRot[1] - distanceNoseHip / 3
                            val bottomPointNorm = hipRot[1] + 2 * distanceNoseHip / 3
                            var padTop = 0
                            var padBottom = 0


                            if (topPointNorm < 0 ||bottomPointNorm > 1){
                                if (topPointNorm < 0) {
                                    padTop = (abs(topPointNorm) * detectionRes.inputImageHeight)
                                        .roundToInt()
                                }
                                if (bottomPointNorm > 1) {
                                    padBottom = ((topPointNorm - 1) * detectionRes.inputImageHeight)
                                        .roundToInt()
                                }

                                transformList.add(PadTransformation(padTop,0,padBottom,0))
                                if (padTop > 0){
                                    topPointNorm = 0.0
                                }
                                if (padBottom > 0){
                                    topPointNorm = 1.0
                                }

                            }

                            //transformList.add(DrawPointTransformation(
                            //    listOf((noseRot[0] * newW).toFloat(),
                            //        (noseRot[1] * newH + padTop).toFloat()),
                            //_size =15f, _color =0xff0000))

                            val topCrop = (topPointNorm * newH + padTop).roundToInt()
                            val bottomCrop =  (bottomPointNorm * newH + padTop + padBottom).roundToInt() - 1
                            val noseX = noseRot[0] * newW
                            val cropH = ((bottomCrop - topCrop) * 1080.0 / 1920.0) / 2

                            val leftCrop = (noseX - cropH).roundToInt()
                            val rightCrop = (noseX + cropH).roundToInt()

                            transformList.add(CropTransformation(
                                topCrop,
                                rightCrop - 1,
                                bottomCrop,
                                leftCrop
                                )
                            )

                            image = image.transform(MultiTransformation(transformList))

                            //image.transform(MultiTransformation(*(transformList)))
                            //image.apply(RequestOptions().transforms(*(transformList)))

                            val fileName = uri.toString().split("/")
                            val photoFile = File(
                                context.filesDir,
                                fileName[fileName.size - 1]
                            )
                            val imageBitmap = image.submit().get()
                            val stream = FileOutputStream(photoFile)
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream)
                            stream.flush()
                            stream.close()
                        }
                            onEvent(
                            ProgressPicEvent.AddProgressPic(
                                ProgressPic(
                                    date = LocalDateTime.now(),
                                    path = uri.toString(),
                                    )
                            )
                        )
                        isShowingCamera=false
                    },
                    onError = {})
            } else {
                Scaffold() { padding ->
                    var currImgIndex by remember {
                        mutableFloatStateOf(0f)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.value.progressPics.isNotEmpty()) {
                            val currImgIndexInt = currImgIndex.roundToInt()
                            Row() {

                                val cacheFile = File(state.value.progressPics[currImgIndexInt].path)
                                val splitCacheFile = cacheFile.toString().split(":")
                                Box() {
                                    Image(
                                        rememberAsyncImagePainter(splitCacheFile[splitCacheFile.size - 1]),
                                        "test",
                                        modifier = Modifier
                                            .padding(8.dp, 8.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                    )
                                    Button(onClick = {
                                           isShowingCamera = true},
                                           modifier = Modifier.align(Alignment.TopEnd)
                                               .padding(16.dp, 16.dp)
                                               .height(48.dp)
                                               .width(48.dp),
                                            shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(
                                            start = 4.dp,
                                            top = 4.dp,
                                            end = 4.dp,
                                            bottom = 4.dp,
                                        )
                                    ) {

                                        Icon(imageVector = Icons.Default.Add,
                                            contentDescription = "Add Exercise")
                                    }
                                }


                            }
                            Row(modifier = Modifier.align(Alignment.CenterHorizontally)){
                                val dateTimeFormatterYear: DateTimeFormatter =
                                    DateTimeFormatter.ofPattern("dd/MM/yy")
                                Text(state.value.progressPics[currImgIndexInt]
                                    .date.format(dateTimeFormatterYear)
                                )
                            }
                            if (state.value.progressPics.size > 1) {
                                Row(modifier = Modifier.padding(15.dp, 5.dp)) {
                                    Slider(
                                        value = state.value.progressPics.size - currImgIndex - 1,
                                        onValueChange = {
                                            currImgIndex = state.value.progressPics.size - it - 1
                                        },
                                        valueRange = 0f..state.value.progressPics.size.toFloat() - 1,
                                        steps = state.value.progressPics.size - 2
                                    )
                                }
                            }
                        }
                    }



                    //Image(, contentDescription = )
                }
            }
        }
        else -> {
            // Asking for permission
            SideEffect {
                launcher.launch(Manifest.permission.CAMERA)
            }
            navController.navigate("main")
        }
    }

}