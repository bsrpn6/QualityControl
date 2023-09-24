package info.onesandzeros.qualitycontrol.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Image
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
    fun fromLines(value: String): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toLines(list: List<String>?): String {
        return Gson().toJson(list)
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

    @TypeConverter
    fun fromImageList(value: List<Image>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toImageList(value: String?): List<Image>? {
        val type = object : TypeToken<List<Image>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromAny(value: Any?): String {
        // Convert the Any? object to a JSON string representation
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toAny(value: String): Any? {
        // Convert the JSON string back to an object.
        // Note: This will not restore the original type; it will give you a LinkedTreeMap.
        // You will need to handle the deserialization properly if you expect specific types.
        return Gson().fromJson(value, Any::class.java)
    }
}

