package app.gyst.validation


data class PasswordRules(
    val lowerCount: Int = 1,
    val upperCount: Int = 1,
    val numberCount: Int = 1,
    val specialCount: Int = 1,
    val minSize: Int = 8,
    val maxSize: Int = 20,
    val specialChars: String = "!@#$%&"
)

/*
    (	            # Start of group
        (?=.*\d)		#   must contains one digit from 0-9
        (?=.*[a-z])		#   must contains one lowercase characters
        (?=.*[A-Z])		#   must contains one uppercase characters
        (?=.*[!@#$%&])	#   must contains one special symbols in the list "@#$%"
                .		#   match anything with previous condition checking
        {6,20}	        #   length at least 6 characters and maximum of 20
    )			    # End of group
 */

fun String.isValidPassword(passwordRules: PasswordRules): Boolean =
    PasswordValidator(this, passwordRules).isValid

fun String.passwordWeaknesses(passwordRules: PasswordRules): List<PasswordStrength> = PasswordValidator(this, passwordRules).weaknesses

class PasswordValidator(val password: String, val rules: PasswordRules) {
    private val lowerPattern: String get() = ".*(?=(.*[a-z]){${rules.lowerCount}}).*"
    private val upperPattern: String get() = ".*(?=(.*[A-Z]){${rules.upperCount}}).*"
    private val digitPattern: String get() = ".*(?=(.*[0-9]){${rules.numberCount}}).*"
    private val specialPattern: String get() = ".*(?=(.*[${rules.specialChars}]){${rules.specialCount}}).*"


    val isTooLong: Boolean
        get() = !Regex("[a-zA-Z0-9${rules.specialChars}]{0,${rules.maxSize}}").matches(
            password
        )
    val isTooShort: Boolean
        get() = !Regex("[a-zA-Z0-9${rules.specialChars}]{${rules.minSize},}").matches(
            password
        )

    val enoughLowerCasedValues: Boolean get() = Regex(lowerPattern).matches(password)
    val enoughUpperCasedValues: Boolean get() = Regex(upperPattern).matches(password)
    val enoughSpecialValues: Boolean get() = Regex(specialPattern).matches(password)
    val enoughDigits: Boolean get() = Regex(digitPattern).matches(password)

    private val validationPattern: String get() = "($digitPattern$specialPattern$lowerPattern$upperPattern[^\\s]{${rules.minSize},${rules.maxSize}})"
    val isValid: Boolean get() = Regex(validationPattern).matches(password)

    val weaknesses: List<PasswordStrength>
        get() = mutableListOf<PasswordStrength>().apply {
            if (isTooLong || isTooShort) add(PasswordStrength.Length)
            if (!enoughDigits) add(PasswordStrength.Digits)
            if (!enoughLowerCasedValues) add(PasswordStrength.LowerCased)
            if (!enoughUpperCasedValues) add(PasswordStrength.UpperCased)
            if (!enoughSpecialValues) add(PasswordStrength.SpecialValues)
        }
}
