package com.stegovault.service;

import java.awt.image.BufferedImage;

public interface ValidationService {
    boolean validateCapacity ( BufferedImage image, int payloadSizeBytes);
}
