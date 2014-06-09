/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.pressure;

import ae.ac.masdar.labs.stevas.adama.Utilities;


/**
 * Intake Manifold Pressure
 */
public class IntakeManifoldPressureObdCommand extends PressureObdCommand {

        /**
         * Default ctor.
         */
        public IntakeManifoldPressureObdCommand() {
                super("01 0B");
        }

        /**
         * Copy ctor.
         *
         * @param other
         */
        public IntakeManifoldPressureObdCommand(
                        IntakeManifoldPressureObdCommand other) {
                super(other);
        }

        @Override
        public String getName() {
                return "Intake Manifold Pressure";
        }
        
        public Integer getValue() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    try {
                    	return (((int)Utilities.getByteList(buff).get(2)) & 0xFF);
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
