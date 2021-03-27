package com.example.wheelgame.activities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import kotlinx.android.synthetic.main.text_centered_toolbar.*
import com.example.wheelgame.R
import com.example.wheelgame.contract.ContractInterface.*
import com.example.wheelgame.models.Entry
import com.example.wheelgame.models.SpinWheelActivityInteractor
import com.example.wheelgame.presenters.MainPresenter
import com.example.wheelgame.views.WheelView
import kotlinx.android.synthetic.main.activity_spin_wheel.*

class SpinWheelActivity : AppCompatActivity(), ActivityView {

    private lateinit var mainPresenter: MainPresenter
    private lateinit var wheelView: WheelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spin_wheel)
        mainPresenter = MainPresenter(this, SpinWheelActivityInteractor())
        mainPresenter.getData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun showProgress() {
        //start loading animation
    }

    override fun hideProgress() {
        //stop loading animation
    }

    override fun setData(wheelValues: List<Entry>) {
        wheelViewContainer.removeAllViews()
        var wheelValuesMock: List<Entry> = ArrayList<Entry>()
        //test scalability/flexabiltiy of animation with additional mock values
        /**wheelValuesMock += Entry("1","$130", 130, "USD")
        wheelValuesMock += Entry("2","$150", 150, "USD")
        wheelValuesMock += Entry("3","$180", 180, "USD")
        wheelValuesMock += Entry("4","$5", 5, "USD")
        wheelValuesMock += Entry("5","$65", 65, "USD")
        wheelValuesMock += Entry("6","$45", 45, "USD")
        wheelValuesMock += Entry("7","$900", 900, "USD")
        wheelValuesMock += Entry("8","$50", 50, "USD")
        wheelValuesMock += Entry("9","$1", 1, "USD")
        wheelValuesMock += Entry("10","$500", 500, "USD")
        wheelView = WheelView(this, wheelValuesMock)**/

        wheelView = WheelView(this, wheelValues)
        wheelViewContainer.addView(wheelView)
        spinTheWheel.setOnClickListener{
            wheelView.startSpinning()
        }
    }

    override fun setDataError(strError: String) {
        val myToast = Toast.makeText(applicationContext,strError, Toast.LENGTH_LONG)
        myToast.setGravity(Gravity.CENTER_HORIZONTAL,200,200)
        myToast.show()
    }

    override fun initView() {
        setSupportActionBar(toolbar)
        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Spin the Wheel"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        // set center aligned title
        supportActionBar?.apply {
            toolbarTitle.text = "Spin the Wheel"
            //recenter text taking the back button into consideration, can also use text length to perfect center metrics
            toolbarTitle.updateLayoutParams<ConstraintLayout.LayoutParams> { horizontalBias = 0.35f }
        }
    }

}