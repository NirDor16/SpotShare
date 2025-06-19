package com.example.spotshare

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotshare.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var adapter: PostAdapter
    private val userPosts = mutableListOf<Post>()
    private val userPostIds = mutableListOf<String>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostAdapter(userPosts, userPostIds, object : OnPostEditListener {
            override fun onEdit(post: Post, postId: String) {
                openEditPostActivity(postId, post)
            }
        })

        binding.profileRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.profileRecyclerView.adapter = adapter

        binding.bottomNavigation.selectedItemId = R.id.nav_profile
        BottomNavigationViewHelper.setup(binding.bottomNavigation, this)

        binding.profileBTNLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        loadUserPosts()
    }

    private fun loadUserPosts() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid
        database = FirebaseDatabase.getInstance().getReference("posts")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempPosts = mutableListOf<Pair<Post, String>>()

                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    val postId = postSnapshot.key
                    if (post?.userId == userId && postId != null) {
                        tempPosts.add(post to postId)
                    }
                }

                val sorted = tempPosts.sortedByDescending { it.first.timestamp ?: 0L }

                userPosts.clear()
                userPostIds.clear()
                userPosts.addAll(sorted.map { it.first })
                userPostIds.addAll(sorted.map { it.second })

                binding.profileRecyclerView.visibility =
                    if (userPosts.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun openEditPostActivity(postId: String, post: Post) {
        val intent = Intent(this, EditPostActivity::class.java).apply {
            putExtra("postId", postId)
            putExtra("placeName", post.placeName)
            putExtra("description", post.description)
            putExtra("category", post.category)
        }
        startActivity(intent)
    }
}
