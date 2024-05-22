package com.example.quickconnect.utils

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.example.quickconnect.model.viewersmodel.SocialLinks
import java.io.File
import java.io.IOException

class ContactHelper(private val context: Context) {

    private val contentResolver: ContentResolver = context.contentResolver

    fun addToContacts(
        userName: String,
        fullName: String,
        mobileNumber : String,
        workAt: String,
        desc: String,
        socialLinks: List<SocialLinks>
    ) {
        try {
            val vCard = createVCard(fullName, mobileNumber, socialLinks)
            saveVCardToFile(vCard)

            Toast.makeText(context, "Contact added successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to add contact", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createVCard(
        fullName: String,
        mobileNumber: String,
        socialLinks: List<SocialLinks>
    ): String {
        val vCardBuilder = StringBuilder()

        // Add contact name
        vCardBuilder.append("BEGIN:VCARD\n")
        vCardBuilder.append("VERSION:3.0\n")
        vCardBuilder.append("FN:$fullName\n")
        vCardBuilder.append("TEL:$mobileNumber\n")

        // Add social links
        for (socialLink in socialLinks) {
            if (socialLink.links?.link != null) {
                val platform = socialLink.links?.link ?: "Social Link"
                vCardBuilder.append("X-SOCIAL-LINK;$platform:${socialLink.links?.link}\n")
            }
        }

        vCardBuilder.append("END:VCARD\n")

        return vCardBuilder.toString()
    }

    private fun saveVCardToFile(vCard: String) {
        try {
            val vCardFile = File(context.getExternalFilesDir(null), "contact.vcf")
            vCardFile.writeText(vCard)

            // Now, prompt the user to open the file or share it.
            // You can implement a file picker or use an Intent to share the file.

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/vcard"
            shareIntent.putExtra(Intent.EXTRA_STREAM, vCardFile.toUri())
            context.startActivity(Intent.createChooser(shareIntent, "Share VCard"))

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save VCard to file", Toast.LENGTH_SHORT).show()
        }
    }
}