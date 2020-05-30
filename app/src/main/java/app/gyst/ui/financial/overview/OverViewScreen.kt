package app.gyst.ui.financial.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import app.gyst.databinding.FragmentFinancialOverviewBinding
import org.koin.android.viewmodel.ext.android.viewModel

class OverViewScreen : Fragment() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var binder: FragmentFinancialOverviewBinding

    private val financialOverviewViewModel: FinancialOverviewViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binder = FragmentFinancialOverviewBinding.inflate(inflater)
        return binder.root
    }

}