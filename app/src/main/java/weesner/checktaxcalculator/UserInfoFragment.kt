package weesner.checktaxcalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_user_info.*

class UserInfoFragment : Fragment() {
    companion object {
        fun newInstance(): UserInfoFragment {
            val fragment = UserInfoFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_user_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taxInfo = CalculatedTaxesObject(context!!)
        taxInfo.getFromPrefs()

        val maritalStatus = taxInfo.maritalStatus.replace(taxInfo.maritalStatus[0], taxInfo.maritalStatus[0].toUpperCase())

        spinnerMaritalStatus.setSelection((spinnerMaritalStatus.adapter as ArrayAdapter<String>).getPosition(maritalStatus))
        spinnerPayPeriod.setSelection((spinnerPayPeriod.adapter as ArrayAdapter<String>).getPosition(taxInfo.payPeriod))
        spinnerAllowances.setSelection(taxInfo.payrollAllowances)

        spinnerMaritalStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                taxInfo.updateMaritalStatus(spinnerMaritalStatus.getItemAtPosition(position).toString().toLowerCase())
            }
        }

        spinnerPayPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                taxInfo.updatePayPeriod(spinnerPayPeriod.getItemAtPosition(position).toString())
            }
        }

        spinnerAllowances.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                taxInfo.updateAllowances(spinnerAllowances.getItemAtPosition(position).toString().toInt())
            }
        }
    }
}