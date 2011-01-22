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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Main class for the JTuner program.
 * 
 * @author benland100
 */
public class Main {

    /**
     * Creates a new Tuner instance and shows it.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Tuner t = new Tuner();
        t.setVisible(true);
    }

}
