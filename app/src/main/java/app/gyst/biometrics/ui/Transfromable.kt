package app.gyst.biometrics.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import app.gyst.R
import com.google.android.material.button.MaterialButton


class ViewBinder(val delegate: AppCompatDelegate) {
    val title: TextView? get() = delegate.findViewById(R.id.title)
    val subTitle: TextView? get() = delegate.findViewById(R.id.subtitle)
    val description: TextView? get() = delegate.findViewById(R.id.description)
    val stateIcon: ImageView? get() = delegate.findViewById(R.id.state_icon)
    val helpMessage: TextView? get() = delegate.findViewById(R.id.help_message)
    val positiveButton: MaterialButton? get() = delegate.findViewById(R.id.biometric_positive_button)
    val negativeButton: MaterialButton? get() = delegate.findViewById(R.id.biometric_negative_button)

}

interface State {
    fun render(viewBinder: ViewBinder) = V23State.Initial.render(viewBinder)
}

interface Transfromable {
    fun transformTo(state: State, viewBinder: ViewBinder) = state.render(viewBinder)
}