package app.gyst.validation

import com.google.common.truth.Truth.assertThat

import org.junit.Test

class PasswordValidatorTest {

    private fun createValidator(
        password: String,
        rules: PasswordRules = PasswordRules()
    ): PasswordValidator = PasswordValidator(password, rules)

    @Test
    fun tooLong() {
        assertThat(createValidator("pas", rules = PasswordRules(maxSize = 4)).isTooLong).isFalse()
        assertThat(createValidator("password", rules = PasswordRules(maxSize = 4)).isTooLong).isTrue()
        assertThat(createValidator("password", rules = PasswordRules(maxSize = 20)).isTooLong).isFalse()
    }

    @Test
    fun tooShort() {
        assertThat(createValidator("pas", rules = PasswordRules(minSize = 6)).isTooShort).isTrue()
        assertThat(createValidator("password", rules = PasswordRules(minSize = 6)).isTooShort).isFalse()
        assertThat(createValidator("password", rules = PasswordRules(minSize = 10)).isTooShort).isTrue()
    }

    @Test
    fun contains_enough__lower_cased_values() {
        assertThat(createValidator("pASSWORD", rules = PasswordRules(lowerCount = 1)).enoughLowerCasedValues).isTrue()
        assertThat(createValidator("PASSWORD", rules = PasswordRules(lowerCount = 1)).enoughLowerCasedValues).isFalse()
        assertThat(createValidator("pASsWORD", rules = PasswordRules(lowerCount = 2)).enoughLowerCasedValues).isTrue()
        assertThat(createValidator("pASSWORD", rules = PasswordRules(lowerCount = 2)).enoughLowerCasedValues).isFalse()
    }

    @Test
    fun contains_enough__upper_cased_values() {
        assertThat(createValidator("pasSword", rules = PasswordRules(upperCount = 1)).enoughUpperCasedValues).isTrue()
        assertThat(createValidator("password", rules = PasswordRules(upperCount = 1)).enoughUpperCasedValues).isFalse()
        assertThat(createValidator("PassWord", rules = PasswordRules(upperCount = 2)).enoughUpperCasedValues).isTrue()
        assertThat(createValidator("passWord", rules = PasswordRules(upperCount = 2)).enoughUpperCasedValues).isFalse()
    }

    @Test
    fun contains_enough__digits() {
        assertThat(createValidator("pasSw0rd", rules = PasswordRules(numberCount = 1)).enoughDigits).isTrue()
        assertThat(createValidator("password", rules = PasswordRules(numberCount = 1)).enoughDigits).isFalse()
        assertThat(createValidator("PassW0rd1", rules = PasswordRules(numberCount = 2)).enoughDigits).isTrue()
        assertThat(createValidator("passW0rd", rules = PasswordRules(numberCount = 2)).enoughDigits).isFalse()
    }

    @Test
    fun contains_enough__special() {
        assertThat(createValidator("pa\$sword", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isTrue()
        assertThat(createValidator("password", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
        assertThat(createValidator("pa\$sword%", rules = PasswordRules(specialCount = 2)).enoughSpecialValues).isTrue()
        assertThat(createValidator("pa\$sword", rules = PasswordRules(specialCount = 2)).enoughSpecialValues).isFalse()

        assertThat(createValidator("password!", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isTrue()
        assertThat(createValidator("password@", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isTrue()
        assertThat(createValidator("password#", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isTrue()
        assertThat(createValidator("password$", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isTrue()
        assertThat(createValidator("password%", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isTrue()
        assertThat(createValidator("password^", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
        assertThat(createValidator("password&", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isTrue()
        assertThat(createValidator("password*", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
        assertThat(createValidator("password(", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
        assertThat(createValidator("password)", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
        assertThat(createValidator("password[", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
        assertThat(createValidator("password]", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
        assertThat(createValidator("password>", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
        assertThat(createValidator("password<", rules = PasswordRules(specialCount = 1)).enoughSpecialValues).isFalse()
    }

    @Test
    fun no_spaces() {
        assertThat("Pass% W0rd1".isValidPassword(PasswordRules())).isFalse()
        assertThat("Pass%W0rd1".isValidPassword(PasswordRules())).isTrue()
    }

    @Test
    fun password__ktx__isValid() {
        assertThat(
            "pa\$\$W0rd".isValidPassword(PasswordRules())
        ).isTrue()

    }

    @Test
    fun password__ktx__weaknesses() {
        assertThat("p".passwordWeaknesses(PasswordRules(lowerCount = 2))).isEqualTo(
            listOf(
                PasswordStrength.Length,
                PasswordStrength.Digits,
                PasswordStrength.LowerCased,
                PasswordStrength.UpperCased,
                PasswordStrength.SpecialValues
            )
        )

        assertThat("p".passwordWeaknesses(PasswordRules())).isEqualTo(
            listOf(
                PasswordStrength.Length,
                PasswordStrength.Digits,
                PasswordStrength.UpperCased,
                PasswordStrength.SpecialValues
            )
        )

        assertThat("password".passwordWeaknesses(PasswordRules())).isEqualTo(
            listOf(
                PasswordStrength.Digits,
                PasswordStrength.UpperCased,
                PasswordStrength.SpecialValues
            )
        )

        assertThat("passWord".passwordWeaknesses(PasswordRules())).isEqualTo(
            listOf(
                PasswordStrength.Digits,
                PasswordStrength.SpecialValues
            )
        )

        assertThat("passW0rd".passwordWeaknesses(PasswordRules())).isEqualTo(
            listOf(
                PasswordStrength.SpecialValues
            )
        )

        assertThat("pa\$sW0rd".passwordWeaknesses(PasswordRules())).isEmpty()
    }
}