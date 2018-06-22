package weesner.checktaxcalculator

import android.content.Context
import android.text.Editable
import androidx.core.content.edit
import weesner.tax_fetcher.*

class CalculatedTaxesObject(val context: Context) {
    private var check: Double = 0.0
    var maritalStatus: String = SINGLE
    var payrollAllowances: Int = 0
    var payPeriod: String = WEEKLY

    private var medicare: Double = 0.0
    private var socialSecurity: Double = 0.0
    private var federalIncomeTax: Double = 0.0

    private var federalTaxesObject: FederalTaxes? = null

    fun updateCheck(editable: Editable?) {
        try {
            check =
                    if (!editable.isNullOrBlank()) editable.toString().toDouble()
                    else 0.0
        } catch (e: NumberFormatException) {
            println("nfe happened: ${e.message}")
            check = 0.0
            zero()
        }

        calculateTax()
    }

    fun updateAllowances(allowances: Int) {
        userPrefs(context).edit { putInt("allowances", allowances) }
    }

    fun updatePayPeriod(payPeriod: String) {
        userPrefs(context).edit { putString("payPeriod", payPeriod) }
    }

    fun updateMaritalStatus(status: String) {
        userPrefs(context).edit { putString("maritalStatus", status) }
    }

    fun getFromPrefs() {
        val prefs = userPrefs(context)
        maritalStatus = prefs.getString("maritalStatus", SINGLE)
        payPeriod = prefs.getString("payPeriod", WEEKLY)
        payrollAllowances = prefs.getInt("allowances", 0)
    }

    private fun calculateTax() {
        println("calculateTax called $check")
        getFromPrefs()

        if (check != 0.0) {
            if (federalTaxesObject == null) federalTaxesObject = getFederalTaxes(context, "2018")
            federalTaxesObject.apply {
                FederalTaxModel.checkAmount = check
                FederalTaxModel.maritalStatus = maritalStatus
                FederalTaxModel.payPeriodType = payPeriod
                FederalTaxModel.payrollAllowances = payrollAllowances
            }

            medicare = federalTaxesObject!!.medicare.amountOfCheck()
            socialSecurity = federalTaxesObject!!.socialSecurity.amountOfCheck()
            val taxWithholding = federalTaxesObject!!.taxWithholding
            federalIncomeTax = federalTaxesObject!!.federalIncomeTax.apply {
                FederalIncomeTax.withholding = taxWithholding
            }.amountOfCheck()
        } else {
            zero()
        }
    }

    private fun zero() {
        println("zero called")

        val zero = 0.0

        medicare = zero
        socialSecurity = zero
        federalIncomeTax = zero
    }

    fun readout(): String {
        println("readout called")
        return "Medicare: $medicare\n" +
                "Social Security: $socialSecurity\n" +
                "Federal Income Tax: $federalIncomeTax"
    }
}