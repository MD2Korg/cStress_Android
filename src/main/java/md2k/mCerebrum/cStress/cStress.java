package md2k.mCerebrum.cStress;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Autosense.SensorConfiguration;
import md2k.mCerebrum.cStress.Features.AccelerometerFeatures;
import md2k.mCerebrum.cStress.Features.ECGFeatures;
import md2k.mCerebrum.cStress.Features.RIPFeatures;
import md2k.mCerebrum.cStress.Library.DataStream;
import md2k.mCerebrum.cStress.Structs.CSVDataPoint;
import md2k.mCerebrum.cStress.Structs.DataPoint;

import libsvm.*;
import md2k.mCerebrum.cStress.Structs.StressProbability;
import md2k.mCerebrum.cStress.legacyJava.ECGQualityCalculation;
import md2k.mCerebrum.cStress.legacyJava.RipQualityCalculation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * - Karen Hovsepian <karoaper@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

public class cStress {

    long windowStartTime = -1;

    long windowSize;


    private HashMap<String, DataStream> datastreams = new HashMap<String, DataStream>();


    // Keep track of mean and stdev.  This should be reset at the beginning of each day.
    // An EWMA-based solution may need to be added to seed the new day with appropriate information
//    BinnedStatistics ECGStats;
//    BinnedStatistics[] RIPBinnedStats;
//    RunningStatistics[] RIPStats;

//    RunningStatistics AccelXStats;
//    RunningStatistics AccelYStats;
//    RunningStatistics AccelZStats;
//    RunningStatistics MagnitudeStats;

    SensorConfiguration sensorConfig;

    //Feature Computation Classes
//    AccelerometerFeatures accelFeatures;
//    ECGFeatures ecgFeatures;
//    RIPFeatures ripFeatures;

//    private svm_model Model;
//    private double[] featureVectorMean;
//    private double[] featureVectorStd;

    public cStress(long windowSize, String svmModelFile, String featureVectorParameterFile) {
        this.windowSize = windowSize;


        DataStream ECG = new DataStream("ECG");
        DataStream RIP = new DataStream("RIP");
        DataStream ACCELX = new DataStream("ACCELX");
        DataStream ACCELY = new DataStream("ACCELY");
        DataStream ACCELZ = new DataStream("ACCELZ");

        //Configure Data Streams

        ECG.metadata.put("frequency", 64.0);
        ECG.metadata.put("channelID", AUTOSENSE.CHEST_ECG);

        RIP.metadata.put("frequency", 64.0 / 3.0);
        RIP.metadata.put("channelID", AUTOSENSE.CHEST_RIP);

        ACCELX.metadata.put("frequency", 64.0 / 6.0);
        ACCELX.metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        ACCELY.metadata.put("frequency", 64.0 / 6.0);
        ACCELY.metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        ACCELZ.metadata.put("frequency", 64.0 / 6.0);
        ACCELZ.metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);


        datastreams.put("org.md2k.cstress.data.ecg",ECG);
        datastreams.put("org.md2k.cstress.data.rip",RIP);
        datastreams.put("org.md2k.cstress.data.accelx",ACCELX);
        datastreams.put("org.md2k.cstress.data.accely",ACCELY);
        datastreams.put("org.md2k.cstress.data.accelz",ACCELZ);


        resetDataStreams();


        //this.ECGStats = new BinnedStatistics(800, 2000);
//        this.RIPBinnedStats = new BinnedStatistics[RIPFeatures.NUM_BASE_FEATURES];
//        this.RIPBinnedStats[RIPFeatures.FIND_EXPR_DURATION] = new BinnedStatistics(500, 12000);
//        this.RIPBinnedStats[RIPFeatures.FIND_INSP_DURATION] = new BinnedStatistics(500, 12000);
//        this.RIPBinnedStats[RIPFeatures.FIND_RESP_DURATION] = new BinnedStatistics(500, 12000);
//        this.RIPBinnedStats[RIPFeatures.FIND_STRETCH] = new BinnedStatistics(0, 5000);
//        this.RIPBinnedStats[RIPFeatures.FIND_RSA] = new BinnedStatistics(0, 4200);
//        this.RIPStats[0] = new RunningStatistics();
//        this.RIPStats[1] = new RunningStatistics();

//        this.ECGStats = new RunningStatistics();
//        this.RIPStats = new RunningStatistics();
//
//        this.AccelXStats = new RunningStatistics();
//        this.AccelYStats = new RunningStatistics();
//        this.AccelZStats = new RunningStatistics();
//        this.MagnitudeStats = new RunningStatistics();



//        try {
//            Model = svm.svm_load_model(svmModelFile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        this.featureVectorMean = new double[37];
//        this.featureVectorStd = new double[37];
//        loadFVParameters(featureVectorParameterFile);

    }

//    private void loadFVParameters(String featureVectorParameterFile) {
//
//        BufferedReader br = null;
//        String line = "";
//        String csvSplitBy = ",";
//        int count;
//        try {
//            br = new BufferedReader(new FileReader(featureVectorParameterFile));
//            count = 0;
//            while ((line = br.readLine()) != null) {
//                String[] meanstd = line.split(csvSplitBy);
//                this.featureVectorMean[count] = Double.parseDouble(meanstd[0]);
//                this.featureVectorStd[count] = Double.parseDouble(meanstd[1]);
//                count++;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


//    private StressProbability evaluteStressModel(AccelerometerFeatures accelFeatures, ECGFeatures ecgFeatures, RIPFeatures ripFeatures, double bias) {
//
//        StressProbability stressResult = new StressProbability(-1, 0.0);
//
//        /* List of features for SVM model
//
//         ECG - RR interval variance
//         ECG - RR interval quartile deviation
//         ECG - RR interval low frequency energy
//         ECG - RR interval medium frequency energy
//         *ECG - RR interval high frequency energy
//         *ECG - RR interval low-high frequency energy ratio
//         *ECG - RR interval mean
//         ECG - RR interval median
//         *ECG - RR interval 80th percentile
//         ECG - RR interval 20th percentile
//         ECG - RR interval heart-rate
//         */
//
//        double ECG_RR_Interval_Variance = ecgFeatures.RRStatsNormalized.getVariance();
//        double ECG_RR_Interval_Quartile_Deviation = (ecgFeatures.RRStatsNormalized.getPercentile(75) - ecgFeatures.RRStatsNormalized.getPercentile(25)) / 2.0;
//        double ECG_RR_Interval_Low_Frequency_Energy = ecgFeatures.getLombLowFrequencyEnergy();
//        double ECG_RR_Interval_Medium_Frequency_Energy = ecgFeatures.getLombMediumFrequencyEnergy();
//        double ECG_RR_Interval_High_Frequency_Energy = ecgFeatures.getLombHighFrequencyEnergy();
//        double ECG_RR_Interval_Low_High_Frequency_Energy_Ratio = ecgFeatures.getLombLowHighFrequencyEnergyRatio();
//        double ECG_RR_Interval_Mean = ecgFeatures.RRStatsNormalized.getMean();
//        double ECG_RR_Interval_Median = ecgFeatures.RRStatsNormalized.getPercentile(50);
//        double ECG_RR_Interval_80thPercentile = ecgFeatures.RRStatsNormalized.getPercentile(80);
//        double ECG_RR_Interval_20thPercentile = ecgFeatures.RRStatsNormalized.getPercentile(20);
//        double ECG_RR_Interval_Heart_Rate = ecgFeatures.getHeartRate();
//
//         /*
//         RIP - Inspiration Duration - quartile deviation
//         RIP - Inspiration Duration - mean
//         RIP - Inspiration Duration - median
//         RIP - Inspiration Duration - 80th percentile
//         */
//        double RIP_Inspiration_Duration_Quartile_Deviation = (ripFeatures.InspDurationNormalized.getPercentile(75) - ripFeatures.InspDurationNormalized.getPercentile(25)) / 2.0;
//        double RIP_Inspiration_Duration_Mean = ripFeatures.InspDurationNormalized.getMean();
//        double RIP_Inspiration_Duration_Median = ripFeatures.InspDurationNormalized.getPercentile(50);
//        double RIP_Inspiration_Duration_80thPercentile = ripFeatures.InspDurationNormalized.getPercentile(80);
//
//         /*
//         RIP - Expiration Duration - quartile deviation
//         RIP - Expiration Duration - mean
//         RIP - Expiration Duration - median
//         RIP - Expiration Duration - 80th percentile
//         */
//        double RIP_Expiration_Duration_Quartile_Deviation = (ripFeatures.ExprDurationNormalized.getPercentile(75) - ripFeatures.ExprDurationNormalized.getPercentile(25)) / 2.0;
//        double RIP_Expiration_Duration_Mean = ripFeatures.ExprDurationNormalized.getMean();
//        double RIP_Expiration_Duration_Median = ripFeatures.ExprDurationNormalized.getPercentile(50);
//        double RIP_Expiration_Duration_80thPercentile = ripFeatures.ExprDurationNormalized.getPercentile(80);
//
//         /*
//         RIP - Respiration Duration - quartile deviation
//         RIP - Respiration Duration - mean
//         RIP - Respiration Duration - median
//         RIP - Respiration Duration - 80th percentile
//         */
//        double RIP_Respiration_Duration_Quartile_Deviation = (ripFeatures.RespDurationNormalized.getPercentile(75) - ripFeatures.RespDurationNormalized.getPercentile(25)) / 2.0;
//        double RIP_Respiration_Duration_Mean = ripFeatures.RespDurationNormalized.getMean();
//        double RIP_Respiration_Duration_Median = ripFeatures.RespDurationNormalized.getPercentile(50);
//        double RIP_Respiration_Duration_80thPercentile = ripFeatures.RespDurationNormalized.getPercentile(80);
//
//         /*
//         RIP - Inspiration-Expiration Duration Ratio - quartile deviation
//         *RIP - Inspiration-Expiration Duration Ratio - mean
//         RIP - Inspiration-Expiration Duration Ratio - median
//         RIP - Inspiration-Expiration Duration Ratio - 80th percentile
//         */
//        double RIP_Inspiration_Expiration_Duration_Quartile_Deviation = (ripFeatures.IERatio.getPercentile(75) - ripFeatures.IERatio.getPercentile(25)) / 2.0;
//        double RIP_Inspiration_Expiration_Duration_Mean = ripFeatures.IERatio.getMean();
//        double RIP_Inspiration_Expiration_Duration_Median = ripFeatures.IERatio.getPercentile(50);
//        double RIP_Inspiration_Expiration_Duration_80thPercentile = ripFeatures.IERatio.getPercentile(80);
//
//         /*
//         RIP - Stretch - quartile deviation
//         RIP - Stretch - mean
//         *RIP - Stretch - median
//         RIP - Stretch - 80th percentile
//         */
//        double RIP_Stretch_Quartile_Deviation = (ripFeatures.StretchNormalized.getPercentile(75) - ripFeatures.StretchNormalized.getPercentile(25)) / 2.0;
//        double RIP_Stretch_Mean = ripFeatures.StretchNormalized.getMean();
//        double RIP_Stretch_Median = ripFeatures.StretchNormalized.getPercentile(50);
//        double RIP_Stretch_80thPercentile = ripFeatures.StretchNormalized.getPercentile(80);
//         /*
//         *RIP - Breath-rate
//         */
//        double RIP_Breath_Rate = ripFeatures.BreathRateNormalized;
//
//         /*
//         *RIP - Inspiration Minute Volume
//         */
//        double RIP_Inspiration_Minute_Volume = ripFeatures.MinuteVolumeNormalized;
//
//         /*
//         RIP+ECG - Respiratory Sinus Arrhythmia (RSA) - quartile deviation
//         RIP+ECG - Respiratory Sinus Arrhythmia (RSA) - mean
//         RIP+ECG - Respiratory Sinus Arrhythmia (RSA) - median
//         RIP+ECG - Respiratory Sinus Arrhythmia (RSA) - 80th percentile
//         */
//        double RSA_Quartile_Deviation = (ecgFeatures.RRStatsNormalized.getPercentile(75) - ecgFeatures.RRStatsNormalized.getPercentile(25)) / 2.0;
//        double RSA_Mean = ecgFeatures.RRStatsNormalized.getMean();
//        double RSA_Median = ecgFeatures.RRStatsNormalized.getPercentile(50);
//        double RSA_80thPercentile = ecgFeatures.RRStatsNormalized.getPercentile(80);
//
//
//        double[] featureVector = {
//                ECG_RR_Interval_Variance,                               // 1
//                ECG_RR_Interval_Low_High_Frequency_Energy_Ratio,        // 2
//                ECG_RR_Interval_High_Frequency_Energy,                  // 3
//                ECG_RR_Interval_Medium_Frequency_Energy,                // 4
//                ECG_RR_Interval_Low_Frequency_Energy,                   // 5
//                ECG_RR_Interval_Mean,                                   // 6
//                ECG_RR_Interval_Median,                                 // 7
//                ECG_RR_Interval_Quartile_Deviation,                     // 8
//                ECG_RR_Interval_80thPercentile,                         // 9
//                ECG_RR_Interval_20thPercentile,                         // 10
//                ECG_RR_Interval_Heart_Rate,                             // 11
//
//                RIP_Breath_Rate,                                        // 12
//                RIP_Inspiration_Minute_Volume,                          // 13
//
//                RIP_Inspiration_Duration_Quartile_Deviation,            // 14
//                RIP_Inspiration_Duration_Mean,                          // 15
//                RIP_Inspiration_Duration_Median,                        // 16
//                RIP_Inspiration_Duration_80thPercentile,                // 17
//
//                RIP_Expiration_Duration_Quartile_Deviation,             // 18
//                RIP_Expiration_Duration_Mean,                           // 19
//                RIP_Expiration_Duration_Median,                         // 20
//                RIP_Expiration_Duration_80thPercentile,                 // 21
//
//                RIP_Respiration_Duration_Quartile_Deviation,            // 22
//                RIP_Respiration_Duration_Mean,                          // 23
//                RIP_Respiration_Duration_Median,                        // 24
//                RIP_Respiration_Duration_80thPercentile,                // 25
//
//                RIP_Inspiration_Expiration_Duration_Quartile_Deviation, // 26
//                RIP_Inspiration_Expiration_Duration_Mean,               // 27
//                RIP_Inspiration_Expiration_Duration_Median,             // 28
//                RIP_Inspiration_Expiration_Duration_80thPercentile,     // 29
//
//                RIP_Stretch_Quartile_Deviation,                         // 30
//                RIP_Stretch_Mean,                                       // 31
//                RIP_Stretch_Median,                                     // 32
//                RIP_Stretch_80thPercentile,                             // 33
//
//                RSA_Quartile_Deviation,                                 // 34
//                RSA_Mean,                                               // 35
//                RSA_Median,                                             // 36
//                RSA_80thPercentile                                      // 37
//        };
//
//        featureVector = normalizeFV(featureVector);
//
//        boolean invalid = false;
//        for(double d: featureVector) {
//            if (Double.isInfinite(d) || Double.isNaN(d)) {
//                invalid = true;
//            }
//        }
//
//        if (!activityCheck(accelFeatures) && !invalid) {
//            //SVM evaluation
//            svm_node[] data = new svm_node[featureVector.length];
//            for (int i = 0; i < featureVector.length; i++) {
//                data[i] = new svm_node();
//                data[i].index = i;
//                data[i].value = featureVector[i];
//            }
//
//            stressResult.probability = svm.svm_predict(Model, data);
//            if (stressResult.probability < bias) {
//                stressResult.label = AUTOSENSE.NOT_STRESSED;
//            } else {
//                stressResult.label = AUTOSENSE.STRESSED;
//            }
//        }
//
//        //Basic Features
//        DataPoint[] af = accelFeatures.rawFeatures();
//        DataPoint[] rr_intervals = ecgFeatures.rawFeatures();
//        DataPoint[] peaks = ripFeatures.rawPeakFeatures();
//        DataPoint[] valleys = ripFeatures.rawValleyFeatures();
//
//        return stressResult;
//    }
//
//    private double[] normalizeFV(double[] featureVector) {
//        double[] result = new double[featureVector.length];
//
//        for (int i = 0; i < featureVector.length; i++) {
//            result[i] = (featureVector[i] - this.featureVectorMean[i]) / this.featureVectorStd[i];
//        }
//        return result;
//    }
//
//    private boolean activityCheck(AccelerometerFeatures accelFeatures) {
//        return accelFeatures.Activity;
//    }


    public StressProbability process() {

        StressProbability probabilityOfStress = null;

        if (    datastreams.get("org.md2k.cstress.data.ecg").data.size() > 500 &&
                datastreams.get("org.md2k.cstress.data.rip").data.size() > 500 &&
                datastreams.get("org.md2k.cstress.data.accelx").data.size() > 300 &&
                datastreams.get("org.md2k.cstress.data.accely").data.size() > 300 &&
                datastreams.get("org.md2k.cstress.data.accelz").data.size() > 300) {

            //TODO: This should be moved outside of the stress applications to intercept data before arriving here
//            //This check must happen before any normalization.  It operates on the RAW signals.
//            RipQualityCalculation ripQuality = new RipQualityCalculation(5, 50, 4500, 20, 2, 20, 150);
//            ECGQualityCalculation ecgQuality = new ECGQualityCalculation(3, 50, 4500, 20, 2, 47);
//
//            if (!ripQuality.computeQuality(rip, 5 * 1000, 0.67) || !ecgQuality.computeQuality(ecg, 5 * 1000, 0.67)) { //Check for 67% of the data to be of Quality within 5 second windows.
//                return probabilityOfStress; //data quality failure
//            }
//
//

            AccelerometerFeatures af = new AccelerometerFeatures(datastreams);
            ECGFeatures ef = new ECGFeatures(datastreams);

            RIPFeatures rf = new RIPFeatures(datastreams);

//                probabilityOfStress = evaluteStressModel(accelFeatures, ecgFeatures, ripFeatures, AUTOSENSE.STRESS_PROBABILTY_THRESHOLD);

        } else {
            System.out.println("Not enough data to process");
        }
        return probabilityOfStress;
    }



    public StressProbability add(int channel, DataPoint dp) {
        StressProbability result = null;

        if (this.windowStartTime < 0)
            this.windowStartTime = nextEpochTimestamp(dp.timestamp);

        if ((dp.timestamp - windowStartTime) >= this.windowSize) { //Process the buffer every windowSize milliseconds
            result = process();
            resetDataStreams();
            this.windowStartTime += AUTOSENSE.SAMPLE_LENGTH_SECS*1000; //Add 60 seconds to the timestamp
        }

        if (dp.timestamp >= this.windowStartTime) {
            switch (channel) {
                case AUTOSENSE.CHEST_ECG:
                    datastreams.get("org.md2k.cstress.data.ecg").add(dp);
                    break;

                case AUTOSENSE.CHEST_RIP:
                    datastreams.get("org.md2k.cstress.data.rip").add(dp);
                    break;

                case AUTOSENSE.CHEST_ACCEL_X:
                    datastreams.get("org.md2k.cstress.data.accelx").add(dp);
                    break;

                case AUTOSENSE.CHEST_ACCEL_Y:
                    datastreams.get("org.md2k.cstress.data.accely").add(dp);
                    break;

                case AUTOSENSE.CHEST_ACCEL_Z:
                    datastreams.get("org.md2k.cstress.data.accelz").add(dp);
                    break;

                default:
                    System.out.println("NOT INTERESTED: " + dp);
                    break;
            }
        }

        return result;
    }

    private long nextEpochTimestamp(long timestamp) {
        long previousMinute = timestamp / (60*1000);
        Date date = new Date((previousMinute+1)*(60*1000));
        return date.getTime();
    }

    private void resetDataStreams() {
        for(String dsname: datastreams.keySet()) {
            datastreams.get(dsname).reset(); //Reset all data, maintain statistics
        }
    }


}
