/*
 * TODO put header
 */
package eu.lighthouselabs.obd.commands.fuel;

import ae.ac.masdar.labs.stevas.adama.Utilities;
import eu.lighthouselabs.obd.commands.ObdCommand;
import eu.lighthouselabs.obd.enums.FuelTrim;

/**
 * Get Fuel Trim.
 *
 */
public class FuelTrimObdCommand extends ObdCommand {

        private float fuelTrimValue = 0.0f;
        private final FuelTrim bank;

        /**
         * Default ctor.
         *
         * Will read the bank from parameters and construct the command accordingly.
         * Please, see FuelTrim enum for more details.
         */
        public FuelTrimObdCommand(FuelTrim bank) {
                super(bank.getObdCommand());
                this.bank = bank;
        }

        /**
         * @param value
         * @return
         */
        private float prepareTempValue(int value) {
                Double perc = (value - 128) * (100.0 / 128);
                return Float.parseFloat(perc.toString());
        }

        @Override
        public String getFormattedResult() {
                String res = getResult();

                if (!"NODATA".equals(res)) {
                        // ignore first two bytes [hh hh] of the response
                        fuelTrimValue = prepareTempValue(((int)Utilities.getByteList(buff).get(2)) & 0xFF);
                        res = String.format("%.2f%s", fuelTrimValue, "%");
                }

                return res;
        }

        /**
         * @return the readed Fuel Trim percentage value.
         */
        public final Float getValue() {
            String res = getResult();

            if (!"NODATA".equals(res)) {
                    // ignore first two bytes [hh hh] of the response
                    return prepareTempValue(((int)Utilities.getByteList(buff).get(2)) & 0xFF);
            }

            return null;
        }

        /**
         * @return the name of the bank in string representation.
         */
        public final String getBank() {
                return bank.getBank();
        }

        @Override
        public String getName() {
                return bank.getBank();
        }
}

