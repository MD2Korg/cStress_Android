package md2k.mCerebrum.cStress.Library;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Autosense.SensorConfiguration;
import md2k.mCerebrum.cStress.Structs.*;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.*;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Ju Gao <gao.363@buckeyemail.osu.edu>
 * - Rummana Bari <rbari@memphis.edu>
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
public class Core {

    /**
     * Compute the signal energy using FFTs
     * @param data
     * @param inc
     * @return
     */
    public static double computeEnergy(double[] data, int inc) {
        double result = 0;

        int NFFT = nextPower2(data.length);

        double[] buffer = new double[NFFT];
        System.arraycopy(data, 0, buffer, 0, data.length);

        FastFourierTransformer f = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftC = f.transform(buffer, TransformType.FORWARD);

        for (Complex aFftC : fftC) {
            result += (aFftC.abs() / inc) * (aFftC.abs() / inc);
        }
        return result;
    }

    /**
     * Determine the next power of 2 larger than length
     * @param length
     * @return
     */
    public static int nextPower2(int length) {
        if(length == 0)
            return 0;
        else
            return (int)Math.ceil(Math.log(length)/Math.log(2));
    }

    /**
     * Convert inputs to a magnitude vector
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double[] magnitude(DataPoint[] x, DataPoint[] y, DataPoint[] z) {
        double[] result = new double[Math.min(Math.min(x.length, y.length), z.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = Math.sqrt(Math.pow(x[i].value, 2) + Math.pow(y[i].value, 2) + Math.pow(z[i].value, 2));
        }
        return result;
    }

    /**
     * Mean crossing algorithm
     * @param x
     * @param mean
     * @return
     */
    public static double[] crossing(DataPoint[] x, double mean) {
        ArrayList<Double> crossings = new ArrayList<Double>();

        for (int i = 0; i < x.length - 1; i++) {
            if ((x[i].value > mean && x[i + 1].value <= mean) || x[i].value < mean && x[i + 1].value >= mean) {
                crossings.add((double) (i + 1));
            }
        }
        double[] result = new double[crossings.size()];
        for (int i = 0; i < crossings.size(); i++) {
            result[i] = crossings.get(i);
        }
        return result;
    }

    /**
     * Compute a discrete derivative
     * @param dp
     * @return
     */
    public static double[] diff(DataPoint[] dp) {
        double[] result;
        if (dp.length == 0) {
            result = new double[0];
        } else {
            result = new double[dp.length - 1];

            for (int i = 1; i < dp.length; i++) {
                result[i - 1] = dp[i].value - dp[i - 1].value;
            }
        }
        return result;
    }


    public static void applyFilterNormalize(DataStream input, DataStream output, DataStream outputNormalized, double[] filter, int normalizePercentile) {
        double[] sample = new double[input.data.size()];
        for (int i = 0; i < sample.length; i++) {
            sample[i] = input.data.get(i).value;
        }
        double[] result = conv(sample, filter);

        //Add value to datastream for computing percentiles
        for (int i=0; i<result.length; i++) {
            output.add(new DataPoint(input.data.get(i).timestamp,result[i]));
        }
        //Normalized based on percentiles
        for (int i = 0; i < sample.length; i++) {
            outputNormalized.add(new DataPoint(output.data.get(i).timestamp,output.data.get(i).value/output.descriptiveStats.getPercentile(normalizePercentile)));
        }
    }

    public static void applySquareFilterNormalize(DataStream input, DataStream output, DataStream outputNormalized, int normalizePercentile) {
        //Add value to datastream for computing percentiles
        for (int i=0; i<input.data.size(); i++) {
            output.add(new DataPoint(input.data.get(i).timestamp,input.data.get(i).value*input.data.get(i).value));
        }
        //Normalized based on percentiles
        for (int i = 0; i < output.data.size(); i++) {
            outputNormalized.add(new DataPoint(output.data.get(i).timestamp,output.data.get(i).value/output.descriptiveStats.getPercentile(normalizePercentile)));
        }
    }


    /**
     * Standard implementation of the Blackman filter
     * @param window_l Window length of the Blackman filter
     * @return blackman filter
     */
    public static double[] blackman(int window_l) {
        double[] result = new double[window_l];
        int M = (int) Math.floor((window_l + 1) / 2);

        for (int i = 0; i < M; i++) {
            result[i] = 0.42 - 0.5 * Math.cos(2.0 * Math.PI * (double) i / (window_l - 1)) + 0.08 * Math.cos(4.0 * Math.PI * (double) i / (window_l - 1)); //These constants are specific to the Blackman filter
            result[window_l - i - 1] = result[i];
        }

        return result;
    }

    /**
     * Finite Impulse Response Least-Squares filter
     * This filter is hard-coded from a Matlab output based on the frequency of the ECG sensor
     * @param fl Not used currently
     * @param f Not used currently
     * @param a Not used currently
     * @param w Not used currently
     * @return Implemented filter
     */
    public static double[] firls(double fl, double[] f, double[] a, double[] w) {
        //Hardcoded to the specifications of the Autosense ECG sensor.  Determined from the Matlab code

        double[] result = {
                -4.7274160329724543e-04, -4.3910892627884660e-04, 8.6902448327532778e-04, 1.2580375981368801e-03,
                -7.0870338859324122e-05, -3.1826848647111390e-04, 2.6535857915570944e-04, -8.1833969244558271e-04,
                -1.4124933569042541e-03, -1.9995228246965813e-05, 6.7313359750941992e-05, -6.0349375063082288e-04,
                8.6714439266190248e-04, 1.5065160936261314e-03, 1.0310266272463235e-04, 5.4715012534279738e-04,
                1.1904732453105013e-03, -7.6681365348830479e-04, -1.2037758114175129e-03, 3.1547725670266574e-05,
                -1.2630183530614810e-03, -1.7863067743220608e-03, 4.5997532952314775e-04, 3.2716936532409155e-04,
                -5.2644177116152754e-04, 1.7385569813462844e-03, 1.9889763081328412e-03, -1.0568777171727037e-04,
                1.0085738914257093e-03, 1.3166081526795959e-03, -1.6828106527494776e-03, -1.3954395354213642e-03,
                3.6314731240927499e-06, -2.3580287405187803e-03, -2.0329727875177984e-03, 1.0570699668543437e-03,
                -1.2914364939980619e-04, -3.8546902662939887e-04, 3.1026282496662044e-03, 2.0891652801290394e-03,
                -1.7040978539860152e-04, 2.2345403662638666e-03, 1.1662141454746484e-03, -2.7739626598314152e-03,
                -9.8117447601674384e-04, -4.4741116462223640e-04, -4.0970300710684790e-03, -1.8325183286848025e-03,
                1.3776445217444490e-03, -1.3211583879184985e-03, 3.7867414252479948e-04, 4.7380839079786659e-03,
                1.6152763281382203e-03, 5.0172009801508800e-04, 4.1276888849617836e-03, 3.2171309686397854e-04,
                -3.5444229001001413e-03, 5.9320034125439428e-05, -1.9474610880852858e-03, -6.1508063614239862e-03,
                -9.9665665297590886e-04, 7.1573500042050489e-04, -3.0500439336000633e-03, 2.2375273611317816e-03,
                6.0494339632141611e-03, 6.6666900112815589e-04, 2.6644168389563642e-03, 6.2464301815488002e-03,
                -1.3664646323453468e-03, -3.1859375309960399e-03, 1.3346226939768935e-03, -5.0751244870958813e-03,
                -7.8491706097835925e-03, 2.0337081693415804e-04, -1.8091340945618182e-03, -4.6875184234183237e-03,
                5.3652517463266009e-03, 6.1905855566496198e-03, -7.4368051532226921e-05, 7.0575372722313061e-03,
                7.8206867803311906e-03, -3.4972150936961369e-03, -7.6079548874512096e-04, 1.8835178013748015e-03,
                -1.0170003906003467e-02, -8.3679423125302028e-03, 7.4577361924604336e-04, -7.1106914021255080e-03,
                -5.2165564908720000e-03, 9.4292401382520472e-03, 4.2997116416556890e-03, 8.9934836096324411e-04,
                1.4397066723214469e-02, 7.9962549473134286e-03, -4.8304877652048297e-03, 4.7345958483973353e-03,
                4.9390510587356005e-05, -1.7446053084899240e-02, -7.0564384166495178e-03, -1.6381246987009848e-03,
                -1.6536831781652900e-02, -3.3074012030242165e-03, 1.3580158825803681e-02, -4.6367025497966462e-04,
                6.7704049102291549e-03, 2.6487077400862109e-02, 6.1540352580244520e-03, -2.4533091741271327e-03,
                1.5497475687230644e-02, -7.7410309481643867e-03, -2.8629194004745218e-02, -3.6522550021604841e-03,
                -1.3632609634575103e-02, -3.6030712287501945e-02, 3.8924925851573531e-03, 1.6707802872026386e-02,
                -1.1053287553442563e-02, 3.1002542423973951e-02, 5.7319009934381555e-02, 1.6637598373033171e-03,
                1.8714840588857272e-02, 5.1616797200589280e-02, -4.8886309433549893e-02, -7.3414476566383474e-02,
                4.0083352853056056e-03, -1.4044338414188356e-01, -2.4550216720451279e-01, 1.4662066861996509e-01,
                4.7941126114385940e-01, 1.4662066861996509e-01, -2.4550216720451279e-01, -1.4044338414188356e-01,
                4.0083352853056056e-03, -7.3414476566383474e-02, -4.8886309433549893e-02, 5.1616797200589280e-02,
                1.8714840588857272e-02, 1.6637598373033171e-03, 5.7319009934381555e-02, 3.1002542423973951e-02,
                -1.1053287553442563e-02, 1.6707802872026386e-02, 3.8924925851573531e-03, -3.6030712287501945e-02,
                -1.3632609634575103e-02, -3.6522550021604841e-03, -2.8629194004745218e-02, -7.7410309481643867e-03,
                1.5497475687230644e-02, -2.4533091741271327e-03, 6.1540352580244520e-03, 2.6487077400862109e-02,
                6.7704049102291549e-03, -4.6367025497966462e-04, 1.3580158825803681e-02, -3.3074012030242165e-03,
                -1.6536831781652900e-02, -1.6381246987009848e-03, -7.0564384166495178e-03, -1.7446053084899240e-02,
                4.9390510587356005e-05, 4.7345958483973353e-03, -4.8304877652048297e-03, 7.9962549473134286e-03,
                1.4397066723214469e-02, 8.9934836096324411e-04, 4.2997116416556890e-03, 9.4292401382520472e-03,
                -5.2165564908720000e-03, -7.1106914021255080e-03, 7.4577361924604336e-04, -8.3679423125302028e-03,
                -1.0170003906003467e-02, 1.8835178013748015e-03, -7.6079548874512096e-04, -3.4972150936961369e-03,
                7.8206867803311906e-03, 7.0575372722313061e-03, -7.4368051532226921e-05, 6.1905855566496198e-03,
                5.3652517463266009e-03, -4.6875184234183237e-03, -1.8091340945618182e-03, 2.0337081693415804e-04,
                -7.8491706097835925e-03, -5.0751244870958813e-03, 1.3346226939768935e-03, -3.1859375309960399e-03,
                -1.3664646323453468e-03, 6.2464301815488002e-03, 2.6644168389563642e-03, 6.6666900112815589e-04,
                6.0494339632141611e-03, 2.2375273611317816e-03, -3.0500439336000633e-03, 7.1573500042050489e-04,
                -9.9665665297590886e-04, -6.1508063614239862e-03, -1.9474610880852858e-03, 5.9320034125439428e-05,
                -3.5444229001001413e-03, 3.2171309686397854e-04, 4.1276888849617836e-03, 5.0172009801508800e-04,
                1.6152763281382203e-03, 4.7380839079786659e-03, 3.7867414252479948e-04, -1.3211583879184985e-03,
                1.3776445217444490e-03, -1.8325183286848025e-03, -4.0970300710684790e-03, -4.4741116462223640e-04,
                -9.8117447601674384e-04, -2.7739626598314152e-03, 1.1662141454746484e-03, 2.2345403662638666e-03,
                -1.7040978539860152e-04, 2.0891652801290394e-03, 3.1026282496662044e-03, -3.8546902662939887e-04,
                -1.2914364939980619e-04, 1.0570699668543437e-03, -2.0329727875177984e-03, -2.3580287405187803e-03,
                3.6314731240927499e-06, -1.3954395354213642e-03, -1.6828106527494776e-03, 1.3166081526795959e-03,
                1.0085738914257093e-03, -1.0568777171727037e-04, 1.9889763081328412e-03, 1.7385569813462844e-03,
                -5.2644177116152754e-04, 3.2716936532409155e-04, 4.5997532952314775e-04, -1.7863067743220608e-03,
                -1.2630183530614810e-03, 3.1547725670266574e-05, -1.2037758114175129e-03, -7.6681365348830479e-04,
                1.1904732453105013e-03, 5.4715012534279738e-04, 1.0310266272463235e-04, 1.5065160936261314e-03,
                8.6714439266190248e-04, -6.0349375063082288e-04, 6.7313359750941992e-05, -1.9995228246965813e-05,
                -1.4124933569042541e-03, -8.1833969244558271e-04, 2.6535857915570944e-04, -3.1826848647111390e-04,
                -7.0870338859324122e-05, 1.2580375981368801e-03, 8.6902448327532778e-04, -4.3910892627884660e-04,
                -4.7274160329724543e-04};

        return result;
    }

    /**
     * Standard convolution implementation for producing the "same" size filter
     * @param signal Input signal
     * @param kernel Kernel to apply to the signal
     * @return Convoluted signal
     */
    public static double[] conv(double[] signal, double[] kernel) {
        double[] result = new double[Math.max(Math.max(signal.length + kernel.length, signal.length), kernel.length)];

        double[] tempsignal = new double[signal.length + kernel.length];
        System.arraycopy(signal, 0, tempsignal, kernel.length/2, signal.length); //Zero pad the end of signal

        for (int i = 0; i < signal.length; i++) {
            result[i] = 0;
            for (int j = 0; j < kernel.length; j++) {
                result[i] += tempsignal[i + j] * kernel[j];
            }
        }

        double[] shortresult = new double[signal.length];
        System.arraycopy(result, 0, shortresult, 0, signal.length); //Remove excess array size
        return shortresult;
    }


    /**
     * Reimplementation of Matlab's smooth function
     * @param rip
     * @param n
     * @return
     */
    public static ArrayList<DataPoint> smooth(ArrayList<DataPoint> rip, int n) {
        ArrayList<DataPoint> result = new ArrayList<DataPoint>();

        int windowSize = 1;
        double sum;
        for(int i=0; i<rip.size(); i++) {
            sum = 0.0;
            int startingPoint;
            if( (rip.size()-i+1) < n ) {
                startingPoint = rip.size()-windowSize;
            } else {
                startingPoint = (int) Math.max(Math.floor(i - n / 2), 0);
            }
            for(int j=startingPoint; j<startingPoint+windowSize; j++) {
                sum += rip.get(j).value;
            }
            sum /= (double) windowSize;

            result.add( new DataPoint(rip.get(i).timestamp, sum) );

            if(windowSize < n && (rip.size()-i) > n) { //Increase windowSize until n
                windowSize += 2;
            } else if ( (rip.size()-i+1) < n) {
                windowSize -= 2;
            }
        }

        return result;
    }


    public static long nextEpochTimestamp(long timestamp) {
        long previousMinute = timestamp / (60*1000);
        Date date = new Date((previousMinute+1)*(60*1000));
        return date.getTime();
    }

    /**
     * Basic time bounded windowing.
     * @param data
     * @param size
     * @return
     */
    public static ArrayList<DataPoint[]> window(ArrayList<DataPoint> data, int size) {
        ArrayList<DataPoint[]> result = new ArrayList<DataPoint[]>();

        long startTime = nextEpochTimestamp(data.get(0).timestamp) - 60*1000; //Get next minute window and subtract a minute to arrive at the appropriate startTime
        ArrayList<DataPoint> tempArray = new ArrayList<DataPoint>();
        DataPoint[] temp;
        for(DataPoint dp: data) {
            if(dp.timestamp < startTime+size) {
                tempArray.add(dp);
            } else {
                temp = new DataPoint[tempArray.size()];
                for(int i=0; i<temp.length; i++) {
                    temp[i] = tempArray.get(i);
                }
                result.add(temp);
                tempArray = new ArrayList<DataPoint>();
                startTime += size;
            }
        }
        temp = new DataPoint[tempArray.size()];
        for(int i=0; i<temp.length; i++) {
            temp[i] = tempArray.get(i);
        }
        result.add(temp);

        return result;
    }

    /**
     * Outlier detection for RR-interval data
     * Reference: detect_outlier_v2.m
     * @param sample RR-interval values
     * @param timestamp RR-interval timestamps
     * @return Outlier array
     */
    public static String detect_outlier_v2(HashMap<String, DataStream> datastreams) {
        ArrayList<Integer> outlier = new ArrayList<Integer>();

        DataStream ds = datastreams.get("org.md2k.cstress.data.ecg.rr_value");

        if (ds.data.size() != 0) {

            if (!datastreams.containsKey("org.md2k.cstress.data.ecg.valid_rr_interval")) {
                datastreams.put("org.md2k.cstress.data.ecg.valid_rr_interval", new DataStream("ECG-valid-rr_interval"));
            }
            for (int i = 0; i < ds.data.size(); i++) {
                if (ds.data.get(i).value > 0.3 && ds.data.get(i).value < 2.0) {
                    datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").add(ds.data.get(i));
                }
            }

            if (!datastreams.containsKey("org.md2k.cstress.data.ecg.rr_value.diff")) {
                datastreams.put("org.md2k.cstress.data.ecg.rr_value.diff", new DataStream("ECG-rr_value-diff"));
            }
            for (int i = 1; i < datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.size(); i++) {
                datastreams.get("org.md2k.cstress.data.ecg.rr_value.diff").add(new DataPoint(datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).timestamp, Math.abs(datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).value - datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i - 1).value)));
            }

            double MED = AUTOSENSE.MED_CONSTANT * 0.5 * ( datastreams.get("org.md2k.cstress.data.ecg.rr_value.diff").descriptiveStats.getPercentile(75) -  datastreams.get("org.md2k.cstress.data.ecg.rr_value.diff").descriptiveStats.getPercentile(25));
            double MAD = (datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").descriptiveStats.getPercentile(50) - AUTOSENSE.MAD_CONSTANT * 0.5 * ( datastreams.get("org.md2k.cstress.data.ecg.rr_value.diff").descriptiveStats.getPercentile(75) -  datastreams.get("org.md2k.cstress.data.ecg.rr_value.diff").descriptiveStats.getPercentile(25))) / 3.0;
            double CBD = (MED + MAD) / 2.0;
            if (CBD < AUTOSENSE.CBD_THRESHOLD) {
                CBD = AUTOSENSE.CBD_THRESHOLD;
            }

            for (DataPoint aSample : ds.data) {
                outlier.add(AUTOSENSE.QUALITY_BAD);
            }
            outlier.set(0, AUTOSENSE.QUALITY_GOOD);
            double standard_rrInterval = datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(0).value;
            boolean prev_beat_bad = false;

            for (int i = 1; i < datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.size() - 1; i++) {
                double ref = datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).value;
                if (ref > AUTOSENSE.REF_MINIMUM && ref < AUTOSENSE.REF_MAXIMUM) {
                    double beat_diff_prevGood = Math.abs(standard_rrInterval - datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).value);
                    double beat_diff_pre = Math.abs(datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i - 1).value - datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).value);
                    double beat_diff_post = Math.abs(datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).value - datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i + 1).value);

                    if ((prev_beat_bad && beat_diff_prevGood < CBD) || (prev_beat_bad && beat_diff_prevGood > CBD && beat_diff_pre <= CBD && beat_diff_post <= CBD)) {
                        for (int j = 0; j < ds.data.size(); j++) {
                            if (ds.data.get(j).timestamp == datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).timestamp) {
                                outlier.set(j, AUTOSENSE.QUALITY_GOOD);
                            }
                        }
                        prev_beat_bad = false;
                        standard_rrInterval = datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).value;
                    } else if (prev_beat_bad && beat_diff_prevGood > CBD && (beat_diff_pre > CBD || beat_diff_post > CBD)) {
                        prev_beat_bad = true;
                    } else if (!prev_beat_bad && beat_diff_pre <= CBD) {
                        for (int j = 0; j < ds.data.size(); j++) {
                            if (ds.data.get(j).timestamp == datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).timestamp) {
                                outlier.set(j, AUTOSENSE.QUALITY_GOOD);
                            }
                        }
                        prev_beat_bad = false;
                        standard_rrInterval = datastreams.get("org.md2k.cstress.data.ecg.valid_rr_interval").data.get(i).value;
                    } else if (!prev_beat_bad && beat_diff_pre > CBD) {
                        prev_beat_bad = true;
                    }

                }
            }

        }

        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.outlier")) {
            datastreams.put("org.md2k.cstress.data.ecg.outlier", new DataStream("ECG-rr-outlier"));
        }
        for (int i = 0; i < outlier.size(); i++) {
            datastreams.get("org.md2k.cstress.data.ecg.outlier").add(new DataPoint(ds.data.get(i).timestamp, outlier.get(i)));
        }

        return "org.md2k.cstress.data.ecg.outlier";
    }


    /**
     * Basic EWMA function
     * @param x
     * @param y
     * @param alpha
     * @return
     */
    public static double ewma(double x, double y, double alpha) {
        return alpha * x + (1-alpha) * y;
    }

    /**
     * R-peak detector
     * @param datapoints Raw ECG datapoints
     * @param frequency ECG sampling frequency
     * @return Indexes of R-peaks
     */
    public static String detect_Rpeak(HashMap<String, DataStream> datastreams) {

        DataStream ECG = datastreams.get("org.md2k.cstress.data.ecg");
        double frequency = (Double)ECG.metadata.get("frequency");

        //Ohio State Algorithm
        int window_l = (int) Math.ceil( frequency / 5.0);

        //Specific to Autosense hardware @ 64Hz and 12-bit values //TODO: Fix this
        double thr1 = AUTOSENSE.THR1_INIT;
        double f = 2.0 / frequency;
        double[] F = {0.0, 4.5 * f, 5.0 * f, 20.0 * f, 20.5 * f, 1};
        double[] A = {0, 0, 1, 1, 0, 0};
        double[] w = {500.0 / 0.02, 1.0 / 0.02, 500 / 0.02};
        double fl = AUTOSENSE.FL_INIT;


        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.y2")) {
            datastreams.put("org.md2k.cstress.data.ecg.y2", new DataStream("Y2-ECG-subfeature"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.y2-normalized")) {
            datastreams.put("org.md2k.cstress.data.ecg.y2-normalized", new DataStream("Y2-ECG-subfeature-normalized"));
        }
        applyFilterNormalize(   datastreams.get("org.md2k.cstress.data.ecg"),
                                datastreams.get("org.md2k.cstress.data.ecg.y2"),
                                datastreams.get("org.md2k.cstress.data.ecg.y2-normalized"),
                                firls(fl, F, A, w),
                                90);

        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.y3")) {
            datastreams.put("org.md2k.cstress.data.ecg.y3", new DataStream("Y3-ECG-subfeature"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.y3-normalized")) {
            datastreams.put("org.md2k.cstress.data.ecg.y3-normalized", new DataStream("Y3-ECG-subfeature-normalized"));
        }
        applyFilterNormalize(   datastreams.get("org.md2k.cstress.data.ecg.y2-normalized"),
                                datastreams.get("org.md2k.cstress.data.ecg.y3"),
                                datastreams.get("org.md2k.cstress.data.ecg.y3-normalized"),
                                new double[]{-1.0 / 8.0, -2.0 / 8.0, 0.0 / 8.0, 2.0 / 8.0, -1.0 / 8.0},
                                90);

        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.y4")) {
            datastreams.put("org.md2k.cstress.data.ecg.y4", new DataStream("Y4-ECG-subfeature"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.y4-normalized")) {
            datastreams.put("org.md2k.cstress.data.ecg.y4-normalized", new DataStream("Y4-ECG-subfeature-normalized"));
        }
        applySquareFilterNormalize(   datastreams.get("org.md2k.cstress.data.ecg.y3-normalized"),
                                datastreams.get("org.md2k.cstress.data.ecg.y4"),
                                datastreams.get("org.md2k.cstress.data.ecg.y4-normalized"),
                                90);

        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.y5")) {
            datastreams.put("org.md2k.cstress.data.ecg.y5", new DataStream("Y5-ECG-subfeature"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.y5-normalized")) {
            datastreams.put("org.md2k.cstress.data.ecg.y5-normalized", new DataStream("Y5-ECG-subfeature-normalized"));
        }
        applyFilterNormalize(   datastreams.get("org.md2k.cstress.data.ecg.y4-normalized"),
                datastreams.get("org.md2k.cstress.data.ecg.y5"),
                datastreams.get("org.md2k.cstress.data.ecg.y5-normalized"),
                blackman(window_l),
                90);



        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.peaks")) {
            datastreams.put("org.md2k.cstress.data.ecg.peaks", new DataStream("Y5-ECG-peaks"));
        }
        DataStream y5 = datastreams.get("org.md2k.cstress.data.ecg.y5-normalized");
        for(int i = 2; i<y5.data.size() - 2; i++) {
            if (    y5.data.get(i - 2).value < y5.data.get(i - 1).value &&
                    y5.data.get(i - 1).value < y5.data.get(i).value &&
                    y5.data.get(i).value >= y5.data.get(i + 1).value &&
                    y5.data.get(i + 1).value > y5.data.get(i + 2).value) { //TODO: Why is this hard-coded to five samples to examine?
                datastreams.get("org.md2k.cstress.data.ecg.peaks").add( new DataPoint(y5.data.get(i)));
            }
        }



        // If CURRENTPEAK > THR_SIG, that location is identified as a ìQRS complex
        // candidateî and the signal level (SIG_LEV) is updated:
        // SIG _ LEV = 0.125 ◊CURRENTPEAK + 0.875◊ SIG _ LEV
        // If THR_NOISE < CURRENTPEAK < THR_SIG, then that location is identified as a
        // ìnoise peakî and the noise level (NOISE_LEV) is updated:
        // NOISE _ LEV = 0.125◊CURRENTPEAK + 0.875◊ NOISE _ LEV 
        // Based on new estimates of the signal and noise levels (SIG_LEV and NOISE_LEV,
        // respectively) at that point in the ECG, the thresholds are adjusted as follows:
        // THR _ SIG = NOISE _ LEV + 0.25 ◊ (SIG _ LEV ? NOISE _ LEV )
        // THR _ NOISE = 0.5◊ (THR _ SIG)


        double thr2 = 0.5 * thr1;
        double sig_lev = AUTOSENSE.SIG_LEV_FACTOR * thr1;
        double noise_lev = AUTOSENSE.NOISE_LEV_FACTOR * sig_lev;


        DataStream peaks = datastreams.get("org.md2k.cstress.data.ecg.peaks");
        DataPoint rr_ave;
        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.peaks.rr_ave")) {
            datastreams.put("org.md2k.cstress.data.ecg.peaks.rr_ave", new DataStream("Y5-ECG-peaks-rr_ave"));
            datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").setPreservedLastInsert(true);
            double rr_avg = 0.0;
            for (int i = 1; i < peaks.data.size(); i++) {
                rr_avg+= peaks.data.get(i).value - peaks.data.get(i - 1).value;
            }
            rr_avg /= (peaks.data.size() - 1);
            rr_ave = new DataPoint(ECG.data.get(0).timestamp, rr_avg);
            datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").add(rr_ave);
        }
        rr_ave = datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").data.get(datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").data.size()-1);


        int c1 = 0;
        ArrayList<Integer> c2 = new ArrayList<Integer>();

        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.peaks.temp1")) {
            datastreams.put("org.md2k.cstress.data.ecg.peaks.temp1", new DataStream("Y5-ECG-peaks-temp1"));
        }
        ArrayList<DataPoint> Rpeak_temp1 = datastreams.get("org.md2k.cstress.data.ecg.peaks.temp1").data;


        for(int i=0; i < peaks.data.size(); i++) {
            if (Rpeak_temp1.size() == 0) {
                if (peaks.data.get(i).value > thr1 && peaks.data.get(i).value < (3.0 * sig_lev)) {
                    if(Rpeak_temp1.size() <= c1) {
                        Rpeak_temp1.add(new DataPoint(0,0.0));
                    }
                    Rpeak_temp1.set(c1,peaks.data.get(i));
                    sig_lev = ewma(peaks.data.get(i).value,sig_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                    if(c2.size() <= c1) {
                        c2.add(0);
                    }
                    c2.set(c1,i);
                    c1 += 1;
                } else if (peaks.data.get(i).value < thr1 && peaks.data.get(i).value > thr2) {
                    noise_lev = ewma(peaks.data.get(i).value,noise_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                }

                thr1 = noise_lev + 0.25 * (sig_lev - noise_lev); //TODO: Candidate for datastream
                thr2 = 0.5 * thr1; //TODO: Candidate for datastream

                rr_ave = rr_ave_update(Rpeak_temp1, datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave"));
            } else {
                if (((peaks.data.get(i).timestamp - peaks.data.get(c2.get(c1 - 1)).timestamp) > 1.66 * rr_ave.value) && (i - c2.get(c1 - 1)) > 1) {
                    ArrayList<Double> searchback_array_inrange = new ArrayList<Double>();
                    ArrayList<Integer> searchback_array_inrange_index = new ArrayList<Integer>();

                    for (int j = c2.get(c1 - 1) + 1; j < i - 1; j++) {
                        if (peaks.data.get(i).value < 3.0 * sig_lev && peaks.data.get(i).value > thr2) {
                            searchback_array_inrange.add(peaks.data.get(i).value);
                            searchback_array_inrange_index.add(j - c2.get(c1 - 1));
                        }
                    }

                    if (searchback_array_inrange.size() > 0) {
                        double searchback_max = searchback_array_inrange.get(0);
                        int searchback_max_index = 0;
                        for (int j = 0; j < searchback_array_inrange.size(); j++) {
                            if (searchback_array_inrange.get(j) > searchback_max) {
                                searchback_max = searchback_array_inrange.get(i);
                                searchback_max_index = j;
                            }
                        }
                        if(Rpeak_temp1.size() >= c1) {
                            Rpeak_temp1.add(new DataPoint(0,0.0));
                        }
                        Rpeak_temp1.set(c1,peaks.data.get(c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index)));
                        sig_lev = ewma(Rpeak_temp1.get(c1-1).value,sig_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                        if(c1 >= c2.size()) {
                            c2.add(0);
                        }
                        c2.set(c1, c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index));
                        i = c2.get(c1 - 1) + 1;
                        c1 += 1;
                        thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                        thr2 = 0.5 * thr1;
                        rr_ave = rr_ave_update(Rpeak_temp1, datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave"));
                        continue;
                    }
                } else if (peaks.data.get(i).value >= thr1 && peaks.data.get(i).value < (3.0 * sig_lev)) {
                    if(Rpeak_temp1.size() >= c1) {
                        Rpeak_temp1.add(new DataPoint(0,0.0));
                    }
                    Rpeak_temp1.set(c1,peaks.data.get(i));
                    sig_lev = ewma(peaks.data.get(i).value,sig_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                    if(c2.size() <= c1) {
                        c2.add(0);
                    }
                    c2.set(c1, i);
                    c1 += 1;
                } else if (peaks.data.get(i).value < thr1 && peaks.data.get(i).value > thr2) {
                    noise_lev = ewma(peaks.data.get(i).value,noise_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                }
                thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                thr2 = 0.5 * thr1;
                rr_ave = rr_ave_update(Rpeak_temp1, datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave"));
            }
        }



        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.peaks.temp2")) {
            datastreams.put("org.md2k.cstress.data.ecg.peaks.temp2", new DataStream("Y5-ECG-peaks-temp2"));
        }
        ArrayList<DataPoint> Rpeak_temp2 = datastreams.get("org.md2k.cstress.data.ecg.peaks.temp2").data;
        Rpeak_temp2.addAll(Rpeak_temp1); //Make a copy of the data vector

        boolean difference = false;

        while (!difference) {
            int length_Rpeak_temp2 = Rpeak_temp2.size();
            ArrayList<DataPoint> diffRpeak = new ArrayList<DataPoint>();
            for(int j=1; j<Rpeak_temp2.size(); j++) {
                diffRpeak.add(new DataPoint(Rpeak_temp2.get(j).timestamp-Rpeak_temp2.get(j-1).timestamp, Rpeak_temp2.get(j).value-Rpeak_temp2.get(j-1).value));
            }

            ArrayList<DataPoint> comp1 = new ArrayList<DataPoint>();
            ArrayList<DataPoint> comp2 = new ArrayList<DataPoint>();
            ArrayList<Integer> eli_index = new ArrayList<Integer>();

            for(int j=0; j<diffRpeak.size(); j++) {
                if (diffRpeak.get(j).timestamp < (AUTOSENSE.RPEAK_INTERPEAK_MULTIPLIER *frequency)) {
                    comp1.add(Rpeak_temp2.get(j));
                    comp2.add(Rpeak_temp2.get(j + 1));
                    if (comp1.get(comp1.size()-1).value < comp2.get(comp2.size()-1).value) {
                        eli_index.add(0);
                    } else {
                        eli_index.add(1);
                    }
                } else {
                    eli_index.add(-999999);
                }
            }

            for(int j=0; j<diffRpeak.size(); j++) {
                if (diffRpeak.get(j).timestamp < (AUTOSENSE.RPEAK_INTERPEAK_MULTIPLIER*frequency)) {
                    Rpeak_temp2.set(j+eli_index.get(j), new DataPoint(0,-999999));
                }
            }

            for (Iterator<DataPoint> it=Rpeak_temp2.iterator(); it.hasNext(); ) {
    			if (it.next().value == -999999) {
    				it.remove();
    			}
    		}

            difference = (length_Rpeak_temp2 == Rpeak_temp2.size());

        }


        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.peaks.temp3")) {
            datastreams.put("org.md2k.cstress.data.ecg.peaks.temp3", new DataStream("Y5-ECG-peaks-temp3"));
        }
        ArrayList<DataPoint> Rpeak_temp3 = datastreams.get("org.md2k.cstress.data.ecg.peaks.temp3").data;
        Rpeak_temp3.add(Rpeak_temp2.get(0));


        for (int k = 1; k < Rpeak_temp2.size()-1; k++) {
            double maxValue = -1e9;


            double peaktime = Rpeak_temp2.get(k).timestamp;
            int windowStart = 0;
            int windowStop = peaks.data.size();
            for(int i=0; i<peaks.data.size(); i++) {
                if (peaks.data.get(i).timestamp < peaktime - (int) Math.ceil(frequency / AUTOSENSE.RPEAK_BIN_FACTOR)) {
                    windowStart = i;
                }
                if (peaks.data.get(i).timestamp > peaktime + (int) Math.ceil(frequency / AUTOSENSE.RPEAK_BIN_FACTOR)) {
                    windowStop = i;
                    break;
                }
            }


            int index = 0;
            DataPoint maxDP = new DataPoint(0,0.0);
            try {
                for (int j = windowStart; j < windowStop; j++) {
                    if (peaks.data.get(j).value > maxValue) {
                        maxValue = peaks.data.get(j).value;
                        maxDP = peaks.data.get(j);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Rpeak_temp3.add(maxDP);
            }
        }



        if (!datastreams.containsKey("org.md2k.cstress.data.ecg.peaks.rpeaks")) {
            datastreams.put("org.md2k.cstress.data.ecg.peaks.rpeaks", new DataStream("Y5-ECG-peaks-rpeaks"));
        }
        datastreams.get("org.md2k.cstress.data.ecg.peaks.rpeaks").data.addAll(Rpeak_temp3);
        return "org.md2k.cstress.data.ecg.peaks.rpeaks";
    }

    /**
     *
     * @param rpeak_temp1
     * @param rr_ave
     * @return
     */
    public static DataPoint rr_ave_update(ArrayList<DataPoint> rpeak_temp1, DataStream rr_ave) { //TODO: Consider replacing this algorithm with something like and EWMA
        ArrayList<Long> peak_interval = new ArrayList<Long>();
        DataPoint result = new DataPoint(0,0.0);
        if (rpeak_temp1.size() != 0) {
            for (int i = 1; i < rpeak_temp1.size(); i++) {
                peak_interval.add(rpeak_temp1.get(i).timestamp - rpeak_temp1.get(i - 1).timestamp);
            }

            if (peak_interval.size() >= AUTOSENSE.PEAK_INTERVAL_MINIMUM_SIZE) {
                for (int i = peak_interval.size() - AUTOSENSE.PEAK_INTERVAL_MINIMUM_SIZE; i < peak_interval.size(); i++) {
                    result.value += peak_interval.get(i);
                }
                result.value /= 8.0;
                result.timestamp = rpeak_temp1.get(rpeak_temp1.size()-1).timestamp;
                rr_ave.add(result);
            }
        }
        return result;
    }

    /**
     * Lomb–Scargle periodogram implementation
     * Reference: HeartRateLomb.m
     * @param dp DataPoint array
     * @return Lomb structure with P and f defined
     */
    public static Lomb lomb(DataPoint[] dp) {
        double T = dp[dp.length - 1].timestamp - dp[0].timestamp;
        int nf = (int) Math.round(0.5 * 4.0 * 1.0 * dp.length);
        double[] f = new double[nf];

        for (int i = 0; i < nf; i++) {
            f[i] = (i + 1) / (T * 4);
        }

        nf = f.length;

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (DataPoint aData : dp) {
            stats.addValue(aData.value);
        }

        double mx = stats.getMean();
        double vx = stats.getVariance();

        for (DataPoint aDp : dp) {
            aDp.value -= mx;
        }

        double[] P = new double[nf];
        double[] wt;
        double[] swt;
        double[] cwt;
        double Ss2wt;
        double Sc2wt;
        double wtau;
        double swtau;
        double cwtau;
        double[] swttau;
        double[] cwttau;
        double swttau2;
        double cwttau2;
        wt = new double[dp.length];
        swt = new double[dp.length];
        cwt = new double[dp.length];
        swttau = new double[swt.length];
        cwttau = new double[swt.length];
        double part1;
        double part2;

        for (int i = 0; i < nf; i++) {
            Ss2wt = 0;
            Sc2wt = 0;

            for (int j = 0; j < wt.length; j++) {
                wt[j] = 2.0 * Math.PI * f[i] * dp[j].timestamp;
                swt[j] = Math.sin(wt[j]);
                cwt[j] = Math.cos(wt[j]);

                Ss2wt += cwt[j] * swt[j];
                Sc2wt += (cwt[j] - swt[j]) * (cwt[j] + swt[j]);

            }
            Ss2wt *= 2;

            wtau = 0.5 * Math.atan2(Ss2wt, Sc2wt);
            swtau = Math.sin(wtau);
            cwtau = Math.cos(wtau);


            swttau2 = 0;
            cwttau2 = 0;

            for (int j = 0; j < swt.length; j++) {
                swttau[j] = swt[j] * cwtau - cwt[j] * swtau;
                cwttau[j] = cwt[j] * cwtau + swt[j] * swtau;

                swttau2 += swttau[j] * swttau[j];
                cwttau2 += cwttau[j] * cwttau[j];
            }


            part1 = 0;
            part2 = 0;
            for (int j = 0; j < cwttau.length; j++) {
                part1 += (dp[j].value * cwttau[j]);
                part2 += (dp[j].value * swttau[j]);
            }
            part1 = part1*part1; //Square result
            part2 = part2*part2; //Square result

            P[i] = ((part1 / cwttau2) + (part2 / swttau2)) / (2 * vx);

        }

        Lomb result = new Lomb();
        result.P = P;
        result.f = f;

        return result;
    }

    /**
     * Heartrate Low Frequency - High Frequency ratio
     * @param P
     * @param f
     * @param lowRate Low frequency cutoff
     * @param highRate High frequency cutoff
     * @return LF/HF ratio
     */
    public static double heartRateLFHF(double[] P, double[] f, double lowRate, double highRate) {
        double result1 = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] < lowRate) {
                result1 += P[i];
            }
        }
        double result2 = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] >= lowRate && f[i] <= highRate) {
                result2 += P[i];
            }
        }
        return result1 / result2;
    }

    /**
     * Heartrate Power
     * @param P
     * @param f
     * @param lowFrequency Low frequency cutoff
     * @param highFrequency High frequency cutoff
     * @return
     */
    public static double heartRatePower(double[] P, double[] f, double lowFrequency, double highFrequency) {
        double result = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] >= lowFrequency && f[i] <= highFrequency) {
                result += P[i];
            }
        }

        return result;
    }

    public static String peakvalley_v2(HashMap<String, DataStream> datastreams) {

        if (!datastreams.containsKey("org.md2k.cstress.data.rip.smooth")) {
            datastreams.put("org.md2k.cstress.data.rip.smooth", new DataStream("RIP-smooth"));
        }
        DataStream rip_smooth = datastreams.get("org.md2k.cstress.data.rip.smooth");

        ArrayList<DataPoint> sample = smooth(datastreams.get("org.md2k.cstress.data.rip").data, AUTOSENSE.PEAK_VALLEY_SMOOTHING_SIZE);
        for(DataPoint dp: sample) {
            rip_smooth.add(dp);
        }


        int windowLength = (int) Math.round(AUTOSENSE.WINDOW_LENGTH_SECS * (Double) datastreams.get("org.md2k.cstress.data.rip").metadata.get("frequency"));

        if (!datastreams.containsKey("org.md2k.cstress.data.rip.mac")) {
            datastreams.put("org.md2k.cstress.data.rip.mac", new DataStream("RIP-mac"));
        }
        DataStream rip_mac = datastreams.get("org.md2k.cstress.data.rip.mac");
        ArrayList<DataPoint> MAC = smooth(rip_smooth.data, windowLength); //TWH: Replaced MAC with Smooth after discussion on 11/9/2015
        for(DataPoint dp: MAC) {
            rip_mac.add(dp);
        }


        if (!datastreams.containsKey("org.md2k.cstress.data.rip.upIntercepts")) {
            datastreams.put("org.md2k.cstress.data.rip.upIntercepts", new DataStream("RIP-upIntercept"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.downIntercepts")) {
            datastreams.put("org.md2k.cstress.data.rip.downIntercepts", new DataStream("RIP-downIntercept"));
        }


        for (int i = 1; i < rip_mac.data.size()-1; i++) {
            if (rip_smooth.data.get(i-1).value < rip_mac.data.get(i).value && rip_smooth.data.get(i+1).value > rip_mac.data.get(i).value) {
                datastreams.get("org.md2k.cstress.data.rip.upIntercepts").add(rip_mac.data.get(i));
            } else if (rip_smooth.data.get(i-1).value > rip_mac.data.get(i).value && rip_smooth.data.get(i+1).value < rip_mac.data.get(i).value) {
                datastreams.get("org.md2k.cstress.data.rip.downIntercepts").add(rip_mac.data.get(i));
            }
        }

        String UIDI = InterceptOutlierDetectorRIPLamia( datastreams, (int) (AUTOSENSE.SAMPLE_LENGTH_SECS*1000));

//        int[] UI = UIDI.UI;
//        int[] DI = UIDI.DI;
//
//        ArrayList<Integer> peakIndex = new ArrayList<Integer>();
//        ArrayList<Integer> valleyIndex = new ArrayList<Integer>();
//
//        for (int i = 0; i < DI.length-1; i++) {
//
//            int peakindex = 0;
//            double peakValue = -1e9;
//            for (int j = UI[i]; j < DI[i + 1]; j++) {
//                if (sample[j].value > peakValue) {
//                    peakindex = j;
//                    peakValue = sample[j].value;
//                }
//            }
//            peakIndex.add(peakindex);
//
//            DataPoint[] temp = new DataPoint[UI[i] - DI[i]];
//            System.arraycopy(sample, DI[i], temp, 0, UI[i] - DI[i]);
//
//            double vlValueMinimum = 1e9;
//            int vlValueIndex = -999999;
//            for(int j=0; j<temp.length; j++) {
//                if(vlValueMinimum > temp[j].value) {
//                    vlValueMinimum = temp[j].value;
//                    vlValueIndex = j;
//                }
//            }
//            if (vlValueIndex == -999999) {
//                continue;
//            }
//            valleyIndex.add(DI[i] + vlValueIndex - 1);
//
//        }
//
//        double[] inspirationAmplitude = new double[valleyIndex.size()];
//        double[] expirationAmplitude;
//
//        double meanInspirationAmplitude = 0.0;
//        double meanExpirationAmplitude;
//
//        for (int i = 0; i < valleyIndex.size() - 1; i++) {
//            inspirationAmplitude[i] = sample[peakIndex.get(i)].value - sample[valleyIndex.get(i)].value;
//
//            meanInspirationAmplitude += inspirationAmplitude[i];
//        }
//        meanInspirationAmplitude /= (valleyIndex.size() - 1);
//
//        ArrayList<Integer> finalPeakIndex = new ArrayList<Integer>();
//        ArrayList<Integer> finalValleyIndex = new ArrayList<Integer>();
//
//        for (int i = 0; i < inspirationAmplitude.length; i++) {
//            if (inspirationAmplitude[i] > AUTOSENSE.INSPIRATION_EXPIRATION_AMPLITUDE_THRESHOLD_FACTOR * meanInspirationAmplitude) {
//                finalPeakIndex.add(peakIndex.get(i));
//                finalValleyIndex.add(valleyIndex.get(i));
//            }
//        }
//
//        PeakValley result = new PeakValley();
//
//        if (finalValleyIndex.size() > 0) {
//            expirationAmplitude = new double[finalValleyIndex.size() - 1];
//            meanExpirationAmplitude = 0.0;
//            for (int i = 0; i < finalValleyIndex.size() - 1; i++) {
//                expirationAmplitude[i] = Math.abs(sample[finalValleyIndex.get(i + 1)].value - sample[finalPeakIndex.get(i)].value);
//                meanExpirationAmplitude += expirationAmplitude[i];
//            }
//            meanExpirationAmplitude /= (finalValleyIndex.size() - 1);
//
//
//            ArrayList<Integer> resultPeakIndex = new ArrayList<Integer>();
//            ArrayList<Integer> resultValleyIndex = new ArrayList<Integer>();
//
//            resultValleyIndex.add(finalValleyIndex.get(0));
//
//            for (int i = 0; i < expirationAmplitude.length; i++) {
//                if (expirationAmplitude[i] > AUTOSENSE.INSPIRATION_EXPIRATION_AMPLITUDE_THRESHOLD_FACTOR * meanExpirationAmplitude) {
//                    resultValleyIndex.add(finalValleyIndex.get(i + 1));
//                    resultPeakIndex.add(finalPeakIndex.get(i));
//                }
//            }
//            resultPeakIndex.add(finalPeakIndex.get(finalPeakIndex.size() - 1));
//
//            result.valleyIndex = resultValleyIndex;
//            result.peakIndex = resultPeakIndex;
//        }
        return "org.";
    }

//    /**
//     * Moving Average Curve
//     * @param sample
//     * @param windowLength
//     * @return
//     */
//    public static ArrayList<DataPoint> mac(ArrayList<DataPoint> sample, int windowLength) {
//
//        ArrayList<DataPoint> result = new ArrayList<DataPoint>();
//
//        for(int i=windowLength;i < sample.size()-windowLength; i++) {
//            result.add( new DataPoint(0, 0.0) );
//            for(int j=-windowLength; j<windowLength; j++) {
//                result.get(i-windowLength).value += sample.get(i+j).value;
//            }
//            result.get(i-windowLength).value /= (2.0*windowLength); //Compute mean
//            result.get(i-windowLength).timestamp = sample.get(i+windowLength).timestamp;
//
//        }
//        return result;
//    }

    /**
     * Intercept Outlier Detector
     * Reference: Intercept_outlier_detector_RIP_lamia.m
     * @param upInterceptIndex
     * @param downInterceptIndex
     * @param sample
     * @param windowLength
     * @return
     */
    public static String InterceptOutlierDetectorRIPLamia(HashMap<String, DataStream> datastreams, int windowLength) {
        String result = "";

        DataStream upIntercepts = datastreams.get("org.md2k.cstress.data.rip.upIntercepts");
        DataStream downIntercepts = datastreams.get("org.md2k.cstress.data.rip.downIntercepts");


        if (!datastreams.containsKey("org.md2k.cstress.data.rip.upIntercepts.filtered")) {
            datastreams.put("org.md2k.cstress.data.rip.upIntercepts.filtered", new DataStream("RIP-upIntercepts-filtered"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.downIntercepts.filtered")) {
            datastreams.put("org.md2k.cstress.data.rip.downIntercepts.filtered", new DataStream("RIP-downIntercepts-filtered"));
        }

        DataStream upInterceptsFiltered = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered");
        DataStream downInterceptsFiltered = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered");

        int upPointer = 0;
        int downPointer = 0;
        boolean updownstate = true; //True check for up intercept
        downInterceptsFiltered.add(downIntercepts.data.get(downPointer)); //Initialize with starting point

        while(downPointer != downIntercepts.data.size() && upPointer != upIntercepts.data.size()) {
            if (updownstate) { //Check for up intercept
                if (downIntercepts.data.get(downPointer).timestamp < upIntercepts.data.get(upPointer).timestamp) {
                    //Replace down intercept
                    downInterceptsFiltered.data.get(downInterceptsFiltered.data.size()-1).timestamp = downIntercepts.data.get(downPointer).timestamp;
                    downInterceptsFiltered.data.get(downInterceptsFiltered.data.size()-1).value = downIntercepts.data.get(downPointer).value;
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
                    upInterceptsFiltered.data.get(upInterceptsFiltered.data.size()-1).timestamp = upIntercepts.data.get(upPointer).timestamp;
                    upInterceptsFiltered.data.get(upInterceptsFiltered.data.size()-1).value = upIntercepts.data.get(upPointer).value;
                    upPointer++;
                } else {
                    //Found down intercept
                    downInterceptsFiltered.add(downIntercepts.data.get(downPointer));
                    downPointer++;
                    updownstate = true;
                }
            }
        }

        //TODO: is this still needed?
//        double fr;
//        for(int ii=0; ii<DI.size()-1; ii++) {
//            fr = (double)windowLength / (sample[DI.get(ii+1)].timestamp - sample[DI.get(ii)].timestamp);
//            if (fr >= AUTOSENSE.RESPIRATION_MINIMUM && fr <= AUTOSENSE.RESPIRATION_MAXIMUM) {
//                DownIntercept.add(DI.get(ii));
//                UpIntercept.add(UI.get(ii));
//            }
//        }

        if (!datastreams.containsKey("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec")) {
            datastreams.put("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec", new DataStream("RIP-upIntercepts-filtered-1sec"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec")) {
            datastreams.put("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec", new DataStream("RIP-downIntercepts-filtered-1sec"));
        }
        DataStream upInterceptsFiltered1sec = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec");
        DataStream downInterceptsFiltered1sec = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec");


        for(int i=1; i<downInterceptsFiltered.data.size(); i++) {
            if( (downInterceptsFiltered.data.get(i).timestamp - downInterceptsFiltered.data.get(i-1).timestamp) > 1000.0) {
                downInterceptsFiltered1sec.add(downInterceptsFiltered.data.get(i-1));
                upInterceptsFiltered1sec.add(upInterceptsFiltered.data.get(i-1));
            }
        }
        downInterceptsFiltered1sec.add(downInterceptsFiltered.data.get(downInterceptsFiltered.data.size()-1));

        if (!datastreams.containsKey("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec.t20")) {
            datastreams.put("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec.t20", new DataStream("RIP-upIntercepts-filtered-1sec-t20"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec.t20")) {
            datastreams.put("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec.t20", new DataStream("RIP-downIntercepts-filtered-1sec-t20"));
        }
        DataStream upInterceptsFiltered1sect20 = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec.t20");
        DataStream downInterceptsFiltered1sect20 = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec.t20");


        downInterceptsFiltered1sect20.add(downInterceptsFiltered1sec.data.get(0));
        for(int i=0; i<upInterceptsFiltered1sec.data.size(); i++) {
            if( (downInterceptsFiltered1sec.data.get(i+1).timestamp - upInterceptsFiltered1sec.data.get(i).timestamp) > (2.0/20.0)) {
                downInterceptsFiltered1sect20.add(downInterceptsFiltered1sec.data.get(i+1));
                upInterceptsFiltered1sect20.add(upInterceptsFiltered.data.get(i));
            }
        }
        downInterceptsFiltered1sect20.data.remove(downInterceptsFiltered1sect20.data.size()-1); //Not necessary to keep

        return result;
    }
}
