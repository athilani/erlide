package org.erlide.wrangler.refactoring.ui.validator;

/**
 * Validates a string which is a double values and is between 0.1 and 1.0
 * 
 * @author Gyorgy Orosz
 * @version %I%, %G%
 */
public class NormalDoulbeValidator implements IValidator {

	public boolean isValid(String text) {
		try {
			Double val = Double.parseDouble(text);
			return (val <= 1) && (val >= 0.1);
		} catch (Exception e) {
			return false;
		}
	}
}
