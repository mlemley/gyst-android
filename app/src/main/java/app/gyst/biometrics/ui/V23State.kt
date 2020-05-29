package app.gyst.biometrics.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import app.gyst.R
import app.gyst.biometrics.ktx.hide
import app.gyst.biometrics.ktx.show
import kotlin.math.roundToInt

sealed class V23State : State, Transfromable {


    object Initial : V23State() {

        override fun render(viewBinder: ViewBinder) {
            viewBinder.stateIcon?.apply {
                colorFilter = null
                setImageResource(R.drawable.fingerprint)
            }

            viewBinder.helpMessage?.hide()

            viewBinder.positiveButton?.apply {
                text = resources.getText(R.string.biometric_retry)
                isEnabled = true
            }
        }

        override fun transformTo(state: State, viewBinder: ViewBinder) {
            viewBinder.stateIcon?.tag = null
            state.render(viewBinder)
        }

    }

    object Capturing : V23State() {
        override fun render(viewBinder: ViewBinder) {
            viewBinder.positiveButton?.apply {
                text = resources.getText(R.string.biometric_prompt_positive_button_text)
                isEnabled = false
            }

            viewBinder.stateIcon?.apply {

                fun adjustAlpha(color: Int, factor: Float): Int {
                    val alpha = (Color.alpha(color) * factor).roundToInt()
                    val red = Color.red(color)
                    val green = Color.green(color)
                    val blue = Color.blue(color)
                    return Color.argb(alpha, red, green, blue)
                }

                val scanColor = resources.getColor(R.color.fingerprint_end, context.theme)

                setImageResource(R.drawable.fingerprint)
                val animator = ValueAnimator.ofFloat(0F, 1F)
                animator.addUpdateListener { animation ->

                    val value: Float = animation.animatedValue as Float
                    val alpha = adjustAlpha(scanColor, value)
                    this.setColorFilter(alpha, PorterDuff.Mode.SRC_ATOP)
                    if (value == 0.0F) {
                        this.colorFilter = null
                    }
                }
                animator.duration = 500
                animator.repeatMode = ValueAnimator.REVERSE
                animator.repeatCount = -1
                animator.start()
                tag = animator
            }

        }

        override fun transformTo(state: State, viewBinder: ViewBinder) {
            viewBinder.stateIcon?.apply {
                tag?.let { (it as Animator).cancel() }
                tag = null
                state.render(viewBinder)
            }
        }
    }


    object Captured : V23State() {
        override fun render(viewBinder: ViewBinder) {
            viewBinder.positiveButton?.apply {
                text = resources.getText(R.string.biometric_prompt_positive_button_text)
                isEnabled = true
            }
        }
    }

    object Failed : V23State() {
        override fun render(viewBinder: ViewBinder) {
            viewBinder.stateIcon?.apply {
                setImageResource(R.drawable.error)
                this.setColorFilter(
                    resources.getColor(
                        R.color.biometric_capture_error,
                        context.theme
                    ), PorterDuff.Mode.SRC_ATOP
                )
            }

            viewBinder.helpMessage?.apply {
                show()
                text = resources.getText(R.string.biometric_auth_failure_message)
                setTextColor(resources.getColor(R.color.biometric_capture_error, context.theme))
            }

            viewBinder.positiveButton?.apply {
                text = resources.getText(R.string.biometric_retry)
                isEnabled = true
            }
        }
    }
}
