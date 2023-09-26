package info.onesandzeros.qualitycontrol.utils

import info.onesandzeros.qualitycontrol.api.models.CheckItem
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.data.models.CheckItemEntity
import info.onesandzeros.qualitycontrol.data.models.CheckTypeEntity
import info.onesandzeros.qualitycontrol.data.models.DepartmentEntity
import info.onesandzeros.qualitycontrol.data.models.IDHNumbersEntity
import info.onesandzeros.qualitycontrol.data.models.LineEntity
import java.io.File
import java.util.Base64


fun List<DepartmentEntity>.toDepartmentList(): List<Department> {
    return map { departmentEntity ->
        Department(
            departmentEntity.departmentId,
            departmentEntity.name,
            departmentEntity.abbreviation,
            departmentEntity.description,
            departmentEntity.lines
        )
    }
}

fun List<LineEntity>.toLineList(): List<Line> {
    return map { lineEntity ->
        Line(lineEntity.lineId, lineEntity.abbreviation, lineEntity.name, lineEntity.checkTypes)
    }
}

fun List<IDHNumbersEntity>.toIdhNumbersList(): List<IDHNumbers> {
    return map { idhNumbersEntity ->
        IDHNumbers(
            idhNumbersEntity.id,
            idhNumbersEntity.productId,
            idhNumbersEntity.lineId,
            idhNumbersEntity.name,
            idhNumbersEntity.description
        )
    }
}

fun List<CheckTypeEntity>.toCheckTypeList(): List<CheckType> {
    return map { checkTypeEntity ->
        CheckType(
            checkTypeEntity.id,
            checkTypeEntity.name,
            checkTypeEntity.lineId,
            checkTypeEntity.displayName,
            checkTypeEntity.checks
        )
    }
}

fun List<CheckItemEntity>.toCheckItemList(): List<CheckItem> {
    return map { checkItemEntity ->
        CheckItem(
            checkItemEntity.id,
            checkItemEntity.section,
            checkItemEntity.type,
            checkItemEntity.title,
            checkItemEntity.description,
            checkItemEntity.expectedValue,
            checkItemEntity.images
        )
    }
}

fun List<Line>.toLineNameList(): List<String> {
    return map { line ->
        line.name
    }
}

//fun uriToBase64(context: Context, uri: Uri): String {
//    val inputStream = context.contentResolver.openInputStream(uri)
//    val byteArrayOutputStream = ByteArrayOutputStream()
//    inputStream?.copyTo(byteArrayOutputStream)
//    val byteArray = byteArrayOutputStream.toByteArray()
//    return Base64.encodeToString(byteArray, Base64.DEFAULT)
//}

fun fileToBase64(filePath: String): String {
    val file = File(filePath)
    val fileBytes = file.readBytes()
    return Base64.getEncoder().encodeToString(fileBytes)
}
