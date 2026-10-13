package com.scrollmeter.app.di

import com.scrollmeter.app.data.repository.ScrollRepositoryImpl
import com.scrollmeter.app.data.resolver.DeviceMetricsProviderImpl
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import com.scrollmeter.app.domain.repository.IScrollRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScrollRepository(
        impl: ScrollRepositoryImpl
    ): IScrollRepository

    @Binds
    @Singleton
    abstract fun bindMetricsProvider(
        impl: DeviceMetricsProviderImpl
    ): IDeviceMetricsProvider

    @Binds
    @Singleton
    abstract fun bindUsageStatsProvider(
        impl: com.scrollmeter.app.data.resolver.UsageStatsProviderImpl
    ): com.scrollmeter.app.domain.repository.IUsageStatsProvider
}
