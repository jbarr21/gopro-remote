package com.github.jbarr21.goproremote.ui;

import android.os.Bundle;

public interface AmbientModeChangedListener {
    void onEnterAmbient(Bundle ambientDetails);
    void onExitAmbient();
}
