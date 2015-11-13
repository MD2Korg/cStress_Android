package md2k.mCerebrum.cStress.Library;


import md2k.mCerebrum.cStress.Structs.DataPoint;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.*;
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
public class DataStream {

    public HashMap<String,Object> metadata;
    public ArrayList<DataPoint> data;

    private SummaryStatistics stats;
    private DescriptiveStatistics descriptiveStats;

    public boolean preserve;

    public DataStream(String name) {
        data = new ArrayList<DataPoint>();
        metadata = new HashMap<String, Object>();
        metadata.put("name", name);
        preserve = false;
        stats = new SummaryStatistics();
        descriptiveStats = new DescriptiveStatistics(10000);
    }

    public DataStream(DataStream other) {
        this.data = new ArrayList<DataPoint>(other.data);
        this.metadata = other.metadata;
        this.stats = other.stats;
        this.descriptiveStats = other.descriptiveStats;
        this.preserve = other.preserve;
    }

    public DataStream(String name, DataPoint[] data) {
        this(name);
        for(DataPoint dp: data) {
            this.add(dp);
        }
    }

    public void setPreservedLastInsert(boolean state) {
        preserve = state;
    }

    public void persist(String filename) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true), "utf-8"));
            for(DataPoint dp: this.data) {
                writer.write(dp.timestamp + ", " + dp.value + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        if(!preserve) {
            data.clear();
        } else {
            DataPoint temp = data.get(data.size()-1);
            data.clear();
            data.add(temp);
        }
    }

    public void add(DataPoint dp) {
        data.add(new DataPoint(dp));
        stats.addValue(dp.value);
        descriptiveStats.addValue(dp.value);
    }

    public String getName() {
        return (String) metadata.get("name");
    }

    public double getPercentile(int i) {
        return descriptiveStats.getPercentile(i);
    }

    public double getMean() {
        return stats.getMean();
    }

    public double getStandardDeviation() {
        return stats.getStandardDeviation();
    }

    public long statsSize() {
        return stats.getN();
    }
}
