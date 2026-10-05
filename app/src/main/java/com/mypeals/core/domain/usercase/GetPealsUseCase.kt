package com.mypeals.core.domain.usercase

import com.mypeals.core.data.room.entities.Peal
import com.mypeals.core.domain.repository.DatabaseRepository
import javax.inject.Inject

class GetPealsUseCase @Inject constructor(private val repository: DatabaseRepository) {

    operator suspend fun invoke(): Result<List<Peal>> = try {
        Result.success(repository.getPeals())
    } catch (exc: Exception) {
        Result.failure(exc)
    }
}