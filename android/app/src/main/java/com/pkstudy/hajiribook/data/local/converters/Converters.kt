package com.pkstudy.hajiribook.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromFloatList(list: List<Float>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun toFloatList(data: String?): List<Float>? {
        if (data == null) return null
        val type = object : TypeToken<List<Float>>() {}.type
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun fromFloatArray(array: FloatArray?): ByteArray? {
        if (array == null) return null
        val buffer = ByteBuffer.allocate(array.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (f in array) {
            buffer.putFloat(f)
        }
        return buffer.array()
    }

    @TypeConverter
    fun toFloatArray(bytes: ByteArray?): FloatArray? {
        if (bytes == null) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val array = FloatArray(bytes.size / 4)
        for (i in array.indices) {
            array[i] = buffer.float
        }
        return array
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        if (data == null) return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, type)
    }
}
