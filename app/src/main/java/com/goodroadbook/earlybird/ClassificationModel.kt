package com.goodroadbook.earlybird

import android.content.Context
import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ClassificationModel(val context: Context) {
    fun execution(bitmap:Bitmap):String {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resized, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB)
        // val inputs = inputTensor.dataAsFloatArray

        val module = LiteModuleLoader.load(assetFilePath(context, "ResNet-model.ptl"))
        val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
        val resultArray = outputTensor.dataAsFloatArray

        val maxValue = resultArray.maxOrNull()
        val result = resultArray.indices.firstOrNull { i:Int -> maxValue == resultArray[i] }
        return result.toString()       // 나중에 종 이름으로 변경 필요!!!
    }

    private fun assetFilePath(context: Context, asset: String): String {
        val file = File(context.filesDir, asset)

        try {
            val inpStream: InputStream = context.assets.open(asset)
            try {
                val outStream = FileOutputStream(file, false)
                val buffer = ByteArray(4 * 1024)
                var read: Int

                while (true) {
                    read = inpStream.read(buffer)
                    if (read == -1) {
                        break
                    }
                    outStream.write(buffer, 0, read)
                }
                outStream.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}