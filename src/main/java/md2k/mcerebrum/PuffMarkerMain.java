package md2k.mcerebrum;

import md2k.mcerebrum.cstress.autosense.PUFFMARKER;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Nazir Saleheen <nsleheen@memphis.edu>
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

public class PuffMarkerMain {
    static int[][] sIds = {{}, {2, 3, 4, 5}, {3, 4, 5, 6, 7}, {1, 2, 3}, {1}, {1}, {1}};
    static String cStressRIPModelPath = "";
    static String cStressModelPath = "";
    static String puffMarkerModelPath = "";

    /**
     * Main driver class for replaying AutoSense data through StreamProcessor
     *
     * @param args Arguments to the program
     */

    public static void main(String[] args) {

        String path = args[0];
        String cStressRIPModelPath = args[1];
        String cStressModelPath = args[2];
        String puffMarkerModelPath = args[3];

        trainPuffMarkerLabData(path); // for training data
//        runPuffmarkerAllsession(path, new String[]{"2006"}); // for testing
    }

    private static void trainPuffMarkerLabData(String path) {
        runPuffmarkerAllsession(path, new String[]{ "p01", "p02", "p03", "p04", "p05", "p06"});
    }

    public static void runPuffmarkerAllsession(String dir, String[] pids) {

        for (String pid : pids) {

            File filefolder = new File(dir+ pid);
            for (final File file : filefolder.listFiles()) {
                String session = file.getName();
                if (!session.startsWith("s")) continue;

                System.out.println(dir + pid+ "\\" + session);
                System.out.println("-------------------------------------------------------------------------------------------");

                doProcess(pid, session, dir);
            }
        }
    }

    private static void doProcess(String pid, String sid, String path) {

        ExecutorService executor = Executors.newFixedThreadPool(4);
//        String session = "s" + String.format("%02d", sid);
        String session = sid;
        Runnable worker = new WorkerThread(path + pid + "\\" + session + "\\", cStressModelPath, cStressRIPModelPath, puffMarkerModelPath);
        executor.execute(worker);
        executor.shutdown();
        while (!executor.isTerminated()) ;
    }
}
