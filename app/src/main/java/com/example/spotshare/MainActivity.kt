package com.example.spotshare

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotshare.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PostAdapter

    private val allPosts = mutableListOf<Post>()
    private val allPostIds = mutableListOf<String>()

    private val posts = mutableListOf<Post>()
    private val postIds = mutableListOf<String>()

    private lateinit var database: DatabaseReference
    private var currentCategoryFilter = "הכל"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categories = listOf("הכל", "חברים", "דייטים", "משפחה")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoryFilter.adapter = categoryAdapter

        binding.spinnerCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentCategoryFilter = parent?.getItemAtPosition(position).toString()
                filterPosts()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        adapter = PostAdapter(posts, postIds)
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPosts.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("posts")
        loadPostsRealtime()

        binding.bottomNavigation.selectedItemId = R.id.nav_home
        BottomNavigationViewHelper.setup(binding.bottomNavigation, this)
    }

    private fun loadPostsRealtime() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allPosts.clear()
                allPostIds.clear()

                val sortedChildren = snapshot.children
                    .mapNotNull { it to it.getValue(Post::class.java) }
                    .filter { it.second != null }
                    .sortedByDescending { it.second?.timestamp ?: 0L }

                for ((postSnapshot, post) in sortedChildren) {
                    val postId = postSnapshot.key
                    if (postId != null && post != null) {
                        allPosts.add(post)
                        allPostIds.add(postId)
                    }
                }


                filterPosts()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterPosts() {
        val filteredPosts: List<Post>
        val filteredIds: List<String>

        if (currentCategoryFilter == "הכל") {
            filteredPosts = allPosts
            filteredIds = allPostIds
        } else {
            filteredPosts = allPosts.filter { it.category == currentCategoryFilter }
            filteredIds = allPostIds.mapIndexedNotNull { index, id ->
                if (allPosts[index].category == currentCategoryFilter) id else null
            }
        }

        posts.clear()
        postIds.clear()
        posts.addAll(filteredPosts)
        postIds.addAll(filteredIds)

        adapter.notifyDataSetChanged()
    }
}
