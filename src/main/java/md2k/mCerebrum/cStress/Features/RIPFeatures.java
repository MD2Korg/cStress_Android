package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Library.Core;
import md2k.mCerebrum.cStress.Autosense.SensorConfiguration;
import md2k.mCerebrum.cStress.Library.DataStream;
import md2k.mCerebrum.cStress.Statistics.BinnedStatistics;
import md2k.mCerebrum.cStress.Statistics.RunningStatistics;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.PeakValley;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
public class RIPFeatures {

    public static final int FIND_INSP_DURATION = 0;
    public static final int FIND_EXPR_DURATION = 1;
    public static final int FIND_RESP_DURATION = 2;
    public static final int FIND_STRETCH = 3;
    public static final int FIND_RSA = 4;
    public static final int NUM_BASE_FEATURES = 5;

    public double MinuteVolume;
    public double BreathRate;

    public double MinuteVolumeNormalized;
    public double BreathRateNormalized;

    public DescriptiveStatistics InspDuration;
    public DescriptiveStatistics ExprDuration;
    public DescriptiveStatistics RespDuration;
    public DescriptiveStatistics Stretch;
    public DescriptiveStatistics IERatio;
    public DescriptiveStatistics RSA;

    public DescriptiveStatistics InspDurationNormalized;
    public DescriptiveStatistics ExprDurationNormalized;
    public DescriptiveStatistics RespDurationNormalized;
    public DescriptiveStatistics StretchNormalized;
    public DescriptiveStatistics RSANormalized;

    private SensorConfiguration sensorConfig;

    private DataPoint[] peaks;
    private DataPoint[] valleys;


    /**
     * Core Respiration Features
     * Reference: ripFeature_Extraction.m
     *
     * @param rip
     * @param ecg
     * @param sc
     */
    public RIPFeatures(HashMap<String, DataStream> datastreams) {

        //Initialize statistics
//        InspDuration = new DescriptiveStatistics();
//        ExprDuration = new DescriptiveStatistics();
//        RespDuration = new DescriptiveStatistics();
//        Stretch = new DescriptiveStatistics();
//        IERatio = new DescriptiveStatistics();
//        RSA = new DescriptiveStatistics();

//        InspDurationNormalized = new DescriptiveStatistics();
//        ExprDurationNormalized = new DescriptiveStatistics();
//        RespDurationNormalized = new DescriptiveStatistics();
//        StretchNormalized = new DescriptiveStatistics();
//        RSANormalized = new DescriptiveStatistics();

        String pv = Core.peakvalley_v2(datastreams); //There is no trailing valley in this output.


//        this.peaks = new DataPoint[pvData.peakIndex.size()];
//        this.valleys = new DataPoint[pvData.valleyIndex.size()];
//
//        for (int i = 0; i < this.peaks.length; i++) {
//            DataPoint dp = new DataPoint(rip[pvData.peakIndex.get(i)].timestamp, rip[pvData.peakIndex.get(i)].value);
//            this.peaks[i] = dp;
//        }
//
//        for (int i = 0; i < this.valleys.length; i++) {
//            DataPoint dp = new DataPoint(rip[pvData.valleyIndex.get(i)].timestamp, rip[pvData.valleyIndex.get(i)].value);
//            this.valleys[i] = dp;
//        }
//
//
//        if (!activity) {
//            for (int i = 0; i < pvData.valleyIndex.size() - 1; i++) {
//                RIPBinnedStats[RIPFeatures.FIND_INSP_DURATION].add((int) (rip[pvData.peakIndex.get(i)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp));
//                RIPBinnedStats[RIPFeatures.FIND_EXPR_DURATION].add((int) (rip[pvData.valleyIndex.get(i + 1)].timestamp - rip[pvData.peakIndex.get(i)].timestamp));
//                RIPBinnedStats[RIPFeatures.FIND_RESP_DURATION].add((int) (rip[pvData.valleyIndex.get(i + 1)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp));
//                RIPBinnedStats[RIPFeatures.FIND_STRETCH].add((int) (rip[pvData.peakIndex.get(i)].value - rip[pvData.valleyIndex.get(i)].value));
//                RIPBinnedStats[RIPFeatures.FIND_RSA].add((int) (rsaCalculateCycle(rip[pvData.valleyIndex.get(i)].timestamp, rip[pvData.valleyIndex.get(i + 1)].timestamp, ecg) * 1000));
//            }
//        }
//
//        //Add values, normalized with Winsorized mean and std
//        for (int i = 0; i < pvData.valleyIndex.size() - 1; i++) {
//            InspDuration.addValue(rip[pvData.peakIndex.get(i)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp);
//            InspDurationNormalized.addValue(((rip[pvData.peakIndex.get(i)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp) - RIPBinnedStats[RIPFeatures.FIND_INSP_DURATION].getWinsorizedMean()) / RIPBinnedStats[RIPFeatures.FIND_INSP_DURATION].getWinsorizedStdev());
//
//            ExprDuration.addValue(rip[pvData.valleyIndex.get(i + 1)].timestamp - rip[pvData.peakIndex.get(i)].timestamp);
//            ExprDurationNormalized.addValue(((rip[pvData.valleyIndex.get(i + 1)].timestamp - rip[pvData.peakIndex.get(i)].timestamp) - RIPBinnedStats[RIPFeatures.FIND_EXPR_DURATION].getWinsorizedMean()) / RIPBinnedStats[RIPFeatures.FIND_EXPR_DURATION].getWinsorizedStdev());
//
//            RespDuration.addValue(rip[pvData.valleyIndex.get(i + 1)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp);
//            RespDurationNormalized.addValue(((rip[pvData.valleyIndex.get(i + 1)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp) - RIPBinnedStats[RIPFeatures.FIND_RESP_DURATION].getWinsorizedMean()) / RIPBinnedStats[RIPFeatures.FIND_RESP_DURATION].getWinsorizedStdev());
//
//            Stretch.addValue(rip[pvData.peakIndex.get(i)].value - rip[pvData.valleyIndex.get(i)].value);
//            StretchNormalized.addValue(((rip[pvData.peakIndex.get(i)].value - rip[pvData.valleyIndex.get(i)].value) - RIPBinnedStats[RIPFeatures.FIND_STRETCH].getWinsorizedMean()) / RIPBinnedStats[RIPFeatures.FIND_STRETCH].getWinsorizedStdev());
//
//            IERatio.addValue(InspDuration.getElement((int) InspDuration.getN() - 1) / ExprDuration.getElement((int) ExprDuration.getN() - 1));
//
//            RSA.addValue(rsaCalculateCycle(rip[pvData.valleyIndex.get(i)].timestamp, rip[pvData.valleyIndex.get(i + 1)].timestamp, ecg));
//            RSANormalized.addValue((rsaCalculateCycle(rip[pvData.valleyIndex.get(i)].timestamp, rip[pvData.valleyIndex.get(i + 1)].timestamp, ecg) - RIPBinnedStats[RIPFeatures.FIND_RSA].getWinsorizedMean() / 1000.0) / (RIPBinnedStats[RIPFeatures.FIND_RSA].getWinsorizedStdev() / 1000));
//
//        }
//
//
//        BreathRate = pvData.valleyIndex.size();
//
//        MinuteVolume = 0.0;
//        for (int i = 0; i < pvData.valleyIndex.size(); i++) {
//            MinuteVolume += (rip[pvData.peakIndex.get(i)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp) / 1000.0 * (rip[pvData.peakIndex.get(i)].value - rip[pvData.valleyIndex.get(i)].value) / 2.0;
//        }
//        MinuteVolume *= pvData.valleyIndex.size();
//
//        if (!activity) {
//            RIPStats[0].add(BreathRate);
//            RIPStats[1].add(MinuteVolume);
//        }
//
//        BreathRateNormalized = (BreathRate - RIPStats[0].getMean()) / RIPStats[0].getStdev();
//        MinuteVolumeNormalized = (MinuteVolume - RIPStats[1].getMean()) / RIPStats[1].getStdev();


    }

    public DataPoint[] rawPeakFeatures() {
        return this.peaks;
    }

    public DataPoint[] rawValleyFeatures() {
        return this.valleys;
    }


    private double rsaCalculateCycle(long starttime, long endtime, ECGFeatures ecg) {
//        ArrayList<Integer> cInd = new ArrayList<Integer>();
//        for (int i = 0; i < ecg.RRStatsTimestamps.size(); i++) {
//            if (ecg.RRStatsTimestamps.get(i) >= starttime && ecg.RRStatsTimestamps.get(i) < endtime) {
//                cInd.add(i);
//            }
//        }
//        double max = 0.0;
//        double min = 0.0;
//
//        if (cInd.size() != 0) {
//            max = ecg.RRStats.getElement(cInd.get(0));
//            min = ecg.RRStats.getElement(cInd.get(0));
//
//            for (int i = 0; i < cInd.size(); i++) {
//                if (ecg.RRStats.getElement(i) > max) {
//                    max = ecg.RRStats.getElement(i);
//                }
//                if (ecg.RRStats.getElement(i) < min) {
//                    min = ecg.RRStats.getElement(i);
//                }
//            }
//        }
//        return max - min;
        return 0.0;
    }


}
