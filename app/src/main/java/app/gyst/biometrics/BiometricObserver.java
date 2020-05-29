package app.gyst.biometrics;

import app.gyst.biometrics.model.BioMetricResult;

public interface BiometricObserver {

    void onBiometricResult(BioMetricResult bioMetricResult);

}
