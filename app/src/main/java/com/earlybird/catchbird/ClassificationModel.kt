package com.earlybird.catchbird

import android.content.Context
import android.graphics.Bitmap
import com.earlybird.catchbird.data.BirdImageList
import com.earlybird.catchbird.data.ModelResultData
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.torchvision.TensorImageUtils

class ClassificationModel(val context: Context) {
    fun execution(bitmap:Bitmap, modelType: String):String {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resized, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB)
        // val inputs = inputTensor.dataAsFloatArray

        when (modelType) {
            "bird" -> {
                val module = LiteModuleLoader.load(Utils.assetFilePath(context, "bird_model.ptl"))
                val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
                val resultArray = outputTensor.dataAsFloatArray

                val maxValue = resultArray.maxOrNull()
                val result = resultArray.indices.firstOrNull { i:Int -> maxValue == resultArray[i] }
                return result.toString()
            }
            "specie" -> {
                val module = LiteModuleLoader.load(Utils.assetFilePath(context, "bird_specie_model.ptl"))
                val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
                val resultArray = outputTensor.dataAsFloatArray

                val arr = arrayListOf<Pair<Int, Float>>()
                for ((idx, percent) in resultArray.withIndex())
                    arr.add(Pair(idx, percent))
                arr.sortByDescending { it.second }      // 정확도가 높은 순으로 정렬

                BirdImageList.modelData.clear()
                for ((idx, percent) in arr)
                    if (percent >= 0.1)
                        BirdImageList.modelData.add(ModelResultData(idx, percent))
            }
        }
        return ""
    }
}