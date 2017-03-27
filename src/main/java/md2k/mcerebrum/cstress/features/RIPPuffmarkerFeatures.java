package md2k.mcerebrum.cstress.features;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Nazir Saleheen <nsleheen@memphis.edu>
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

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.autosense.AUTOSENSE;
import md2k.mcerebrum.cstress.autosense.PUFFMARKER;
import md2k.mcerebrum.cstress.library.Vector;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.signalprocessing.Smoothing;
import md2k.mcerebrum.cstress.library.structs.DataPoint;
import md2k.mcerebrum.cstress.library.structs.MetadataDouble;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Respiration feature computation class
 */
public class RIPPuffmarkerFeatures {

    final int BUFFER_SIZE = 5;

    /**
     * Core Respiration Features for puffMarker
     * <p>
     * Reference: ripFeature_Extraction.m
     * </p>
     *
     * @param datastreams Global data stream object
     */
    public RIPPuffmarkerFeatures(DataStreams datastreams) {

        DataPointStream rip = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP);
        DataPointStream rip2min = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP);
        int wLen = (int) Math.round(PUFFMARKER.BUFFER_SIZE_HALF_MIN_SEC * ((MetadataDouble) datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP).metadata.get("frequency")).value);
        rip2min.setHistoricalBufferSize(wLen);
        long timestamp2minbefore = rip.data.get(0).timestamp - PUFFMARKER.BUFFER_SIZE_HALF_MIN_SEC * 1000;
        List<DataPoint> listHistoryRIP = new ArrayList<>(rip2min.getHistoricalValues(timestamp2minbefore));
        rip2min.addAll(listHistoryRIP);
        rip2min.addAll(rip.data);

        DataPointStream rip_smooth = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_SMOOTH);
        Smoothing.smooth(rip_smooth, rip2min, AUTOSENSE.PEAK_VALLEY_SMOOTHING_SIZE+3);

/*
        DataPointStream rip_smooth_kalman = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_SMOOTH+".KALMAN.FILTER");
        kalmanFilter(rip2min, rip_smooth_kalman, 500, 1, 50);
*/

        int windowLength = (int) Math.round(AUTOSENSE.WINDOW_LENGTH_SECS * ((MetadataDouble) datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP).metadata.get("frequency")).value);
        DataPointStream rip_mac = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_MAC);
        Smoothing.smooth(rip_mac, rip_smooth, windowLength); //TWH: Replaced MAC with Smooth after discussion on 11/9/2015

        DataPointStream upIntercepts = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_UP_INTERCEPTS);
        DataPointStream downIntercepts = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_DOWN_INTERCEPTS);
        generateIntercepts(upIntercepts, downIntercepts, rip_smooth, rip_mac);

        DataPointStream upInterceptsFiltered = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_UP_INTERCEPTS_FILTERED);
        DataPointStream downInterceptsFiltered = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_DOWN_INTERCEPTS_FILTERED);
        filterIntercepts(upInterceptsFiltered, downInterceptsFiltered, upIntercepts, downIntercepts);

        DataPointStream upInterceptsFilteredTime = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_UP_INTERCEPTS_FILTERED_TIME);
        DataPointStream downInterceptsFilteredTime = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_DOWN_INTERCEPTS_FILTERED_TIME);
        filterByTime(upInterceptsFilteredTime, downInterceptsFilteredTime, upInterceptsFiltered, downInterceptsFiltered);

        DataPointStream peaks = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_PEAKS);
        generatePeaks(peaks, upInterceptsFilteredTime, downInterceptsFilteredTime, rip2min); //rip2min

        DataPointStream valleys = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_VALLEYS);
        generateValleys(valleys, upInterceptsFilteredTime, downInterceptsFilteredTime, rip2min);//rip2min

        DataPointStream valleysFiltered = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_VALLEYS_FILTERED);
        DataPointStream peaksFiltered = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_PEAKS_FILTERED);
        filterPeaksAndValleys(peaksFiltered, valleysFiltered,  peaks, valleys);

        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_RESPIRATION_CYCLE_COUNT).add(new DataPoint(rip.data.get(0).timestamp, valleysFiltered.data.size()));

        //Key features
        DataPointStream inspDurationDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_INSPDURATION);
        inspDurationDataStream.setHistoricalBufferSize(BUFFER_SIZE);

        DataPointStream exprDurationDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_EXPRDURATION);
        exprDurationDataStream.setHistoricalBufferSize(BUFFER_SIZE);

        DataPointStream respDurationDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_RESPDURATION);
        respDurationDataStream.setHistoricalBufferSize(BUFFER_SIZE);

        DataPointStream stretchDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_STRETCH);
        stretchDataStream.setHistoricalBufferSize(BUFFER_SIZE);

        int nCycle = valleysFiltered.data.size() - 1;

        for (int i = 0; i < nCycle; i++) {
            long timestamp = valleysFiltered.data.get(i).timestamp;

            DataPoint inspDuration = new DataPoint(timestamp, peaksFiltered.data.get(i).timestamp - valleysFiltered.data.get(i).timestamp);
            inspDurationDataStream.add(inspDuration);

            DataPoint exprDuration = new DataPoint(timestamp, valleysFiltered.data.get(i + 1).timestamp - peaksFiltered.data.get(i).timestamp);
            exprDurationDataStream.add(exprDuration);

            DataPoint respDuration = new DataPoint(timestamp, valleysFiltered.data.get(i + 1).timestamp - valleysFiltered.data.get(i).timestamp);
            respDurationDataStream.add(respDuration);

            DataPoint stretch = new DataPoint(timestamp, peaksFiltered.data.get(i).value - valleysFiltered.data.get(i + 1).value);
            stretchDataStream.add(stretch);

            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_IERATIO)).add(new DataPoint(valleysFiltered.data.get(i).timestamp, inspDuration.value / exprDuration.value));
        }
        for (int i = 0; i < nCycle; i++) {
            long timestamp = valleysFiltered.data.get(i).timestamp;
            double backwardInspDur = 0;
            double backwardExprDur = 0;
            double backwardRespDur = 0;
            double backwardStretchDur = 0;
            double forwardInspDur = 0;
            double forwardExprDur = 0;
            double forwardRespDur = 0;
            double forwardStretchDur = 0;

            if (i > 0) {
                backwardInspDur = inspDurationDataStream.data.get(i).value - inspDurationDataStream.data.get(i - 1).value;
                backwardExprDur = exprDurationDataStream.data.get(i).value - exprDurationDataStream.data.get(i - 1).value;
                backwardRespDur = respDurationDataStream.data.get(i).value - respDurationDataStream.data.get(i - 1).value;
                backwardStretchDur = stretchDataStream.data.get(i).value - stretchDataStream.data.get(i - 1).value;
            }
            if (i < nCycle-1) {
                forwardInspDur = inspDurationDataStream.data.get(i).value - inspDurationDataStream.data.get(i + 1).value;
                forwardExprDur = exprDurationDataStream.data.get(i).value - exprDurationDataStream.data.get(i + 1).value;
                forwardRespDur = respDurationDataStream.data.get(i).value - respDurationDataStream.data.get(i + 1).value;
                forwardStretchDur = stretchDataStream.data.get(i).value - stretchDataStream.data.get(i + 1).value;
            }

            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_INSPDURATION_BACK_DIFFERENCE)).add(new DataPoint(timestamp, backwardInspDur));
            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_INSPDURATION_FORWARD_DIFFERENCE)).add(new DataPoint(timestamp, forwardInspDur));

            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_EXPRDURATION_BACK_DIFFERENCE)).add(new DataPoint(timestamp, backwardExprDur));
            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_EXPRDURATION_FORWARD_DIFFERENCE)).add(new DataPoint(timestamp, forwardExprDur));

            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_RESPDURATION_BACK_DIFFERENCE)).add(new DataPoint(timestamp, backwardRespDur));
            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_RESPDURATION_FORWARD_DIFFERENCE)).add(new DataPoint(timestamp, forwardRespDur));

            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_STRETCH_BACK_DIFFERENCE)).add(new DataPoint(timestamp, backwardStretchDur));
            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_STRETCH_FORWARD_DIFFERENCE)).add(new DataPoint(timestamp, forwardStretchDur));

            double d5_stretch = 0;
            double d5_exp = 0;
            int cnt = 0;
            for (int j = -2; j <= 2 && i + j < nCycle; j++) {
                if (i + j < 0 || j == 0) continue;
                d5_stretch += stretchDataStream.data.get(i + j).value;
                d5_exp += exprDurationDataStream.data.get(i + j).value;
                cnt++;
            }
            d5_stretch /= cnt;
            d5_stretch = stretchDataStream.data.get(i).value / d5_stretch;
            d5_exp /= cnt;
            d5_exp = exprDurationDataStream.data.get(i).value / d5_exp;

            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_D5_STRETCH)).add(new DataPoint(valleysFiltered.data.get(i).timestamp, d5_stretch));
            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_D5_EXPRDURATION)).add(new DataPoint(valleysFiltered.data.get(i).timestamp, d5_exp));

            double maxAmplitude = 0;
            double minAmplitude = Double.MAX_VALUE;

            for (int j=0; j<rip2min.data.size()-1;j++) {
                if (rip2min.data.get(j).timestamp == rip2min.data.get(j+1).timestamp) continue;

                if (rip2min.data.get(j).timestamp>=valleysFiltered.data.get(i).timestamp && rip2min.data.get(j).timestamp<=valleysFiltered.data.get(i+1).timestamp) {
                    maxAmplitude = Math.max(maxAmplitude, rip2min.data.get(j).value);
                    minAmplitude = Math.min(minAmplitude, rip2min.data.get(j).value);
                }
            }
            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_MAX_AMPLITUDE)).add(new DataPoint(timestamp, (maxAmplitude-minAmplitude)/2));
            (datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_MIN_AMPLITUDE)).add(new DataPoint(timestamp, (minAmplitude-maxAmplitude)/2 ));

        }
    }

    private void kalmanFilter(DataPointStream rip2min, DataPointStream rip_filter_signal, double p, double q, double r) {
        double x=0;
        for (DataPoint dp:rip2min.data) {
            p=p+q;
            double k = p / (p+r);
            x = x+k*(dp.value-x);
            p = (1-k)*p;
            rip_filter_signal.add(new DataPoint(dp.timestamp, x));
        }
    }

    private double getPreviousValue(DataPointStream dataPointStream, int currentIndex, int previousOffset, double defaultValue) {
        if (currentIndex + previousOffset >= 0) return dataPointStream.data.get(currentIndex + previousOffset).value;
        return defaultValue;
    }

    /**
     * Filter peaks and valleys from data streams
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param peaksFiltered            Filtered peaks output
     * @param valleysFiltered          Filtered valleys output
     * @param peaks                    Peak datastream
     * @param valleys                  Valley datastream
     */
    private void filterPeaksAndValleys(DataPointStream peaksFiltered, DataPointStream valleysFiltered, DataPointStream peaks, DataPointStream valleys) {

        List<DataPoint> tempValleys = new ArrayList<DataPoint>();
        List<DataPoint> tempPeaks = new ArrayList<DataPoint>();
        double TH_MIN_INS_EXP_DUR = 400;

        for (int i1 = 0; i1 < valleys.data.size(); i1++) {
            if (peaks.data.get(i1).timestamp - valleys.data.get(i1).timestamp > TH_MIN_INS_EXP_DUR) {
                tempValleys.add(valleys.data.get(i1));
                tempPeaks.add(peaks.data.get(i1));
            }
        }
        valleysFiltered.add(tempValleys.get(0));

        for (int i1 = 1; i1 < tempValleys.size(); i1++) {
            if (tempValleys.get(i1).timestamp - tempPeaks.get(i1-1).timestamp  > TH_MIN_INS_EXP_DUR) {
                peaksFiltered.add(tempPeaks.get(i1-1));
                valleysFiltered.add(tempValleys.get(i1));
            }
        }
        peaksFiltered.add(tempPeaks.get(tempPeaks.size()-1));
    }

    /**
     * Compute valleys in a respiration datastream
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param valleys                       Output valley datastream
     * @param U   Up intercept datastream
     * @param D Down insercept datastream
     * @param rip_smooth                    Smoothed RIP datastream
     */
    private void generateValleys(DataPointStream valleys, DataPointStream U, DataPointStream D, DataPointStream rip_smooth) {

        for (int i1 = 0; i1 < D.data.size() - 1; i1++) {
            DataPoint valley = findMinPoint(D.data.get(i1), U.data.get(i1), rip_smooth);
            if (valley.timestamp != 0) {
                valleys.add(valley);
            }
        }
    }

    /**
     * Compute peaks in a respiration datastream
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param peaks                         Output valley datastream
     * @param U   Up intercept datastream
     * @param D Down insercept datastream
     * @param rip_smooth                    Smoothed RIP datastream
     */
    private void generatePeaks(DataPointStream peaks, DataPointStream U, DataPointStream D, DataPointStream rip_smooth) {

        for (int i1 = 1; i1 < D.data.size(); i1++) {
            DataPoint peak = findMaxPoint(U.data.get(i1-1), D.data.get(i1), rip_smooth);
            if (peak.timestamp != 0) {
                peaks.add(peak);
            }
        }

    }

    /**
     * Filter up and down intercepts based on 1 second window
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param UTime   Output filtered up intercepts
     * @param DTime Output filtered down intercepts
     * @param U       Input up intercepts
     * @param D     Input down intercepts
     */
    private void filterByTime(DataPointStream UTime, DataPointStream DTime, DataPointStream U, DataPointStream D) {
        double TH_MIN_GAP_INS_EXP = 50.0;

        DTime.add(D.data.get(0));
        int i=0;
        while (U.data.get(i).timestamp < D.data.get(0).timestamp+TH_MIN_GAP_INS_EXP) i++;
        UTime.add(U.data.get(i));

        for (int i1 = i+1; i1 < D.data.size()-1; i1++) {
            if ((U.data.get(i1).timestamp - D.data.get(i1).timestamp) < TH_MIN_GAP_INS_EXP) {
                continue;
            } else if ((D.data.get(i1).timestamp - UTime.data.get(UTime.data.size()-1).timestamp) < TH_MIN_GAP_INS_EXP) {
                UTime.data.get(UTime.data.size()-1).timestamp = U.data.get(i1).timestamp;
                UTime.data.get(UTime.data.size()-1).value = U.data.get(i1).value;
            } else {
                DTime.add(D.data.get(i1));
                UTime.add(U.data.get(i1));
            }
        }
        DTime.add(D.data.get(D.data.size() - 1));

    }


    /**
     * Filter up and down intercepts
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param upInterceptsFiltered   Output filtered up intercepts
     * @param downInterceptsFiltered Output filtered down intercepts
     * @param upIntercepts           Input up intercepts
     * @param downIntercepts         Input down intercepts
     */
    private void filterIntercepts(DataPointStream upInterceptsFiltered, DataPointStream downInterceptsFiltered, DataPointStream upIntercepts, DataPointStream downIntercepts) {
        int upPointer = 0;
        int downPointer = 0;
        boolean updownstate = true; //True check for up intercept


        downInterceptsFiltered.add(downIntercepts.data.get(downPointer)); //Initialize with starting point

        while (downPointer != downIntercepts.data.size() && upPointer != upIntercepts.data.size()) {
            if (updownstate) { //Check for up intercept
                if (downIntercepts.data.get(downPointer).timestamp < upIntercepts.data.get(upPointer).timestamp) {
                    //Replace down intercept
                    downInterceptsFiltered.data.get(downInterceptsFiltered.data.size() - 1).timestamp = downIntercepts.data.get(downPointer).timestamp;
                    downInterceptsFiltered.data.get(downInterceptsFiltered.data.size() - 1).value = downIntercepts.data.get(downPointer).value;
                    downPointer++;
                } else {
                    //Found up intercept
                    upInterceptsFiltered.add(upIntercepts.data.get(upPointer));
                    upPointer++;
                    updownstate = false;
                }
            } else { //Check for down intercept
                if (downIntercepts.data.get(downPointer).timestamp > upIntercepts.data.get(upPointer).timestamp) {
                    //Replace up intercept
                    upInterceptsFiltered.data.get(upInterceptsFiltered.data.size() - 1).timestamp = upIntercepts.data.get(upPointer).timestamp;
                    upInterceptsFiltered.data.get(upInterceptsFiltered.data.size() - 1).value = upIntercepts.data.get(upPointer).value;
                    upPointer++;
                } else {
                    //Found down intercept
                    downInterceptsFiltered.add(downIntercepts.data.get(downPointer));
                    downPointer++;
                    updownstate = true;
                }
            }
        }

    }

    /**
     * Compute up and down intercepts from RIP signal
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param upIntercepts   Output up intercept datastream
     * @param downIntercepts Output down intercept datastream
     * @param rip_smooth     Smoothed RIP datastream
     * @param rip_mac        RIP datastream
     */
    private void generateIntercepts(DataPointStream upIntercepts, DataPointStream downIntercepts, DataPointStream rip_smooth, DataPointStream rip_mac) {
        for (int i1 = 1; i1 < rip_mac.data.size() - 1; i1++) {

            if (rip_smooth.data.get(i1 - 1).value < rip_mac.data.get(i1).value && rip_smooth.data.get(i1 + 1).value >= rip_mac.data.get(i1).value) {
                upIntercepts.add(rip_mac.data.get(i1));
            } else if (rip_smooth.data.get(i1 - 1).value >= rip_mac.data.get(i1).value && rip_smooth.data.get(i1 + 1).value < rip_mac.data.get(i1).value) {
                downIntercepts.add(rip_mac.data.get(i1));
            }

        }
    }


    /**
     * Compute up and down intercepts from RIP signal
     * <p>
     * Reference: Matlab code \\TODO
     * <a href="https://en.wikipedia.org/wiki/Vagal_tone#Relation_to_respiratory_sinus_arrhythmia">https://en.wikipedia.org/wiki/Vagal_tone#Relation_to_respiratory_sinus_arrhythmia</a>
     * </p>
     *
     * @param starttime   Beginning time of data window
     * @param endtime     Ending time of data window
     * @param rrintervals Input rr-interval datastream
     * @return Max - min of the ECG signal within the RSA window
     */
    private DataPoint rsaCalculateCycle(long starttime, long endtime, DataPointStream rrintervals) {
        DataPoint result = new DataPoint(starttime, -1.0);

        DataPoint max = new DataPoint(0, 0.0);
        DataPoint min = new DataPoint(0, 0.0);
        boolean maxFound = false;
        boolean minFound = false;
        for (DataPoint dp : rrintervals.data) {
            if (dp.timestamp > starttime && dp.timestamp < endtime) {
                if (max.timestamp == 0 && min.timestamp == 0) {
                    max = new DataPoint(dp);
                    min = new DataPoint(dp);
                } else {
                    if (dp.value > max.value) {
                        max = new DataPoint(dp);
                        maxFound = true;
                    }
                    if (dp.value < min.value) {
                        min = new DataPoint(dp);
                        minFound = true;
                    }
                }
            }
        }

        if (maxFound && minFound) {
            result.value = max.value - min.value; //RSA amplitude
        }
        return result;
    }

    /**
     * Identifies valleys in a datastream
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param downIntercept Down intercept DataPoint
     * @param upIntercept   Up intercept DataPoint
     * @param data          Input datastream
     * @return Valley point from data located between the downIntercept and upIntercept
     */
    private DataPoint findValley(DataPoint downIntercept, DataPoint upIntercept, DataPointStream data) {
        DataPoint result = new DataPoint(upIntercept);

        List<DataPoint> temp = new ArrayList<DataPoint>();
        for (int i = 0; i < data.data.size(); i++) { //Identify potential data points
            if (downIntercept.timestamp < data.data.get(i).timestamp && data.data.get(i).timestamp < upIntercept.timestamp) {
                temp.add(data.data.get(i));
            }
        }
        if (temp.size() > 1) {
            List<DataPoint> diff = Vector.diff(temp);
            boolean positiveSlope = false;
            if (diff.get(0).value > 0) {
                positiveSlope = true;
            }

            List<Integer> localMinCandidates = new ArrayList<Integer>();
            for (int i = 1; i < diff.size(); i++) {
                if (positiveSlope) {
                    if (diff.get(i).value < 0) {
                        //Local Max
                        positiveSlope = false;
                    }
                } else {
                    if (diff.get(i).value > 0) {
                        //Local Min
                        localMinCandidates.add(i);
                        positiveSlope = true;
                    }
                }
            }

            int maximumSlopeLength = 0;
            for (Integer i : localMinCandidates) {
                int tempLength = 0;
                for (int j = i; j < diff.size(); j++) {
                    if (diff.get(j).value > 0) {
                        tempLength++;
                    } else {
                        break;
                    }
                }
                if (tempLength > maximumSlopeLength) {
                    maximumSlopeLength = tempLength;
                    result = temp.get(i);
                }
            }
        }
        return result;
    }

    /**
     * Identifies peaks in a datastream
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param downIntercept Down intercept DataPoint
     * @param upIntercept   Up intercept DataPoint
     * @param data          Input datastream
     * @return Peak point from data located between the upIntercept and downIntercept
     */
    public DataPoint findPeak(DataPoint upIntercept, DataPoint downIntercept, DataPointStream data) {

        ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
        for (int i = 0; i < data.data.size(); i++) { //Identify potential data points
            if (upIntercept.timestamp < data.data.get(i).timestamp && data.data.get(i).timestamp < downIntercept.timestamp) {
                temp.add(data.data.get(i));
            }
        }
        if (temp.size() > 0) {
            DataPoint max = temp.get(0);
            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).value > max.value) {
                    max = temp.get(i);
                }
            }

            return max;
        } else {
            return new DataPoint(0, 0.0);
        }
    }

    /**
     * Identifies peaks in a datastream
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param endPoint Down intercept DataPoint
     * @param startPoint   Up intercept DataPoint
     * @param data          Input datastream
     * @return Peak point from data located between the upIntercept and downIntercept
     */
    public DataPoint findMaxPoint(DataPoint startPoint, DataPoint endPoint, DataPointStream data) {

        ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
        DataPoint max = new DataPoint(0, -222222.0);
        for (int i = 0; i < data.data.size(); i++) { //Identify potential data points
            if (startPoint.timestamp <= data.data.get(i).timestamp && data.data.get(i).timestamp <= endPoint.timestamp) {
                temp.add(data.data.get(i));
                if (data.data.get(i).value > max.value)
                    max = data.data.get(i);
            }
        }
        return max;
    }


    /**
     * Identifies peaks in a datastream
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param endPoint Down intercept DataPoint
     * @param startPoint   Up intercept DataPoint
     * @param data          Input datastream
     * @return Peak point from data located between the upIntercept and downIntercept
     */
    public DataPoint findMinPoint(DataPoint startPoint, DataPoint endPoint, DataPointStream data) {

        ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
        DataPoint min = new DataPoint(0, 2222222);
        for (int i = 0; i < data.data.size(); i++) { //Identify potential data points
            if (startPoint.timestamp <= data.data.get(i).timestamp && data.data.get(i).timestamp <= endPoint.timestamp) {
                temp.add(data.data.get(i));
                if (data.data.get(i).value < min.value)
                    min = data.data.get(i);
            }
        }
        return min;
    }

}
