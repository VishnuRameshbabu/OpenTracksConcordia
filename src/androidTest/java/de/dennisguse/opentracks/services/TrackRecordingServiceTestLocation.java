package de.dennisguse.opentracks.services;

import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.rule.ServiceTestRule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.dennisguse.opentracks.content.data.Speed;
import de.dennisguse.opentracks.content.data.TestDataUtil;
import de.dennisguse.opentracks.content.data.Track;
import de.dennisguse.opentracks.content.data.TrackPoint;
import de.dennisguse.opentracks.content.provider.ContentProviderUtils;
import de.dennisguse.opentracks.content.provider.CustomContentProvider;
import de.dennisguse.opentracks.content.sensor.SensorDataHeartRate;
import de.dennisguse.opentracks.content.sensor.SensorDataSet;
import de.dennisguse.opentracks.io.file.importer.TrackPointAssert;
import de.dennisguse.opentracks.services.sensors.BluetoothRemoteSensorManager;
import de.dennisguse.opentracks.util.PreferencesUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Tests insert location.
 * <p>
 * //TODO ATTENTION: This tests deletes all stored tracks in the database.
 * So, if it is executed on a real device, data might be lost.
 */
@RunWith(AndroidJUnit4.class)
//TODO Implement as mock test; no need to store data in database
public class TrackRecordingServiceTestLocation {

    @Rule
    public final ServiceTestRule mServiceRule = ServiceTestRule.withTimeout(5, TimeUnit.SECONDS);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    private final Context context = ApplicationProvider.getApplicationContext();
    private ContentProviderUtils contentProviderUtils;

    private TrackRecordingService service;

    @BeforeClass
    public static void preSetUp() {
        // Prepare looper for Android's message queue
        if (Looper.myLooper() == null) Looper.prepare();
    }

    @AfterClass
    public static void finalTearDown() {
        if (Looper.myLooper() != null) Looper.myLooper().quit();
    }

    @Before
    public void setUp() throws TimeoutException {
        // Set up the mock content resolver
        ContentProvider customContentProvider = new CustomContentProvider() {
        };
        customContentProvider.attachInfo(context, null);

        contentProviderUtils = new ContentProviderUtils(context);
        tearDown();

        // Let's use default values.
        SharedPreferences sharedPreferences = PreferencesUtils.getSharedPreferences(context);
        sharedPreferences.edit().clear().commit();

        service = ((TrackRecordingService.Binder) mServiceRule.bindService(TrackRecordingServiceTest.createStartIntent(context)))
                .getService();
    }

    @After
    public void tearDown() {
        // Ensure that the database is empty after every test
        contentProviderUtils.deleteAllTracks(context);
    }

    @MediumTest
    @Test
    public void testOnLocationChangedAsync_movingAccurate() throws Exception {
        // given
        Track.Id trackId = service.startNewTrack();

        // when
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 1, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0001, 35.0, 2, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0002, 35.0, 3, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0003, 35.0, 4, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0004, 35.0, 5, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0005, 35.0, 6, 15);

        service.endCurrentTrack();

        // then
        assertFalse(service.isRecording());

        List<TrackPoint> trackPoints = TestDataUtil.getTrackPoints(contentProviderUtils, trackId);
        TrackPointAssert a = new TrackPointAssert()
                .ignoreTime();
        a.assertEquals(List.of(
                new TrackPoint(TrackPoint.Type.SEGMENT_START_MANUAL),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(1)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(2)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(3)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(4)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(5)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(6)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.SEGMENT_END_MANUAL)
        ), trackPoints);
    }

    @MediumTest
    @Test
    public void testOnLocationChangedAsync_slowMovingAccurate() throws Exception {
        // given
        Track.Id trackId = service.startNewTrack();
        assertNotNull(trackId);

        // when
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 1, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.000001, 35.0, 2, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.000002, 35.0, 3, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.000003, 35.0, 4, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.000004, 35.0, 5, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.000005, 35.0, 6, 15);

        service.endCurrentTrack();

        // then
        assertFalse(service.isRecording());

        List<TrackPoint> trackPoints = TestDataUtil.getTrackPoints(contentProviderUtils, trackId);
        TrackPointAssert a = new TrackPointAssert()
                .ignoreTime();
        a.assertEquals(List.of(
                new TrackPoint(TrackPoint.Type.SEGMENT_START_MANUAL),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(1)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(6)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.SEGMENT_END_MANUAL)
        ), trackPoints);
    }

    @MediumTest
    @Test
    public void testOnLocationChangedAsync_idle() throws Exception {
        // given
        Track.Id trackId = service.startNewTrack();

        // when
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 1, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 2, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 3, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 4, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 5, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 6, 0);

        service.endCurrentTrack();

        // then
        assertFalse(service.isRecording());

        List<TrackPoint> trackPoints = TestDataUtil.getTrackPoints(contentProviderUtils, trackId);
        TrackPointAssert a = new TrackPointAssert()
                .ignoreTime();
        a.assertEquals(List.of(
                new TrackPoint(TrackPoint.Type.SEGMENT_START_MANUAL),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(1)
                        .setSpeed(Speed.of(0)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT) //TODO Why is this added? Systems is idle and not moving at all.
                        .setAccuracy(2)
                        .setSpeed(Speed.of(0)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(6)
                        .setSpeed(Speed.of(0)),
                new TrackPoint(TrackPoint.Type.SEGMENT_END_MANUAL)
        ), trackPoints);
    }

    @MediumTest
    @Test
    public void testOnLocationChangedAsync_idle_withMovement() throws Exception {
        // given
        Track.Id trackId = service.startNewTrack();

        // when
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 1, 15);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 2, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 3, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 4, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 5, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 6, 15);

        service.endCurrentTrack();

        // then
        assertFalse(service.isRecording());

        List<TrackPoint> trackPoints = TestDataUtil.getTrackPoints(contentProviderUtils, trackId);
        TrackPointAssert a = new TrackPointAssert()
                .ignoreTime();
        a.assertEquals(List.of(
                new TrackPoint(TrackPoint.Type.SEGMENT_START_MANUAL),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(1)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT) //TODO Check why this trackPoint is inserted.
                        .setAccuracy(2)
                        .setSpeed(Speed.of(0)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT) //TODO Check why this trackPoint is inserted.
                        .setAccuracy(5)
                        .setSpeed(Speed.of(0)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(6)
                        .setSpeed(Speed.of(15)),
                new TrackPoint(TrackPoint.Type.SEGMENT_END_MANUAL)
        ), trackPoints);
    }

    @MediumTest
    @Test
    public void testOnLocationChangedAsync_idle_withSensorData() throws Exception {
        // given
        Track.Id trackId = service.startNewTrack();

        service.setRemoteSensorManager(new BluetoothRemoteSensorManager(context) {

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public SensorDataSet getSensorData() {
                SensorDataSet sensorDataSet = new SensorDataSet();
                sensorDataSet.set(new SensorDataHeartRate("sensorName", "sensorAddress", 5f));
                return sensorDataSet;
            }
        });

        // when
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 1, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 2, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 3, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 4, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 5, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 6, 0);

        service.endCurrentTrack();

        // then
        assertFalse(service.isRecording());

        List<TrackPoint> trackPoints = TestDataUtil.getTrackPoints(contentProviderUtils, trackId);
        TrackPointAssert a = new TrackPointAssert()
                .ignoreTime();
        a.assertEquals(List.of(
                new TrackPoint(TrackPoint.Type.SEGMENT_START_MANUAL),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(1)
                        .setSpeed(Speed.of(0))
                        .setHeartRate_bpm(5f),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(2)
                        .setSpeed(Speed.of(0))
                        .setHeartRate_bpm(5f),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(3)
                        .setSpeed(Speed.of(0))
                        .setHeartRate_bpm(5f),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(4)
                        .setSpeed(Speed.of(0))
                        .setHeartRate_bpm(5f),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(5)
                        .setSpeed(Speed.of(0))
                        .setHeartRate_bpm(5f),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(6)
                        .setSpeed(Speed.of(0))
                        .setHeartRate_bpm(5f),
                new TrackPoint(TrackPoint.Type.SEGMENT_END_MANUAL)
                        .setHeartRate_bpm(5f)
        ), trackPoints);
    }

    @MediumTest
    @Test
    public void testOnLocationChangedAsync_segment() throws Exception {
        // given
        Track.Id trackId = service.startNewTrack();

        // when
        TrackRecordingServiceTest.newTrackPoint(service, 45.0, 35.0, 1, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.1, 35.0, 2, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.1, 35.0, 3, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.2, 35.0, 4, 0);
        TrackRecordingServiceTest.newTrackPoint(service, 45.2, 35.0, 5, 0);

        service.endCurrentTrack();

        // then
        assertFalse(service.isRecording());

        List<TrackPoint> trackPoints = TestDataUtil.getTrackPoints(contentProviderUtils, trackId);

        TrackPointAssert a = new TrackPointAssert()
                .ignoreTime();
        a.assertEquals(List.of(
                new TrackPoint(TrackPoint.Type.SEGMENT_START_MANUAL),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(1)
                        .setSpeed(Speed.of(0)),

                new TrackPoint(TrackPoint.Type.SEGMENT_START_AUTOMATIC)
                        .setAccuracy(2)
                        .setSpeed(Speed.of(0)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(3)
                        .setSpeed(Speed.of(0)),

                new TrackPoint(TrackPoint.Type.SEGMENT_START_AUTOMATIC)
                        .setAccuracy(4)
                        .setSpeed(Speed.of(0)),
                new TrackPoint(TrackPoint.Type.TRACKPOINT)
                        .setAccuracy(5)
                        .setSpeed(Speed.of(0)),
                new TrackPoint(TrackPoint.Type.SEGMENT_END_MANUAL)
        ), trackPoints);
    }
}
