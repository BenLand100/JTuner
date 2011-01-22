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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Uses the Scope class as a display and provides easy adjustment of frequency 
 * and cycles whithin a JFrame. Includes a simple set of instructions as well.
 * 
 * @author benland100
 */
public class Tuner extends JFrame implements WindowListener, ChangeListener, ActionListener {

    public static final String instructions = 
            "<html>Set the frequency of the pitch and the number of cycles you want visible (ten should be fine).<br>" +
            "Too few or too many cycles will cause the display to be distorted and require too much processing power<br>" +
            "The display is adjusted to show the specified number of cycles of the given frequency.<br>" +
            "Sound data is displayed as a waveform in the black area, allowing you to see the individual cycles<br>" +
            "The note will be in tune when the cycles do not appear to precess or recess across the screen.<br>" +
            "A well tuned note will not appear to move because its wavelength is a multiple of the display length.<br>" +
            "Precession (moving to the right) indicates the note is flat, Recession (moving to the left) indicates sharp.<br>" +
            "It's worth noting that this program is nothing more than a simple continual sweep/trace oscilloscope.<br>" +
            "If you see a flat line, ensure your microphone is connected and sound is working properly in Java.</html>";

    private Scope scope;
    private SpinnerNumberModel cyclesModel;
    private JSpinner cycles;
    private SpinnerNumberModel freqModel;
    private JSpinner freq;
    private JButton help;

    /**
     * Creates a new Tuner instance. Does not show the frame, must be done by
     * the caller. 
     */
    public Tuner() {
        super("JTuner - Benjamin Land");
        scope = new Scope();
        freqModel = new SpinnerNumberModel(440, 1, 20000, 1);
        freqModel.addChangeListener(this);
        freq = new JSpinner(freqModel);
        cyclesModel = new SpinnerNumberModel(10, 1, 500, 1);
        cyclesModel.addChangeListener(this);
        cycles = new JSpinner(cyclesModel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        setLayout(new BorderLayout());
        add(scope, BorderLayout.CENTER);
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(1, 5));
        top.add(new JLabel("Frequency ( Hz ): ",JLabel.CENTER));
        top.add(freq);
        top.add(new JLabel("Cycles ( \u03BB Visible ): ",JLabel.CENTER));
        top.add(cycles);
        help = new JButton("Instructions");
        help.addActionListener(this);
        top.add(help);
        add(top,BorderLayout.NORTH);
        pack();
    }

    public void windowOpened(WindowEvent e) {
        scope.start();
    }

    public void windowClosing(WindowEvent e) {
        scope.stop();
        setVisible(false);
        dispose();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == cyclesModel) {
            scope.setCycles(((SpinnerNumberModel)cycles.getModel()).getNumber().intValue());
        }
        if (e.getSource() == freqModel) {
            scope.setFrequency(((SpinnerNumberModel)freq.getModel()).getNumber().intValue());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == help) {
            JOptionPane.showMessageDialog(this, instructions, "Instructions", JOptionPane.INFORMATION_MESSAGE); 
        }
    }
}
