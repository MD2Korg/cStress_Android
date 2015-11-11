package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Library.Core;
import md2k.mCerebrum.cStress.Library.DataStream;
import md2k.mCerebrum.cStress.Statistics.BinnedStatistics;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.Lomb;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
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
public class ECGFeatures {

//    public DescriptiveStatistics RRStats;
//    public DescriptiveStatistics RRHeartRateStats;
//    public DescriptiveStatistics RRStatsNormalized;
//    public ArrayList<Long> RRStatsTimestamps;

//    //Data inputs
//    private final DataPoint[] datapoints;
//    private final double frequency;
//
//    private double HeartRate;
//    private double LombLowFrequencyEnergy;
//    private double LombMediumFrequencyEnergy;
//    private double LombHighFrequencyEnergy;
//    private double LombLowHighFrequencyEnergyRatio;

    public ECGFeatures(HashMap<String, DataStream> datastreams) {

        //Compute RR Intervals
        datastreams = computeRR(datastreams);

        double activity = datastreams.get("org.md2k.cstress.data.accel.activity").data.get(0).value;
        //Decide if we should add the RR intervals from this minute to the running stats
        if (activity == 0.0) {

            if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr")) {
                datastreams.put("org.md2k.cstress.data.ecg.rr", new DataStream("ECG-rr"));
            }
            if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr.heartrate")) {
                datastreams.put("org.md2k.cstress.data.ecg.rr.heartrate", new DataStream("ECG-rr-heartrate"));
            }
            for (int i = 0; i < datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.size(); i++) {
                if (datastreams.get("org.md2k.cstress.data.ecg.outlier").data.get(i).value == AUTOSENSE.QUALITY_GOOD) {
                    datastreams.get("org.md2k.cstress.data.ecg.rr").add(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i));
                    DataPoint hr = new DataPoint(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i).timestamp, 60.0 / datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i).value);
                    datastreams.get("org.md2k.cstress.data.ecg.rr.heartrate").add(hr);
                }
            }
            //HeartRate = datastreams.get("org.md2k.cstress.data.ecg.rr.heartrate").descriptiveStats.getPercentile(50);

            DataPoint[] rrDatapoints = new DataPoint[(int) datastreams.get("org.md2k.cstress.data.ecg.rr").data.size()];
            for (int i = 0; i < rrDatapoints.length; i++) {
                rrDatapoints[i] = new DataPoint(i, datastreams.get("org.md2k.cstress.data.ecg.rr").data.get(i).value);
            }


            Lomb HRLomb = Core.lomb(rrDatapoints);

            if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr.LowHighFrequencyEnergyRatio")) {
                datastreams.put("org.md2k.cstress.data.ecg.rr.LowHighFrequencyEnergyRatio", new DataStream("ECG-rr-LowHighFrequencyEnergyRatio"));
            }
            if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr.LombLowFrequencyEnergy")) {
                datastreams.put("org.md2k.cstress.data.ecg.rr.LombLowFrequencyEnergy", new DataStream("ECG-rr-LombLowFrequencyEnergy"));
            }
            if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr.LombMediumFrequencyEnergy")) {
                datastreams.put("org.md2k.cstress.data.ecg.rr.LombMediumFrequencyEnergy", new DataStream("ECG-rr-LombMediumFrequencyEnergy"));
            }
            if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr.LombHighFrequencyEnergy")) {
                datastreams.put("org.md2k.cstress.data.ecg.rr.LombHighFrequencyEnergy", new DataStream("ECG-rr-LombHighFrequencyEnergy"));
            }

            datastreams.get("org.md2k.cstress.data.ecg.rr.LowHighFrequencyEnergyRatio").add(new DataPoint( datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, Core.heartRateLFHF(HRLomb.P, HRLomb.f, 0.09, 0.15)));
            datastreams.get("org.md2k.cstress.data.ecg.rr.LombLowFrequencyEnergy").add(new DataPoint( datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, Core.heartRatePower(HRLomb.P, HRLomb.f, 0.1, 0.2)));
            datastreams.get("org.md2k.cstress.data.ecg.rr.LombMediumFrequencyEnergy").add(new DataPoint( datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, Core.heartRatePower(HRLomb.P, HRLomb.f, 0.2, 0.3)));
            datastreams.get("org.md2k.cstress.data.ecg.rr.LombHighFrequencyEnergy").add(new DataPoint( datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, Core.heartRatePower(HRLomb.P, HRLomb.f, 0.3, 0.4)));
        }
    }

    /**
     * Compute RR intervals
     */
    private HashMap<String, DataStream> computeRR(HashMap<String, DataStream> datastreams) {

        String rpeaks = Core.detect_Rpeak(datastreams);

        if(datastreams.get(rpeaks).data.size() < 2 ) {
            System.err.println("Not enough peaks, aborting computation");
            return datastreams;
        }

        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr_value")) {
            datastreams.put("org.md2k.cstress.data.ecg.rr_value", new DataStream("ECG-rr_value"));
        }

        DataStream rpeaks_temp = datastreams.get("org.md2k.cstress.data.ecg.peaks.rpeaks");
        for (int i = 0; i < rpeaks_temp.data.size()-1; i++) {
            datastreams.get("org.md2k.cstress.data.ecg.rr_value").add(new DataPoint (rpeaks_temp.data.get(i).timestamp, (rpeaks_temp.data.get(i+1).timestamp - rpeaks_temp.data.get(i).timestamp) / 1000.0));
        }

        String rr_outlier = Core.detect_outlier_v2(datastreams);
        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr_value.filtered")) {
            datastreams.put("org.md2k.cstress.data.ecg.rr_value.filtered", new DataStream("ECG-rr_value-filtered"));
        }
        for(int i=0; i< datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.size(); i++) {
            if(datastreams.get(rr_outlier).data.get(i).value == AUTOSENSE.QUALITY_GOOD) {
                datastreams.get("org.md2k.cstress.data.ecg.rr_value.filtered").add(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i));
            }
        }

        double mu = datastreams.get("org.md2k.cstress.data.ecg.rr_value.filtered").stats.getMean();
        double sigma = datastreams.get("org.md2k.cstress.data.ecg.rr_value.filtered").stats.getStandardDeviation();
        for (int i = 0; i < datastreams.get(rr_outlier).data.size(); i++) {
            if (datastreams.get(rr_outlier).data.get(i).value == AUTOSENSE.QUALITY_GOOD) {
                if (Math.abs(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i).value - mu) > (3.0 * sigma)) {
                    datastreams.get(rr_outlier).data.get(i).value = AUTOSENSE.QUALITY_NOISE;
                } else {
                    datastreams.get(rr_outlier).data.get(i).value = AUTOSENSE.QUALITY_GOOD;
                }
            }
        }

        return datastreams;
    }


//    public double getHeartRate() {
//        return HeartRate;
//    }
//
//    public double getLombLowFrequencyEnergy() {
//        return LombLowFrequencyEnergy;
//    }
//
//    public double getLombMediumFrequencyEnergy() {
//        return LombMediumFrequencyEnergy;
//    }
//
//    public double getLombHighFrequencyEnergy() {
//        return LombHighFrequencyEnergy;
//    }
//
//    public double getLombLowHighFrequencyEnergyRatio() {
//        return LombLowHighFrequencyEnergyRatio;
//    }
}
