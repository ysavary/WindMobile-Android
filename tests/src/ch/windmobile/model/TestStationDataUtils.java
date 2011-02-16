package ch.windmobile.model;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestStationDataUtils extends TestCase {

    String[] directionLabels;

    @Override
    protected void setUp() throws Exception {
        directionLabels = new String[] { "N", "NE", "E", "SE", "S", "SO", "O", "NO" };
    }

    public void testDirections() {
        Assert.assertEquals("NO", StationDataUtils.getWindDirectionLabel(directionLabels, 337));
        Assert.assertEquals("N", StationDataUtils.getWindDirectionLabel(directionLabels, 338));
        Assert.assertEquals("N", StationDataUtils.getWindDirectionLabel(directionLabels, 359));
        Assert.assertEquals("N", StationDataUtils.getWindDirectionLabel(directionLabels, 360));
        Assert.assertEquals("N", StationDataUtils.getWindDirectionLabel(directionLabels, 22));
        Assert.assertEquals("NE", StationDataUtils.getWindDirectionLabel(directionLabels, 23));
    }
}
