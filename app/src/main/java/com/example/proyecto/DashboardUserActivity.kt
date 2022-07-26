package com.example.proyecto

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.proyecto.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var SliderArrayList: ArrayList<ModelImages>
    private lateinit var adapterSlider: AdapterSliderProduct
    private lateinit var viewPager: ViewPager2






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        loadSliderUser()


        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)


        //handle click, logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        //handle click, open profile
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }



    private fun loadSliderUser() {
        SliderArrayList = ArrayList()
        viewPager = binding.viewpagerImageSlider

        val ref = FirebaseDatabase.getInstance().getReference("Sliders").child("Images")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                SliderArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelImages::class.java)

                    SliderArrayList.add(model!!)
                }
                adapterSlider = AdapterSliderProduct(this@DashboardUserActivity,SliderArrayList)

                viewPager.adapter = adapterSlider
                viewPager.autoScroll(lifecycleScope, 3000)


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun ViewPager2.autoScroll(lifecyclerScope: LifecycleCoroutineScope, interval: Long){
        lifecyclerScope.launchWhenResumed {
            scrollIndefinitely(interval)
        }
    }
    private  suspend fun ViewPager2.scrollIndefinitely(interval:Long){
        delay(interval)
        val numberOfItems = adapterSlider?.itemCount ?:0
        val lastIndex = if (numberOfItems>0) numberOfItems-1 else 0
        val nextItem = if (currentItem==lastIndex) 0 else currentItem+1

        setCurrentItem(nextItem, true)
        scrollIndefinitely(interval)

    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager){
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager,
        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
        this
        )

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = ModelCategory("01", "Todos", 1,"")
                val modelMostViewed = ModelCategory("01", "Mas Vistos", 1,"")

                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                viewPagerAdapter.addFragment(
                    ProductsUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    ProductsUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )
                viewPagerAdapter.notifyDataSetChanged()

                for (ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)

                    viewPagerAdapter.addFragment(
                        ProductsUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fm, behavior){

        private val fragmentList: ArrayList<ProductsUserFragment> = ArrayList()
        private val fragmentTitleList : ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }
        public fun addFragment(fragment: ProductsUserFragment, title: String){

            fragmentList.add(fragment)
            fragmentTitleList.add(title)

        }
    }

    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //not logged in ,user can stay in user dashboard without login too
            binding.subTitleTv.text = "No has Iniciado Sesión"

            //Hide profile, logout
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE

        }
        else{
            //logged in , get and show user info
            val email = firebaseUser.email
            //set to textview of toolbar
            binding.subTitleTv.text = email

            //Hide profile, logout
            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
        }
    }


}