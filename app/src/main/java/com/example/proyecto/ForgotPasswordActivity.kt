package com.example.proyecto

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.proyecto.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Porfavor Espere")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }
    private var email = ""
    private fun validateData() {

        email = binding.emailEt.text.toString().trim()

        if (email.isEmpty()){
            Toast.makeText(this,"Ingrese su correo..",Toast.LENGTH_SHORT).show()
        }
        else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Patron de Correo Invalido..",Toast.LENGTH_SHORT).show()
        }
        else{
            recoveryPassword()
        }
    }

    private fun recoveryPassword() {
        progressDialog.setMessage("Enviando intrucciones para recuperar su contraseña")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Las Instrucciones Fueron enviadas a \n$email",Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this,"Fallo al enviar debido a  ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
}