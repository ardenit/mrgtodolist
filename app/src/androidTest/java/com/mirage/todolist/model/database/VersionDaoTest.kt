package com.mirage.todolist.model.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mirage.todolist.di.App
import com.mirage.todolist.di.DaggerTestAppComponent
import com.mirage.todolist.di.TestAppComponent
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject
import com.google.common.truth.Truth.assertThat
import com.mirage.todolist.util.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.*
import java.time.Clock

@RunWith(AndroidJUnit4::class)
class VersionDaoTest {

    @Inject
    lateinit var database: AppDatabase
    @Inject
    lateinit var versionDao: VersionDao

    private val versionOne = VersionEntity(
        accountName = "test@example.org",
        dataVersion = UUID.randomUUID(),
        mustBeProcessed = false
    )
    private val versionTwo = VersionEntity(
        accountName = "prod@company.org",
        dataVersion = UUID.randomUUID(),
        mustBeProcessed = false
    )
    private val testVersions = listOf(versionOne, versionTwo)

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
    fun testLiveVersion() {
        with(versionDao) {
            println("TEST STARTED")
            assertThat(getAllVersions()).isEmpty()
            val liveVersions = getLiveVersions()
            var currentValue = runBlocking(Dispatchers.Main) { liveVersions.value }
            assertThat(currentValue).isNull()
            insertVersion(versionOne)
            assertThat(liveVersions.getOrAwaitValue()).isNotEmpty()
            currentValue = runBlocking(Dispatchers.Main) { liveVersions.value }
            val foreverObserver = Observer<List<VersionEntity>> {
                println("foreverObserver $it")
            }
            runBlocking(Dispatchers.Main) { liveVersions.observeForever(foreverObserver) }
            insertVersion(versionTwo)
            val liveVersion = getLiveDataVersion(versionOne.accountName)
            val currentVersion = runBlocking(Dispatchers.Main) { liveVersion.value }
            assertThat(currentVersion).isNull()
            runBlocking(Dispatchers.Main) { liveVersions.removeObserver(foreverObserver) }
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