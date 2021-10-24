package com.mirage.todolist.ui

import java.security.MessageDigest
import java.util.regex.Pattern

object PasswordValidator {

    private val passwordPattern = Pattern.compile("""[a-zA-Zа-яА-Я0-9!@#_\-$]{4,24}""")

    fun isPasswordValid(password: String): Boolean {
        return password.isNotBlank() && passwordPattern.matcher(password).matches()
    }

    fun getSHA256(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hash = messageDigest.digest(password.encodeToByteArray())
        return hash.decodeToString()
    }

}