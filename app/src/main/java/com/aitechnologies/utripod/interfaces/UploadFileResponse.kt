package com.aitechnologies.utripod.interfaces

interface UploadFileResponse {

    fun onSuccess(filePath: String)

    fun onProgress(progress: Int)

    fun onFailure(message: String)

}