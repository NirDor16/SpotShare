package com.example.spotshare

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

interface OnPostEditListener {
    fun onEdit(post: Post, postId: String)
}

class PostAdapter(
    private val posts: List<Post>,
    private val postIds: List<String>,
    private val editListener: OnPostEditListener? = null
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnMapReadyCallback {
        val image: ImageView = itemView.findViewById(R.id.post_IMG_image)
        val placeName: TextView = itemView.findViewById(R.id.post_LBL_place)
        val description: TextView = itemView.findViewById(R.id.post_LBL_description)
        val category: TextView = itemView.findViewById(R.id.post_LBL_category)
        val editButton: Button = itemView.findViewById(R.id.post_BTN_edit)
        val likeButton: ImageView = itemView.findViewById(R.id.post_IMG_like)
        val mapView: MapView = itemView.findViewById(R.id.post_MAP_view)
        val showMapButton: Button = itemView.findViewById(R.id.post_BTN_show_map)

        private var map: GoogleMap? = null
        private var latitude: Double? = null
        private var longitude: Double? = null

        fun bindMap(lat: Double?, lng: Double?) {
            if (lat != null && lng != null) {
                latitude = lat
                longitude = lng
                mapView.visibility = View.VISIBLE
                mapView.onCreate(null)
                mapView.getMapAsync(this)
            } else {
                mapView.visibility = View.GONE
            }
        }

        override fun onMapReady(googleMap: GoogleMap) {
            map = googleMap
            map?.uiSettings?.setAllGesturesEnabled(false)
            val location = LatLng(latitude!!, longitude!!)
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))
            map?.addMarker(MarkerOptions().position(location).title("מיקום"))
        }

        fun onResumeMap() {
            mapView.onResume()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val postId = postIds[position]

        holder.placeName.text = post.placeName
        holder.description.text = post.description
        holder.category.text = post.category

        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .override(800, 600)
            .centerCrop()
            .into(holder.image)

        val liked = currentUserId != null && post.likedBy.contains(currentUserId)
        holder.likeButton.setImageResource(
            if (liked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        holder.likeButton.setOnClickListener {
            toggleLike(postId, liked)
        }

        if (editListener != null) {
            holder.editButton.visibility = View.VISIBLE
            holder.editButton.setOnClickListener {
                editListener.onEdit(post, postId)
            }
            holder.mapView.visibility = View.GONE
            holder.showMapButton.visibility = View.GONE
        } else {
            holder.editButton.visibility = View.GONE
            holder.bindMap(post.latitude, post.longitude)
            holder.showMapButton.visibility = View.VISIBLE

            holder.showMapButton.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, MapActivity::class.java)
                intent.putExtra("latitude", post.latitude ?: 0.0)
                intent.putExtra("longitude", post.longitude ?: 0.0)
                intent.putExtra("placeName", post.placeName ?: "מיקום")
                context.startActivity(intent)
            }
        }

        holder.onResumeMap()
    }

    private fun toggleLike(postId: String, currentlyLiked: Boolean) {
        val ref = FirebaseDatabase.getInstance().getReference("posts").child(postId).child("likedBy")
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentList = mutableData.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()
                val updatedList = if (currentlyLiked) {
                    currentList.filter { it != currentUserId }
                } else {
                    currentList + currentUserId
                }
                mutableData.value = updatedList
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {}
        })
    }

    override fun getItemCount(): Int = posts.size
}
