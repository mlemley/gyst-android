package app.gyst.persistence

import androidx.room.Room
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module


@ExperimentalCoroutinesApi
val persistenceModule = module {

    single(named("dbName")) { "gyst_db" }

    single {
        Room.databaseBuilder(
            androidApplication(),
            GystDatabase::class.java,
            get(named("dbName"))
        )
            .allowMainThreadQueries()
            .addMigrations()
            .addCallback(get<GystDatabaseConnectionCallback>())
            .build()
    }

    single { GystDatabaseConnectionCallback() }

    // Expose DAO's

    factory { get<GystDatabase>().userDao }
/*
    factory { get<GystDatabase>().userProfileDao }
 */
}