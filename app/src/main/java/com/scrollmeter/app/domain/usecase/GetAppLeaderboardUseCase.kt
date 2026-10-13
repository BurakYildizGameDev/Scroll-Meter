package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.domain.model.AppScrollSession
import com.scrollmeter.app.domain.repository.IScrollRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAppLeaderboardUseCase @Inject constructor(
    private val scrollRepository: IScrollRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(epochDays: Long, limit: Int = 50): Flow<Result<List<AppScrollSession>>> {
        return scrollRepository.getDailyAppSessions(epochDays)
            .map { sessions ->
                sessions
                    .filter { it.totalPixels > 0.0 }
                    .sortedByDescending { it.totalPixels }
                    .take(limit)
            }
            .map<List<AppScrollSession>, Result<List<AppScrollSession>>> { Result.Success(it) }
            .catch { emit(Result.Error(it)) }
            .flowOn(ioDispatcher)
    }
}
