package app.gyst.persistence.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.gyst.common.toInstant
import app.gyst.common.toIso8601TimeStamp
import app.gyst.common.utc
import com.google.common.truth.Truth.assertThat

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DateConverterTest {

    @Test
    fun converts__string__to__instant() {
        assertThat("2020-05-24T21:47:54Z".toInstant()!!.utc().toEpochSecond()).isEqualTo(1590356874)
    }

    @Test
    fun converts__millis__to__instant() {
        assertThat(1590356874793.toInstant().toEpochMilli()).isEqualTo(1590356874793)
    }

    @Test
    fun converts__instant__to__string() {
        assertThat(1590356874793.toInstant().utc().toIso8601TimeStamp()).isEqualTo("2020-05-24T21:47:54Z")
    }

    @Test
    fun parses_text_date() {
        assertThat("2020-05-25T00:20:29.969Z".toInstant()!!.utc().toIso8601TimeStamp()).isEqualTo("2020-05-25T00:20:29Z")
    }
}
