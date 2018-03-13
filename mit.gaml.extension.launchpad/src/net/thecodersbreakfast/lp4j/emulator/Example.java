package net.thecodersbreakfast.lp4j.emulator;

import java.util.concurrent.CountDownLatch;

import net.thecodersbreakfast.lp4j.api.BackBufferOperation;
import net.thecodersbreakfast.lp4j.api.Button;
import net.thecodersbreakfast.lp4j.api.Color;
import net.thecodersbreakfast.lp4j.api.Launchpad;
import net.thecodersbreakfast.lp4j.api.LaunchpadClient;
import net.thecodersbreakfast.lp4j.api.LaunchpadListener;
import net.thecodersbreakfast.lp4j.api.LaunchpadListenerAdapter;
import net.thecodersbreakfast.lp4j.api.Pad;
import net.thecodersbreakfast.lp4j.midi.*;
public class Example {

    private static CountDownLatch stop = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        // Physical device (with auto-detected ports configuration)
        Launchpad launchpad = new MidiLaunchpad(MidiDeviceConfiguration.autodetect());

        //Launchpad launchpad = new EmulatorLaunchpad(9000);
        LaunchpadClient client = launchpad.getClient();

        MyListener myListener = new MyListener(client);
        launchpad.setListener((LaunchpadListener) myListener);

        // Set a red light under the STOP button
        client.reset();
        client.setButtonLight(Button.STOP, Color.RED, BackBufferOperation.NONE);

        stop.await();
        client.reset();
        launchpad.close();
    }

    public static class MyListener extends LaunchpadListenerAdapter {

        private final LaunchpadClient client;

        public MyListener(LaunchpadClient client) {
            this.client = client;
        }

        @Override
        public void onPadPressed(Pad pad, long timestamp) {
            client.setPadLight(pad, Color.YELLOW, BackBufferOperation.NONE);
            
            System.out.println("p"+pad.getX()+" "+pad.getY());
        }

        @Override
        public void onPadReleased(Pad pad, long timestamp) {
            client.setPadLight(pad, Color.BLACK, BackBufferOperation.NONE);
            System.out.println("p"+pad.getX()+" "+pad.getY());
        }

        @Override
        public void onButtonReleased(Button button, long timestamp) {
            client.setButtonLight(button, Color.BLACK, BackBufferOperation.NONE);
            switch (button) {
                case STOP:
                    stop.countDown();
                    break;
            }
        }
    }

}