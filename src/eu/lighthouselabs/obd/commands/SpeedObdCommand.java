/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands;

import ae.ac.masdar.labs.stevas.adama.Utilities;

/**
 * TODO put description
 *
 * Current speed.
 */
public class SpeedObdCommand extends ObdCommand implements SystemOfUnits {

        private int metricSpeed = -1;

        /**
         * Default ctor.
         */
        public SpeedObdCommand() {
                super("01 0D");
        }

        /**
         * Copy ctor.
         *
         * @param other
         */
        public SpeedObdCommand(SpeedObdCommand other) {
                super(other);
        }

        /**
         * Convert from km/h to mph
         */
        public float getImperialUnit(float value) {
                Double tempValue = value * 0.621371192;
                return Float.valueOf(tempValue.toString());
        }

        /**
         *
         */
        public String getFormattedResult() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    // ignore first two bytes [hh hh] of the response
                    metricSpeed = Utilities.getByteList(buff).get(2) & 0xFF; // unsigned short
                    res = String.format("%d%s", metricSpeed, "km/h");

                    if (useImperialUnits)
                            res = String.format("%.2f%s", getImperialUnit(metricSpeed),
                                            "mph");
            }

            return res;
    }

        public Integer getValue() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    // ignore first two bytes [hh hh] of the response
                    return Utilities.getByteList(buff).get(2) & 0xFF; // unsigned short
            }

            return null;
    }

        /**
         * @return the speed in metric units.
         */
        public int getMetricSpeed() {
                return metricSpeed;
        }

        /**
         * @return the speed in imperial units.
         */
        public float getImperialSpeed() {
                return getImperialUnit(metricSpeed);
        }

        @Override
        public String getName() {
                return "Vehicle Speed";
        }

}
