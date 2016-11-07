package md2k.mcerebrum.cstress.features;

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.autosense.AUTOSENSE;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.signalprocessing.AutoSense;
import md2k.mcerebrum.cstress.library.signalprocessing.ECG;
import md2k.mcerebrum.cstress.library.signalprocessing.Filter;
import md2k.mcerebrum.cstress.library.signalprocessing.Smoothing;
import md2k.mcerebrum.cstress.library.structs.DataPoint;
import md2k.mcerebrum.cstress.library.structs.Lomb;
import md2k.mcerebrum.cstress.library.structs.MetadataDouble;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
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


/**
 * ECG feature computation class
 */
public class ECGFeatures {


    /**
     * ECG Constructor which handles feature computation.
     *
     * @param datastreams Global data stream object
     */
    public ECGFeatures(DataStreams datastreams) {

        //Compute RR Intervals
        DataPointStream ECGstream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG);
        double frequency = ((MetadataDouble) ECGstream.metadata.get("frequency")).value;

        //Ohio State Algorithm
        int window_l = (int) Math.ceil(frequency / 5.0);

        //Specific to Autosense hardware @ 64Hz and 12-bit values
        double f = 2.0 / frequency;
        double[] F = {0.0, 4.5 * f, 5.0 * f, 20.0 * f, 20.5 * f, 1};
        double[] A = {0, 0, 1, 1, 0, 0};
        double[] w = {500.0 / 0.02, 1.0 / 0.02, 500 / 0.02};
        double fl = AUTOSENSE.FL_INIT;

        DataPointStream y2 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_Y2);
        DataPointStream y2normalized = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_Y2_NORMALIZED);
        AutoSense.applyFilterNormalize(ECGstream, y2, y2normalized, Filter.firls(fl, F, A, w), 90);

        DataPointStream y3 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_Y3);
        DataPointStream y3normalized = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_Y3_NORMALIZED);
        AutoSense.applyFilterNormalize(y2normalized, y3, y3normalized, new double[]{-1.0 / 8.0, -2.0 / 8.0, 0.0 / 8.0, 2.0 / 8.0, 1.0 / 8.0}, 90);

        DataPointStream y4 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_Y4);
        DataPointStream y4normalized = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_Y4_NORMALIZED);
        AutoSense.applySquareFilterNormalize(y3normalized, y4, y4normalized, 90);

        DataPointStream y5 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_Y5);
        DataPointStream y5normalized = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_Y5_NORMALIZED);
        AutoSense.applyFilterNormalize(y4normalized, y5, y5normalized, Filter.blackman(window_l), 90);


        DataPointStream peaks = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_PEAKS);
        findpeaks(peaks, y5normalized);

        DataPointStream rr_ave = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_AVE);
        DataPointStream Rpeak_temp1 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_PEAKS_TEMP1);
        DataPointStream thr1 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_PEAKS_THR1);
        thr1.setPreservedLastInsert(true);
        DataPointStream thr2 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_PEAKS_THR2);
        thr2.setPreservedLastInsert(true);
        DataPointStream sig_lev = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_PEAKS_SIG_LEV);
        sig_lev.setPreservedLastInsert(true);
        DataPointStream noise_lev = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_PEAKS_NOISE_LEV);
        noise_lev.setPreservedLastInsert(true);
        filterPeaks(rr_ave, Rpeak_temp1, peaks, ECGstream, thr1, thr2, sig_lev, noise_lev);

        DataPointStream Rpeak_temp2 = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_PEAKS_TEMP2);
        filterPeaksTemp2(Rpeak_temp2, Rpeak_temp1);

        DataPointStream rpeaks = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_PEAKS_RPEAKS);
        filterRpeaks(rpeaks, Rpeak_temp2, peaks, frequency);

        DataPointStream rr_value = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE);
        computeRRValue(rr_value, rpeaks);

        DataPointStream rr_value_diff = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE_DIFF);
        DataPointStream validfilter_rr_interval = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_VALIDFILTER_RR_VALUE);
        DataPointStream rr_outlier = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_OUTLIER);
        validRRinterval(rr_outlier, validfilter_rr_interval, rr_value_diff, rr_value);

        DataPointStream rr_value_filtered = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE_FILTERED);
        rpeakFilter(validfilter_rr_interval, rr_value_filtered, rr_outlier);

        double activity = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCEL_ACTIVITY).data.get(0).value;
        //Decide if we should add the RR intervals from this minute to the running stats
        if (activity == 0.0) {

            for (int i = 0; i < (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE_FILTERED)).data.size(); i++) {
                if (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_OUTLIER).data.get(i).value == AUTOSENSE.QUALITY_GOOD) {
                    datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR).add((datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE_FILTERED)).data.get(i));
                    DataPoint hr = new DataPoint((datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE_FILTERED)).data.get(i).timestamp, 60.0 / (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE_FILTERED)).data.get(i).value);
                    datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_HEARTRATE).add(hr);
                }
            }

            DataPoint[] rrDatapoints = new DataPoint[datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR).data.size()];
            for (int i = 0; i < rrDatapoints.length; i++) {
                rrDatapoints[i] = new DataPoint(i, datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR).data.get(i).value);
            }

            if (rrDatapoints.length > 0) {
                Lomb HRLomb = ECG.lomb(rrDatapoints);

                double lfhf = ECG.heartRateLFHF(HRLomb.P, HRLomb.f, 0.09, 0.15);
                double lf = ECG.heartRatePower(HRLomb.P, HRLomb.f, 0.1, 0.2);
                double mf = ECG.heartRatePower(HRLomb.P, HRLomb.f, 0.2, 0.3);
                double hf = ECG.heartRatePower(HRLomb.P, HRLomb.f, 0.3, 0.4);

                if (!Double.isInfinite(lfhf) && !Double.isNaN(lfhf)) {
                    (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_LOW_HIGH_FREQUENCY_ENERGY_RATIO)).add(new DataPoint((datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE)).data.get(0).timestamp, lfhf));
                }
                if (!Double.isInfinite(lf) && !Double.isNaN(lf)) {
                    (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_LOMB_LOW_FREQUENCY_ENERGY)).add(new DataPoint((datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE)).data.get(0).timestamp, lf));
                }
                if (!Double.isInfinite(mf) && !Double.isNaN(mf)) {
                    (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_LOMB_MEDIUM_FREQUENCY_ENERGY)).add(new DataPoint((datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE)).data.get(0).timestamp, mf));
                }
                if (!Double.isInfinite(hf) && !Double.isNaN(hf)) {
                    (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_LOMB_HIGH_FREQUENCY_ENERGY)).add(new DataPoint((datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_RR_VALUE)).data.get(0).timestamp, hf));
                }
            }
        }
    }

    /**
     * Determine valid RR-intervals and outliers
     * <p>
     * Reference: Matlab code
     * </p>
     *
     * @param outlierresult     Output outlier datastream
     * @param valid_rr_interval Output valid rr-interval datastream
     * @param rr_value_diff     Output derivitive of rr-intervals datastream
     * @param ds                Input datastream
     */
    private void validRRinterval(DataPointStream outlierresult, DataPointStream valid_rr_interval, DataPointStream rr_value_diff, DataPointStream ds) {
        List<Integer> outlier = new ArrayList<Integer>();

        for (int i1 = 0; i1 < ds.data.size(); i1++) {
            if (ds.data.get(i1).value > 0.3 && ds.data.get(i1).value < 2.0) {
                valid_rr_interval.add(ds.data.get(i1));
            }
        }

        for (int i1 = 1; i1 < valid_rr_interval.data.size(); i1++) {
            rr_value_diff.add(new DataPoint(valid_rr_interval.data.get(i1).timestamp, Math.abs(valid_rr_interval.data.get(i1).value - valid_rr_interval.data.get(i1 - 1).value)));
        }

        double MED = AUTOSENSE.MED_CONSTANT * 0.5 * (rr_value_diff.getPercentile(75) - rr_value_diff.getPercentile(25));
        double MAD = (valid_rr_interval.getPercentile(50) - AUTOSENSE.MAD_CONSTANT * 0.5 * (rr_value_diff.getPercentile(75) - rr_value_diff.getPercentile(25))) / 3.0;
        double CBD = (MED + MAD) / 2.0;
        if (CBD < AUTOSENSE.CBD_THRESHOLD) {
            CBD = AUTOSENSE.CBD_THRESHOLD;
        }

        for (DataPoint aSample : ds.data) {
            outlier.add(AUTOSENSE.QUALITY_BAD);
        }
        outlier.set(0, AUTOSENSE.QUALITY_GOOD);

        double standard_rrInterval;
        if (valid_rr_interval.data.size() > 0) {
            standard_rrInterval = valid_rr_interval.data.get(0).value;
        } else {
            standard_rrInterval = valid_rr_interval.getMean();
        }
        boolean prev_beat_bad = false;

        for (int i1 = 1; i1 < valid_rr_interval.data.size() - 1; i1++) {
            double ref = valid_rr_interval.data.get(i1).value;
            if (ref > AUTOSENSE.REF_MINIMUM && ref < AUTOSENSE.REF_MAXIMUM) {
                double beat_diff_prevGood = Math.abs(standard_rrInterval - valid_rr_interval.data.get(i1).value);
                double beat_diff_pre = Math.abs(valid_rr_interval.data.get(i1 - 1).value - valid_rr_interval.data.get(i1).value);
                double beat_diff_post = Math.abs(valid_rr_interval.data.get(i1).value - valid_rr_interval.data.get(i1 + 1).value);

                if ((prev_beat_bad && beat_diff_prevGood < CBD) || (prev_beat_bad && beat_diff_prevGood > CBD && beat_diff_pre <= CBD && beat_diff_post <= CBD)) {
                    for (int j = 0; j < ds.data.size(); j++) {
                        if (ds.data.get(j).timestamp == valid_rr_interval.data.get(i1).timestamp) {
                            outlier.set(j, AUTOSENSE.QUALITY_GOOD);
                        }
                    }
                    prev_beat_bad = false;
                    standard_rrInterval = valid_rr_interval.data.get(i1).value;
                } else if (prev_beat_bad && beat_diff_prevGood > CBD && (beat_diff_pre > CBD || beat_diff_post > CBD)) {
                    prev_beat_bad = true;
                } else if (!prev_beat_bad && beat_diff_pre <= CBD) {
                    for (int j = 0; j < ds.data.size(); j++) {
                        if (ds.data.get(j).timestamp == valid_rr_interval.data.get(i1).timestamp) {
                            outlier.set(j, AUTOSENSE.QUALITY_GOOD);
                        }
                    }
                    prev_beat_bad = false;
                    standard_rrInterval = valid_rr_interval.data.get(i1).value;
                } else if (!prev_beat_bad && beat_diff_pre > CBD) {
                    prev_beat_bad = true;
                }

            }
        }


        for (int i1 = 0; i1 < outlier.size(); i1++) {
            outlierresult.add(new DataPoint(ds.data.get(i1).timestamp, outlier.get(i1)));
        }
    }

    /**
     * Compute RR intervals in seconds
     * <p>
     * Reference: Matlab code
     * </p>
     *
     * @param rr_value Output rr-interval datastream
     * @param rpeaks   Input r-peak datastream
     */
    private void computeRRValue(DataPointStream rr_value, DataPointStream rpeaks) {
        for (int i1 = 0; i1 < rpeaks.data.size() - 1; i1++) {
            rr_value.add(new DataPoint(rpeaks.data.get(i1).timestamp, (rpeaks.data.get(i1 + 1).timestamp - rpeaks.data.get(i1).timestamp) / 1000.0));
        }

    }

    /**
     * Filter r-peaks
     * <p>
     * Reference: Matlab code
     * </p>
     *
     * @param rr_value          Input rr-value datastream
     * @param rr_value_filtered Output filtered rr-value datastream
     * @param rr_outlier        Input outlier identification of rr-interval datastream
     */
    private void rpeakFilter(DataPointStream rr_value, DataPointStream rr_value_filtered, DataPointStream rr_outlier) {
        for (int i1 = 0; i1 < rr_value.data.size(); i1++) {
            if (rr_outlier.data.get(i1).value == AUTOSENSE.QUALITY_GOOD) {
                rr_value_filtered.add(rr_value.data.get(i1));
            }
        }

        double mu = rr_value_filtered.getMean();
        double sigma = rr_value_filtered.getStandardDeviation();
        for (int i1 = 0; i1 < rr_outlier.data.size(); i1++) {
            if (rr_outlier.data.get(i1).value == AUTOSENSE.QUALITY_GOOD) {
                if (Math.abs(rr_value.data.get(i1).value - mu) > (3.0 * sigma)) {
                    rr_outlier.data.get(i1).value = AUTOSENSE.QUALITY_NOISE;
                } else {
                    rr_outlier.data.get(i1).value = AUTOSENSE.QUALITY_GOOD;
                }
            }
        }
    }

    /**
     * Filter r-peaks
     * <p>
     * Reference: Matlab code
     * </p>
     *
     * @param Rpeaks      output r-peak datastream
     * @param Rpeak_temp2 Input temporary r-peak datastream
     * @param peaks       Input peak datastream
     * @param frequency   Sampling frequence
     */
    private void filterRpeaks(DataPointStream Rpeaks, DataPointStream Rpeak_temp2, DataPointStream peaks, double frequency) {
        List<DataPoint> Rpeak_temp3 = new ArrayList<DataPoint>();
        if (Rpeak_temp2.data.size() > 0) {
            Rpeak_temp3.add(Rpeak_temp2.data.get(0));


            for (int k = 1; k < Rpeak_temp2.data.size() - 1; k++) {
                double maxValue = -1e9;


                double peaktime = Rpeak_temp2.data.get(k).timestamp;
                int windowStart = 0;
                int windowStop = peaks.data.size();
                for (int i1 = 0; i1 < peaks.data.size(); i1++) {
                    if (peaks.data.get(i1).timestamp < (peaktime - (int) Math.ceil(frequency / AUTOSENSE.RPEAK_BIN_FACTOR))) {
                        windowStart = i1;
                    }
                    if (peaks.data.get(i1).timestamp > (peaktime + (int) Math.ceil(frequency / AUTOSENSE.RPEAK_BIN_FACTOR))) {
                        windowStop = i1;
                        break;
                    }
                }

                DataPoint maxDP = new DataPoint(0, 0.0);
                try {
                    for (int j = windowStart + 1; j < windowStop; j++) {
                        if (peaks.data.get(j).value > maxValue) {
                            maxValue = peaks.data.get(j).value;
                            maxDP = new DataPoint(peaks.data.get(j));
                        }
                    }
                } catch (Exception e) {
                    //Do nothing here
                } finally {
                    Rpeak_temp3.add(maxDP);
                }
            }
        }

        for (DataPoint dp : Rpeak_temp3) {
            Rpeaks.add(new DataPoint(dp));
        }
    }

    /**
     * Filter r-peaks Temp 2
     * <p>
     * Reference: Matlab code
     * </p>
     *
     * @param Rpeak_temp2 Output datastream
     * @param Rpeak_temp1 Input datastream
     */
    private void filterPeaksTemp2(DataPointStream Rpeak_temp2, DataPointStream Rpeak_temp1) {
        Rpeak_temp2.data.addAll(Rpeak_temp1.data);

        boolean difference = false;

        while (!difference) {
            int length_Rpeak_temp2 = Rpeak_temp2.data.size();
            List<Long> diffRpeak = new ArrayList<Long>();
            for (int j = 1; j < Rpeak_temp2.data.size(); j++) {
                diffRpeak.add(Rpeak_temp2.data.get(j).timestamp - Rpeak_temp2.data.get(j - 1).timestamp);
            }

            List<Integer> eli_index = new ArrayList<Integer>();

            for (int j = 0; j < diffRpeak.size(); j++) {
                if (diffRpeak.get(j) < (AUTOSENSE.RPEAK_INTERPEAK_LIMIT)) {
                    if (Rpeak_temp2.data.get(j).value < Rpeak_temp2.data.get(j + 1).value) {
                        eli_index.add(0);
                    } else {
                        eli_index.add(1);
                    }
                } else {
                    eli_index.add(-999999);
                }
            }

            for (int j = 0; j < diffRpeak.size(); j++) {
                if (diffRpeak.get(j) < (AUTOSENSE.RPEAK_INTERPEAK_LIMIT)) {
                    Rpeak_temp2.data.set(j + eli_index.get(j), new DataPoint(0, -999999));
                }
            }


            for (Iterator<DataPoint> it = Rpeak_temp2.data.iterator(); it.hasNext(); ) {
                if (it.next().value == -999999) {
                    it.remove();
                }
            }

            difference = (length_Rpeak_temp2 == Rpeak_temp2.data.size());

        }
    }


    /**
     * Filter r-peaks
     * <p>
     * Reference: Matlab code
     * </p>
     * <p>
     * <code>
     * If CURRENTPEAK > THR_SIG, that location is identified as a ìQRS complex
     * candidateî and the signal level (SIG_LEV) is updated:
     * SIG _ LEV = 0.125 ◊CURRENTPEAK + 0.875◊ SIG _ LEV
     * If THR_NOISE < CURRENTPEAK < THR_SIG, then that location is identified as a
     * ìnoise peakî and the noise level (NOISE_LEV) is updated:
     * NOISE _ LEV = 0.125◊CURRENTPEAK + 0.875◊ NOISE _ LEV
     * Based on new estimates of the signal and noise levels (SIG_LEV and NOISE_LEV,
     * respectively) at that point in the ECG, the thresholds are adjusted as follows:
     * THR _ SIG = NOISE _ LEV + 0.25 ◊ (SIG _ LEV ? NOISE _ LEV )
     * THR _ NOISE = 0.5◊ (THR _ SIG)
     * </code>
     * </p>
     *
     * @param rrAverage Output datastream
     * @param temp1     Output temp datastream
     * @param peaks     Input datastream
     * @param ECG       Input ECG datastream
     */
    private void filterPeaks(DataPointStream rrAverage, DataPointStream temp1, DataPointStream peaks, DataPointStream ECG, DataPointStream thr1, DataPointStream thr2, DataPointStream sig_lev, DataPointStream noise_lev) {
        if (thr1.data.size() == 0) {
            thr1.add(new DataPoint(ECG.data.get(0).timestamp, AUTOSENSE.THR1_INIT));
            thr2.add(new DataPoint(ECG.data.get(0).timestamp, 0.5 * AUTOSENSE.THR1_INIT));
            sig_lev.add(new DataPoint(ECG.data.get(0).timestamp, AUTOSENSE.SIG_LEV_FACTOR * AUTOSENSE.THR1_INIT));
            noise_lev.add(new DataPoint(ECG.data.get(0).timestamp, AUTOSENSE.NOISE_LEV_FACTOR * AUTOSENSE.SIG_LEV_FACTOR * AUTOSENSE.THR1_INIT));
        }
        DataPoint rr_ave;

        if (rrAverage.stats.getN() == 0) {
            rrAverage.setPreservedLastInsert(true);
            double rr_avg = 0.0;
            for (int i1 = 1; i1 < peaks.data.size(); i1++) {
                rr_avg += peaks.data.get(i1).value - peaks.data.get(i1 - 1).value;
            }
            rr_avg /= (peaks.data.size() - 1);
            rr_ave = new DataPoint(ECG.data.get(0).timestamp, rr_avg);
            rrAverage.add(rr_ave);
        }
        rr_ave = rrAverage.data.get(rrAverage.data.size() - 1);


        int c1 = 0;
        List<Integer> c2 = new ArrayList<Integer>();

        List<DataPoint> Rpeak_temp1 = temp1.data;

        int i1 = 0;
        while (i1 < peaks.data.size()) {
            if (Rpeak_temp1.size() == 0) {
                if (peaks.data.get(i1).value > thr1.getLatestValue() && peaks.data.get(i1).value < (3.0 * sig_lev.getLatestValue())) {
                    if (Rpeak_temp1.size() <= c1) {
                        Rpeak_temp1.add(new DataPoint(0, 0.0));
                    }
                    Rpeak_temp1.set(c1, peaks.data.get(i1));
                    sig_lev.add(new DataPoint(peaks.data.get(i1).timestamp,Smoothing.ewma(peaks.data.get(i1).value,sig_lev.getLatestValue(),AUTOSENSE.EWMA_ALPHA)));
                    if (c2.size() <= c1) {
                        c2.add(0);
                    }
                    c2.set(c1, i1);
                    c1 += 1;
                } else if (peaks.data.get(i1).value < thr1.getLatestValue() && peaks.data.get(i1).value > thr2.getLatestValue()) {
                    noise_lev.add(new DataPoint(peaks.data.get(i1).timestamp,Smoothing.ewma(peaks.data.get(i1).value,noise_lev.getLatestValue(),AUTOSENSE.EWMA_ALPHA)));
                }

                thr1.add(new DataPoint(peaks.data.get(i1).timestamp, noise_lev.getLatestValue() + 0.25 * (sig_lev.getLatestValue() - noise_lev.getLatestValue())));
                thr2.add(new DataPoint(peaks.data.get(i1).timestamp, 0.5 * thr1.getLatestValue()));
                i1++;
                rr_ave = rrAveUpdate(Rpeak_temp1, rrAverage, ECG.data.get(0).timestamp);
            } else {
                if (((peaks.data.get(i1).timestamp - peaks.data.get(c2.get(c1 - 1)).timestamp) > 1.66 * rr_ave.value) && (i1 - c2.get(c1 - 1)) > 1) {
                    List<Double> searchback_array_inrange = new ArrayList<Double>();
                    List<Integer> searchback_array_inrange_index = new ArrayList<Integer>();
                    
                    for (int j = c2.get(c1 - 1)+1; j < i1 - 1; j++) {
                        if (peaks.data.get(j).value < 3.0 * sig_lev.getLatestValue() && peaks.data.get(j).value > thr2.getLatestValue()) {
                            searchback_array_inrange.add(peaks.data.get(j).value);
                            searchback_array_inrange_index.add(j - c2.get(c1 - 1));
                        }
                    }
                    
                    if (searchback_array_inrange.size() > 0) {
                        double searchback_max = searchback_array_inrange.get(0);
                        int searchback_max_index = 0;
                        for (int j = 0; j < searchback_array_inrange.size(); j++) {
                            if (searchback_array_inrange.get(j) > searchback_max) {
                                searchback_max = searchback_array_inrange.get(j);
                                searchback_max_index = j;
                            }
                        }
                        if (Rpeak_temp1.size() >= c1) {
                            Rpeak_temp1.add(new DataPoint(0, 0.0));
                        }
                        Rpeak_temp1.set(c1, peaks.data.get(c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index)));
                        sig_lev.add(new DataPoint(Rpeak_temp1.get(c1).timestamp,Smoothing.ewma(Rpeak_temp1.get(c1).value,sig_lev.getLatestValue(),AUTOSENSE.EWMA_ALPHA)));
                        if (c1 >= c2.size()) {
                            c2.add(0);
                        }
                        c2.set(c1, c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index));
                        i1 = c2.get(c1) + 1;
                        c1 += 1;
                        thr1.add(new DataPoint(peaks.data.get(i1).timestamp,noise_lev.getLatestValue() + 0.25 * (sig_lev.getLatestValue() - noise_lev.getLatestValue())));
                        thr2.add(new DataPoint(peaks.data.get(i1).timestamp,0.5 * thr1.getLatestValue()));
                        rr_ave = rrAveUpdate(Rpeak_temp1, rrAverage, ECG.data.get(0).timestamp);
                        continue;
                    }
                } else if (peaks.data.get(i1).value >= thr1.getLatestValue() && peaks.data.get(i1).value < (3.0 * sig_lev.getLatestValue())) {
                    if (Rpeak_temp1.size() >= c1) {
                        Rpeak_temp1.add(new DataPoint(0, 0.0));
                    }
                    Rpeak_temp1.set(c1, peaks.data.get(i1));
                    sig_lev.add(new DataPoint(peaks.data.get(i1).timestamp,Smoothing.ewma(peaks.data.get(i1).value,sig_lev.getLatestValue(),AUTOSENSE.EWMA_ALPHA)));
                    if (c2.size() <= c1) {
                        c2.add(0);
                    }
                    c2.set(c1, i1);
                    c1 += 1;
                } else if (peaks.data.get(i1).value < thr1.getLatestValue() && peaks.data.get(i1).value > thr2.getLatestValue()) {
                    noise_lev.add(new DataPoint(peaks.data.get(i1).timestamp,Smoothing.ewma(peaks.data.get(i1).value,noise_lev.getLatestValue(),AUTOSENSE.EWMA_ALPHA)));
                }
                thr1.add(new DataPoint(peaks.data.get(i1).timestamp, noise_lev.getLatestValue() + 0.25 * (sig_lev.getLatestValue() - noise_lev.getLatestValue())));
                thr2.add(new DataPoint(peaks.data.get(i1).timestamp, 0.5 * thr1.getLatestValue()));
                rr_ave = rrAveUpdate(Rpeak_temp1, rrAverage, ECG.data.get(0).timestamp);
                i1++;
            }
        }
    }

    /**
     * Identfy peaks in the y5 nroamlized datastream
     * <p>
     * Reference: Matlab code
     * </p>
     *
     * @param peaks        Output datastream
     * @param y5normalized Input datastream
     */
    private void findpeaks(DataPointStream peaks, DataPointStream y5normalized) {
        for (int i = 2; i < y5normalized.data.size() - 2; i++) {
            if (y5normalized.data.get(i - 2).value < y5normalized.data.get(i - 1).value &&
                    y5normalized.data.get(i - 1).value < y5normalized.data.get(i).value &&
                    y5normalized.data.get(i).value >= y5normalized.data.get(i + 1).value &&
                    y5normalized.data.get(i + 1).value > y5normalized.data.get(i + 2).value) {
                peaks.add(new DataPoint(y5normalized.data.get(i)));
            }
        }
    }


    /**
     * rr-ave (Average) update method
     * <p>
     * Reference: Matlab code
     * </p>
     *
     * @param rpeak_temp1 Input datastream
     * @param rr_ave      Current rr average
     * @return New rr average
     */
    private DataPoint rrAveUpdate(List<DataPoint> rpeak_temp1, DataPointStream rr_ave, long initial_ts) {
        List<Long> peak_interval = new ArrayList<Long>();
        DataPoint result = rr_ave.data.get(rr_ave.data.size() - 1);
        if (rpeak_temp1.size() != 0) {
            peak_interval.add(rpeak_temp1.get(0).timestamp-initial_ts);
            for (int i = 1; i < rpeak_temp1.size(); i++) {
                peak_interval.add(rpeak_temp1.get(i).timestamp - rpeak_temp1.get(i - 1).timestamp);
            }

            if (peak_interval.size() >= AUTOSENSE.PEAK_INTERVAL_MINIMUM_SIZE) {
                long rr_ave_temp = 0;
                for (int i = peak_interval.size() - AUTOSENSE.PEAK_INTERVAL_MINIMUM_SIZE; i < peak_interval.size(); i++) {
                    rr_ave_temp += peak_interval.get(i);
                }
                rr_ave.add(new DataPoint(rpeak_temp1.get(rpeak_temp1.size() - 1).timestamp, rr_ave_temp / 8.0));
                result = rr_ave.data.get(rr_ave.data.size() - 1);
            }
        }
        return result;
    }

}
