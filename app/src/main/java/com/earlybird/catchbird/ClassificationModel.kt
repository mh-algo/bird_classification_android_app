package com.earlybird.catchbird

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.opencsv.CSVReader
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class ClassificationModel(val context: Context) {
    fun execution(bitmap:Bitmap, modelType: String):String {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resized, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB)
        // val inputs = inputTensor.dataAsFloatArray

        val model:String = when (modelType) {
            "bird" -> {
                "bird_model.ptl"
            }
            "specie" -> {
                "bird_specie_model.ptl"
            }
            else -> {
                ""
            }
        }

        val module = LiteModuleLoader.load(Utils.assetFilePath(context, model))
        val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
        val resultArray = outputTensor.dataAsFloatArray

        val maxValue = resultArray.maxOrNull()
        val result = resultArray.indices.firstOrNull { i:Int -> maxValue == resultArray[i] }
        return result.toString()
    }
}