package za.co.icurity.usermanagement.util;

import org.springframework.stereotype.Component;

@Component
public class GenerateSSAID {

	/**
	 * @return ssaid
	 */
	public static String getSSAID() {

		String ssaid = "";
		String pchar = "123456789";
		int pLen = pchar.length();

		for (int i = 1; i <= 8; ++i) {
			int index = (int) (Math.random() * pLen);
			ssaid = ssaid + pchar.substring(index, 1 + index);
		}

		return ssaid;
	}

}
