package com.example.wheelgame.contract
import com.example.wheelgame.models.Entry

interface ContractInterface {

    interface ActivityView {
        fun showProgress()
        fun hideProgress()
        fun setData(wheelValues: List<Entry>)
        fun setDataError(strError: String)
        fun initView()
    }

    interface Presenter {
        fun getData()
        fun onDestroy()
    }

    interface OnAPICompleteListener {
        fun onResultSuccess(wheelValues: List<Entry>)
        fun onResultFail(strError: String)
    }

    interface Interactor {
        fun requestGetDataAPI(onAPICompleteListener: OnAPICompleteListener)
    }

}
