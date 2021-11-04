package com.mirage.todolist.model.database

import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mirage.todolist.di.App
import com.mirage.todolist.di.DaggerTestAppComponent
import com.mirage.todolist.di.TestAppComponent
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.*
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class VersionDaoTest {

    @Inject
    lateinit var database: AppDatabase
    @Inject
    lateinit var versionDao: VersionDao

    private val testingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Before
    fun setup() {
        (App.instance.appComponent as TestAppComponent).inject(this)
    }

    @After
    fun clear() {
        versionDao.clear()
        assertThat(versionDao.getAllVersions()).isEmpty()
    }

    @Test
    fun testInsertion() {
        with(versionDao) {
            assertThat(getAllVersions()).isEmpty()
            insertVersion(versionOne)
            assertThat(getAllVersions()).hasSize(1)
            insertVersion(versionTwo)
            assertThat(getAllVersions()).hasSize(2)
            insertVersion(versionTwo)
            assertThat(getAllVersions()).hasSize(2)
        }
    }

    @Test
    fun testUpdates() {
        with(versionDao) {
            insertAllVersions(testVersions)
            assertThat(getAllVersions()).hasSize(2)
            val uuid = UUID.randomUUID()
            setDataVersion("unknown@wtf.org", uuid, true)
            assertThat(getDataVersion(versionOne.accountName).first()).isEqualTo(versionOne.dataVersion)
            setDataVersion(versionTwo.accountName, uuid, true)
            assertThat(getDataVersion(versionTwo.accountName).first()).isEqualTo(uuid)
            assertThat(getMustBeProcessed(versionTwo.accountName)).isTrue()
            setMustBeProcessed(versionOne.accountName, true)
            assertThat(getMustBeProcessed(versionTwo.accountName)).isTrue()
        }
    }

    @Test
    fun testDataVersionFlow() {
        with(versionDao) {
            val liveVersion = getDataVersionFlow(versionOne.accountName)
            val collectorEntries: MutableList<UUID> = CopyOnWriteArrayList()
            val collectorJob = testingScope.launch {
                liveVersion.collect {
                    collectorEntries += it
                }
            }
            Thread.sleep(20L)
            insertVersion(versionOne)
            Thread.sleep(20L)
            insertVersion(versionTwo)
            runBlocking {
                delay(20L)
                collectorJob.cancelAndJoin()
            }
            val expected = listOf(
                null,
                versionOne.dataVersion,
                versionOne.dataVersion
            )
            assertThat(collectorEntries).containsExactlyElementsIn(expected)
        }
    }

    @Test
    fun testAllVersionsFlow() {
        with(versionDao) {
            val liveVersions = getAllVersionsFlow()
            val collectorEntries: MutableList<List<VersionEntity>> = CopyOnWriteArrayList()
            val collectorJob = testingScope.launch {
                liveVersions.collect {
                    collectorEntries += it
                }
            }
            Thread.sleep(20L)
            insertVersion(versionOne)
            Thread.sleep(20L)
            insertVersion(versionTwo)
            runBlocking {
                delay(20L)
                collectorJob.cancelAndJoin()
            }
            val expected = listOf(
                listOf(),
                listOf(versionOne),
                listOf(versionOne, versionTwo)
            )
            assertThat(collectorEntries).containsExactlyElementsIn(expected)
        }
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun init() {
            App.instance.setTestComponent(DaggerTestAppComponent.create())
        }
    }
}