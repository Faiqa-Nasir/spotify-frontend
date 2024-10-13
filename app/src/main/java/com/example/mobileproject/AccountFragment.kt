package com.example.mobileproject
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        setupButtonListeners(view)
        initializeTextViews(view)

        return view
    }

    private fun setupButtonListeners(view: View) {
        /*val playlistsButton: Button = view.findViewById(R.id.playlists_button)
        playlistsButton.setOnClickListener {
            navigateToFragment(PlaylistFragment())
        }*/

        val followedArtistsButton: Button = view.findViewById(R.id.followed_artists_button)
        followedArtistsButton.setOnClickListener {
            navigateToFragment(HomeFragment())
        }
    }

    private fun initializeTextViews(view: View) {
        usernameTextView = view.findViewById(R.id.username)
        emailTextView = view.findViewById(R.id.email)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            usernameTextView.text = user.displayName ?: "Username"
            emailTextView.text = user.email ?: "Email"
        } else {
            usernameTextView.text = "Username"
            emailTextView.text = "Email"
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutButton = view.findViewById<Button>(R.id.logout_button)

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
