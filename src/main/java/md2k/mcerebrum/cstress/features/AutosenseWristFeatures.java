package md2k.mcerebrum.cstress.features;

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.autosense.PUFFMARKER;
import md2k.mcerebrum.cstress.library.Vector;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.signalprocessing.Smoothing;
import md2k.mcerebrum.cstress.library.structs.DataPoint;
import md2k.mcerebrum.cstress.library.structs.MetadataDouble;

import java.util.ArrayList;
import java.util.List;

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


/**
 * AutoSense Accelerometer and Gyrometer processor.
 */
public class AutosenseWristFeatures {


    /**
     * Constructor
     *
     * @param datastreams Global datastreams object
     * @param wrist       Which wrist to operate on
     */
    public AutosenseWristFeatures(DataStreams datastreams, String wrist) {

        DataPointStream gyrox = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_X + wrist);
        DataPointStream gyroy = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_Y + wrist);
        DataPointStream gyroz = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_Z + wrist);

        DataPointStream gyrox2min = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_X_2_MIN + wrist);
        DataPointStream gyroy2min = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_Y_2_MIN + wrist);
        DataPointStream gyroz2min = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_Z_2_MIN + wrist);

        int wLen = (int) Math.round(PUFFMARKER.BUFFER_SIZE_HALF_MIN_SEC * ((MetadataDouble) datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_X + wrist).metadata.get("frequency")).value);
        long timestamp2minbefore = gyrox.data.get(0).timestamp - PUFFMARKER.BUFFER_SIZE_HALF_MIN_SEC * 1000;
        gyrox2min.setHistoricalBufferSize(wLen);
        gyroy2min.setHistoricalBufferSize(wLen);
        gyroz2min.setHistoricalBufferSize(wLen);

        mergeWithPreviousData(gyrox, gyrox2min, timestamp2minbefore);
        mergeWithPreviousData(gyroy, gyroy2min, timestamp2minbefore);
        mergeWithPreviousData(gyroz, gyroz2min, timestamp2minbefore);
        DataPointStream gyr_mag = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_GYRO_MAG + wrist);
        doInterpolation(gyrox2min, gyroy2min, gyroz2min, null, null, null);
        Vector.magnitude(gyr_mag, gyrox2min.data, gyroy2min.data, gyroz2min.data);

        int firstSize = (int) Math.round(PUFFMARKER.GYR_MAG_FIRST_MOVING_AVG_SMOOTHING_SIZE_TH_TIME * ((MetadataDouble) datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_X + wrist).metadata.get("frequency")).value);
        int slowSize = (int) Math.round(PUFFMARKER.GYR_MAG_SLOW_MOVING_AVG_SMOOTHING_SIZE_TH_TIME * ((MetadataDouble) datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_X + wrist).metadata.get("frequency")).value);

        DataPointStream gyr_mag_800 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_GYRO_MAG800 + wrist);
        Smoothing.smooth(gyr_mag_800, gyr_mag, firstSize);
        DataPointStream gyr_mag_8000 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_GYRO_MAG8000 + wrist);
        Smoothing.smooth(gyr_mag_8000, gyr_mag, slowSize);

        DataPointStream gyr_intersections = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_GYRO_INTERSECTIONS + wrist);
        segmentationUsingTwoMovingAverage(gyr_intersections, gyr_mag_8000, gyr_mag_800, 0, 4);

        DataPointStream accelx = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_X + wrist);
        DataPointStream accely = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Y + wrist);
        DataPointStream accelz = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Z + wrist);
        DataPointStream accelx2min = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_X_2_MIN + wrist);
        DataPointStream accely2min = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Y_2_MIN + wrist);
        DataPointStream accelz2min = datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Z_2_MIN + wrist);

        wLen = (int) Math.round(PUFFMARKER.BUFFER_SIZE_HALF_MIN_SEC * ((MetadataDouble) datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_X + wrist).metadata.get("frequency")).value);

        accelx2min.setHistoricalBufferSize(wLen);
        accely2min.setHistoricalBufferSize(wLen);
        accelz2min.setHistoricalBufferSize(wLen);

        mergeWithPreviousData(accelx, accelx2min, timestamp2minbefore);
        mergeWithPreviousData(accely, accely2min, timestamp2minbefore);
        mergeWithPreviousData(accelz, accelz2min, timestamp2minbefore);
        doInterpolation(accelx2min, accely2min, accelz2min, null, null, null);

        DataPointStream acl_x_800 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_ACCL_X_MAG800 + wrist);
        Smoothing.smooth(acl_x_800, accelx2min, firstSize);
        DataPointStream acl_z_800 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_ACCL_Z_MAG800 + wrist);
        Smoothing.smooth(acl_z_800, accelz2min, firstSize);
        DataPointStream acl_y_800 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_ACCL_Y_MAG800 + wrist);
        Smoothing.smooth(acl_y_800, accely2min, firstSize);
        DataPointStream acl_y_8000 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_ACCL_Y_MAG8000 + wrist);
        Smoothing.smooth(acl_y_8000, accely2min, slowSize);
        DataPointStream acl_intersections = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_ACCEL_Y_INTERSECTIONS+ wrist);
//        segmentationUsingTwoMovingAverage(acl_intersections, acl_y_8000, acl_y_800, 0, 2);
        segmentationUsingThreshold(acl_intersections, acl_y_800, 0.20, 2);

        DataPointStream roll = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_WRIST_ROLL + wrist);
        DataPointStream pitch = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_WRIST_PITCH + wrist);
        DataPointStream yaw = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_WRIST_YAW + wrist);

        calculateRollPitchYawSegment(roll, pitch, yaw, acl_x_800, acl_y_800, acl_z_800);
    }

    /**
     * Segmentation based on two moving averages
     *
     * @param output            Output datastream
     * @param slowMovingAverage Input slow moving average
     * @param fastMovingAverage Input fast moving average
     * @param THRESHOLD         Threshold //TODO: more details
     * @param near              //TODO: What is this?
     * @return
     */
    public static int[] segmentationUsingTwoMovingAverage(DataPointStream output, DataPointStream slowMovingAverage
            , DataPointStream fastMovingAverage
            , int THRESHOLD, int near) {
        int[] indexList = new int[slowMovingAverage.data.size()];
        int curIndex = 0;
        for (int i = 0; i < slowMovingAverage.data.size(); i++) {
            double diff = slowMovingAverage.data.get(i).value - fastMovingAverage.data.get(i).value;
            if (diff > THRESHOLD) {
                if (curIndex == 0) {
                    indexList[curIndex++] = i;
                    indexList[curIndex] = i;
                } else {
                    if (i <= indexList[curIndex] + near) indexList[curIndex] = i;
                    else {
                        indexList[++curIndex] = i;
                        indexList[++curIndex] = i;
                    }
                }
            }
        }
        int[] intersectionIndex = new int[curIndex + 1];

        if (curIndex > 0)
            for (int i = 0; i < curIndex; i += 2) {
                output.data.add(new DataPoint(indexList[i], indexList[i + 1]));
                intersectionIndex[i] = indexList[i];
            }
        return intersectionIndex;
    }

    /**
     * Segmentation based on threshold
     *
     * @param output            Output datastream
     * @param input Input slow moving average
     * @param THRESHOLD         Threshold //TODO: more details
     * @param near              //TODO: What is this?
     * @return
     */
    public static int[] segmentationUsingThreshold(DataPointStream output, DataPointStream input
            , double THRESHOLD, int near) {
        int[] indexList = new int[input.data.size()];
        int curIndex = 0;
        for (int i = 0; i < input.data.size(); i++) {
            double diff = input.data.get(i).value;
            if (diff > THRESHOLD) {
                if (curIndex == 0) {
                    indexList[curIndex++] = i;
                    indexList[curIndex] = i;
                } else {
                    if (i <= indexList[curIndex] + near) indexList[curIndex] = i;
                    else {
                        indexList[++curIndex] = i;
                        indexList[++curIndex] = i;
                    }
                }
            }
        }
        int[] intersectionIndex = new int[curIndex + 1];

        if (curIndex > 0)
            for (int i = 0; i < curIndex; i += 2) {
                output.data.add(new DataPoint(indexList[i], indexList[i + 1]));
                intersectionIndex[i] = indexList[i];
            }
        return intersectionIndex;
    }

    /**
     * Compute roll from accelerometer inputs
     *
     * @param ax Accelerometer x-axis
     * @param ay Accelerometer y-axis
     * @param az Accelerometer z-axis
     * @return
     */
    public static double roll(double ax, double ay, double az) {
        return 180 * Math.atan2(ax, Math.sqrt(ay * ay + az * az)) / Math.PI;
    }

    /**
     * Compute pitch from accelerometer inputs
     *
     * @param ax Accelerometer x-axis
     * @param ay Accelerometer y-axis
     * @param az Accelerometer z-axis
     * @return
     */
    public static double pitch(double ax, double ay, double az) {
        return 180 * Math.atan2(-ay, -az) / Math.PI;
    }

    /**
     * Compute yaw from accelerometer inputs
     *
     * @param ax Accelerometer x-axis
     * @param ay Accelerometer y-axis
     * @param az Accelerometer z-axis
     * @return
     */
    public static double yaw(double ax, double ay, double az) {
        return 180 * Math.atan2(ay, ax) / Math.PI;
    }

    private void mergeWithPreviousData(DataPointStream currentDataStream, DataPointStream mergedDataStream, long timestamp) {

        List<DataPoint> listHistory = new ArrayList<>(mergedDataStream.getHistoricalValues(timestamp));
        mergedDataStream.addAll(listHistory);
        mergedDataStream.addAll(currentDataStream.data);
    }

    private void doInterpolation(DataPointStream signalX, DataPointStream signalY, DataPointStream signalZ, DataPointStream interpolateX, DataPointStream interpolateY, DataPointStream interpolateZ) {

        while (signalX.data.size() > signalY.data.size())
            signalY.add(signalY.data.get(signalY.data.size() - 1));

        while (signalX.data.size() > signalZ.data.size())
            signalZ.add(signalZ.data.get(signalZ.data.size() - 1));
    }

    /**
     * Segment datastreams based on roll and pitch
     *
     * @param roll   Output roll datastream
     * @param pitch  Output pitch datastream
     * @param accelx Input accelerometer x datastream
     * @param accely Input accelerometer y datastream
     * @param accelz Input accelerometer z datastream
     */
    private void calculateRollPitchYawSegment(DataPointStream roll, DataPointStream pitch, DataPointStream yaw, DataPointStream accelx, DataPointStream accely, DataPointStream accelz) {
        for (int i = 0; i < accelx.data.size(); i++) {
            double rll = roll(accelx.data.get(i).value, accely.data.get(i).value, accelz.data.get(i).value);
            double ptch = pitch(accelx.data.get(i).value, accely.data.get(i).value, accelz.data.get(i).value);
            double yw = yaw(accelx.data.get(i).value, accely.data.get(i).value, accelz.data.get(i).value);
            roll.data.add(new DataPoint(accelx.data.get(i).timestamp, rll));
            pitch.data.add(new DataPoint(accelx.data.get(i).timestamp, ptch));
            yaw.data.add(new DataPoint(accelx.data.get(i).timestamp, yw));
        }
    }


}
