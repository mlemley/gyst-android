package app.gyst.app

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import app.gyst.R
import app.gyst.common.showSnackBar
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton

fun CircularProgressButton.revert(): CircularProgressButton = this.also {
    revertAnimation()
}

fun CircularProgressButton.onSuccess(): CircularProgressButton = this.also {
    this.doneLoadingAnimation(
        ContextCompat.getColor(context, R.color.colorAccent),
        ContextCompat.getDrawable(context, R.drawable.ic_progress_success)
            ?.toBitmap(this.width, this.height) as Bitmap
    )
}

fun CircularProgressButton.revertWithSnackBarMessage(@StringRes messageId: Int) = apply {
    revertAnimation()
    showSnackBar(messageId)
}
