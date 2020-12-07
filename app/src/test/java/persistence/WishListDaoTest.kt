package persistence

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raywenderlich.android.wishlist.Wishlist
import com.raywenderlich.android.wishlist.persistence.WishlistDao
import com.raywenderlich.android.wishlist.persistence.WishlistDatabase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.nhaarman.mockitokotlin2.*
import com.raywenderlich.android.wishlist.WishlistFactory
import junit.framework.Assert.assertTrue
import org.mockito.ArgumentCaptor
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P, minSdk = Build.VERSION_CODES.P)
class WishListDaoTest {
    // устанавливаем правило перевода асинхронности в синх.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var wishlistDatabase: WishlistDatabase
    private lateinit var wishlistDao: WishlistDao

    @Before
    fun initDb() {
        // Room.inMemoryDatabaseBuilder(context Application, java.class) - при каждой новой записи старая удаялется
        wishlistDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
                WishlistDatabase::class.java).build()
        // вызываем dao class
        wishlistDao = wishlistDatabase.wishlistDao()
    }

    // После теста закрываем DB
    @After
    fun closeDb() {
        wishlistDatabase.close()
    }

    @Test
    fun getAllReturnsEmptyList() {
        val testObserver: Observer<List<Wishlist>> = mock()
        wishlistDao.getAll().observeForever(testObserver)
        verify(testObserver).onChanged(emptyList())
    }

    @Test
    fun saveWishlistsSavesData() {
        val wishlist1 = Wishlist("Victoria", listOf(), 1)
        val wishlist2 = Wishlist("Tyler", listOf(), 2)
        wishlistDao.save(wishlist1, wishlist2)

        val testObserver: Observer<List<Wishlist>> = mock()
        wishlistDao.getAll().observeForever(testObserver)

        val listClass = ArrayList::class.java as Class<ArrayList<Wishlist>>
        val argumentCaptor = ArgumentCaptor.forClass(listClass)
        // проверка что список не пустой
        verify(testObserver).onChanged(argumentCaptor.capture())
        // получаем true если было довалено хояты бы одно значение
        assertTrue(argumentCaptor.value.size > 0)
    }

    @Test
    fun getAllRetrievesData() {
        val wishlist1 = WishlistFactory.makeWishlist()
        val wishlist2 = WishlistFactory.makeWishlist()
        wishlistDao.save(wishlist1, wishlist2)
        val testObserver: Observer<List<Wishlist>> = mock()

        wishlistDao.getAll().observeForever(testObserver)
        val listClass = ArrayList::class.java as Class<ArrayList<Wishlist>>
        val argumentCaptor = ArgumentCaptor.forClass(listClass)
        verify(testObserver).onChanged(argumentCaptor.capture())
        val capturedArgument = argumentCaptor.value
        assertTrue(capturedArgument.containsAll(listOf(wishlist1, wishlist2)))

    }

}