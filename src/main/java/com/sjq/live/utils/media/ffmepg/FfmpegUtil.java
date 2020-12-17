package com.sjq.live.utils.media.ffmepg;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class FfmpegUtil {

    private static String ffmpegPath;

    static {
        try {
            File sourceFile = ResourceUtils.getFile("classpath:ffmpeg/ffmpeg.exe");
            ffmpegPath = sourceFile.getParent();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Boolean ffmpeg(final String ffmpegPath,
                                 final String inputPath,
                                 final String outputPath) throws FFmpegException {
        if (!checkFile(inputPath)) {
            throw new FFmpegException("文件格式不合法");
        }
        List<String> command = getFfmpegCommand(ffmpegPath, inputPath, outputPath);
        if (CollectionUtils.isEmpty(command)) {
            return false;
        }
        return process(command);
    }

    public static Process convertStream(final String srcUrl,
                                        final String dstUrl) throws FFmpegException {
        if (StringUtils.isEmpty(srcUrl) || StringUtils.isEmpty(dstUrl)) {
            throw new FFmpegException("srcUrl,dstUrl不能为空");
        }
        List<String> command = getConvertToFlvStreamCommand(ffmpegPath, srcUrl, dstUrl);
        if (CollectionUtils.isEmpty(command)) {
            throw new FFmpegException("convertStream开启ffmpeg进程失败");
        }
        return processWithHandler(command);
    }

    private static List<String> getConvertToFlvStreamCommand(final String ffmpegPath,
                                                             final String srcUrl,
                                                             final String dstUrl) {
        List<String> command = Lists.newArrayList();
        command.add(ffmpegPath + "\\ffmpeg");

        command.add("-i");
        command.add(srcUrl);

        command.add("-tune");
        command.add("zerolatency");

        command.add("-threads");
        command.add("4");

        command.add("-c:v");
        command.add("libx264");

        command.add("-acodec");
        command.add("aac");

        command.add("-crf");
        command.add("28");

        command.add("-preset");
        command.add("ultrafast");

        command.add("-f");
        command.add("flv");
        command.add(dstUrl);

        return command;
    }

    private static int checkContentType(final String inputPath) {
        String type = inputPath.substring(inputPath.lastIndexOf(".") + 1).toLowerCase();
        //ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
        if (StringUtils.equals("avi", type)) {
            return 1;
        } else if (StringUtils.equals("mpg", type)) {
            return 1;
        } else if (StringUtils.equals("wmv", type)) {
            return 1;
        } else if (StringUtils.equals("3gp", type)) {
            return 1;
        } else if (StringUtils.equals("mov", type)) {
            return 1;
        } else if (StringUtils.equals("mp4", type)) {
            return 1;
        } else if (StringUtils.equals("mkv", type)) {
            return 1;
        } else if (StringUtils.equals("asf", type)) {
            return 0;
        } else if (StringUtils.equals("flv", type)) {
            return 0;
        } else if (StringUtils.equals("rm", type)) {
            return 0;
        } else if (StringUtils.equals("rmvb", type)) {
            return 1;
        } else if (StringUtils.equals("webm", type)) {
            return 2;
        }
        return 9;
    }

    private static boolean checkFile(final String path) {
        return new File(path).isFile();
    }

    private static Process processWithHandler(final List<String> command) throws FFmpegException {
        try {
            if (CollectionUtils.isEmpty(command)) {
                return null;
            }

            Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();

            new PrintStream(videoProcess.getErrorStream()).start();

            new PrintStream(videoProcess.getInputStream()).start();

            return videoProcess;
        } catch (Exception e) {
            throw new FFmpegException("FFMPEG处理失败", e);
        }
    }

    private static boolean process(final List<String> command) throws FFmpegException {

        try {
            if (CollectionUtils.isEmpty(command)) {
                return false;
            }

            Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();

            new PrintStream(videoProcess.getErrorStream()).start();

            new PrintStream(videoProcess.getInputStream()).start();

            return videoProcess.waitFor() != 1;
        } catch (Exception e) {
            throw new FFmpegException("FFMPEG处理失败", e);
        }
    }

    private static List<String> getFfmpegCommand(final String ffmpegPath,
                                                 final String oldFilepath,
                                                 final String outputPath) throws FFmpegException {
        List<String> command = Lists.newArrayList();
        int type = checkContentType(oldFilepath);
        if (type == 1) {
            command.add(ffmpegPath + "\\ffmpeg");
            command.add("-i");
            command.add(oldFilepath);
            command.add("-c:v");
            command.add("libx264");
            command.add("-x264opts");
            command.add("force-cfr=1");
            command.add("-c:a");
            command.add("mp2");
            command.add("-b:a");
            command.add("256k");
            command.add("-vsync");
            command.add("cfr");
            command.add("-f");
            command.add("mpegts");
            command.add(outputPath);
        } else if (type == 0) {
            command.add(ffmpegPath + "\\ffmpeg");
            command.add("-i");
            command.add(oldFilepath);
            command.add("-c:v");
            command.add("libx264");
            command.add("-x264opts");
            command.add("force-cfr=1");
            command.add("-vsync");
            command.add("cfr");
            command.add("-vf");
            command.add("idet,yadif=deint=interlaced");
            command.add("-filter_complex");
            command.add("aresample=async=1000");
            command.add("-c:a");
            command.add("libmp3lame");
            command.add("-b:a");
            command.add("192k");
            command.add("-pix_fmt");
            command.add("yuv420p");
            command.add("-f");
            command.add("mpegts");
            command.add(outputPath);
        } else if (type == 2) {
            command.add(ffmpegPath + "\\ffmpeg");
            command.add("-i");
            command.add(oldFilepath);

            command.add("-c:v");
            command.add("libx264");
            command.add("-acodec");
            command.add("aac");
            //command.add("-ar");
            //command.add("22050");
            //command.add("-crf");
            //command.add("28");

            command.add(outputPath);
        } else {
            throw new FFmpegException("不支持当前上传的文件格式");
        }
        return command;
    }
}

