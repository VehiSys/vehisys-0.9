/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.temperature;

import ae.ac.masdar.labs.stevas.adama.Utilities;

/**
 * TODO
 *
 * put description
 */
public class AirIntakeTemperatureObdCommand extends TemperatureObdCommand {

        public AirIntakeTemperatureObdCommand() {
                super("01 0F");
        }

        public AirIntakeTemperatureObdCommand(AirIntakeTemperatureObdCommand other) {
                super(other);
        }

        @Override
        public String getName() {
                return "Air Intake Temperature";
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