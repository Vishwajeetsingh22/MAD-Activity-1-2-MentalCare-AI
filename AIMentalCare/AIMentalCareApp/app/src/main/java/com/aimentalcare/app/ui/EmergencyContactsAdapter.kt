package com.aimentalcare.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aimentalcare.app.data.TrustedContactEntity
import com.aimentalcare.app.databinding.ItemEmergencyContactBinding

class EmergencyContactsAdapter(
    private var contacts: List<TrustedContactEntity>,
    private val onCall: (TrustedContactEntity) -> Unit,
    private val onEdit: (TrustedContactEntity) -> Unit,
    private val onDelete: (TrustedContactEntity) -> Unit
) : RecyclerView.Adapter<EmergencyContactsAdapter.ContactViewHolder>() {

    fun updateList(newList: List<TrustedContactEntity>) {
        this.contacts = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemEmergencyContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    inner class ContactViewHolder(private val binding: ItemEmergencyContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: TrustedContactEntity) {
            binding.txtContactName.text = contact.contactName
            binding.txtContactPhone.text = contact.contactPhone
            binding.txtRelationshipBadge.text = if (contact.relationship.isNotBlank()) contact.relationship else "Contact"

            binding.btnCallContact.setOnClickListener { onCall(contact) }
            binding.btnEditContact.setOnClickListener { onEdit(contact) }
            binding.btnDeleteContact.setOnClickListener { onDelete(contact) }
        }
    }
}
