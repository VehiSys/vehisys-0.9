/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.temperature;

import ae.ac.masdar.labs.stevas.adama.Utilities;
import eu.lighthouselabs.obd.commands.ObdCommand;
import eu.lighthouselabs.obd.commands.SystemOfUnits;

/**
 * TODO
 *
 * put description
 */
public abstract class TemperatureObdCommand extends ObdCommand implements SystemOfUnits {

        /**
         * Default ctor.
         *
         * @param cmd
         */
        public TemperatureObdCommand(String cmd) {
                super(cmd);
        }

        /**
         * Copy ctor.
         *
         * @param other
         */
        public TemperatureObdCommand(TemperatureObdCommand other) {
                super(other);
        }

        /**
         * TODO
         *
         * put description of why we subtract 40
         *
         * @param temp
         * @return
         */
        protected final float prepareTempValue(float temp) {
                return temp - 40;
        }

        /**
         * Get values from 'buff', since we can't rely on char/string for calculations.
         *
         * @return Temperature in Celsius or Fahrenheit.
         */
        @Override
        public String getFormattedResult() {
                String res = getResult();
                float value = 0f;

                if (!"NODATA".equals(res)) {
                        // ignore first two bytes [hh hh] of the response
                        value = prepareTempValue((((int)Utilities.getByteList(buff).get(2)) & 0xFF));
                       
                        // convert?
                        if (useImperialUnits)
                                res = String.format("%.1f%s", getImperialUnit(value), "F");
                        else
                                res = String.format("%.0f%s", value, "C");
                }

                return res;
        }

        /**
         * Converts from Celsius to Fahrenheit.
         */
        public float getImperialUnit(float value) {
                return value * 1.8f + 32;
        }
       
        /**
         * @return the OBD command name.
         */
        public abstract String getName();
       
}
