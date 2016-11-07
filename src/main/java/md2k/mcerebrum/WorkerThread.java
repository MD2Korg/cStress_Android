package md2k.mcerebrum;

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.StreamProcessor;
import md2k.mcerebrum.cstress.autosense.AUTOSENSE;
import md2k.mcerebrum.cstress.autosense.PUFFMARKER;
import md2k.mcerebrum.cstress.library.Time;
import md2k.mcerebrum.cstress.library.datastream.DataPointInterface;
import md2k.mcerebrum.cstress.library.structs.DataPoint;
import md2k.mcerebrum.cstress.library.structs.DataPointArray;

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
public class WorkerThread implements Runnable {

    private String path;
    private String cStressModelPath;
    private String cStressRIPModelPath;
    private String puffMarkerModelPath;

    public WorkerThread(String path) {
        this.path = path;
    }

    public WorkerThread(String path, String cStressModelPath, String cStressRIPModelPath, String puffMarkerModelPath) {
        this.path = path;
        this.cStressModelPath = cStressModelPath;
        this.cStressRIPModelPath = cStressRIPModelPath;
        this.puffMarkerModelPath = puffMarkerModelPath;
    }


    @Override
    public void run() {

        CSVParser tp = new CSVParser();

        tp.importData(path + "/rip.txt", AUTOSENSE.CHEST_RIP);
        tp.importData(path + "/ecg.txt", AUTOSENSE.CHEST_ECG);
        tp.importData(path + "/accelx.txt", AUTOSENSE.CHEST_ACCEL_X);
        tp.importData(path + "/accely.txt", AUTOSENSE.CHEST_ACCEL_Y);
        tp.importData(path + "/accelz.txt", AUTOSENSE.CHEST_ACCEL_Z);

        tp.importData(path + "/left-wrist-accelx.txt", PUFFMARKER.LEFTWRIST_ACCEL_X);
        tp.importData(path + "/left-wrist-accely.txt", PUFFMARKER.LEFTWRIST_ACCEL_Y);
        tp.importData(path + "/left-wrist-accelz.txt", PUFFMARKER.LEFTWRIST_ACCEL_Z);
        tp.importData(path + "/left-wrist-gyrox.txt", PUFFMARKER.LEFTWRIST_GYRO_X);
        tp.importData(path + "/left-wrist-gyroy.txt", PUFFMARKER.LEFTWRIST_GYRO_Y);
        tp.importData(path + "/left-wrist-gyroz.txt", PUFFMARKER.LEFTWRIST_GYRO_Z);

        tp.importData(path + "/right-wrist-accely.txt", PUFFMARKER.RIGHTWRIST_ACCEL_Y);
        tp.importData(path + "/right-wrist-accelx.txt", PUFFMARKER.RIGHTWRIST_ACCEL_X);
        tp.importData(path + "/right-wrist-accelz.txt", PUFFMARKER.RIGHTWRIST_ACCEL_Z);
        tp.importData(path + "/right-wrist-gyrox.txt", PUFFMARKER.RIGHTWRIST_GYRO_X);
        tp.importData(path + "/right-wrist-gyroy.txt", PUFFMARKER.RIGHTWRIST_GYRO_Y);
        tp.importData(path + "/right-wrist-gyroz.txt", PUFFMARKER.RIGHTWRIST_GYRO_Z);

        tp.sort();

        int windowSize = 60000;

        StreamProcessor streamProcessor = new StreamProcessor(windowSize);
//        streamProcessor.importDatastreams(path + "/streamProcessor.serialized");
        streamProcessor.setPath(path);
        streamProcessor.loadModel("cStressModel", cStressModelPath);
        streamProcessor.loadModel("cStressRIPModel", cStressRIPModelPath);
        streamProcessor.loadModel("puffMarkerModel", puffMarkerModelPath);

        streamProcessor.dpInterface = new DataPointInterface() {
            @Override
            public void dataPointHandler(String stream, DataPoint dp) {
                System.out.println(path + "/" + stream + " " + dp);
            }

            @Override
            public void dataPointArrayHandler(String stream, DataPointArray dp) {
                System.out.println(path + "/" + stream + " " + dp);
            }
        };

//        streamProcessor.registerCallbackDataArrayStream(StreamConstants.ORG_MD2K_CSTRESS_FV);
//        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCEL_ACTIVITY);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_STRESSLABEL);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCEL_ACTIVITY);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_PUFFMARKER_PROBABILITY);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_PUFFMARKER_PUFFLABEL);

        long windowStartTime = -1;
        for (CSVDataPoint ap : tp) {
            DataPoint dp = new DataPoint(ap.timestamp, ap.value);

            if (windowStartTime < 0) {
                windowStartTime = Time.nextEpochTimestamp(dp.timestamp, windowSize);
            }

            if ((dp.timestamp - windowStartTime) >= windowSize) { //Process the buffer every windowSize milliseconds
                streamProcessor.go();
                windowStartTime += windowSize;
//                streamProcessor.exportDatastreams(path + "/streamProcessor.serialized");
            }

            streamProcessor.add(ap.channel, dp);

        }
        streamProcessor.go();
    }


}
