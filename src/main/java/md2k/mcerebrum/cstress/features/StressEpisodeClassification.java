package md2k.mcerebrum.cstress.features;

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.library.datastream.DataArrayStream;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.signalprocessing.Smoothing;
import md2k.mcerebrum.cstress.library.structs.DataPoint;
import md2k.mcerebrum.cstress.library.structs.DataPointArray;

import java.util.List;
import java.util.ArrayList;

/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Hillol Sarker <hsarker@memphis.edu>
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

public class StressEpisodeClassification {

    public static final double macdParamFast = 7;
    public static final double macdParamSlow = 19;
    public static final double macdParamSignal = 2;

    public static final double thresholdYes = 0.36;
    public static final double thresholdNo = 0.36;

    public static final int stressProbabilitySmoothingWindow = 3; // 3 minutes
    public static final int BUFFER_SIZE = 1000;
    public static final int BUFFER_SIZE_SMALL = 100;

    public StressEpisodeClassification(DataStreams datastreams, long windowSize) {

        DataPointStream dsStressProbability = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY);
        DataPointStream dsStressRIPProbability = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_RIP_PROBABILITY);

        DataPointStream dsStressProbabilityImputed = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY_IMPUTED);
        dsStressProbabilityImputed.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsStressProbabilitySmoothed = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY_SMOOTHED);
        dsStressProbabilitySmoothed.setHistoricalBufferSize(BUFFER_SIZE);

        DataPointStream dsStressProbabilityAvailable = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY_AVAILABLE);
        dsStressProbabilityAvailable.setHistoricalBufferSize(BUFFER_SIZE);

        DataPointStream dsEmaFast = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_EMA_FAST);
        dsEmaFast.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsEmaSlow = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_EMA_SLOW);
        dsEmaSlow.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsEmaSignal = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_EMA_SIGNAL);
        dsEmaSignal.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsEmaHistogram = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_HISTOGRAM);
        dsEmaHistogram.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsStressEpisodeClassification = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_CLASSIFICATION);
        dsStressEpisodeClassification.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataArrayStream arStressEpisodeClassification = datastreams.getDataArrayStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_ARRAY_CLASSIFICATION);
        arStressEpisodeClassification.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsStressEpisodeStart = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_START);
        dsStressEpisodeClassification.setHistoricalBufferSize(BUFFER_SIZE_SMALL);



        if (dsStressProbability.data.size() > 0) {
            DataPoint stressProbability = new DataPoint(dsStressProbability.getLatestTimestamp(), dsStressProbability.getLatestValue());
            dsStressProbabilityImputed.add(stressProbability);

            dsStressProbabilityAvailable.add(new DataPoint(dsStressProbability.getLatestTimestamp(), 1.0));
        } else if (dsStressRIPProbability.data.size() > 0) {
            DataPoint stressProbability = new DataPoint(dsStressRIPProbability.getLatestTimestamp(), dsStressRIPProbability.getLatestValue());
            dsStressProbabilityImputed.add(stressProbability);

            dsStressProbabilityAvailable.add(new DataPoint(dsStressRIPProbability.getLatestTimestamp(), 1.0));
        } else {
            //Handle missing datapoint. Imputation by carry forwarding the last known value.
            long currentTimestamp = -1;
            List<DataPoint> imputedHistoryLast = dsStressProbabilityImputed.getHistoricalNValues(1);
            if (imputedHistoryLast.size() > 0) {
                if (imputedHistoryLast.get(0).timestamp == -1) {
                    currentTimestamp = -1;
                } else {
                    currentTimestamp = imputedHistoryLast.get(0).timestamp + windowSize; // 60000 should be replaced by windowSize
                }
            }
            if (currentTimestamp == -1) {
                return;
            }
            DataPoint stressProbability = new DataPoint(currentTimestamp, getLastHistoricalValue(dsStressProbability, -1));
            if (stressProbability.timestamp == -1 || stressProbability.value == -1) {
                return;
            }
            dsStressProbabilityImputed.add(stressProbability);

            dsStressProbabilityAvailable.add(new DataPoint(currentTimestamp, 0.0));
        }

        smoothViaHistoricalValues(dsStressProbabilitySmoothed, dsStressProbabilityImputed, stressProbabilitySmoothingWindow);

        DataPoint stressProbability = new DataPoint(dsStressProbabilitySmoothed.getLatestTimestamp(), dsStressProbabilitySmoothed.getLatestValue());

        double emaFastPrev;
        List<DataPoint> emaFastHistory = dsEmaFast.getHistoricalNValues(1);
        if (emaFastHistory.size() == 0) {
            emaFastPrev = stressProbability.value;
        } else {
            emaFastPrev = emaFastHistory.get(0).value;
        }
        double emaFast = Smoothing.ewma(stressProbability.value, emaFastPrev, 2.0 / (macdParamFast + 1));
        dsEmaFast.add(new DataPoint(stressProbability.timestamp, emaFast));

        double emaSlowPrev;
        List<DataPoint> emaSlowHistory = dsEmaSlow.getHistoricalNValues(1);
        if (emaSlowHistory.size() == 0) {
            emaSlowPrev = stressProbability.value;
        } else {
            emaSlowPrev = emaSlowHistory.get(0).value;
        }
        double emaSlow = Smoothing.ewma(stressProbability.value, emaSlowPrev, 2.0 / (macdParamSlow + 1));
        dsEmaSlow.add(new DataPoint(stressProbability.timestamp, emaSlow));

        double macdPrev = emaFastPrev - emaSlowPrev;
        double macd = emaFast - emaSlow;

        double emaSignalPrev;
        List<DataPoint> emaSignalHistory = dsEmaSignal.getHistoricalNValues(1);
        if (emaSignalHistory.size() == 0) {
            emaSignalPrev = 0;//stressProbability.value;
        } else {
            emaSignalPrev = emaSignalHistory.get(0).value;
        }
        double emaSignal = Smoothing.ewma(macd, emaSignalPrev, 2.0 / (macdParamSignal + 1));
        dsEmaSignal.add(new DataPoint(stressProbability.timestamp, emaSignal));

        double histogramPrev = macdPrev - emaSignalPrev;
        double histogram = macd - emaSignal;
        dsEmaHistogram.add(new DataPoint(stressProbability.timestamp, histogram));

        if (histogramPrev >= 0 && histogram < 0) {
            //Episode is ended; Started Increasing again
            long episodeStartTimestamp = getEpisodeStartTimestamp(datastreams);
            if(episodeStartTimestamp==-1) {
                List<Double> stressEpisodeValues = new ArrayList<Double>();
                stressEpisodeValues.add((double) -1);
                stressEpisodeValues.add(StressEpisodeClass.NotClassified.value);
                arStressEpisodeClassification.add(new DataPointArray(episodeStartTimestamp, stressEpisodeValues));
                dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.NotClassified.value));
            } else {
                List<DataPoint> listAvailable = dsStressProbabilityAvailable.getHistoricalValues(episodeStartTimestamp);
                double sumAvailable = 0;
                for (DataPoint available : listAvailable) {
                    sumAvailable += available.value;
                }
                double proportionAvailable = sumAvailable / listAvailable.size();
                if (proportionAvailable < .5) {
                    //More than 50% data is lost.
                    dsStressEpisodeStart.add(new DataPoint(episodeStartTimestamp, StressEpisodeClass.Unknown.value));
                    List<Double> stressEpisodeValues = new ArrayList<Double>();
                    stressEpisodeValues.add((double) stressProbability.timestamp);
                    stressEpisodeValues.add(StressEpisodeClass.Unknown.value);
                    arStressEpisodeClassification.add(new DataPointArray(episodeStartTimestamp, stressEpisodeValues));
                    dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.Unknown.value));
                } else {
                    List<DataPoint> listStressProbability = dsStressProbabilitySmoothed.getHistoricalValues(episodeStartTimestamp);
                    if(listStressProbability.size()==0) {
                        dsStressEpisodeStart.add(new DataPoint(episodeStartTimestamp, StressEpisodeClass.Unknown.value));
                        List<Double> stressEpisodeValues = new ArrayList<Double>();
                        stressEpisodeValues.add((double) stressProbability.timestamp);
                        stressEpisodeValues.add(StressEpisodeClass.Unknown.value);
                        arStressEpisodeClassification.add(new DataPointArray(episodeStartTimestamp, stressEpisodeValues));
                        dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.Unknown.value));
                    } else {
                        double sumStressProbability = 0;
                        for (DataPoint stressProbabilityDP : listStressProbability) {
                            sumStressProbability += stressProbabilityDP.value;
                        }
                        double stressDensity = sumStressProbability / listStressProbability.size();
                        if (stressDensity >= thresholdYes) {
                            dsStressEpisodeStart.add(new DataPoint(episodeStartTimestamp, StressEpisodeClass.YesStress.value));
                            List<Double> stressEpisodeValues = new ArrayList<Double>();
                            stressEpisodeValues.add((double) stressProbability.timestamp);
                            stressEpisodeValues.add(StressEpisodeClass.YesStress.value);
                            arStressEpisodeClassification.add(new DataPointArray(episodeStartTimestamp, stressEpisodeValues));
                            dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.YesStress.value));
                        } else if (stressDensity <= thresholdNo) {
                            dsStressEpisodeStart.add(new DataPoint(episodeStartTimestamp, StressEpisodeClass.NotStress.value));
                            List<Double> stressEpisodeValues = new ArrayList<Double>();
                            stressEpisodeValues.add((double) stressProbability.timestamp);
                            stressEpisodeValues.add(StressEpisodeClass.NotStress.value);
                            arStressEpisodeClassification.add(new DataPointArray(episodeStartTimestamp, stressEpisodeValues));
                            dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.NotStress.value));
                        } else {
                            dsStressEpisodeStart.add(new DataPoint(episodeStartTimestamp, StressEpisodeClass.Unsure.value));
                            List<Double> stressEpisodeValues = new ArrayList<Double>();
                            stressEpisodeValues.add((double) stressProbability.timestamp);
                            stressEpisodeValues.add(StressEpisodeClass.Unsure.value);
                            arStressEpisodeClassification.add(new DataPointArray(episodeStartTimestamp, stressEpisodeValues));
                            dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.Unsure.value));
                        }
                    }
                }
            }
        } //else Episode in the middle; Started Decreasing
    }

    public long getEpisodeStartTimestamp(DataStreams datastreams) {
        DataPointStream dsStressEpisodeClassification = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_CLASSIFICATION);
        List<DataPoint> historicalNValues = dsStressEpisodeClassification.getHistoricalNValues(3);
        long timestampPrev = -1;
        // historicalNValues.size()-1 is  most recent; 0 is the oldest

        if(historicalNValues.size()>0) {
            timestampPrev = historicalNValues.get(0).timestamp;
        }


        DataPointStream dsEmaHistogram = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_HISTOGRAM);
        List<DataPoint> listHistogram = dsEmaHistogram.getHistoricalValues(timestampPrev); // For -1 it will return all
        if(listHistogram.size()<=1) {
            return -1;
        }
        for (int i = listHistogram.size()-2; i >=0; i--) {
            // discard newly added one.
            DataPoint dataPoint = listHistogram.get(i);
            if(dataPoint.value<=0) {
                return listHistogram.get(i+1).timestamp;
            }
        }
        return listHistogram.get(0).timestamp;
    }

    public double getLastHistoricalValue(DataPointStream dataPointStream, double defaultValue) {
        List<DataPoint> listHistory = dataPointStream.getHistoricalNValues(1);
        if (listHistory.size() == 0) {
            return defaultValue;
        }
        return listHistory.get(0).value;
    }

    public void smoothViaHistoricalValues(DataPointStream output, DataPointStream input, int n) {
        List<DataPoint> listHistoryDP = input.getHistoricalNValues(n);
        if (listHistoryDP.size() == 0) {
            return;
        }
        double sumValue = 0;
        for (DataPoint historyDP : listHistoryDP) {
            sumValue += historyDP.value;
        }
        output.add(new DataPoint(input.getLatestTimestamp(), sumValue / listHistoryDP.size()));
    }

    public enum StressEpisodeClass {
        NotStress(0), Unsure(1), YesStress(2), Unknown(3), NotClassified(4);
        private double value;

        StressEpisodeClass(double value) {
            this.value = value;
        }
    }

    /*
    public long getCurrentTimestamp() {
        Date date = new Date();
        return date.getTime();
    }
    */
}
