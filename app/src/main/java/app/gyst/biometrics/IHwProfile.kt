package app.gyst.biometrics

interface IHwProfile {
    fun canAuthenticate(): Boolean
}