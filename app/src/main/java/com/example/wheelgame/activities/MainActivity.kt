package com.example.wheelgame.activities
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.wheelgame.R
import com.example.wheelgame.contract.ContractInterface.*
import com.example.wheelgame.models.Entry
import com.example.wheelgame.models.MainActivityInteractor
import com.example.wheelgame.presenters.MainPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.text_centered_toolbar.*

class MainActivity : AppCompatActivity(), ActivityView {

    private lateinit var mainPresenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainPresenter = MainPresenter(this, MainActivityInteractor())
        mainPresenter.getData()
    }

    override fun showProgress() {
        //loading visual
    }

    override fun hideProgress() {
        //hide loading visual
    }

    override fun setData(wheelValues: List<Entry>) {
        //no data in this activity
    }

    override fun setDataError(strError: String) {
        //error message
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        // set center aligned title
        supportActionBar?.apply {
            toolbarTitle.text = "Wheel Game"
        }
        switchActivities.setOnClickListener{
            val intent = Intent(this, SpinWheelActivity::class.java)
            startActivity(intent)
        }
    }

}