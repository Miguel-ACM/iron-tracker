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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.daykon.irontracker.R
import com.daykon.irontracker.db.Database
import com.daykon.irontracker.db.ProgressPic
import com.daykon.irontracker.viewModels.ProgressPicViewModel
import com.daykon.irontracker.viewModels.events.ProgressPicEvent
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

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

    var isShowingCamera by remember { mutableStateOf(false) }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {
        }
    }
    val context = LocalContext.current

    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) -> {
            Log.d("TESTDEBUG", context.filesDir.toString())

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
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            isShowingCamera = true
                        }) {
                            Icon(imageVector = Icons.Default.Add,
                                contentDescription = "Add Exercise")
                        }
                    }
                    ) { padding ->
                    var currImgIndex by remember {
                        mutableFloatStateOf(0f)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.value.progressPics.isNotEmpty()) {
                            val currImgIndexInt = currImgIndex.roundToInt()
                            Row() {

                                val cacheFile = File(state.value.progressPics[currImgIndexInt].path)
                                val splitCacheFile = cacheFile.toString().split(":")
                                Image(
                                    rememberAsyncImagePainter(splitCacheFile[splitCacheFile.size - 1]),
                                    "test"
                                )

                            }
                            Row(modifier = Modifier.align(Alignment.CenterHorizontally)){
                                val dateTimeFormatterYear: DateTimeFormatter =
                                    DateTimeFormatter.ofPattern("dd/MM/yy")
                                Text(state.value.progressPics[currImgIndexInt]
                                    .date.format(dateTimeFormatterYear)
                                )
                            }

                            Log.d("TESTDEBUG", "size${state.value.progressPics.size}")
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