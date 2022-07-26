package com.example.proyecto

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var productsArrayList: ArrayList<ModelProduct>
    private lateinit var adapterProductFavorite: AdapterProductFavorite
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favoriteProductCountTv.text = "N/A"
        binding.StatusAccountTv.text = "N/A"

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Porfavor Espere..!")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadFavoriteProducts()
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ProfileEditActivity::class.java))
        }

        binding.StatusAccountTv.setOnClickListener {
            if (firebaseUser.isEmailVerified){
                Toast.makeText(this, "Estas Verificado", Toast.LENGTH_SHORT).show()
            }
            else{
                emailVerificationDialog()
            }
        }

    }

    private fun emailVerificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verificar Email")
            .setMessage("Quieres que te enviemos un email con intrucciones a ${firebaseUser.email} para que puedas verificar tu cuenta?")
            .setPositiveButton("SI"){d, e->
                sendEmailVerification()
            }
            .setNegativeButton("NO"){d, e->
                d.dismiss()
            }
            .show()
    }

    private fun sendEmailVerification() {
        progressDialog.setMessage("Enviando las intrucciones para verificacion de su correo a ${firebaseUser.email} ")
        progressDialog.show()

        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Instrucciones enviada! Revisa tu email ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al enviar las intrucciones debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        if (firebaseUser.isEmailVerified){
            binding.StatusAccountTv.text = "Verificado"
        }
        else{
            binding.StatusAccountTv.text = "No Verificado"
        }



        val ref  = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    binding.nameTV.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType

                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    }catch (e: Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadFavoriteProducts(){
        productsArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    productsArrayList.clear()
                    for (ds in snapshot.children){
                        val productId = "${ds.child("productId").value}"

                        val modelProduct = ModelProduct()
                        modelProduct.id = productId

                        productsArrayList.add(modelProduct)
                    }
                    binding.favoriteProductCountTv.text = "${productsArrayList.size}"
                    adapterProductFavorite = AdapterProductFavorite(this@ProfileActivity, productsArrayList)
                    binding.favoriteRv.adapter = adapterProductFavorite
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}