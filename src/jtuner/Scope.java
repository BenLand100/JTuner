/**
 *  Copyright 2009 by Benjamin J. Land (a.k.a. BenLand100)
 *
 *  This file is part of JTuner.
 *
 *  JTuner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JTuner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JTuner. If not, see <http://www.gnu.org/licenses/>.
 */

package jtuner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JPanel;

/**
 * Implements a simple Oscilloscope in a JPanel for use in Swing. Contains 
 * methods to start and stop the display, as well as adjusting the virtual 
 * dimensions of the waveform displayed. Limited to 700x256 pixels.
 *
 * @author benland100
 */
public class Scope extends JPanel {

    private TargetDataLine line;
    private double frequency = 440D;
    private int cycles = 10;
    private double samplesTaken;
    private BufferedImage buf;
    private volatile boolean active;
    private volatile Object lock;
    private Thread thread;

    /**
     * Creates a new Scope instance using the default TargetDataLine from the 
     * Sound API. This method should suffice in most circumstances.
     */
    public Scope() {
        this(getDefaultTargetDataLine());
    }

    /**
     * Creates a new Scope instance using the specified TargetDataLine. Use this 
     * method if the default method will not work and the proper TargetDataLine
     * can be determined at runtime. 
     * 
     * @param input Source for the waveform data
     */
    public Scope(TargetDataLine input) {
        super();
        setPreferredSize(new Dimension(700, 256));
        line = input;
        try {
            line.open(getDefaultAudioFormat(), line.getBufferSize());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        line.start();
        frequency = 440D;
        cycles = 10;
        samplesTaken = line.getFormat().getSampleRate() / frequency * cycles;
        lock = new Object();
        buf = new BufferedImage(700, 256, BufferedImage.TYPE_INT_RGB);
        Graphics g = buf.getGraphics();
        g.clearRect(0, 0, 700, 256);
        g.dispose();
        active = false;
        thread = new ScopeReader();
    }

    /**
     * Creates an AudioFormat with generic specifications.
     * @return Default AudioFormat
     */
    private static AudioFormat getDefaultAudioFormat() {
        float sampleRate = 44100.0F;
        int sampleSizeInBits = 8;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    /**
     * Attempts to locate a sutible TargetDataLine from the Sound API. If none
     * can be found, the exception is printed and null is returned. 
     * @return Default TargetDataLine or null
     */
    private static TargetDataLine getDefaultTargetDataLine() {
        try {
            DataLine.Info linfo = new DataLine.Info(TargetDataLine.class, getDefaultAudioFormat());
            return (TargetDataLine) AudioSystem.getLine(linfo);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Safely starts the Thread responsible for reading from the TargetDataLine
     * and drawing onto the JPanel, but only if it is not already running. Does 
     * not return until the thread has started.
     */
    public void start() {
        if (!active) {
            synchronized (lock) {
                active = true;
                thread = new ScopeReader();
                thread.start();
            }
        }
    }

    /**
     * Safely stops the Thread responsible for reading from the TargetDataLine
     * and drawing onto the JPanel, but only if it is already running. Does not
     * return until the thread has stopped.
     */
    public void stop() {
        if (active) {
            active = false;
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the frequency in Hertz that is used to calculate the wavelength for the 
     * display. Resolution beyond 1 Hz is not perfectly accurate.
     * 
     * @param freq Frequency in Hertz
     */
    public void setFrequency(double freq) {
        boolean wasActive = active;
        stop();
        synchronized (lock) {
            frequency = freq;
            samplesTaken = line.getFormat().getSampleRate() / frequency * cycles;
        }
        if (wasActive) {
            start();
        }
    }

    /**
     * Returns the current frequency of the display.
     * 
     * @return Current frequency
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Sets the number of cycles at the set frequency to show in the display.
     * Too many or too few cycles will cause excess processor usage, and 
     * distort the display.
     * 
     * @param cycles Number of cycles to display
     */
    public void setCycles(int cycles) {
        System.out.println(cycles);
        boolean wasActive = active;
        active = false;
        stop();
        synchronized (lock) {
            System.out.println(cycles);
            this.cycles = cycles;
            samplesTaken = line.getFormat().getSampleRate() / frequency * cycles;
        }
        System.out.println(cycles);
        if (wasActive) {
            start();
        }
        System.out.println(cycles);
    }

    /**
     * Returns the current number of cycles in the display.
     * 
     * @return Current number of cycles
     */
    public int getCycles() {
        return cycles;
    }

    /**
     * Silently stops any running threads of this class when it is finalized.
     * 
     * @throws java.lang.Throwable
     */
    protected void finalize() throws Throwable {
        super.finalize();
        active = false;
    }

    /**
     * Draws the backbuffer of the scope on to the Graphics object
     * 
     * @param g Graphics to be drawn on
     */
    public void paint(Graphics g) {
        g.drawImage(buf, 0, 0, null);
    }

    /**
     * Implementation of Thread that reads from this Scope's TargetDataLine
     * and draws the resulting waveform data onto the back buffer, and that on 
     * the surface of JPanel. Starts and stops the TargetDataLine along with the
     * execution of the Thread. Continues to draw at the same scale as when it 
     * was started, so when settings change, it needs to be restarted.
     */
    private class ScopeReader extends Thread {

        /**
         * Reads from the TargetDataLine and draws the scaled data onto the 
         * backbuffer and the surface of the JPanel as long as the variable 
         * active remains true.
         */
        public void run() {
            synchronized (lock) {
                int samples = (int) Math.round(samplesTaken);
                byte[] sample = new byte[samples];
                AffineTransform scale = AffineTransform.getScaleInstance(700D / samplesTaken, 1);
                AffineTransform translate = AffineTransform.getTranslateInstance(0, 127);
                scale.concatenate(translate);
                BufferedImage buf = new BufferedImage(700, 256, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = (Graphics2D) buf.getGraphics();
                g.transform(scale);
                line.start();
                while (active) {
                    yield();
                    line.read(sample, 0, samples);
                    Graphics2D c = (Graphics2D) getGraphics();
                    if (c != null) {
                        g.setColor(Color.BLACK);
                        g.clearRect(0, -127, samples, 256);
                        g.setColor(Color.GREEN);
                        int next, last = sample[0];
                        for (int i = 1; i < samples; i++) {
                            next = sample[i];
                            g.drawLine(i, last, i, next);
                            last = next;
                        }
                        c.drawImage(buf, 0, 0, null);
                    }
                }
                line.stop();
                line.flush();
            }
        }
    }
}
