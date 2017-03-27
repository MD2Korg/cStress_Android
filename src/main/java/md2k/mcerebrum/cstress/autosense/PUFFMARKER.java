package md2k.mcerebrum.cstress.autosense;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center 
 * - Nazir Saleneen <nsleheen@memphis.edu>
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

public class PUFFMARKER {
    public static final double PUFF_MARKER_ROLL_MEAN = 26.7810;
    public static final double PUFF_MARKER_PITCH_MEAN = -80.3673;
    public static final double PUFF_MARKER_ROLL_STD = 13.9753;
    public static final double PUFF_MARKER_PITCH_STD = 13.4698;
    public static final double[][] PUFFMARKER_SIGMA = new double[][]{{195.3085, -92.7786}, {-92.7786, 181.4359}};
    public static final double[] PUFF_MARKER_TH = new double[]{10.1511, 7.8746, 11.7729, 11.2226};

    //------ Sensor KEY ------//
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIP = "org.md2k.puffMarker.data.rip";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_X = "org.md2k.puffMarker.data.accel.x";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Y = "org.md2k.puffMarker.data.accel.y";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Z = "org.md2k.puffMarker.data.accel.z";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_X = "org.md2k.puffMarker.data.gyro.x";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_Y = "org.md2k.puffMarker.data.gyro.y";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_Z = "org.md2k.puffMarker.data.gyro.z";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_X_2_MIN = "org.md2k.puffMarker.data.accel.x.2min";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Y_2_MIN = "org.md2k.puffMarker.data.accel.y.2min";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Z_2_MIN = "org.md2k.puffMarker.data.accel.z.2min";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_X_2_MIN = "org.md2k.puffMarker.data.gyro.x.2min";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_Y_2_MIN = "org.md2k.puffMarker.data.gyro.y.2min";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_Z_2_MIN = "org.md2k.puffMarker.data.gyro.z.2min";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_X = "org.md2k.puffMarker.data.accel.interpolate.x";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_Y = "org.md2k.puffMarker.data.accel.interpolate.y";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_Z = "org.md2k.puffMarker.data.accel.interpolate.z";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_X = "org.md2k.puffMarker.data.gyro.interpolate.x";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_Y = "org.md2k.puffMarker.data.gyro.interpolate.y";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_Z = "org.md2k.puffMarker.data.gyro.interpolate.z";
    public static final String LEFT_WRIST = ".leftwrist";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_Z = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Z + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_Y = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Y + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_X = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_X + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_Z = ORG_MD2K_PUFF_MARKER_DATA_GYRO_Z + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_Y = ORG_MD2K_PUFF_MARKER_DATA_GYRO_Y + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_X = ORG_MD2K_PUFF_MARKER_DATA_GYRO_X + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_INTERPOLATE_Z = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_Z + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_INTERPOLATE_Y = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_Y + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_INTERPOLATE_X = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_X + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_INTERPOLATE_Z = ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_Z + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_INTERPOLATE_Y = ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_Y + LEFT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_INTERPOLATE_X = ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_X + LEFT_WRIST;
    public static final String RIGHT_WRIST = ".rightwrist";
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_Z = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Z + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_Y = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Y + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_X = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_X + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_Z = ORG_MD2K_PUFF_MARKER_DATA_GYRO_Z + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_Y = ORG_MD2K_PUFF_MARKER_DATA_GYRO_Y + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_X = ORG_MD2K_PUFF_MARKER_DATA_GYRO_X + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_INTERPOLATE_Z = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_Z + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_INTERPOLATE_Y = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_Y + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_INTERPOLATE_X = ORG_MD2K_PUFF_MARKER_DATA_ACCEL_INTERPOLATE_X + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_INTERPOLATE_Z = ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_Z + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_INTERPOLATE_Y = ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_Y + RIGHT_WRIST;
    public static final String ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_INTERPOLATE_X = ORG_MD2K_PUFF_MARKER_DATA_GYRO_INTERPOLATE_X + RIGHT_WRIST;
    //------ Sensor ID ------//
    public static final int LEFTWRIST_ACCEL_X = 26;
    public static final int LEFTWRIST_ACCEL_Y = 27;
    public static final int LEFTWRIST_ACCEL_Z = 28;
    public static final int LEFTWRIST_GYRO_X = 29;
    public static final int LEFTWRIST_GYRO_Y = 30;
    public static final int LEFTWRIST_GYRO_Z = 31;
    public static final int RIGHTWRIST_ACCEL_X = 33;
    public static final int RIGHTWRIST_ACCEL_Y = 34;
    public static final int RIGHTWRIST_ACCEL_Z = 35;
    public static final int RIGHTWRIST_GYRO_X = 36;
    public static final int RIGHTWRIST_GYRO_Y = 37;
    public static final int RIGHTWRIST_GYRO_Z = 38;

    public static double GYR_MAG_FIRST_MOVING_AVG_SMOOTHING_SIZE_TH_TIME = 0.609; //0.609 sec, 13 samples
    public static double GYR_MAG_SLOW_MOVING_AVG_SMOOTHING_SIZE_TH_TIME= 6.140; // 6.14, 131 samples

    public static int MINIMUM_CANDIDATE_WINDOW_DURATION_ = 1000;
    public static int MAXIMUM_CANDIDATE_WINDOW_DURATION_ = 5000;

    public static double MINIMUM_GYRO_MEAN_HEIGHT_DIFFERENCE = 250*50.00/2048; // adc value=50

    public static int PUFF = 1;
    public static int NOT_PUFF = 0;
    public static int UNSURE = 2;

    public static final int BUFFER_SIZE_HALF_MIN_SEC = 30;

}
