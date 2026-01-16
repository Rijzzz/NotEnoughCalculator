package com.rijz.notenoughcalculator;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Main mod entry point
// Most of the actual work happens on the client side
// Created by Rijz & Laze
public class NotEnoughCalculator implements ModInitializer {

    public static final String MOD_ID = "notenoughcalculator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Not Enough Calculator loading...");
        // All the real initialization happens in NotEnoughCalculatorClient
        // since this is a client-side only mod
    }
}