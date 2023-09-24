package info.onesandzeros.qualitycontrol.utils

open class CustomException(message: String) : Throwable(message)
class NetworkException(message: String) : CustomException(message)
class ServerException(message: String) : CustomException(message)
class DatabaseException(message: String) : CustomException(message)
