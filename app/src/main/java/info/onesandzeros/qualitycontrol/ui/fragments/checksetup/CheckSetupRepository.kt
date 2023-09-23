package info.onesandzeros.qualitycontrol.data

import info.onesandzeros.qualitycontrol.api.MyApi
import info.onesandzeros.qualitycontrol.api.models.CheckType
import info.onesandzeros.qualitycontrol.api.models.Department
import info.onesandzeros.qualitycontrol.api.models.IDHNumbers
import info.onesandzeros.qualitycontrol.api.models.Line
import info.onesandzeros.qualitycontrol.api.models.ProductSpecsResponse
import info.onesandzeros.qualitycontrol.data.dao.CheckTypeDao
import info.onesandzeros.qualitycontrol.data.dao.DepartmentDao
import info.onesandzeros.qualitycontrol.data.dao.IDHNumbersDao
import info.onesandzeros.qualitycontrol.data.dao.LineDao
import info.onesandzeros.qualitycontrol.data.models.CheckTypeEntity
import info.onesandzeros.qualitycontrol.data.models.DepartmentEntity
import info.onesandzeros.qualitycontrol.data.models.IDHNumbersEntity
import info.onesandzeros.qualitycontrol.data.models.LineEntity
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils.CustomException
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils.DatabaseException
import info.onesandzeros.qualitycontrol.info.onesandzeros.qualitycontrol.utils.NetworkException
import info.onesandzeros.qualitycontrol.utils.toCheckTypeList
import info.onesandzeros.qualitycontrol.utils.toDepartmentList
import info.onesandzeros.qualitycontrol.utils.toIdhNumbersList
import info.onesandzeros.qualitycontrol.utils.toLineList
import javax.inject.Inject

class CheckSetupRepository @Inject constructor(
    private val myApi: MyApi,
    private val lineDao: LineDao,
    private val departmentDao: DepartmentDao,
    private val idhNumbersDao: IDHNumbersDao,
    private val checkTypeDao: CheckTypeDao
) {

    suspend fun getDepartmentsForSite(siteId: String): List<Department> {
        return fetchDataFromNetworkFirst(networkCall = { myApi.getDepartmentsForSite(siteId) },
            databaseCall = { getDepartmentsForSiteFromDatabase(siteId) },
            saveToDatabase = { insertDepartments(it) })
    }

    suspend fun getLinesForDepartment(departmentId: String): List<Line> {
        return fetchDataFromNetworkFirst(networkCall = { myApi.getLinesForDepartment(departmentId) },
            databaseCall = { getLinesForDepartmentFromDatabase(departmentId) },
            saveToDatabase = { insertLines(it, departmentId) })
    }

    suspend fun getCheckTypesForLine(lineId: String): List<CheckType> {
        return fetchDataFromNetworkFirst(networkCall = { myApi.getCheckTypesForLine(lineId) },
            databaseCall = { getSpecsForProductFromDatabase(lineId) },
            saveToDatabase = { insertCheckTypes(it, lineId) })
    }

    suspend fun getProductsForLine(lineId: String): List<IDHNumbers> {
        return fetchDataFromNetworkFirst(networkCall = { myApi.getProductsForLine(lineId) },
            databaseCall = { getProductsForLineFromDatabase(lineId) },
            saveToDatabase = { insertProducts(it) })
    }

    suspend fun getSpecsForProduct(id: String): ProductSpecsResponse {
        val response = myApi.getSpecsForProduct(id)
        if (response.isSuccessful) {
            return response.body() ?: throw CustomException("No specs received.")
        } else {
            throw NetworkException("Failed to fetch specs.")
        }
    }

    private inline fun <T> fetchDataFromNetworkFirst(
        networkCall: () -> T, databaseCall: () -> T, saveToDatabase: (T) -> Unit
    ): T {
        return try {
            val data = networkCall.invoke()
            saveToDatabase.invoke(data)
            data
        } catch (exception: Exception) {
            databaseCall.invoke()
                ?: throw DatabaseException("Failed to fetch data from the database.")
        }
    }

    private suspend fun getDepartmentsForSiteFromDatabase(siteId: String): List<Department> {
        val departmentEntities = departmentDao.getAllDepartments()
        return departmentEntities.toDepartmentList()
    }

    private suspend fun getLinesForDepartmentFromDatabase(departmentId: String): List<Line> {
        val lineEntities = lineDao.getLinesByDepartmentId(departmentId)
        return lineEntities.toLineList()
    }

    private suspend fun getSpecsForProductFromDatabase(lineId: String): List<CheckType> {
        val checkTypeEntities = checkTypeDao.getAllCheckTypes(lineId)
        return checkTypeEntities.toCheckTypeList()
    }

    private suspend fun getProductsForLineFromDatabase(lineId: String): List<IDHNumbers> {
        val productList = idhNumbersDao.getIDHNumbersByLineId(lineId)
        return productList.toIdhNumbersList()
    }

    private suspend fun insertDepartments(departments: List<Department>) {
        val departmentEntities = departments.map { department ->
            DepartmentEntity(
                department.id,
                department.name,
                department.abbreviation,
                department.description,
                department.lines
            )
        }
        departmentDao.insertDepartments(departmentEntities)
    }

    private suspend fun insertLines(lines: List<Line>, departmentId: String) {
        val lineEntities = lines.map { line ->
            LineEntity(
                line.id, line.abbreviation, line.name, departmentId, line.checkTypes
            )
        }
        lineDao.insertLines(lineEntities)
    }

    private suspend fun insertProducts(products: List<IDHNumbers>) {
        val productEntities = products.map { product ->
            IDHNumbersEntity(
                product.id, product.productId, product.lineId, product.name, product.description
            )
        }
        idhNumbersDao.insertIDHNumbers(productEntities)
    }

    private suspend fun insertCheckTypes(checkTypes: List<CheckType>, lineId: String) {
        val checkTypeEntities = checkTypes.map { checkType ->
            CheckTypeEntity(
                checkType.id, checkType.name, lineId, checkType.displayName, checkType.checks
            )
        }
        checkTypeDao.insertCheckTypes(checkTypeEntities)
    }
}
