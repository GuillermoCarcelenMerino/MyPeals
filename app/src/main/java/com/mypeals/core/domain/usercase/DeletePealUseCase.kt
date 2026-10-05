package com.mypeals.core.domain.usercase

import com.mypeals.core.data.room.entities.Peal
import com.mypeals.core.domain.repository.DatabaseRepository
import javax.inject.Inject

class DeletePealUseCase @Inject constructor(private val repository: DatabaseRepository) {

    operator suspend fun invoke(peal: Peal): Result<Unit> = try {
        Result.success(repository.deletePeal(peal))
    } catch (exc: Exception) {
        Result.failure(exc)
    }
}