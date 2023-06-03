package com.example.contactstest

import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()

    }

    private fun checkPermission() {
        val permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            requestContacts()
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_CONTACTS),
            READ_CONTACTS_RC
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == READ_CONTACTS_RC && grantResults.isNotEmpty()) {
            val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (permissionGranted) {
                requestContacts()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun requestContacts() {
        thread {

            val cursor = contentResolver.query(
                Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            while (cursor?.moveToNext() == true) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME))
                if (cursor.getLong(cursor.getColumnIndexOrThrow(Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCursor = contentResolver.query(
                        Phone.CONTENT_URI,
                        null,
                        "${Phone.CONTACT_ID} = ?",
                        arrayOf(id.toString()),
                        null
                    )
                    while (pCursor?.moveToNext() == true) {
                        val phoneNumber =
                            pCursor.getString(pCursor.getColumnIndexOrThrow(Phone.NUMBER))
                        val contact = Contact(id, name, phoneNumber)
                        Log.d("Contacts", contact.toString())
                    }
                    pCursor?.close()
                }
            }
            cursor?.close()
        }
    }

    companion object {
        private const val READ_CONTACTS_RC = 44
    }


}