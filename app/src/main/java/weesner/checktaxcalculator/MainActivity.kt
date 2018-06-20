package weesner.checktaxcalculator

import android.content.Context
import android.content.res.Resources.Theme
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ThemedSpinnerAdapter
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.list_item.view.*
import weesner.tax_fetcher.*
import weesner.tax_fetcher.FederalIncomeTax.Companion.withholding
import weesner.tax_fetcher.FederalTaxModel.Companion.checkAmount
import weesner.tax_fetcher.FederalTaxModel.Companion.maritalStatus
import weesner.tax_fetcher.FederalTaxModel.Companion.payPeriodType
import weesner.tax_fetcher.FederalTaxModel.Companion.payrollAllowances

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Setup spinner
        spinner.adapter = MyAdapter(
                toolbar.context,
                arrayOf("Section 1", "Section 2", "Section 3"))

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private class MyAdapter(context: Context, objects: Array<String>) : ArrayAdapter<String>(context, R.layout.list_item, objects), ThemedSpinnerAdapter {
        private val mDropDownHelper: ThemedSpinnerAdapter.Helper = ThemedSpinnerAdapter.Helper(context)

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                val inflater = mDropDownHelper.dropDownViewInflater
                view = inflater.inflate(R.layout.list_item, parent, false)
            } else {
                view = convertView
            }

            view.text1.text = getItem(position)

            return view
        }

        override fun getDropDownViewTheme(): Theme? {
            return mDropDownHelper.dropDownViewTheme
        }

        override fun setDropDownViewTheme(theme: Theme?) {
            mDropDownHelper.dropDownViewTheme = theme
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        lateinit var federalTaxes: FederalTaxes
        var check: Double = 0.0
        var status: String = SINGLE
        var period: String = WEEKLY
        var allowances: Int = 0

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            rootView.textTaxes.text = getString(R.string.section_format, arguments?.getInt(ARG_SECTION_NUMBER))

            federalTaxes = getFederalTaxes(this.context!!, "2018")

            return rootView
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            tietCheck.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(editable: Editable?) {
                    try {
                        if (editable.isNullOrBlank()) {
                            textTaxes.text = "Medicare: 0.0\nSocial Security: 0.0\nFederal Income Tax: 0.0"
                        } else {
                            check = editable.toString().toDouble()
                            calculateTax()
                        }
                    } catch (e: NumberFormatException) {
                        textTaxes.text = "Medicare: 0.0\nSocial Security: 0.0\nFederal Income Tax: 0.0"
                    }
                }
            })

            spinnerMaritalStatus.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    status = spinnerMaritalStatus.getItemAtPosition(position).toString().toLowerCase()
                    Log.d("test", "marital status: $status")

                    calculateTax()
                }
            }

            spinnerPayPeriod.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    period = spinnerPayPeriod.getItemAtPosition(position).toString()
                    Log.d("test", "pay period: $period")

                    calculateTax()
                }
            }

            spinnerAllowances.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    allowances = spinnerAllowances.getItemAtPosition(position).toString().toInt()
                    Log.d("test", "pay period: $allowances")

                    calculateTax()
                }
            }
        }

        fun calculateTax() {
            if(check == 0.0) check = 0.00000001

            federalTaxes.apply {
                checkAmount = check
                maritalStatus = status
                payPeriodType = period
                payrollAllowances = allowances
            }

            val medicare = federalTaxes.medicare.amountOfCheck().toPercentage()
            val socialSecurity = federalTaxes.socialSecurity.amountOfCheck().toPercentage()
            val taxWithholding = federalTaxes.taxWithholding
            val federalIncomeTax = federalTaxes.federalIncomeTax

            federalIncomeTax.apply { withholding = taxWithholding }

            Log.d("test", "m: $medicare")
            Log.d("test", "ss: $socialSecurity")
            Log.d("test", "tw: ${taxWithholding.getIndividualCost()}")
            Log.d("test", "fit: ${federalIncomeTax.amountOfCheck()}")

            textTaxes.text = "Medicare: $medicare\n" +
                    "Social Security: $socialSecurity\n" +
                    "Federal Income Tax: ${federalIncomeTax.amountOfCheck()}"
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
