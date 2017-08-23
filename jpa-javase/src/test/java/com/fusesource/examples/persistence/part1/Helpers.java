/**
 *  Copyright 2005-2017 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package com.fusesource.examples.persistence.part1;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

import org.apache.aries.transaction.internal.XidFactoryImpl;
import org.apache.geronimo.transaction.log.HOWLLog;
import org.apache.geronimo.transaction.manager.XidFactory;

public class Helpers {

    private static XidFactory xidFactory = new XidFactoryImpl("org.apache.aries.transaction".getBytes());
    private static File BASE = new File("target/txlogs");

    public static HOWLLog createDefaultTransactionLog() throws Exception {
        return createTransactionLog(UUID.randomUUID().toString(), "transaction", 3, -1, 1, new Hashtable<String, Object>());
    }

    /**
     * From <code>org.apache.aries.transaction.internal.LogConversionTest</code>
     * @param logFileDir
     * @param logFileName
     * @param maxLogFiles
     * @param maxBlocksPerFile
     * @param bufferSizeInKB
     * @param properties
     * @return
     * @throws Exception
     */
    public static HOWLLog createTransactionLog(String logFileDir, String logFileName,
                                               int maxLogFiles, int maxBlocksPerFile, int bufferSizeInKB,
                                               Dictionary<String, Object> properties) throws Exception {
        properties.put("aries.transaction.recoverable", "true");
        properties.put("aries.transaction.howl.bufferClassName", "org.objectweb.howl.log.BlockLogBuffer");
        properties.put("aries.transaction.howl.checksumEnabled", "true");
        properties.put("aries.transaction.howl.adler32Checksum", "true");
        properties.put("aries.transaction.howl.flushSleepTime", "50");
        properties.put("aries.transaction.howl.logFileExt", "log");
        properties.put("aries.transaction.howl.logFileName", logFileName);
        properties.put("aries.transaction.howl.minBuffers", "1");
        properties.put("aries.transaction.howl.maxBuffers", "0");
        properties.put("aries.transaction.howl.threadsWaitingForceThreshold", "-1");
        properties.put("aries.transaction.flushPartialBuffers", "true");
        String absoluteLogFileDir = new File(BASE, logFileDir).getAbsolutePath() + "/";
        properties.put("aries.transaction.howl.logFileDir", absoluteLogFileDir);
        properties.put("aries.transaction.howl.bufferSize", Integer.toString(bufferSizeInKB));
        properties.put("aries.transaction.howl.maxBlocksPerFile", Integer.toString(maxBlocksPerFile));
        properties.put("aries.transaction.howl.maxLogFiles", Integer.toString(maxLogFiles));

        return new HOWLLog("org.objectweb.howl.log.BlockLogBuffer", bufferSizeInKB,
                true, true, 50,
                absoluteLogFileDir, "log", logFileName,
                maxBlocksPerFile, 0, maxLogFiles, 1, -1, true, xidFactory, null);
    }

}
