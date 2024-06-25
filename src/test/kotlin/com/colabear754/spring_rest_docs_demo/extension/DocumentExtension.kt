package com.colabear754.spring_rest_docs_demo.extension

import org.springframework.restdocs.operation.preprocess.OperationPreprocessor
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor
import org.springframework.restdocs.operation.preprocess.Preprocessors.*

fun requestPreprocessor(vararg preprocessors: OperationPreprocessor): OperationRequestPreprocessor =
    preprocessRequest(prettyPrint(), *preprocessors)

fun responsePreprocessor(vararg preprocessors: OperationPreprocessor): OperationResponsePreprocessor =
    preprocessResponse(
        modifyHeaders()
            .remove("X-Content-Type-Options")
            .remove("X-XSS-Protection")
            .remove("X-Frame-Options")
            .remove("Strict-Transport-Security")
            .remove("Cache-Control")
            .remove("Pragma")
            .remove("Expires"),
        prettyPrint(),
        *preprocessors
    )