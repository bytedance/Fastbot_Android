/*
 * This code is licensed under the Fastbot license. You may obtain a copy of this license in the LICENSE.txt file in the root directory of this source tree.
 */

package com.android.commands.monkey.events;

import android.graphics.PointF;
import android.graphics.Rect;
import android.view.KeyCharacterMap;
import android.view.Surface;

import com.android.commands.monkey.events.customize.AirplaneEvent;
import com.android.commands.monkey.events.customize.AlwaysFinishActivityEvent;
import com.android.commands.monkey.events.customize.ClickEvent;
import com.android.commands.monkey.events.customize.DragEvent;
import com.android.commands.monkey.events.customize.KeyEvent;
import com.android.commands.monkey.events.customize.PinchOrZoomEvent;
import com.android.commands.monkey.events.customize.RotationEvent;
import com.android.commands.monkey.events.customize.SwitchEvent;
import com.android.commands.monkey.events.customize.TrackballEvent;
import com.android.commands.monkey.events.customize.WifiEvent;
import com.android.commands.monkey.framework.AndroidDevice;
import com.android.commands.monkey.utils.Config;
import com.android.commands.monkey.utils.Logger;
import com.android.commands.monkey.utils.RandomHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhao Zhang
 */

public class CustomEventFuzzer {


    //private static Long CLICK_TIME = 1000L;

    /* private */ static final int GESTURE_TAP = 0;
    /**
     * Possible screen rotation degrees
     **/
    private static final int[] SCREEN_ROTATION_DEGREES = {Surface.ROTATION_0, Surface.ROTATION_90,
            Surface.ROTATION_180, Surface.ROTATION_270,};
    private static final int[] NAV_KEYS = {android.view.KeyEvent.KEYCODE_DPAD_UP, android.view.KeyEvent.KEYCODE_DPAD_DOWN,
            android.view.KeyEvent.KEYCODE_DPAD_LEFT, android.view.KeyEvent.KEYCODE_DPAD_RIGHT,};
    /**
     * Key events that perform major navigation options (so shouldn't be sent as
     * much).
     */
    private static final int[] MAJOR_NAV_KEYS = {android.view.KeyEvent.KEYCODE_MENU, /*
     * KeyEvent
     * .
     * KEYCODE_SOFT_RIGHT,
     */
            android.view.KeyEvent.KEYCODE_DPAD_CENTER,};
    /**
     * Key events that perform system operations.
     */
    private static final int[] SYS_KEYS = {
            android.view.KeyEvent.KEYCODE_HOME, android.view.KeyEvent.KEYCODE_BACK,
            android.view.KeyEvent.KEYCODE_CALL,
            //KeyEvent.KEYCODE_ENDCALL,
            //KeyEvent.KEYCODE_VOLUME_UP,
            android.view.KeyEvent.KEYCODE_VOLUME_DOWN, android.view.KeyEvent.KEYCODE_VOLUME_MUTE,
            android.view.KeyEvent.KEYCODE_MUTE,};
    /**
     * If a physical key exists?
     */
    private static final boolean[] PHYSICAL_KEY_EXISTS = new boolean[android.view.KeyEvent.getMaxKeyCode() + 1];
    //private static Pattern BOUNDS_RECT = Pattern.compile("([0-9]+),([0-9]+),([0-9]+),([0-9]+)");

    static {
        for (int i = 0; i < PHYSICAL_KEY_EXISTS.length; ++i) {
            PHYSICAL_KEY_EXISTS[i] = true;
        }
        // Only examine SYS_KEYS
        for (int i = 0; i < SYS_KEYS.length; ++i) {
            PHYSICAL_KEY_EXISTS[SYS_KEYS[i]] = KeyCharacterMap.deviceHasKey(SYS_KEYS[i]);
        }
    }

    public static List<CustomEvent> generateSimplifyFuzzingEvents() {
        List<CustomEvent> events = new ArrayList<CustomEvent>();
        int repeat = RandomHelper.nextBetween(1, 3);
        while (repeat > 0) {
            repeat--;
            int eventType = RandomHelper.nextInt(10);
            switch (eventType) {
                case 0:
                    Logger.infoPrintln("Fuzzing: generate rotation events.");
                    generateRotationEvent(events);
                    break;
                case 1:
                    Logger.infoPrintln("Fuzzing: generate app switch events.");
                    generateAppSwitchEvent(events);
                    break;
                default:
                    switch (eventType % 3) {
                        case 0:
                            Logger.infoPrintln("Fuzzing: generate drag.");
                            generateDragEvent(events);
                            break;
                        case 1:
                            Logger.infoPrintln("Fuzzing: generate pinch or zoom.");
                            generatePinchOrZoomEvent(events);
                            break;
                        default:
                            Logger.infoPrintln("Fuzzing: generate random click.");
                            generateClickEvent(events, RandomHelper.nextInt(1000));
                            break;
                    }
            }
        }
        return events;
    }

    public interface EventFuzzer {
        // add
        void genEvent(List<CustomEvent> events, boolean enableRate);
        double getRate();
    }

    private static ArrayList<EventFuzzer> eventFuzzers = null;

    // If you want to modify the weight, just add it to eventFuzzers a few more times.
    private static void initEventFuzzers() {
        if (eventFuzzers == null)
            eventFuzzers = new ArrayList<EventFuzzer>();
        else
            return; // Guaranteed to only initialize once

        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate rotation events.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doRotationFuzzing) {
                    generateRotationEvent(events);
                }
            }
            public double getRate(){return Config.doRotationFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate app switch events.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doAppSwitchFuzzing) {
                    generateAppSwitchEvent(events);
                }
            }
            public double getRate(){return Config.doAppSwitchFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate trackball.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doTrackballFuzzing) {
                    generateTrackballEvent(events);
                }
            }
            public double getRate(){return Config.doTrackballFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate navigation events.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doNavKeyFuzzing) {
                    generateFuzzingNavKeyEvent(events);
                }
            }
            public double getRate(){return Config.doNavKeyFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate major navigation events.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doNavKeyFuzzing) {
                    generateFuzzingMajorNavKeyEvent(events);
                }
            }
            public double getRate(){return Config.doNavKeyFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate keycode events.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doKeyCodeFuzzing) {
                    int lastKey;
                    while (true) {
                        lastKey = 1 + RandomHelper.nextInt(android.view.KeyEvent.getMaxKeyCode() - 1);
                        if (lastKey != android.view.KeyEvent.KEYCODE_POWER && lastKey != android.view.KeyEvent.KEYCODE_ENDCALL
                                && lastKey != android.view.KeyEvent.KEYCODE_SLEEP && PHYSICAL_KEY_EXISTS[lastKey]) {
                            break;
                        }
                    }
                    generateKeyEvent(events, lastKey);
                }
            }
            public double getRate(){return Config.doKeyCodeFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate system key events.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doSystemKeyFuzzing) {
                    generateFuzzingSysKeyEvent(events);
                }
            }
            public double getRate(){return Config.doSystemKeyFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate drag.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doDragFuzzing) {
                    generateDragEvent(events);
                }
            }
            public double getRate(){return Config.doDragFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate pinch or zoom.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doPinchZoomFuzzing) {
                    generatePinchOrZoomEvent(events);
                }
            }
            public double getRate(){return Config.doPinchZoomFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate wifi switch.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doMutationWifiFuzzing) {
                    generateWifiEvent(events);
                }
            }
            public double getRate(){return Config.doMutationWifiFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate airplane switch .");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doMutationAirplaneFuzzing) {
                    generateAirplaneEvent(events);
                }
            }
            public double getRate(){return Config.doMutationAirplaneFuzzing;}
        });
        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate always finish activity switch .");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doMutationMutationAlwaysFinishActivitysFuzzing) {
                    generateAlwaysFinishActivitiesEvent(events);
                }
            }
            public double getRate(){return Config.doMutationMutationAlwaysFinishActivitysFuzzing;}
        });

        eventFuzzers.add(new EventFuzzer() {
            @Override
            public void genEvent(List<CustomEvent> events, boolean enableRate) {
                Logger.infoPrintln("Fuzzing: generate random click.");
                if(!enableRate|| RandomHelper.nextFloat() < Config.doClickFuzzing) {
                    generateClickEvent(events, RandomHelper.nextInt(1000));
                }
            }
            public double getRate(){return Config.doClickFuzzing;}
        });
    }

    public static List<CustomEvent> generateFuzzingEvents() {
        if (eventFuzzers == null)
            initEventFuzzers();

        int eventSize = RandomHelper.nextBetween(5, 10);
        List<CustomEvent> fuzzingEvents = new ArrayList<CustomEvent>();
        double totalRate = 0.0;
        for(int i = 0 ; i < eventFuzzers.size(); i++)
        {
            totalRate +=  eventFuzzers.get(i).getRate();
        }
        while(fuzzingEvents.size() < eventSize)
        {
            // Select by probability, generate with 100% probability
            double pickIndex = RandomHelper.nextDouble() * totalRate;
            double offsetRate = 0.0;
            for(int i = 0 ; i < eventFuzzers.size(); i++)
            {
                offsetRate +=  eventFuzzers.get(i).getRate();
                Logger.infoPrintln("Fuzzing total "+totalRate+" offset "+offsetRate +" i " + i);
                if(offsetRate > pickIndex)
                {
                    eventFuzzers.get(i).genEvent(fuzzingEvents, false);
                    break;
                }
            }
        }
        return fuzzingEvents;
    }

    private static void generateClickEvent(List<CustomEvent> events, long waitTime) {
        Rect displayBounds = AndroidDevice.getDisplayBounds();
        int x = RandomHelper.nextInt(displayBounds.right);
        int y = RandomHelper.nextInt(displayBounds.bottom);
        events.add(new ClickEvent(new PointF(x, y), waitTime));
    }


    private static void generateWifiEvent(List<CustomEvent> events) {
        Rect displayBounds = AndroidDevice.getDisplayBounds();
        int x = RandomHelper.nextInt(displayBounds.right);
        int y = RandomHelper.nextInt(displayBounds.bottom);
        events.add(new WifiEvent());
    }


    private static void generateAirplaneEvent(List<CustomEvent> events) {
        events.add(new AirplaneEvent());
    }


    private static void generateAlwaysFinishActivitiesEvent(List<CustomEvent> events) {
        events.add(new AlwaysFinishActivityEvent());
    }


    protected static PointF randomPoint(int width, int height) {
        return new PointF(RandomHelper.nextInt(width), RandomHelper.nextInt(height));
    }

    protected static PointF randomVector() {
        return new PointF((RandomHelper.nextFloat() - 0.5f) * 50, (RandomHelper.nextFloat() - 0.5f) * 50);
    }

    protected static PointF randomWalk(int width, int height, PointF point, PointF vector) {
        float x = (float) Math.max(Math.min(point.x + RandomHelper.nextFloat() * vector.x, width), 0);
        float y = (float) Math.max(Math.min(point.y + RandomHelper.nextFloat() * vector.y, height), 0);
        return new PointF(x, y);
    }

    private static void generatePinchOrZoomEvent(List<CustomEvent> events) {
        Rect displayBounds = AndroidDevice.getDisplayBounds();
        int width = displayBounds.right;
        int height = displayBounds.bottom;
        int count = RandomHelper.nextInt(10);
        int index = 0;
        PointF[] points = new PointF[6 + count * 2];
        PointF p1 = randomPoint(width, height);
        points[index++] = p1; // first action down

        p1 = randomPoint(width, height);
        PointF p2 = randomPoint(width, height);
        points[index++] = p1;
        points[index++] = p2;

        PointF v1 = randomVector();
        PointF v2 = randomVector();
        // An extra point in addition to count is required to simulate finger lift
        for (int i = 0; i < count + 1; i++) {
            p1 = randomWalk(width, height, p1, v1);
            p2 = randomWalk(width, height, p2, v2);
            points[index++] = p1;
            points[index++] = p2;
        }
        p1 = randomWalk(width, height, p1, v1);
        points[index] = p1;
        events.add(new PinchOrZoomEvent(points));
    }

    private static void generateDragEvent(List<CustomEvent> events) {
        Rect displayBounds = AndroidDevice.getDisplayBounds();
        int width = displayBounds.right;
        int height = displayBounds.bottom;
        int count = RandomHelper.nextInt(10);
        int index = 0;
        PointF[] points = new PointF[2 + count];
        PointF p = randomPoint(width, height);
        points[index++] = p;
        PointF v1 = randomVector();
        for (; index < points.length; index++) {
            p = randomWalk(width, height, p, v1);
            points[index] = p;
        }
        events.add(new DragEvent(points));
    }

    private static void generateKeyEvent(List<CustomEvent> events, int key) {
        events.add(new KeyEvent(key));
    }


    private static void generateFuzzingSysKeyEvent(List<CustomEvent> events) {
        int key = SYS_KEYS[RandomHelper.nextInt(SYS_KEYS.length)];
        generateKeyEvent(events, key);
    }

    private static void generateFuzzingMajorNavKeyEvent(List<CustomEvent> events) {
        int key = MAJOR_NAV_KEYS[RandomHelper.nextInt(MAJOR_NAV_KEYS.length)];
        generateKeyEvent(events, key);
    }

    private static void generateFuzzingNavKeyEvent(List<CustomEvent> events) {
        int key = NAV_KEYS[RandomHelper.nextInt(NAV_KEYS.length)];
        generateKeyEvent(events, key);
    }

    private static void generateTrackballEvent(List<CustomEvent> events) {
        int moves = 10;
        int[] deltaX = new int[moves];
        int[] deltaY = new int[moves];
        for (int i = 0; i < moves; ++i) {
            deltaX[i] = RandomHelper.nextInt(10) - 5;
            deltaY[i] = RandomHelper.nextInt(10) - 5;
        }
        events.add(new TrackballEvent(deltaX, deltaY, RandomHelper.nextBoolean()));
    }

    private static void generateAppSwitchEvent(List<CustomEvent> events) {
        events.add(new SwitchEvent(RandomHelper.nextBoolean()));
    }

    private static void generateRotationEvent(List<CustomEvent> events) {
        int degree = SCREEN_ROTATION_DEGREES[RandomHelper.nextInt(SCREEN_ROTATION_DEGREES.length)];
        events.add(new RotationEvent(degree, RandomHelper.nextBoolean()));
    }


}
