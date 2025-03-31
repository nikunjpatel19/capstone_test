package tech.zodiac.px_um.service;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha256HashingService implements HashingService {
    @Override
    public String hash(String input) {
        return DigestUtils.sha256Hex(input);
    }
}
