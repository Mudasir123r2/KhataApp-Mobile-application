package com.example.khataapp.data.repository

import com.example.khataapp.data.local.dao.UserDao
import com.example.khataapp.data.local.datastore.ShopProfile
import com.example.khataapp.data.local.datastore.UserPreferences
import com.example.khataapp.data.local.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

class AuthRepository(
    private val userDao: UserDao,
    private val sessionPrefs: UserPreferences
) {
    val currentUserId: Flow<Int?> = sessionPrefs.currentUserId

    val currentUser: Flow<User?> = currentUserId.flatMapLatest { id ->
        if (id != null && id > 0) userDao.getUserById(id) else flowOf(null)
    }

    val shopProfile: Flow<ShopProfile> = currentUser.map { user ->
        ShopProfile(
            ownerName = user?.ownerName ?: "",
            shopName  = user?.shopName  ?: "",
            phone     = user?.phone     ?: ""
        )
    }

    val isLoggedIn: Flow<Boolean> = currentUserId.map { it != null && it > 0 }

    suspend fun hasAnyUser(): Boolean = userDao.getUserCount() > 0

    suspend fun login(username: String, password: String): Result<Unit> {
        val user = userDao.getUserByUsername(username.trim())
            ?: return Result.failure(Exception("Username not found"))
        if (user.passwordHash != hash(password))
            return Result.failure(Exception("Incorrect password"))
        sessionPrefs.saveSession(user.id)
        return Result.success(Unit)
    }

    suspend fun register(
        username: String,
        password: String,
        shopName: String,
        ownerName: String,
        phone: String
    ): Result<Unit> {
        if (userDao.getUserByUsername(username.trim()) != null)
            return Result.failure(Exception("Username already taken"))
        val id = userDao.insertUser(
            User(
                username     = username.trim(),
                passwordHash = hash(password),
                shopName     = shopName.trim(),
                ownerName    = ownerName.trim(),
                phone        = phone.trim(),
                createdAt    = System.currentTimeMillis()
            )
        )
        sessionPrefs.saveSession(id.toInt())
        return Result.success(Unit)
    }

    suspend fun updateProfile(ownerName: String, shopName: String, phone: String) {
        val id   = currentUserId.first() ?: return
        val user = userDao.getUserByIdOnce(id) ?: return
        userDao.updateUser(user.copy(ownerName = ownerName.trim(), shopName = shopName.trim(), phone = phone.trim()))
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val id   = currentUserId.first() ?: return Result.failure(Exception("Not logged in"))
        val user = userDao.getUserByIdOnce(id) ?: return Result.failure(Exception("User not found"))
        if (user.passwordHash != hash(currentPassword))
            return Result.failure(Exception("Current password is incorrect"))
        userDao.updateUser(user.copy(passwordHash = hash(newPassword)))
        return Result.success(Unit)
    }

    suspend fun logout() = sessionPrefs.clearSession()

    private fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
