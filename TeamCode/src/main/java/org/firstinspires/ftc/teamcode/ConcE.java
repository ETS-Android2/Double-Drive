package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

public enum ConcE {
    CONCURRENT, BLOCKING;

    @Override
    public String toString() {
        switch(this) {
            case CONCURRENT: return "CONCURRENT";
            case BLOCKING  : return "BLOCKING";
            default: return "";
        }
    }
}
