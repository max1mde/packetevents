/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.util.crypto;

import com.github.retrooper.packetevents.util.AdventureSerializer;
import net.kyori.adventure.text.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MessageVerifier {
    private static byte[] byteArray(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
        return result;
    }

    public static boolean verify(UUID uuid, MessageSignData signData, PublicKey publicKey, Component component)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return verify(uuid, signData, publicKey, AdventureSerializer.toJson(component));
    }

    public static boolean verify(UUID uuid, MessageSignData signData, PublicKey publicKey, String jsonMessage)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        //Create signature
        Signature signature = Signature.getInstance("SHA256withRSA");
        //Initialize it with public key
        signature.initVerify(publicKey);
        //Adding data to be verified (Salt, UUID, Timestamp, Message)
        byte[] data = new byte[32];
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        //Verify salt
        buffer.putLong(signData.getSaltSignature().getSalt());

        //Verify UUID
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        //Verify timestamp
        long timeMillis = System.currentTimeMillis();
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        System.out.println("timestamp: " + signData.getTimestamp() + ", time seconds: " + timeSeconds);
        //TODO Look into why it cant verify? Instant.now().getEpochSecond()
        buffer.putLong(timeSeconds);

        //Update this stuff
        signature.update(data);

        //Verify message content
        signature.update(jsonMessage.getBytes(StandardCharsets.UTF_8));

        //Verifying the signature
        return signature.verify(signData.getSaltSignature().getSignature());
    }
}