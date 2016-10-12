package md2k.mcerebrum;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

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
public class CSVParser implements Iterable<CSVDataPoint> {

    private final List<CSVDataPoint> data;


    public CSVParser() {
        this.data = new ArrayList<CSVDataPoint>();
    }

    public void importData(String filename, int channel) {

        CSVDataPoint tempPacket;

        String[] tokens;
        double data;
        long timestamp;

        File file = new File(filename);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNext()) {
                tokens = scanner.nextLine().split(",");
                double ts = Double.parseDouble(tokens[0]);
                timestamp = (long) ts;
                data = Double.parseDouble(tokens[1]);

                tempPacket = new CSVDataPoint(channel, timestamp, data);
                this.data.add(tempPacket);
            }
        } catch (FileNotFoundException ignored) {
        }
    }


    public void sort() {
        Collections.sort(this.data);
    }

    @Override
    public Iterator<CSVDataPoint> iterator() {
        return this.data.iterator();
    }
}
