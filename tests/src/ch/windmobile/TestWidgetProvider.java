package ch.windmobile;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestWidgetProvider extends TestCase {

    public void testMigrateLegacyStationId() {
        Assert.assertEquals("jdc:jdc-1001", WidgetProvider.migrateLegacyStationId("jdc:1001"));
        Assert.assertEquals(null, WidgetProvider.migrateLegacyStationId("jdc:wrong_id"));
    }
}
