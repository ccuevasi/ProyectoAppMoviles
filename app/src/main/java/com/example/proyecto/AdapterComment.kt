package com.example.proyecto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto.databinding.RowCommentBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment:RecyclerView.Adapter<AdapterComment.HolderComment> {
    val context: Context

    val commentArrayList: ArrayList<ModelComment>

    private lateinit var binding: RowCommentBinding

    private  var firebaseAuth: FirebaseAuth



    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList


        firebaseAuth = FirebaseAuth.getInstance()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent,false)
            return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        val model = commentArrayList[position]

        val id = model.id
        val productId = model.productId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        val date = MyApplication.formatTimeStamp(timestamp.toLong())

        holder.dateTv.text = date
        holder.commentTv.text = comment

        loadUserDetails(model, holder)
        showComments()

        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid){
                deletedCommentDialog(model, holder)
            }
        }
    }

    private fun showComments() {

    }

    private fun deletedCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar Comentario")
            .setMessage("Estas seguro de eliminar este comentario?")
            .setPositiveButton("ELIMINAR"){d, e->

                val productId = model.productId
                val commentId = model.id

                   val ref = FirebaseDatabase.getInstance().getReference("Products")
                ref.child(productId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Comentario Eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Fallo al eliminar comentario debido a ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("CANCELAR"){d, e->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    holder.nameTv.text = name
                    try {
                       Glide.with(context)
                           .load(profileImage)
                           .placeholder(R.drawable.ic_person_gray)
                           .into(holder.profileIv)
                    }
                    catch (e: Exception){
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    inner class HolderComment(itemView: View): RecyclerView.ViewHolder(itemView){
        val profileIv: ImageView = binding.profileIv
        val nameTv: TextView = binding.nameTV
        val dateTv: TextView = binding.dateTv
        val commentTv : TextView = binding.commenTv

    }
}