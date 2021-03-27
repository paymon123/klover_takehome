package com.example.wheelgame.presenters


import com.example.wheelgame.contract.ContractInterface.*
import com.example.wheelgame.models.Entry

//works with interchangeable views (ActivityView) and models (Interactor)
class MainPresenter(private var mainView: ActivityView?, private val mainInteractor: Interactor): Presenter, OnAPICompleteListener {

    init {
        mainView?.initView()
    }
    override fun getData() {
        mainView?.showProgress()
        mainInteractor.requestGetDataAPI(this)
    }

    override fun onResultSuccess(wheelValues: List<Entry>) {
        mainView?.hideProgress()
        mainView?.setData(wheelValues)
    }

    override fun onResultFail(strError: String) {
        mainView?.hideProgress()
        mainView?.setDataError(strError)
    }

    override fun onDestroy() {
        mainView = null
    }

}