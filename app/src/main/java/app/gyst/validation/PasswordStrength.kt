package app.gyst.validation


sealed class PasswordStrength {
    object LowerCased: PasswordStrength()
    object UpperCased: PasswordStrength()
    object Digits: PasswordStrength()
    object SpecialValues: PasswordStrength()
    object Length: PasswordStrength()

    companion object {

        fun all():List<PasswordStrength> = listOf(
            LowerCased,
            UpperCased,
            Digits,
            SpecialValues,
            Length
        )
    }
}