package com.example.spotshare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotshare.databinding.ActivityFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: PostAdapter
    private val favoritePosts = mutableListOf<Post>()
    private val favoritePostIds = mutableListOf<String>()
    private lateinit var database: DatabaseReference
    private lateinit var likesRef: DatabaseReference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter(favoritePosts, favoritePostIds)
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFavorites.adapter = adapter

        binding.bottomNavigation.selectedItemId = R.id.nav_favorites
        BottomNavigationViewHelper.setup(binding.bottomNavigation, this)

        database = FirebaseDatabase.getInstance().getReference("posts")
        likesRef = FirebaseDatabase.getInstance().getReference("posts")

        loadFavorites()
    }

    private fun loadFavorites() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempPosts = mutableListOf<Pair<Post, String>>()

                for (postSnap in snapshot.children) {
                    val post = postSnap.getValue(Post::class.java)
                    val postId = postSnap.key ?: continue
                    val likedBy = post?.likedBy ?: emptyList()

                    if (post != null && currentUserId in likedBy) {
                        tempPosts.add(post to postId)
                    }
                }

                // מיון לפי זמן בירידה
                val sorted = tempPosts.sortedByDescending { it.first.timestamp ?: 0L }

                favoritePosts.clear()
                favoritePostIds.clear()
                favoritePosts.addAll(sorted.map { it.first })
                favoritePostIds.addAll(sorted.map { it.second })

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}
