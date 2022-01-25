package ru.gb.course1.l1_mvp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// это должно лежать где-то в domain/entity пакете
data class EmployeeEntity(
    val name: String,
    val salary: Int
)

data class OneTimeError(
    private val error: Throwable
) {
    private var wasUsed = false
    fun getValue(): Throwable? = if (wasUsed) {
        null
    } else {
        wasUsed = true
        error
    }
}

(R)(Y)(G) = 8

// Хорошие состояния
(*)()()
()(*)()
()()(*)
()()()

// Плохие
(*)(*)(*)
(*)(*)()
()(*)(*)
(*)()(*)

interface EmployeesListContract {
    // MVI - Model View Intent
    sealed interface MviViewState {
        class IdleState: MviViewState
        data class LoadingState(val progress: Int): MviViewState
        data class ContentState(val employeeList: List<EmployeeEntity>): MviViewState
        data class ErrorState(val error: Throwable): MviViewState
    }

    sealed class MviAction {
        data class Error(
            val msg: String,
            val thr: Throwable
        )

        data class LicenseAlert(val html: String)
    }

    sealed interface MviEvent {
        data class ShowEmployee(val employeeEntity: EmployeeEntity) : MviEvent
        class LoadList : MviEvent
    }

    interface MviViewModel {
        val viewState: LiveData<MviViewState>
        val actions: LiveData<MviAction> // todo завернуть в SingleEventLiveData

        fun onEvent(event: MviEvent)
    }
}

class ViewModel : EmployeesListContract.MviViewModel {
    private var lastViewState: EmployeesListContract.MviViewState = EmployeesListContract.MviViewState.IdleState()

    private fun <T> LiveData<T>.mutable(): MutableLiveData<T> {
        return this as MutableLiveData<T>
    }

    override val viewState: LiveData<EmployeesListContract.MviViewState> = MutableLiveData()

    override val actions: LiveData<EmployeesListContract.MviAction>
        get() = TODO("Not yet implemented")


    override fun onEvent(event: EmployeesListContract.MviEvent) {
        when (event) {
            is EmployeesListContract.MviEvent.LoadList -> {
                onLoadList()
            }
            is EmployeesListContract.MviEvent.ShowEmployee -> {
                // todo
            }
        }
    }

    private fun onLoadList() {
        lastViewState = EmployeesListContract.MviViewState.LoadingState(0)
        viewState.mutable().postValue(lastViewState)
        api.loadUsers( // 30 sec
            onError = { error: Throwable ->
                lastViewState = EmployeesListContract.MviViewState.ErrorState(error)
                viewState.mutable().postValue(lastViewState)
            },
            onSuccess = { data: List<EmployeeEntity> ->
                lastViewState = EmployeesListContract.MviViewState.ContentState(data)
                viewState.mutable().postValue(lastViewState)
            }
        )
    }

}


class EmployeesView {
    private lateinit var context: Context
    private lateinit var viewModel: EmployeesListContract.MviViewModel

    fun onCreate() {
        // ...
        viewModel.onEvent(EmployeesListContract.MviEvent.LoadList())

        button.setOnClickListener { v ->
            viewModel.onEvent(EmployeesListContract.MviEvent.ShowEmployee())
        }

        viewModel.viewState.observe(this) { state -> render(state) }
    }

    private fun render(state: EmployeesListContract.MviViewState) {
        when (state) {
            is EmployeesListContract.MviViewState.ContentState -> renderContent(state)
            is EmployeesListContract.MviViewState.ErrorState -> renderError(state)
            is EmployeesListContract.MviViewState.IdleState -> renderIdle(state)
            is EmployeesListContract.MviViewState.LoadingState -> renderLoading(state)
        }
    }

    private fun renderLoading(state: EmployeesListContract.MviViewState.LoadingState) {
        errorView.isVisible = false

        progressView.isVisible = state.progress
    }

    private fun renderIdle(state: EmployeesListContract.MviViewState.IdleState) {
        progressView.isVisible = false
        errorView.isVisible = false
        // ничего
    }

    private fun renderError(state: EmployeesListContract.MviViewState.ErrorState) {
        progressView.isVisible = false

        errorView.isVisible = state.error
        errorTextView.text = state.error.message
    }

    private fun renderContent(state: EmployeesListContract.MviViewState.ContentState) {
        progressView.isVisible = false
        errorView.isVisible = false

        adapter.setData(state.employeeList)
    }


}