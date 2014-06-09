/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.temperature;

import ae.ac.masdar.labs.stevas.adama.Utilities;

/**
 * Ambient Air Temperature.
 */
public class AmbientAirTemperatureObdCommand extends TemperatureObdCommand {

        /**
         * @param cmd
         */
        public AmbientAirTemperatureObdCommand() {
                super("01 46");
        }

        /**
         * @param other
         */
        public AmbientAirTemperatureObdCommand(TemperatureObdCommand other) {
                super(other);
        }

        @Override
    public String getName() {
                return "Ambient Air Temperature";
    }
        public Integer getValue() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    try {
                    	return (((int)Utilities.getByteList(buff).get(2)) & 0xFF)-40;
                    } catch (Exception e) {
                            /*
                             * TODO this must be revised.
                             */
                            return null;
                    }
            }
            return null;
        }

}
