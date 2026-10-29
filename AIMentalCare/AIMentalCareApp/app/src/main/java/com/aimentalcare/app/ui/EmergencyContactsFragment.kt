package com.aimentalcare.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.data.TrustedContactEntity
import com.aimentalcare.app.databinding.DialogAddEditContactBinding
import com.aimentalcare.app.databinding.FragmentEmergencyContactsBinding
import kotlinx.coroutines.launch

class EmergencyContactsFragment : Fragment() {

    private var _binding: FragmentEmergencyContactsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EmergencyContactsAdapter
    private lateinit var db: MentalCareDatabase
    private var currentUserId: String = "user_default"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        db = MentalCareDatabase.getDatabase(ctx)

        val prefs = ctx.getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        setupRecyclerView()
        observeContacts()

        binding.btnAddContact.setOnClickListener {
            showAddEditContactDialog(null)
        }
    }

    private fun setupRecyclerView() {
        adapter = EmergencyContactsAdapter(
            emptyList(),
            onCall = { contact -> showCallConfirmation(contact) },
            onEdit = { contact -> showAddEditContactDialog(contact) },
            onDelete = { contact -> showDeleteConfirmation(contact) }
        )
        binding.recyclerContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerContacts.adapter = adapter
    }

    private fun observeContacts() {
        db.checkInDao().getTrustedContactsByUser(currentUserId).observe(viewLifecycleOwner) { list ->
            val b = _binding ?: return@observe
            adapter.updateList(list)
            if (list.isNullOrEmpty()) {
                b.layoutEmptyContacts.visibility = View.VISIBLE
                b.recyclerContacts.visibility = View.GONE
            } else {
                b.layoutEmptyContacts.visibility = View.GONE
                b.recyclerContacts.visibility = View.VISIBLE
            }
        }
    }

    private fun showAddEditContactDialog(existingContact: TrustedContactEntity?) {
        val dialogBinding = DialogAddEditContactBinding.inflate(layoutInflater)
        val isEdit = existingContact != null

        if (isEdit) {
            dialogBinding.txtDialogTitle.text = "Edit Emergency Contact"
            dialogBinding.etContactName.setText(existingContact?.contactName)
            dialogBinding.etContactPhone.setText(existingContact?.contactPhone)
            dialogBinding.etContactRelationship.setText(existingContact?.relationship)
        } else {
            dialogBinding.txtDialogTitle.text = "Add Emergency Contact"
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Save", null) // Set null here to override click and prevent premature dismiss on error
            .setNegativeButton("Cancel", null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = dialogBinding.etContactName.text.toString().trim()
                        val phone = dialogBinding.etContactPhone.text.toString().trim()
                        val rel = dialogBinding.etContactRelationship.text.toString().trim()
                        val cleanDigits = phone.replace(Regex("[^0-9]"), "")

                        if (name.isEmpty()) {
                            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (phone.isEmpty() || cleanDigits.length < 7) {
                            Toast.makeText(requireContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        lifecycleScope.launch {
                            try {
                                if (isEdit && existingContact != null) {
                                    val updated = existingContact.copy(
                                        contactName = name,
                                        contactPhone = phone,
                                        relationship = rel
                                    )
                                    db.checkInDao().updateTrustedContact(updated)
                                    com.aimentalcare.app.data.FirebaseSyncHelper.syncEmergencyContactToCloud(requireContext(), updated)
                                    Toast.makeText(requireContext(), "Contact updated successfully.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newContact = TrustedContactEntity(
                                        userId = currentUserId,
                                        contactName = name,
                                        contactPhone = phone,
                                        relationship = rel
                                    )
                                    val id = db.checkInDao().insertTrustedContact(newContact)
                                    com.aimentalcare.app.data.FirebaseSyncHelper.syncEmergencyContactToCloud(requireContext(), newContact.copy(contactId = id))
                                    Toast.makeText(requireContext(), "Emergency contact added successfully.", Toast.LENGTH_SHORT).show()
                                }
                                dismiss()
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Unable to save contact.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                show()
            }
    }

    private fun showDeleteConfirmation(contact: TrustedContactEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this emergency contact (${contact.contactName})?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        db.checkInDao().deleteTrustedContact(contact)
                        Toast.makeText(requireContext(), "Contact deleted.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error deleting contact.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCallConfirmation(contact: TrustedContactEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Emergency Call")
            .setMessage("Are you sure you want to call ${contact.contactName} (${contact.contactPhone})?")
            .setPositiveButton("Call") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.contactPhone}"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Unable to start the call.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
