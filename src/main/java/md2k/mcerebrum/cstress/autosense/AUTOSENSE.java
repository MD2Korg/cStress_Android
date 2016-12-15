package md2k.mcerebrum.cstress.autosense;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Static enumerations and configurable constants for the AutoSense platform
 */
public class AUTOSENSE {

    //Autosense hardware-specific constants
    public static final int CHEST_ECG = 0;
    public static final int CHEST_ACCEL_X = 1;
    public static final int CHEST_ACCEL_Y = 2;
    public static final int CHEST_ACCEL_Z = 3;
    public static final int CHEST_GSR = 4;

    public static final int CHEST_RIP = 7;
    public static final int CHEST_BATTERY_SKINTEMP_AMBIENTTEMP = 8;

    public static final int QUALITY_GOOD = 0;
    public static final int QUALITY_NOISE = 1;
    public static final int QUALITY_BAD = 2;
    public static final int QUALITY_BAND_OFF = 3;
    public static final int QUALITY_MISSING = 4;
    public static final int QUALITY_BAND_LOOSE = 2;

    public static final int NOT_STRESSED = 0;
    public static final int STRESSED = 1;
    public static final int UNSURE = 2;

    //Library signal processing constants
    public static final double MED_CONSTANT = 4.5;
    public static final double MAD_CONSTANT = 2.8;
    public static final double CBD_THRESHOLD = 0.2;
    public static final double REF_MINIMUM = 0.3;
    public static final double REF_MAXIMUM = 2.0;
    public static final double THR1_INIT = 0.5;
    public static final int FL_INIT = 256;
    public static final double SIG_LEV_FACTOR = 4.0;
    public static final double NOISE_LEV_FACTOR = 0.1;
    public static final double EWMA_ALPHA = 0.125;
    public static final double RPEAK_BIN_FACTOR = 10.0;
    public static final int PEAK_INTERVAL_MINIMUM_SIZE = 8;
    public static final int PEAK_VALLEY_SMOOTHING_SIZE = 5;
    public static final double WINDOW_LENGTH_SECS = 8.0;
    public static final double INSPIRATION_EXPIRATION_AMPLITUDE_THRESHOLD_FACTOR = 0.15;
    public static final double RESPIRATION_MINIMUM = 8.0;
    public static final int RESPIRATION_MAXIMUM = 65;
    public static final double WINDOW_DIVIDER_FACTOR = 20.0;
    public static final double STRESS_PROBABILTY_THRESHOLD = 0.339329059788;

    public static final double RPEAK_INTERPEAK_MULTIPLIER = 0.5;
    public static final int RPEAK_INTERPEAK_LIMIT = 500;
    public static final int SAMPLE_LENGTH_SECS = 60;

    public static final double ACTIVITY_THRESHOLD = 0.21; //From Figure 3: http://www.cs.memphis.edu/~santosh/Papers/Timing-JIT-UbiComp-2014.pdf
    public static final int ACCEL_WINDOW_SIZE = 10000;
    public static final double AUTOSENSE_ECG_QUALITY = 0.67;
    public static final double AUTOSENSE_RIP_QUALITY = 0.67;

    //Data Quality
    public static int SEGMENT_GOOD = 0;
    public static int SEGMENT_BAD = 1;
}
