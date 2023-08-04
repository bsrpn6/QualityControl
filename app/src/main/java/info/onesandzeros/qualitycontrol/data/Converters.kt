package info.onesandzeros.qualitycontrol.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line

class Converters {
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        return value?.split(",")?.map { it.toInt() }
    }

    @TypeConverter
    fun fromDepartment(value: Department?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toDepartment(value: String?): Department? {
        return Gson().fromJson(value, Department::class.java)
    }

    @TypeConverter
    fun fromLine(value: Line?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toLine(value: String?): Line? {
        return Gson().fromJson(value, Line::class.java)
    }

    @TypeConverter
    fun fromIdhNumber(value: IDHNumbers?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIdhNumber(value: String?): IDHNumbers? {
        return Gson().fromJson(value, IDHNumbers::class.java)
    }

    @TypeConverter
    fun fromCheckItemList(value: Map<String, List<CheckItem>>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCheckItemList(value: String?): Map<String, List<CheckItem>>? {
        val type = object : TypeToken<Map<String, List<CheckItem>>>() {}.type
        return Gson().fromJson(value, type)
    }
}

