package de.dennisguse.opentracks.io.file.importer;

import org.junit.Assert;

import java.util.List;

import de.dennisguse.opentracks.content.data.TrackPoint;

public class TrackPointAssert {

    private boolean assertTime = true;
    private boolean assertAccuracy = true;

    public TrackPointAssert() {
    }

    public void assertEquals(TrackPoint expected, TrackPoint actual) {
        Assert.assertNotNull(actual.getTime());
        if (assertTime) {
            Assert.assertEquals(expected.getTime(), actual.getTime());
        }

        Assert.assertEquals(expected.getType(), actual.getType());

        if (expected.hasLocation()) {
            Assert.assertEquals(expected.hasLocation(), actual.hasLocation());
            if (expected.hasLocation()) {
                Assert.assertEquals(expected.getLatitude(), actual.getLatitude(), 0.001);
                Assert.assertEquals(expected.getLongitude(), actual.getLongitude(), 0.001);
            }

            Assert.assertEquals(expected.hasAltitude(), actual.hasAltitude());
            if (expected.hasAltitude()) {
                Assert.assertEquals(expected.getAltitude().toM(), actual.getAltitude().toM(), 0.001);
            }
        }

        Assert.assertEquals(expected.hasAltitudeGain(), actual.hasAltitudeGain());
        if (expected.hasAltitudeGain()) {
            Assert.assertEquals(expected.getAltitudeGain(), actual.getAltitudeGain(), 0.001);
        }
        Assert.assertEquals(expected.hasAltitudeLoss(), actual.hasAltitudeLoss());
        if (expected.hasAltitudeLoss()) {
            Assert.assertEquals(expected.getAltitudeLoss(), actual.getAltitudeLoss(), 0.001);
        }

        Assert.assertEquals(expected.hasSpeed(), actual.hasSpeed());
        if (expected.hasSpeed()) {
            Assert.assertEquals(expected.getSpeed().toMPS(), actual.getSpeed().toMPS(), 0.001);
        }

        if (assertAccuracy) {
            Assert.assertEquals(expected.hasAccuracy(), actual.hasAccuracy());
            if (expected.hasAccuracy()) {
                Assert.assertEquals(expected.getAccuracy(), actual.getAccuracy(), 0.001);
            }
        } else {
            Assert.assertFalse(actual.hasAccuracy());
        }

        Assert.assertEquals(expected.hasSensorDistance(), actual.hasSensorDistance());
        if (expected.hasSensorDistance()) {
            Assert.assertEquals(expected.getSensorDistance().toM(), actual.getSensorDistance().toM(), 0.001);
        }

        Assert.assertEquals(expected.hasHeartRate(), actual.hasHeartRate());
        if (expected.hasHeartRate()) {
            Assert.assertEquals(expected.getHeartRate_bpm(), actual.getHeartRate_bpm(), 0.001);
        }

        Assert.assertEquals(expected.hasPower(), actual.hasPower());
        if (expected.hasPower()) {
            Assert.assertEquals(expected.getPower(), actual.getPower(), 0.001);
        }

        Assert.assertEquals(expected.hasCyclingCadence(), actual.hasCyclingCadence());
        if (expected.hasCyclingCadence()) {
            Assert.assertEquals(expected.getCyclingCadence_rpm(), actual.getCyclingCadence_rpm(), 0.001);
        }
    }

    public void assertEquals(List<TrackPoint> expected, List<TrackPoint> actual) {
        Assert.assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    public TrackPointAssert ignoreTime() {
        this.assertTime = false;
        return this;
    }

    public TrackPointAssert noAccuracy() {
        this.assertAccuracy = false;
        return this;
    }
}
