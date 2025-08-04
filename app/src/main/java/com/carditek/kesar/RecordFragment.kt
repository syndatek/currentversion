package com.carditek.kesar

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.carditek.kesar.databinding.FragmentRecordBinding
import com.carditek.kesar.module.Patient
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordFragment : Fragment() {
    @Inject
    lateinit var patient: Patient

    @Inject
    lateinit var device: Device

    private val intent = Intent(
        Intent.ACTION_PICK,
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRecordBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.patient = patient
        binding.device = device
        val view = binding.root

        view.findViewById<Button>(R.id.select_or_clear_patient).apply {
            setOnClickListener {
                if (patient.empty.value!!)
                    startActivityForResult(intent, REQUEST_CONTACT)
                else
                    patient.clear()
            }
        }
        view.findViewById<Button>(R.id.record_or_stop).setOnClickListener {
            device.setRecording(!device.recording.value!!)
        }
        return view
    }

    @SuppressLint("Range")
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        when {
            result != RESULT_OK -> return
            request == REQUEST_CONTACT && data != null -> {
                data.data?.let {
                    requireActivity().contentResolver.query(it, null, null, null, null)
                        .use { cursor ->
                            cursor?.use {
                                if (cursor.count == 0) return
                                cursor.moveToFirst()
                                val name = cursor.getString(
                                    cursor.getColumnIndex(
                                        ContactsContract.Contacts.DISPLAY_NAME
                                    )
                                )
                                val phone = cursor.getString(
                                    cursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER
                                    )
                                )

                                // TODO(vjn): find a better way than duplicating these checks.
                                if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
                                    Snackbar.make(
                                        this.requireView(),
                                        "Invalid phone number: '$phone'",
                                        Snackbar.LENGTH_LONG
                                    ).setAction("Action", null).show()

                                } else if (name.length < 3) {
                                    Snackbar.make(
                                        this.requireView(),
                                        "Name should be at least 3 letters: '$name'",
                                        Snackbar.LENGTH_LONG
                                    ).setAction("Action", null).show()
                                } else {
                                    patient.set(name, phone)
                                }

                            }
                        }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CONTACT = 1001
    }
}



