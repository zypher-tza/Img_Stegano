package org.tza.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class ImageUtil {

    public static byte[] embedMessage(byte[] originalImageBytes, String secretMessage) throws IOException {
        BufferedImage originalImage = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(originalImageBytes)) {
            originalImage = ImageIO.read(bis);
            if (originalImage == null) {
                System.err.println("ImageUtil ERROR: Could not read original image for embedding. Invalid image format?");
                throw new IOException("Could not read original image from bytes. Invalid image format?");
            }
        }

        String messageWithTerminator = secretMessage + '\0';
        byte[] messageBytes = messageWithTerminator.getBytes("UTF-8");
        int bitsToEmbed = messageBytes.length * 8;
        int pixelsNeeded = (int) Math.ceil((double) bitsToEmbed / 3);

        if (pixelsNeeded > (originalImage.getWidth() * originalImage.getHeight())) {
            throw new IllegalArgumentException(
                "Message too long to embed in this image. " +
                "Image capacity: " + (originalImage.getWidth() * originalImage.getHeight() * 3 / 8) + " bytes. " +
                "Message length (including terminator): " + messageBytes.length + " bytes."
            );
        }

        BufferedImage embeddedImage = new BufferedImage(
            originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        int bitIndex = 0; 
        for (int y = 0; y < originalImage.getHeight(); y++) {
            for (int x = 0; x < originalImage.getWidth(); x++) {
                int originalPixel = originalImage.getRGB(x, y);

               
                int alpha = (originalPixel >> 24) & 0xFF;
                int red = (originalPixel >> 16) & 0xFF;
                int green = (originalPixel >> 8) & 0xFF;
                int blue = originalPixel & 0xFF;

               for (int i = 0; i < 3; i++) {
                    if (bitIndex < bitsToEmbed) {
                        int byteIdx = bitIndex / 8; 
                        int bitPosInByte = bitIndex % 8;
                        int bitToEmbed = (messageBytes[byteIdx] >> (7 - bitPosInByte)) & 0x01;

                        // Modify the LSB of the current color channel
                        if (i == 0) {
                            red = (red & 0xFE) | bitToEmbed;
                        } else if (i == 1) { 
                            green = (green & 0xFE) | bitToEmbed;
                        } else { // Blue channel
                            blue = (blue & 0xFE) | bitToEmbed;
                        }
                        bitIndex++;
                    } else {
                        break;
                    }
                }
                
                
                int newPixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                embeddedImage.setRGB(x, y, newPixel);
                
                if (bitIndex >= bitsToEmbed) {
                    for (int remY = y; remY < originalImage.getHeight(); remY++) {
                        for (int remX = (remY == y ? x + 1 : 0); remX < originalImage.getWidth(); remX++) {
                            embeddedImage.setRGB(remX, remY, originalImage.getRGB(remX, remY));
                        }
                    }
                    y = originalImage.getHeight(); 
                    x = originalImage.getWidth(); 
                    break;
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(embeddedImage, "png", baos);
        System.out.println("DEBUG (ImageUtil): Message embedded. Total bits embedded: " + bitIndex + " (including terminator).");
        return baos.toByteArray();
    }

    public static String extractMessage(byte[] imageBytes) throws IOException {
        BufferedImage image = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
            image = ImageIO.read(bis);
            if (image == null) {
                System.err.println("ImageUtil ERROR: ImageIO.read returned null. Invalid image format?");
                return "Error: Could not read image. Invalid format?"; 
            }
        } catch (IOException e) {
            System.err.println("ImageUtil ERROR: IOException reading image: " + e.getMessage());
            e.printStackTrace();
            return "Error: Image read failed due to I/O issue.";
        }

        StringBuilder extractedBits = new StringBuilder();
        int width = image.getWidth();
        int height = image.getHeight();
        boolean messageTerminatorFound = false;

       
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                extractedBits.append((pixel >> 16) & 0x01); 
                extractedBits.append((pixel >> 8) & 0x01); 
                extractedBits.append(pixel & 0x01); 

                if (extractedBits.length() >= 8 && (extractedBits.length() % 8 == 0)) {
                    String currentByteBits = extractedBits.substring(extractedBits.length() - 8);
                    try {
                        byte b = (byte) Integer.parseInt(currentByteBits, 2);
                        if (b == 0x00) { 
                            messageTerminatorFound = true;
                            extractedBits.setLength(extractedBits.length() - 8);
                            break; 
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("ImageUtil ERROR: NumberFormatException parsing bits to byte: " + currentByteBits);
                        e.printStackTrace();
                        return "Error: Data corruption detected during bit processing.";
                    }
                }
            }
            if (messageTerminatorFound) {
                break;
            }
        }

        System.out.println("DEBUG (ImageUtil): Total bits extracted before decoding: " + extractedBits.length());
        System.out.println("DEBUG (ImageUtil): Raw extracted bits: " + extractedBits.toString());

        if (!messageTerminatorFound && extractedBits.length() > 0) {
            System.out.println("ImageUtil WARNING: Message terminator not found. Decoding all available bits.");
          
        } else if (!messageTerminatorFound && extractedBits.length() == 0) {
             return "No hidden message found in the image.";
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < extractedBits.length(); i += 8) {
            // Ensure we only process full bytes
            if (i + 8 <= extractedBits.length()) {
                String byteString = extractedBits.substring(i, i + 8);
                try {
                    byte b = (byte) Integer.parseInt(byteString, 2);
                    baos.write(b);
                } catch (NumberFormatException e) {
                    System.err.println("ImageUtil ERROR: NumberFormatException converting byte string '" + byteString + "' to byte. Skipping this byte.");
                    e.printStackTrace();
                    
                }
            }
        }

        byte[] extractedBytes = baos.toByteArray();
        System.out.println("DEBUG (ImageUtil): Final Extracted Bytes: " + Arrays.toString(extractedBytes));

        String extractedText = new String(extractedBytes, "UTF-8");
        System.out.println("DEBUG (ImageUtil): Final Extracted Text: '" + extractedText + "'");

        return extractedText;
    }
}