package com.memu.modules.DocUpload

data class DocUpload(
    val document_path: String,
    val file_id: String,
    val file_name: String,
    val status: String
)