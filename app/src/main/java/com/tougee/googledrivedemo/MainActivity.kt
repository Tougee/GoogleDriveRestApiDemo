package com.tougee.googledrivedemo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private var driveManager: DriveManager? = null

    @SuppressLint("SimpleDateFormat")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        upload.setOnClickListener { upload() }
        restore.setOnClickListener { restore() }
        find.setOnClickListener { find() }
        delete_local.setOnClickListener { deleteLocal() }
        delete.setOnClickListener { delete() }
        find_local.setOnClickListener { findLocal() }
        create_local.setOnClickListener { createLocal() }

        signIn()
        initFile()
    }

    private fun initFile() {
        val file = File(this.filesDir, LOCAL_FILE_NAME)
        file.writeText("This is the test file text")
    }

    private fun signIn() {
        val googleSignInClient = buildGoogleSignInClient()
        startActivityForResult(googleSignInClient.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(this, signInOptions)
    }

    private fun upload() {
        GlobalScope.launch(Dispatchers.IO) {
            driveManager?.upload(
                File(filesDir, LOCAL_FILE_NAME),
                "text/plain",
                REMOTE_FILE_NAME
            )
            Log.d("@@@", "upload file")
        }
    }

    private fun restore() {
        GlobalScope.launch(Dispatchers.IO) {
            val files = driveManager?.query(REMOTE_FILE_NAME)
            files?.forEach { file ->
                val localFile = File(filesDir, LOCAL_FILE_NAME)
                Log.d(
                    "@@@",
                    "before restore local file last modified time: ${dateFormat.format(localFile.lastModified())}"
                )
                driveManager?.download(file.id, localFile.absolutePath)
                Log.d(
                    "@@@", "restore file: ${file.name}, " +
                            "local file modified time : ${dateFormat.format(localFile.lastModified())}"
                )
            }
        }
    }

    private fun find() {
        GlobalScope.launch(Dispatchers.IO) {
            val files = driveManager?.query(REMOTE_FILE_NAME)
            Log.d("@@@", "find files size: ${files?.size}")
            files?.forEach {
                Log.d("@@@", "find file: ${it.name}")
            }
        }
    }

    private fun delete() {
        GlobalScope.launch(Dispatchers.IO) {
            val files = driveManager?.query(REMOTE_FILE_NAME)
            files?.forEach { file ->
                driveManager?.delete(file)
                Log.d("@@@", "delete file: ${file.name}")
            }
        }
    }

    private fun createLocal() {
        val file = File(filesDir, LOCAL_FILE_NAME)
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("file created by use click")
            Log.d("@@@", "create file")
        } else {
            Log.d("@@@", "local file exists")
        }
    }

    private fun findLocal() {
        val file = File(filesDir, LOCAL_FILE_NAME)
        Log.d("@@@", "find local file: ${file.exists()}")
    }

    private fun deleteLocal() {
        File(filesDir, LOCAL_FILE_NAME).delete()
        Log.d("@@@", "delete local file")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == Activity.RESULT_OK) {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            name.text = account?.email
            driveManager = DriveManager.getInstance(this)
        }
    }

    companion object {
        const val REQUEST_CODE_SIGN_IN = 0

        const val LOCAL_FILE_NAME = "file.txt"
        const val REMOTE_FILE_NAME = "test-text"
    }
}
