/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.pressure;


/**
 * Barometric pressure.
 */
public class BarometricPressureObdCommand extends PressureObdCommand {

        /**
         * @param cmd
         */
        public BarometricPressureObdCommand() {
                super("01 33");
        }

        /**
         * @param other
         */
        public BarometricPressureObdCommand(PressureObdCommand other) {
                super(other);
                // TODO Auto-generated constructor stub
        }

        /* (non-Javadoc)
         * @see eu.lighthouselabs.obd.commands.ObdCommand#getName()
         */
        @Override
        public String getName() {
                return "Barometric Pressure";
        }

}
