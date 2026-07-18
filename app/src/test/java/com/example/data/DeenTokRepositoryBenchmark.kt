package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class DeenTokRepositoryBenchmark {

    private lateinit var database: AppDatabase
    private lateinit var dao: DeenTokDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.deenTokDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun benchmarkPrepopulateReportsLoop() = runBlocking {
        // Generate a large number of reports for the benchmark
        val numReports = 1000
        val reports = mutableListOf<Report>()
        for (i in 1..numReports) {
             reports.add(Report(type = "SPAM", contentId = "spammer_boy_$i", reportedItemTitle = "@spam_bot_$i Profile", reason = "Spamming", reportedBy = "@alice_codes", status = "PENDING"))
        }

        val time = measureTimeMillis {
            for (rep in reports) {
                dao.insertReport(rep)
            }
        }

        println("BENCHMARK: Insertion of $numReports reports using loop took $time ms")
    }
}
