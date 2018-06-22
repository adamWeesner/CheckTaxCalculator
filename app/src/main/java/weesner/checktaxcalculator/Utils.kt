package weesner.checktaxcalculator

import android.content.Context
import android.content.SharedPreferences

fun userPrefs(context: Context): SharedPreferences = context.getSharedPreferences("weesner.checktaxcalculator.user_prefs", Context.MODE_PRIVATE)