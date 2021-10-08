package com.itaddr.demo.testing;

import com.itaddr.common.tools.utils.ByteUtil;
import com.itaddr.common.tools.utils.CodecUtil;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 详细讲解：http://www.yanzuoguang.com/article/708
 *
 * @Title null.java
 * @Package com.itaddr.demo.testing
 * @Author 马嘉祺
 * @Date 2021/7/1 19:53
 * @Description
 */
public class FFMpegTest {

    @Test
    public void test01() throws IOException {
        File file = new File("D:\\workspaces\\javaspaces\\ads-ui\\public\\upload\\operate\\material\\k-fS-gVQ8UGII0FgE8m96tfagzI=.png");

//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        OpenCVFrameConverter.ToMat openCVConverter = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter java2DConverter = new Java2DFrameConverter();
        org.bytedeco.opencv.opencv_core.Mat mat = openCVConverter.convert(java2DConverter.convert(ImageIO.read(file)));
        org.bytedeco.opencv.opencv_core.Mat clone = mat.clone();

    }

    @Test
    public void test02() throws FrameGrabber.Exception {
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber("D:\\video.mp4");
        frameGrabber.start();

        System.out.println(frameGrabber.getFormat());
        System.out.println(frameGrabber.getVideoBitrate());

        String rotateOld = frameGrabber.getVideoMetadata("rotate");
        System.out.println(rotateOld);

//        FFmpegFrameRecorder frameRecorder = new FFmpegFrameRecorder();


//        frameGrabber.close();
        frameGrabber.stop();
        frameGrabber.release();
    }

    @Test
    public void test03() throws FrameGrabber.Exception, FFmpegFrameRecorder.Exception {
        String filePath = "D:\\workspaces\\multimedia\\video1.mp4";
        String ext = filePath.substring(filePath.lastIndexOf("."));
        String newFilePath = filePath.replace(ext, "_recode.mp4");

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filePath);
        grabber.start();

//        System.out.println("AudioChannels: " + grabber.getAudioChannels());
//        System.out.println("PixelFormat: " + grabber.getPixelFormat());
//        System.out.println("Format: " + grabber.getFormat());
        System.out.printf("imageWidth=%d, imageHeight=%d | audioChannels=%d, sampleRate=%d, sampleFormat=%d\n",
                grabber.getImageWidth(), grabber.getImageHeight(),
                grabber.getAudioChannels(), grabber.getSampleRate(), grabber.getSampleFormat());

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(newFilePath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
        recorder.setFrameNumber(grabber.getFrameNumber());
        recorder.setTimestamp(grabber.getTimestamp());
        recorder.setAspectRatio(grabber.getAspectRatio());
        recorder.setAudioBitrate(grabber.getAudioBitrate());
        recorder.setAudioCodec(grabber.getAudioCodec());
        recorder.setAudioQuality(1f);
        recorder.setFormat(grabber.getFormat());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoQuality(1f);
        recorder.setSampleRate(grabber.getSampleRate());

//        recorder.setSampleFormat(grabber.getSampleFormat());
//        recorder.setPixelFormat(grabber.getPixelFormat());

        recorder.start();

        Frame frame;
//        Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
        while (null != (frame = grabber.grabImage())) {
            System.out.printf("streamIndex=%d, timestamp=%d, keyFrame=%b | imageChannels=%d, imageWidth=%d, imageHeight=%d, imageDepth=%d, imageStride=%d | audioChannels=%d, sampleRate=%d\n",
                    frame.streamIndex, frame.timestamp, frame.keyFrame,
                    frame.imageChannels, frame.imageWidth, frame.imageHeight, frame.imageDepth, frame.imageStride,
                    frame.audioChannels, frame.sampleRate);


            recorder.record(frame);

        }

        /*AVPacket packet;
        long dts = 0;
        while ((packet = grabber.grabPacket()) != null) {


            recorder.recordPacket(packet);

            long currentDts = packet.dts();
            if (currentDts >= dts) {
                recorder.recordPacket(packet);
            }
            dts = currentDts;
        }*/

        recorder.stop();
        recorder.release();
        grabber.stop();
        grabber.release();
    }

    @Test
    public void watermark() throws IOException {
        String inputPath = "D:\\workspaces\\multimedia\\video-001.mp4";
        String outputPath = "D:\\workspaces\\multimedia\\video-001-output_watermark.mp4";
        String watermark = "www.dongyou.com";

        System.out.println(Files.size(Paths.get(inputPath)));
        System.out.println(ByteUtil.toLowerHexString(CodecUtil.md5(Files.readAllBytes(Paths.get(inputPath)))));

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
        grabber.start();
        System.out.println("PixelFormat: " + grabber.getPixelFormat());
        System.out.println("Format: " + grabber.getFormat());

        System.out.println("width: " + grabber.getImageWidth());
        System.out.println("height: " + grabber.getImageHeight());
        System.out.println("AudioChannels: " + grabber.getAudioChannels());

        System.out.println(grabber.getFrameNumber());
        System.out.println(grabber.getTimestamp());
        System.out.println(grabber.getAspectRatio());
        System.out.println(grabber.getAudioBitrate());
        System.out.println(grabber.getAudioCodec() + grabber.getAudioCodecName());
        System.out.println(grabber.getFormat());
        System.out.println(grabber.getFrameRate());
        System.out.println(grabber.getVideoBitrate());
        System.out.println(grabber.getVideoCodec() + grabber.getVideoCodecName());
        System.out.println(grabber.getSampleRate());

        System.out.println(grabber.getAudioFrameRate());
        System.out.println(grabber.getLengthInTime());
        System.out.println(grabber.getLengthInVideoFrames());
        System.out.println(grabber.getLengthInAudioFrames());
        System.out.println(grabber.getGamma());
        System.out.println(grabber.getSampleMode());
        System.out.println(grabber.getSampleFormat());
        System.out.println(grabber.getPixelFormat());

//        System.out.printf(" | imageWidth=%d, imageHeight=%d | audioChannels=%d, sampleRate=%d, sampleFormat=%d\n",
//                grabber.getImageWidth(), grabber.getImageHeight(),
//                grabber.getAudioChannels(), grabber.getSampleRate(), grabber.getSampleFormat());

        FFmpegFrameRecorder recorder = createFrameRecorder(grabber, outputPath);
        recorder.start();

        // 记录音频
        Frame frame;
        while ((frame = grabber.grabSamples()) != null) {
            recorder.record(frame);
            frame.close();
        }

        grabber.restart();
        // 遍历并编辑每一帧图片
        Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
        while (null != (frame = grabber.grabImage())) {
            BufferedImage bImage = java2dConverter.getBufferedImage(frame, 1.0, Boolean.FALSE, null);
            Graphics2D graphics = bImage.createGraphics();
            graphics.drawString(watermark, 30, 30);
            graphics.dispose();
            Frame nFrame = java2dConverter.getFrame(bImage, 1.0, Boolean.FALSE);

            nFrame.streamIndex = frame.streamIndex;
            nFrame.timestamp = frame.timestamp;
            nFrame.keyFrame = frame.keyFrame;
            nFrame.imageWidth = frame.imageWidth;
            nFrame.imageHeight = frame.imageHeight;
            nFrame.imageDepth = frame.imageDepth;
            nFrame.imageStride = frame.imageStride;
            nFrame.opaque = frame.opaque;

            recorder.record(nFrame);

            nFrame.close();
            frame.close();
        }

        recorder.close();
        grabber.close();

    }

    @Test
    public void rotate() throws IOException {
        String inputPath = "D:\\workspaces\\multimedia\\video-001.mp4";
        String outputPath = "D:\\workspaces\\multimedia\\video-001-output_rotate.mp4";

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
        grabber.start();

        FFmpegFrameRecorder recorder = createFrameRecorder(grabber, outputPath);
        recorder.start();

        // 记录音频
        Frame frame;
        while ((frame = grabber.grabSamples()) != null) {
            recorder.record(frame);
            frame.close();
        }

        grabber.restart();
        // 遍历并编辑每一帧图片
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        Java2DFrameConverter java2dConverter = new Java2DFrameConverter();

        int width = grabber.getImageWidth();
        int height = grabber.getImageHeight();
//        width = new BigDecimal(width * width).divide(new BigDecimal(height), 2, BigDecimal.ROUND_DOWN).intValue();
//        height = grabber.getImageWidth();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (null != (frame = grabber.grabImage())) {

            /*IplImage iplImage = converter.convertToIplImage(frame);
            IplImage iplImage1 = IplImage.create(iplImage.width() / 2, iplImage.height() / 2, iplImage.depth(), iplImage.nChannels());
            iplImage1.imageData(iplImage.imageData());*/

            Mat source = converter.convert(frame);
            Mat target = new Mat(source.cols() / 2, source.rows() / 2, source.type());
            target.data(source.data());

            /*UByteIndexer sourceIndexer = source.createIndexer();
            UByteIndexer targetIndexer = target.createIndexer();
            for (int row = 0; row < source.rows(); ++row) {
                for (int col = 0; col < source.cols(); ++col) {
                    for (int channel = 0; channel < source.channels(); ++channel) {
                        targetIndexer.put(row, col, channel, sourceIndexer.get(row, col, channel));
                    }
                }
            }*/

            /*Mat dst = image.clone();
            org.opencv.core.Point center = new org.opencv.core.Point(dst.width() / 2.0, dst.height() / 2.0);
            Mat affineTrans = Imgproc.getRotationMatrix2D(center, 90, 1.0);
            Imgproc.warpAffine(image, dst, affineTrans, dst.size(), Imgproc.INTER_NEAREST);*/

//            Rect rect = new Rect().x(0).y(0).width(width / 2).height(height / 2);
//            Mat apply = image.apply(rect);

            /*UByteIndexer indexer = image.createIndexer();
            //图像通道
            int nbChannels = image.channels();
            //加盐数量
            for (int i = 0; i < 2000; i++) {
                int row = random.nextInt(image.rows());
                int col = random.nextInt(image.cols());
                // 处理全部通道数据，均进行加盐，设置为最大值255
                for (int channel = 0; channel < nbChannels; channel++) {
                    indexer.put(row, col, channel, 255);
                }
            }*/

            Frame nFrame = converter.convert(target);

//            BufferedImage source = java2dConverter.getBufferedImage(frame);

//            BufferedImage target = Thumbnails.of(source).scale(0.5f).asBufferedImage();

//            Image image = source.getScaledInstance(width / 2, height / 2, Image.SCALE_DEFAULT);
//            BufferedImage target = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());

//            Graphics2D graphics = target.createGraphics();
//            graphics.drawImage(source, 0, 0, null);
//            graphics.rotate(90f);
//            graphics.dispose();
//            Frame nFrame = java2dConverter.getFrame(target);

            nFrame.streamIndex = frame.streamIndex;
            nFrame.timestamp = frame.timestamp;
            nFrame.keyFrame = frame.keyFrame;
            nFrame.imageWidth = frame.imageWidth;
            nFrame.imageHeight = frame.imageHeight;
            nFrame.imageDepth = frame.imageDepth;
            nFrame.imageStride = frame.imageStride;
            nFrame.opaque = frame.opaque;

            recorder.record(nFrame);

//            targetIndexer.release();
            target.release();
            nFrame.close();
//            sourceIndexer.release();
            source.release();
            frame.close();
        }

        recorder.close();
        grabber.close();
    }

    private FFmpegFrameRecorder createFrameRecorder(FFmpegFrameGrabber grabber, String filePath) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(filePath, grabber.getImageWidth() / 2, grabber.getImageHeight() / 2, grabber.getAudioChannels());
        recorder.setFrameNumber(grabber.getFrameNumber());
        recorder.setTimestamp(grabber.getTimestamp());
        recorder.setAspectRatio(grabber.getAspectRatio());
        recorder.setAudioBitrate(grabber.getAudioBitrate());
        recorder.setAudioCodec(grabber.getAudioCodec());
        recorder.setAudioQuality(1f);
        recorder.setFormat(grabber.getFormat());
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setVideoQuality(1f);
        recorder.setSampleRate(grabber.getSampleRate());
        return recorder;
    }

}
