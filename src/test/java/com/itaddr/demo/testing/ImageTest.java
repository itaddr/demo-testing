package com.itaddr.demo.testing;

import com.idrsolutions.image.png.PngCompressor;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;
import net.coobird.thumbnailator.util.ThumbnailatorUtils;
import org.junit.Test;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * @Title null.java
 * @Package com.itaddr.demo.testing
 * @Author 马嘉祺
 * @Date 2021/6/29 13:40
 * @Description
 */
public class ImageTest {

    @Test
    public void metadata() throws IOException {

        String[] readFormats = ImageIO.getReaderFormatNames();
        String[] writeFormats = ImageIO.getWriterFormatNames();
        System.out.println("Readers: " + Arrays.asList(readFormats));
        System.out.println("Writers: " + Arrays.asList(writeFormats));
        StringJoiner joiner = new StringJoiner(" ");
        for (String formatName : ImageIO.getWriterFormatNames()) {
            if (ImageIO.getImageWritersByFormatName(formatName).next().getDefaultWriteParam().canWriteCompressed()) {
                joiner.add(formatName);
            }
        }
        System.out.println("Compressed: [" + joiner.toString() + "]");

        File file = new File("D:\\workspaces\\multimedia\\image-003.png");
//        File file = new File("D:\workspaces\multimedia\\image-006.webp");

        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(Files.readAllBytes(file.toPath())))) {
            ImageReader imageReader = ImageIO.getImageReaders(iis).next();
            System.out.println(imageReader.getClass().getName());
            System.out.println(imageReader.getFormatName());

            imageReader.setInput(iis, Boolean.TRUE);
            System.out.println(imageReader.getWidth(0));
            System.out.println(imageReader.getHeight(0));

        }

    }

    /**
     * 指定缩放比例，等比伸缩像素
     *
     * @throws IOException
     */
    @Test
    public void scaled1() throws IOException {
        File inputFile = new File("D:\\workspaces\\multimedia\\image-011.jpg");
        File outputFile = new File("D:\\workspaces\\multimedia\\image-011_scaled.jpg");

        BufferedImage image = ImageIO.read(new FileInputStream(inputFile));

        int type = image.getType();
        int width = image.getWidth();
        int height = image.getHeight();
        System.out.println(image.getType());
        System.out.println(width);
        System.out.println(height);

        float scaled = new BigDecimal(512).divide(new BigDecimal(width), 2, BigDecimal.ROUND_HALF_DOWN).floatValue();
        BufferedImage target = new BufferedImage(512, 512, type);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawRenderedImage(image, AffineTransform.getScaleInstance(scaled, scaled));
        graphics.dispose();

        ImageIO.write(target, "jpg", outputFile);


    }

    /**
     * 指定目标图片像素，等比伸缩像素
     *
     * @throws IOException
     */
    @Test
    public void scaled2() throws IOException {
        File inputFile = new File("D:\\workspaces\\multimedia\\image-011.jpg");
        File outputFile = new File("D:\\workspaces\\multimedia\\image-011_scaled.jpg");

        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(Files.readAllBytes(inputFile.toPath())))) {
            ImageReader iReader = ImageIO.getImageReaders(iis).next();
            System.out.println(iReader.getClass().getName());
            System.out.println(iReader.getFormatName());

            iReader.setInput(iis, Boolean.TRUE);
            System.out.println(iReader.getWidth(0) + "x" + iReader.getHeight(0));
            BufferedImage bImage = iReader.read(0);
            Image image = bImage.getScaledInstance(512, 512, Image.SCALE_DEFAULT);

            BufferedImage target = new BufferedImage(512, 512, bImage.getType());
            Graphics2D graphics = target.createGraphics();
            graphics.drawImage(image, 0, 0, null); // 绘制缩小后的图

            graphics.dispose();
            ImageIO.write(target, iReader.getFormatName(), outputFile); // 输出到文件流
        }

    }

    /**
     * 裁剪图片
     *
     * @throws IOException
     */
    @Test
    public void cutImage() throws IOException {
        File inputFile = new File("D:\\workspaces\\multimedia\\image-010.jpg");
        File outputFile = new File("D:\\workspaces\\multimedia\\image-011.jpg");

        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(Files.readAllBytes(inputFile.toPath())))) {
            ImageReader iReader = ImageIO.getImageReaders(iis).next();
            System.out.println(iReader.getClass().getName());
            System.out.println(iReader.getFormatName());

            iReader.setInput(iis, Boolean.TRUE);
            ImageReadParam readParam = iReader.getDefaultReadParam();
            readParam.setSourceRegion(new Rectangle(0, 0, 655, 655));
            BufferedImage bi = iReader.read(0, readParam);
            ImageIO.write(bi, iReader.getFormatName(), outputFile);
        }

    }

    /**
     * 指定抽样因子，抽样压缩像素
     *
     * @throws IOException
     */
    @Test
    public void sampling() throws IOException {
        File inputFile = new File("D:\\workspaces\\multimedia\\image-011.jpg");
        File outputFile = new File("D:\\workspaces\\multimedia\\image-011_scaled.jpg");

        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(Files.readAllBytes(inputFile.toPath())))) {
            ImageReader iReader = ImageIO.getImageReaders(iis).next();
            System.out.println(iReader.getClass().getName());
            System.out.println(iReader.getFormatName());

            iReader.setInput(iis, Boolean.TRUE);
            ImageReadParam readParam = iReader.getDefaultReadParam();
            // 读取每三个像素中的一个，产生一个原图片1/9大小的图片
            readParam.setSourceSubsampling(3, 3, 0, 0);
            BufferedImage bi = iReader.read(0, readParam);
            ImageIO.write(bi, iReader.getFormatName(), outputFile);
        }

    }

    @Test
    public void compressorPng() throws IOException {
        File inputFile = new File("D:\\workspaces\\multimedia\\image-003.png");
        File outfile = new File("D:\\workspaces\\multimedia\\image-003-output_compressor.png");
        PngCompressor.compress(inputFile, outfile);
    }

    @Test
    public void compressorJpeg() throws Exception {
        File file = new File("D:\\workspaces\\multimedia\\image-011.jpg");

        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(Files.readAllBytes(file.toPath())))) {

            ImageReader iReader = ImageIO.getImageReaders(iis).next();
            System.out.println(iReader.getClass().getName());
            System.out.println(iReader.getFormatName());
            iReader.setInput(iis, Boolean.TRUE);
            BufferedImage source = iReader.read(0);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageWriter iWriter = ImageIO.getImageWritersByFormatName(iReader.getFormatName()).next();
//                ImageWriter iWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                iWriter.setOutput(ImageIO.createImageOutputStream(baos));

                ImageWriteParam writeParam = iWriter.getDefaultWriteParam();
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(0.2f);
                writeParam.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
//                ColorModel colorModel = source.getColorModel();
//                writeParam.setDestinationType(new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(16, 16)));

                iWriter.write(source); // write(null, new IIOImage(source, null, null), writeParam);
                iWriter.dispose();

                Files.write(Paths.get("D:\\workspaces\\multimedia\\image-011-output_compressor.jpg"), baos.toByteArray());
            }
        }

    }


    @Test
    public void thumbnails() throws IOException {
        File inputFile = new File("D:\\workspaces\\multimedia\\image-011.jpg");
        File outputFile = new File("D:\\workspaces\\multimedia\\image-011-output_compressor.jpg");

        System.out.println(ThumbnailatorUtils.getSupportedOutputFormats());
        System.out.println(ThumbnailatorUtils.getSupportedOutputFormatTypes("png"));

        BufferedImage bImage = Thumbnails.of(inputFile).scale(1f).outputQuality(0.2f).useOriginalFormat().scalingMode(ScalingMode.BICUBIC).asBufferedImage();
        ImageIO.write(bImage, "jpg", outputFile);

    }

}
