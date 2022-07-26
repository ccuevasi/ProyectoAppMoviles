package com.example.proyecto

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.proyecto.databinding.ActivitySliderBinding
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class SliderActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySliderBinding
    private lateinit var ImageArrayList: ArrayList<ModelImages>
    private var databaseReference: DatabaseReference? = null
    private val PICK_IMG = 1
    private val ImageList = ArrayList<Uri>()
    private var uploads = 0
    private lateinit var adapterImage: AdapterImage
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySliderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProductImage()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Sliders")
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Porfavor Espere")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.addSlider.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, PICK_IMG)
        }
        binding.submitBtn.setOnClickListener {
            progressDialog.setMessage("Subiendo Imagenes")
            progressDialog!!.show()
            val ImageFolder = FirebaseStorage.getInstance().reference.child("Sliders")
            uploads = 0
            while (uploads < ImageList.size) {
                val Image = ImageList[uploads]
                val imagename = ImageFolder.child("image/" + Image.lastPathSegment)
                imagename.putFile(ImageList[uploads]).addOnSuccessListener {
                    imagename.downloadUrl.addOnSuccessListener { uri ->
                        val url = uri.toString()
                        SendLink(url)
                    }
                }
                uploads++
            }
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadProductImage() {
        ImageArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Sliders").child("Images")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                ImageArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelImages::class.java)

                    ImageArrayList.add(model!!)
                }
                adapterImage = AdapterImage(this@SliderActivity,ImageArrayList)

                binding.imagesRv.adapter = adapterImage

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMG) {
            if (resultCode == RESULT_OK) {
                if (data!!.clipData != null) {
                    val count = data.clipData!!.itemCount
                    var CurrentImageSelect = 0
                    while (CurrentImageSelect < count) {
                        val imageuri = data.clipData!!.getItemAt(CurrentImageSelect).uri
                        ImageList.add(imageuri)
                        CurrentImageSelect = CurrentImageSelect + 1
                    }

                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun SendLink(url: String) {
        val hashMap = HashMap<String, String>()
        hashMap["link"] = url

        databaseReference!!.child("Images").push()
            .setValue(hashMap).addOnCompleteListener {
                progressDialog!!.dismiss()
                Toast.makeText(this, "Imagenes Subidas", Toast.LENGTH_SHORT).show()

                ImageList.clear()
            }
    }


}