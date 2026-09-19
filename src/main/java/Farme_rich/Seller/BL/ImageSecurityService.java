package Farme_rich.Seller.BL;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.IIOImage;
import javax.imageio.stream.ImageOutputStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

@Service
public class ImageSecurityService {

    private static final int MAX_WIDTH = 8000;
    private static final int MAX_HEIGHT = 8000;

    public byte[] scanAndCompress(byte[] imageBytes, String extension) throws Exception {

        if (!isValidImageMagicBytes(imageBytes)) {
            throw new SecurityException("Invalid image magic bytes");
        }

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new SecurityException("Invalid image content");
        }

        if (image.getWidth() > MAX_WIDTH || image.getHeight() > MAX_HEIGHT) {
            throw new SecurityException("Image dimensions too large");
        }

        // Re-encode + compress
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

//        if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg")) {
//
//            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
//            ImageWriter writer = writers.next();
//
//            ImageWriteParam param = writer.getDefaultWriteParam();
//            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//            param.setCompressionQuality(0.7f); // 70% quality compression
//
//            writer.setOutput(ImageIO.createImageOutputStream(outputStream));
//            writer.write(null, new IIOImage(image, null, null), param);
//            writer.dispose();
//
//        } else {
//            // PNG re-encode (removes metadata)
//            ImageIO.write(image, "png", outputStream);
//        }

        if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg")) {

            // ✅ Convert to RGB if image has alpha (fix for "Bogus input colorspace")
            BufferedImage rgbImage = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            rgbImage.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.7f);

            ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(ios);

            writer.write(null, new IIOImage(rgbImage, null, null), param);

            ios.close();
            writer.dispose();

        } else {
            // PNG re-encode (keeps transparency)
            ImageIO.write(image, "png", outputStream);
        }

        return outputStream.toByteArray();
    }

    private boolean isValidImageMagicBytes(byte[] bytes) {

        if (bytes.length < 4) return false;

        // JPEG
        if ((bytes[0] & 0xFF) == 0xFF &&
                (bytes[1] & 0xFF) == 0xD8 &&
                (bytes[2] & 0xFF) == 0xFF) return true;

        // PNG
        if ((bytes[0] & 0xFF) == 0x89 &&
                bytes[1] == 0x50 &&
                bytes[2] == 0x4E &&
                bytes[3] == 0x47) return true;

        return false;
    }
}
