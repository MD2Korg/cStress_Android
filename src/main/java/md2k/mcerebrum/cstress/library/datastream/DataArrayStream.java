package md2k.mcerebrum.cstress.library.datastream;


import md2k.mcerebrum.cstress.library.structs.DataPointArray;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
 * DataArray version of the DataPointStream object
 */
public class DataArrayStream extends DataStream {

    public List<DataPointArray> data;
    private CircularFifoQueue<DataPointArray> history;

    /**
     * Constructor
     *
     * @param name Unique name of the DataArrayStream object
     */
    public DataArrayStream(String name) {
        data = new ArrayList<DataPointArray>();
        history = new CircularFifoQueue<DataPointArray>(1);
        metadata = new HashMap<String, Object>();
        metadata.put("name", name);
        preserve = false;
    }

    /**
     * Constructor
     *
     * @param name Unique name of the DataArrayStream object
     */
    public DataArrayStream(String name, int historySize) {
        this(name);
        history = new CircularFifoQueue<DataPointArray>(historySize);
    }

    /**
     * Copy Constructor
     *
     * @param other DataArrayStream object to copy
     */
    public DataArrayStream(DataArrayStream other) {
        this.data = new ArrayList<DataPointArray>(other.data);
        this.history = new CircularFifoQueue<DataPointArray>(other.history);
        this.metadata = other.metadata;
        this.preserve = other.preserve;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        //Initialize transients
        metadata = new HashMap<String, Object>();
    }

    public void setHistoricalBufferSize(int historySize) {
        if (history.size() != historySize) {
            List<DataPointArray> temp = new ArrayList<DataPointArray>();
            for (DataPointArray dp : history) {
                temp.add(dp);
            }
            history = new CircularFifoQueue<DataPointArray>(historySize);
            for (DataPointArray dp : temp) {
                history.add(dp);
            }
        }
    }

    /**
     * Set method for data stream preservation
     *
     * @param state True to preserve last inserted value after a reset
     */
    public void setPreservedLastInsert(boolean state) {
        preserve = state;
    }


    /**
     * Retrieve historical data including the current window of data
     *
     * @param starttime The time from which to get data to the present
     * @return List of data that is within the time window
     */
    public List<DataPointArray> getHistoricalValues(long starttime) {
        List<DataPointArray> result = new ArrayList<DataPointArray>();

        for (DataPointArray dpa : history) {
            if (dpa.timestamp > starttime) {
                result.add(dpa);
            }
        }
        return result;
    }

    /**
     * Retrieve historical data including the current window of data
     *
     * @param numSamples Get previous N samples
     * @return List of data that is within the time window
     */
    public List<DataPointArray> getHistoricalNValues(int numSamples) {
        List<DataPointArray> result = new ArrayList<DataPointArray>();

        int fromI = history.size() - numSamples;
        if (fromI < 0) {
            fromI = 0;
        }
        for (int i = history.size() - 1; i >= fromI; i--) {
            result.add(history.get(i));
        }

        Collections.reverse(result);
        return result;
    }


    /**
     * Persist the data stream to the local file system
     *
     * @param filename File name and path where to append the data stream.
     */
    @Override
    public void persist(String filename) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true), "utf-8"));
            for (DataPointArray dp : this.data) {
                writer.write(Long.toString(dp.timestamp));
                for (Double d : dp.value) {
                    writer.write("," + d);
                }
                writer.write("\n");
            }
            writer.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Reset the data stream array for the next interval
     */
    @Override
    public void reset() {
        if (!preserve) {
            data.clear();
        } else {
            if (data.size() > 0) {
                DataPointArray temp = data.get(data.size() - 1);
                data.clear();
                data.add(temp);
            } else {
                data.clear();
            }
        }
    }


    /**
     * Main method to add DatapointArrays to the data stream
     *
     * @param dp New DatapointArray to add to the data stream
     */
    public void add(DataPointArray dp) {
        data.add(new DataPointArray(dp));
        history.add(new DataPointArray(dp));

        if (dataPointInterface != null) {
            dataPointInterface.dataPointArrayHandler((String) metadata.get("name"), dp);
        }
    }

    /**
     * Retrieve stream name
     *
     * @return The unique stream name
     */
    public String getName() {
        return (String) metadata.get("name");
    }

}
