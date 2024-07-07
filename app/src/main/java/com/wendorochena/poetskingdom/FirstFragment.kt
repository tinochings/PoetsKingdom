package com.wendorochena.poetskingdom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.wendorochena.poetskingdom.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setNickname()
        val sharedPreferences = context?.getSharedPreferences("my_shared_pref", Context.MODE_PRIVATE)
        if (sharedPreferences?.getBoolean("firstUse", false) == false) {
            sharedPreferences.edit()?.putBoolean("firstUse",true)?.apply()
            onFirstUse()
        }
    }

    private fun onFirstUse() {
        val alertDialogBuilder = context?.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        alertDialogBuilder?.setTitle(R.string.guide_title)
            ?.setPositiveButton(R.string.builder_understood) { dialog, _ ->
                dialog.dismiss()
                binding.personalisationImage.performClick()
            }?.setMessage(R.string.guide_first_fragment)?.setOnDismissListener { binding.personalisationImage.performClick() }?.show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Method that defines all listeners for the main page
     */
    private fun setupListeners() {
        binding.myImages.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_FirstFragment_toMyImages)
        }
        binding.createPoemImage.setOnClickListener {
            startActivity(Intent(context, PoemThemeActivity::class.java))
        }

        binding.myPoemImage.setOnClickListener {
            startActivity(Intent(context, MyPoems::class.java))
        }

        binding.personalisationImage.setOnClickListener {
            startActivity(Intent(context, PersonalisationActivity::class.java))
        }
    }

    /**
     * Sets nickname value
     */
    private fun setNickname() {
        val nickname = activity?.getSharedPreferences(
            getString(R.string.personalisation_sharedpreferences_key),
            AppCompatActivity.MODE_PRIVATE
        )?.getString("appNickname", null)

        if (nickname != null) {
            binding.nicknameText?.text = nickname
        }
    }
}