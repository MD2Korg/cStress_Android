package md2k.mcerebrum.cstress;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * - Karen Hovsepian <karoaper@gmail.com>
 * - Nazir Saleneen <nsleheen@memphis.edu>
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

import com.google.gson.Gson;
import md2k.mcerebrum.cstress.autosense.AUTOSENSE;
import md2k.mcerebrum.cstress.autosense.PUFFMARKER;
import md2k.mcerebrum.cstress.features.*;
import md2k.mcerebrum.cstress.library.datastream.DataArrayStream;
import md2k.mcerebrum.cstress.library.datastream.DataPointInterface;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.structs.DataPoint;
import md2k.mcerebrum.cstress.library.structs.DataPointArray;
import md2k.mcerebrum.cstress.library.structs.Model;
import md2k.mcerebrum.cstress.library.structs.SVCModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.exception.NotANumberException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.TreeMap;

/**
 * Main class that implements StreamProcessor and controls the data processing pipeline
 */
public class StreamProcessor {

    public DataPointInterface dpInterface;
    private long windowSize;
    private String path = null;
    private DataStreams datastreams = new DataStreams();
    private TreeMap<String, Object> models = new TreeMap<String, Object>();


    /**
     * Main constructor for StreamProcessor
     *
     * @param windowSize Time in milliseconds to segment and buffer data before processing
     */
    public StreamProcessor(long windowSize) {
        this.windowSize = windowSize;

        configureDataStreams();
    }

    /**
     * load and create a model object from a model file
     *
     * @param path Path to the model file (in JSON format)
     */
    public void loadModel(String name, String path) {
        Gson gson = new Gson();
        try {
            String jsonstring = FileUtils.readFileToString(new File(path));
            Model genericModel = gson.fromJson(jsonstring, Model.class);
            if (genericModel.getModelType().equals("svc"))
                models.put(name, gson.fromJson(jsonstring, SVCModel.class));
        } catch (IOException ignored) {
        }
        //gson parse a JSON model file and create a SVMModel object, and add it to models
    }

    private void configureDataStreams() {
        //Configure Data Streams
        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG).metadata.put("frequency", 64.0);
        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG).metadata.put("channelID", AUTOSENSE.CHEST_ECG);

        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP).metadata.put("frequency", 64.0 / 3.0);
        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP).metadata.put("channelID", AUTOSENSE.CHEST_RIP);

        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELX).metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELX).metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELY).metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELY).metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELZ).metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELZ).metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY).metadata.put("frequency", 1000.0 / windowSize);
        datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESSLABEL).metadata.put("frequency", 1000.0 / windowSize);

        configurePuffMarkerWristDataStreams(PUFFMARKER.LEFT_WRIST, 16.0, 32.0);
        configurePuffMarkerWristDataStreams(PUFFMARKER.RIGHT_WRIST, 16.0, 32.0);

    }

    public void settingWristFrequencies(String wrist, double freqAccel, double freqGyro) {
        configurePuffMarkerWristDataStreams(wrist, freqAccel, freqGyro);
    }

    private void configurePuffMarkerWristDataStreams(String wrist, double freqAccel, double freqGyro) {
        datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_X + wrist).metadata.put("frequency", freqAccel);
        datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Y + wrist).metadata.put("frequency", freqAccel);
        datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_ACCEL_Z + wrist).metadata.put("frequency", freqAccel);

        datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_X + wrist).metadata.put("frequency", freqGyro);
        datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_Y + wrist).metadata.put("frequency", freqGyro);
        datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_GYRO_Z + wrist).metadata.put("frequency", freqGyro);

    }

    /**
     * set the path for the feature files used by this stream processor
     *
     * @param path Path to the folder with the feature files
     */
    public void setPath(String path) {
        this.path = path;
        for (File f : new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("org.md2k") && name.endsWith(".csv");
            }
        })) {
            f.delete();
        }
    }


    /**
     * Main computation loop that processes all buffered data through several different classes
     */
    public void process() {

        try {
            //Data quality computations
            try {
                ECGDataQuality ecgDQ = new ECGDataQuality(datastreams, AUTOSENSE.AUTOSENSE_ECG_QUALITY);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("ECGDataQuality Exception Handler: IndexOutOfBoundsException");
            }
            try {
                RIPDataQuality ripDQ = new RIPDataQuality(datastreams, AUTOSENSE.AUTOSENSE_RIP_QUALITY);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("RIPDataQuality Exception Handler: IndexOutOfBoundsException");
            }

            //AutoSense features
            try {
                AccelerometerFeatures af = new AccelerometerFeatures(datastreams, AUTOSENSE.ACTIVITY_THRESHOLD, AUTOSENSE.ACCEL_WINDOW_SIZE);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("AccelerometerFeatures Exception Handler: IndexOutOfBoundsException");
            }
            try {
                ECGFeatures ef = new ECGFeatures(datastreams);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("ECGFeatures Exception Handler: IndexOutOfBoundsException");
            }
            try {
                RIPFeatures rf = new RIPFeatures(datastreams);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("RIPFeatures Exception Handler: IndexOutOfBoundsException");
            }

            try {
                RIPPuffmarkerFeatures rpf = new RIPPuffmarkerFeatures(datastreams);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("RIPPuffmarkerFeatures Exception Handler: IndexOutOfBoundsException");
            }

            try {
                AutosenseWristFeatures leftWrist = new AutosenseWristFeatures(datastreams, PUFFMARKER.LEFT_WRIST);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("AutosenseWristFeatures Exception Handler: IndexOutOfBoundsException");
            }

            try {
                AutosenseWristFeatures rightWrist = new AutosenseWristFeatures(datastreams, PUFFMARKER.RIGHT_WRIST);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("AutosenseWristFeatures Exception Handler: IndexOutOfBoundsException");
            }


        } catch (IndexOutOfBoundsException e) {
            System.out.println("Stress Exception Handler: IndexOutOfBoundsException");
        }


    }

    /**
     * Main processing method for computing results after basic datastream processing
     */
    public void generateResults() {
        try {
            CStressFeatureVector cs = new CStressFeatureVector(datastreams);
        } catch (NotANumberException e) {
            System.out.println("Generate result error");
        }

        try {
            PuffMarker pm = new PuffMarker(datastreams);
        } catch (NotANumberException e) {
            System.out.println("PuffMarker: Generate result error");
        }
    }

    /**
     * Method for processing the base features, computing the model feature vectors, and deploying the model to get model outputs
     */
    public void go() {
        process();
        generateResults();
        runcStress();
        runcStressEpisode();

        runpuffMarker();
        runSmokingEpisode();

        resetDataStreams();
    }

    private void runSmokingEpisode() {
        try {
            SmokingEpisodeGeneration seg = new SmokingEpisodeGeneration(datastreams);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("SmokingEpisodeGeneration Exception Handler: IndexOutOfBoundsException");
        }
    }

    /**
     * Method for running the puffMarker model on any available feature vectors to get corresponding stress probabilities
     */
    private void runpuffMarker() {
        SVCModel model = (SVCModel) models.get("puffMarkerModel");
        DataArrayStream featurevector = datastreams.getDataArrayStream(StreamConstants.ORG_MD2K_PUFFMARKER_FV_MINUTE);

        for (DataPointArray ap : featurevector.data) {
            double prob = model.computeProbability(ap);
            int label;
            if (prob > model.getLowBias()) {
                label = PUFFMARKER.PUFF;
            } else if (prob < model.getLowBias())
                label = PUFFMARKER.NOT_PUFF;
            else
                label = PUFFMARKER.UNSURE;

            datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PROBABILITY_MINUTE).add(new DataPoint(ap.timestamp, prob));
            datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PUFFLABEL_MINUTE).add(new DataPoint(ap.timestamp, label));
        }
    }

    /**
     * Method for running the cStress model on any available feature vectors to get corresponding stress probabilities
     */
    private void runcStress() {
        SVCModel model = (SVCModel) models.get("cStressModel");
        DataArrayStream featurevector = datastreams.getDataArrayStream(StreamConstants.ORG_MD2K_CSTRESS_FV);

        for (DataPointArray ap : featurevector.data) {
            double prob = model.computeProbability(ap);
            int label;
            if (prob > model.getHighBias())
                label = AUTOSENSE.STRESSED;
            else if (prob < model.getLowBias())
                label = AUTOSENSE.NOT_STRESSED;
            else
                label = AUTOSENSE.UNSURE;


            datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY).add(new DataPoint(ap.timestamp, prob));
            datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESSLABEL).add(new DataPoint(ap.timestamp, label));
        }


        //RIP only model
        model = (SVCModel) models.get("cStressRIPModel");
        featurevector = datastreams.getDataArrayStream(StreamConstants.ORG_MD2K_CSTRESS_FV_RIP);

        for (DataPointArray ap : featurevector.data) {
            double prob = model.computeProbability(ap);
            int label;
            if (prob > model.getHighBias())
                label = AUTOSENSE.STRESSED;
            else if (prob < model.getLowBias())
                label = AUTOSENSE.NOT_STRESSED;
            else
                label = AUTOSENSE.UNSURE;


            datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_RIP_PROBABILITY).add(new DataPoint(ap.timestamp, prob));
            datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_RIP_STRESSLABEL).add(new DataPoint(ap.timestamp, label));
        }

    }

    /**
     * Method for generating stress episodes and classify them
     */
    private void runcStressEpisode() {
        try {
            StressEpisodeClassification sef = new StressEpisodeClassification(datastreams, windowSize);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("StressEpisodeClassification Exception Handler: IndexOutOfBoundsException");
        }
    }


    /**
     * Add new DataPoint to the buffers
     *
     * @param channel Identifies which sensor stream the DataPoint is associated with
     * @param dp      DataPoint containing a timestamp and value
     */
    public void add(int channel, DataPoint dp) {
        switch (channel) {
            case AUTOSENSE.CHEST_ECG:
                datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG).add(dp);
                break;

            case AUTOSENSE.CHEST_RIP:
                datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP).add(dp);
                break;

            case AUTOSENSE.CHEST_ACCEL_X:
                datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELX).add(dp);
                break;

            case AUTOSENSE.CHEST_ACCEL_Y:
                datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELY).add(dp);
                break;

            case AUTOSENSE.CHEST_ACCEL_Z:
                datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCELZ).add(dp);
                break;


            case PUFFMARKER.LEFTWRIST_ACCEL_X:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_X)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_ACCEL_Y:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_Y)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_ACCEL_Z:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_ACCEL_Z)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_GYRO_X:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_X)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_GYRO_Y:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_Y)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_GYRO_Z:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_LEFTWRIST_GYRO_Z)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_ACCEL_X:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_X)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_ACCEL_Y:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_Y)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_ACCEL_Z:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_ACCEL_Z)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_GYRO_X:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_X)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_GYRO_Y:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_Y)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_GYRO_Z:
                (datastreams.getDataPointStream(PUFFMARKER.ORG_MD2K_PUFF_MARKER_DATA_RIGHTWRIST_GYRO_Z)).add(dp);
                break;

            default:
                System.out.println("NOT INTERESTED: " + dp);
                break;
        }
    }

    /**
     * Persist and reset all datastreams
     */
    private void resetDataStreams() {
        if (path != null)
            datastreams.persist(path + "/");
        datastreams.reset();
    }


    /**
     * Handle registration of a callback interface
     *
     * @param s Stream identifier
     */
    public void registerCallbackDataStream(String s) {
        datastreams.registerDataPointInterface(s, dpInterface);
    }


    /**
     * Handle registration of a callback interface
     *
     * @param s Stream identifier
     */
    public void registerCallbackDataArrayStream(String s) {
        datastreams.registerDataArrayInterface(s, dpInterface);
    }
}
