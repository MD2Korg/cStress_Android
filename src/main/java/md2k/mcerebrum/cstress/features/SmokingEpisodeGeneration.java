package md2k.mcerebrum.cstress.features;

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.library.datastream.DataArrayStream;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.structs.DataPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nsleheen on 5/19/2016.
 */
public class SmokingEpisodeGeneration {
    public static int PUFFLABEL_BUFFER_SIZE = 10;
    public static int MINIMUM_TIME_DIFFERENCE_BETWEEN_EPISODES = 10 * 60 * 1000;
    public static int MINIMUM_TIME_DIFFERENCE_FIRST_AND_LAST_PUFFS = 5 * 60 * 1000;
    public static int MINIMUM_INTER_PUFF_DURATION = 5 * 1000;
    public static int MINIMUM_PUFFS_IN_EPISODE = 5;
    public static int MINIMUM_PUFFS_IN_LAPSE_EPISODE = 4;

    public SmokingEpisodeGeneration(DataStreams datastreams) {

        DataPointStream puffCountPerMinuteDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PUFF_COUNT_PER_MINUTE);

        DataPointStream puffLabelDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PUFFLABEL);
        puffLabelDataStream.setHistoricalBufferSize(PUFFLABEL_BUFFER_SIZE);
        DataPointStream puffProbDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PROBABILITY);
        DataArrayStream puffFVDataStream = datastreams.getDataArrayStream(StreamConstants.ORG_MD2K_PUFFMARKER_FV);

        DataPoint lastPuff = getLastHistoricalValue(puffLabelDataStream);
        Long lastPuffTimeStamp = 0L;
        if (lastPuff != null)
            lastPuffTimeStamp = lastPuff.timestamp;

        DataPointStream puffLabelDataStreamMinute = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PUFFLABEL_MINUTE);
        DataPointStream puffProbDataStreamMinute = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PROBABILITY_MINUTE);
        DataArrayStream puffFVDataStreamMinute = datastreams.getDataArrayStream(StreamConstants.ORG_MD2K_PUFFMARKER_FV_MINUTE);

        for (int i = 0; i < puffLabelDataStreamMinute.data.size(); i++) {
            DataPoint dp = puffLabelDataStreamMinute.data.get(i);
            if (dp.value == 1 && dp.timestamp > lastPuffTimeStamp + MINIMUM_INTER_PUFF_DURATION) {
                puffLabelDataStream.add(dp);
                puffProbDataStream.add(puffProbDataStreamMinute.data.get(i));
                puffFVDataStream.add(puffFVDataStreamMinute.data.get(i));
                lastPuffTimeStamp = dp.timestamp;
            }
        }

        DataPointStream smokingEpisodeDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_SMOKING_EPISODE);
        smokingEpisodeDataStream.setHistoricalBufferSize(1);

        List<DataPoint> last10Puffs = getLastNHistoricalValue(puffLabelDataStream, 15);
        List<DataPoint> last10PuffsRev = new ArrayList<DataPoint>();
        for (int i=last10Puffs.size()-1; i>=0; i--)
            last10PuffsRev.add(last10Puffs.get(i));

        if (last10PuffsRev.size() >= MINIMUM_PUFFS_IN_EPISODE && Math.abs(last10PuffsRev.get(0).timestamp - last10PuffsRev.get(MINIMUM_PUFFS_IN_EPISODE-1).timestamp) <= MINIMUM_TIME_DIFFERENCE_FIRST_AND_LAST_PUFFS) {
            int i = MINIMUM_PUFFS_IN_EPISODE;
            while (last10PuffsRev.size() > i && Math.abs(last10PuffsRev.get(0).timestamp - last10PuffsRev.get(i).timestamp) <= MINIMUM_TIME_DIFFERENCE_FIRST_AND_LAST_PUFFS)
                i++;
            long currentTimestamp = last10PuffsRev.get(i - 1).timestamp;
            DataPoint lastEpi = getLastHistoricalValue(smokingEpisodeDataStream);
            if (lastEpi == null || currentTimestamp - lastEpi.timestamp > MINIMUM_TIME_DIFFERENCE_BETWEEN_EPISODES) {
                smokingEpisodeDataStream.add(new DataPoint(currentTimestamp, 1));
            }
        }

/*        DataPointStream smokingLapseEpisodeDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_SMOKING_LAPSE_EPISODE);
        smokingLapseEpisodeDataStream.setHistoricalBufferSize(1);

        last10Puffs = getLastNHistoricalValue(puffLabelDataStream, 10);
        last10PuffsRev = new ArrayList<DataPoint>();
        for (int i=last10Puffs.size()-1; i>=0; i--)
            last10PuffsRev.add(last10Puffs.get(i));

        if (last10PuffsRev.size() >= MINIMUM_PUFFS_IN_LAPSE_EPISODE && Math.abs(last10PuffsRev.get(0).timestamp - last10PuffsRev.get(3).timestamp) <= MINIMUM_TIME_DIFFERENCE_FIRST_AND_LAST_PUFFS) {
            int i = MINIMUM_PUFFS_IN_LAPSE_EPISODE;
            while (last10PuffsRev.size() > i && Math.abs(last10PuffsRev.get(0).timestamp - last10PuffsRev.get(i).timestamp) <= MINIMUM_TIME_DIFFERENCE_FIRST_AND_LAST_PUFFS)
                i++;
            long currentTimestamp = last10PuffsRev.get(i - 1).timestamp;
            DataPoint lastEpi = getLastHistoricalValue(smokingLapseEpisodeDataStream);
            if (lastEpi == null || currentTimestamp - lastEpi.timestamp > MINIMUM_TIME_DIFFERENCE_BETWEEN_EPISODES) {
                smokingLapseEpisodeDataStream.add(new DataPoint(currentTimestamp, 1));
            }
        }*/
    }

    public List<DataPoint> getLastNHistoricalValue(DataPointStream dataPointStream, int n) {
        List<DataPoint> listHistory = dataPointStream.getHistoricalNValues(n);
        if (listHistory.size() == 0) {
            return new ArrayList<DataPoint>();
        }
        return listHistory;
    }

    public DataPoint getLastHistoricalValue(DataPointStream dataPointStream) {
        List<DataPoint> listHistory = dataPointStream.getHistoricalNValues(1);
        if (listHistory.size() == 0) {
            return null;
        }
        return listHistory.get(0);
    }

}
