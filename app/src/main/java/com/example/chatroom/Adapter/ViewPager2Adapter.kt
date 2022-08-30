package com.example.chatroom.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager2Adapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val fragments = ArrayList<Fragment>()
    override fun getItemCount() = fragments.size

    fun addFragment(f: Fragment) {
        fragments.add(f)
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}