package md2k.mcerebrum.cstress.library.structs;

import java.io.Serializable;
import java.util.ArrayList;
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

/**
 * Array version of DataPoint
 */
public class DataPointArray implements Serializable {
    public List<Double> value;
    public long timestamp;


    /**
     * DataPointArray Constructor
     *
     * @param timestamp Time in milliseconds since Jan 1st, 1970
     * @param value     Array of floating point values
     */
    public DataPointArray(long timestamp, List<Double> value) {
        this.value = new ArrayList<Double>(value);
        this.timestamp = timestamp;
    }

    /**
     * Copy Constructor
     *
     * @param other DataPointArray object
     */
    public DataPointArray(DataPointArray other) {
        this.value = other.value;
        this.timestamp = other.timestamp;
    }

    public DataPointArray(DataPoint dp) {
        this.timestamp = dp.timestamp;
        this.value.add(dp.value);
    }

    /**
     * @return String representation of a DataPoint object
     */
    @Override
    public String toString() {
        String result = "DPArray:(" + this.timestamp;
        for (Double d : value) {
            result += "," + d;
        }
        result += ")";
        return result;
    }
}
