package com.example.medicationreminder.di

import com.example.medicationreminder.data.repository.MedicationRepositoryImpl
import com.example.medicationreminder.domain.repository.MedicationRepository
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
    abstract fun bindMedicationRepository(
        impl: MedicationRepositoryImpl
    ): MedicationRepository
}
