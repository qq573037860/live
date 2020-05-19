package com.sjq.live.utils.ffmepg;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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

	public static Boolean ffmpeg(String ffmpegPath, String inputPath, String outputPath) throws FFmpegException{
		 
		if (!checkfile(inputPath)) {
			throw new FFmpegException("文件格式不合法");
		}
		 
		int type =checkContentType(inputPath);
		List command = getFfmpegCommand(type,ffmpegPath, inputPath, outputPath);
		if (null != command &&command.size() > 0) {
			return process(command);
		}
		return false;
	}

	public static Process convertStream(String srcUrl, String dstUrl) throws FFmpegException {
		if (StringUtils.isEmpty(srcUrl) || StringUtils.isEmpty(dstUrl)) {
			throw new FFmpegException("srcUrl,dstUrl不能为空");
		}
		List command = getConvertToFlvStreamCommand(srcUrl, dstUrl);
		if (null != command &&command.size() > 0) {
			return processWithHandler(command);
		}
		throw new FFmpegException("convertStream开启ffmpeg进程失败");
	}

	private static List<String> getConvertToFlvStreamCommand(String srcUrl, String dstUrl) {
		List<String> command = new ArrayList();
		command.add(ffmpegPath +"\\ffmpeg");

		command.add("-hwaccel");
		command.add("cuvid");

		command.add("-i");
		command.add(srcUrl);

		command.add("-tune");
		command.add("zerolatency");

		command.add("-threads");
		command.add("2");

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
		 
	private static int checkContentType(String inputPath) {
		String type =inputPath.substring(inputPath.lastIndexOf(".") + 1,inputPath.length()).toLowerCase();
		//ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
		if (type.equals("avi")) {
			return 1;
		} else if (type.equals("mpg")){
			return 1;
		} else if (type.equals("wmv")){
			return 1;
		} else if (type.equals("3gp")){
			return 1;
		} else if (type.equals("mov")){
			return 1;
		} else if (type.equals("mp4")){
			return 1;
		} else if(type.equals("mkv")){
			return 1;
		} else if (type.equals("asf")){
			return 0;
		} else if (type.equals("flv")){
			return 0;
		} else if (type.equals("rm")){
			return 0;
		} else if (type.equals("rmvb")){
			return 1;
		} else if (type.equals("webm")) {
			return 2;
		}
			return 9;
	}
		 
	private static boolean checkfile(String path) {
		File file = new File(path);
		if (!file.isFile()) {
			return false;
		}
		return true;
	}

	private static Process processWithHandler(List command) throws FFmpegException{

		try {

			if (null == command || command.size() ==0)
				return null;
			Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();

			new PrintStream(videoProcess.getErrorStream()).start();

			new  PrintStream(videoProcess.getInputStream()).start();

			return videoProcess;
		} catch (Exception e) {
			throw new FFmpegException("file uploadfailed",e);
		}
	}
		 
	private static boolean process(List command) throws FFmpegException{
		 
		try {
		 
			if (null == command || command.size() ==0)
				return false;
			Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();

			new PrintStream(videoProcess.getErrorStream()).start();

			new  PrintStream(videoProcess.getInputStream()).start();

			int exitcode =videoProcess.waitFor();

			if (exitcode == 1) {
				return false;
			}
			return true;
		} catch (Exception e) {
			throw new FFmpegException("file uploadfailed",e);
		}
	}
		 
	private static List getFfmpegCommand(int type, String ffmpegPath, String oldfilepath, String outputPath)throws FFmpegException {
		List command = new ArrayList();
		if (type == 1) {
			command.add(ffmpegPath +"\\ffmpeg");
			command.add("-i");
			command.add(oldfilepath);
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
		} else if(type==0){
			command.add(ffmpegPath +"\\ffmpeg");
			command.add("-i");
			command.add(oldfilepath);
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
		} else if (type==2) {
			command.add(ffmpegPath +"\\ffmpeg");
			command.add("-i");
			command.add(oldfilepath);

			command.add("-c:v");
			command.add("libx264");
			command.add("-acodec");
			command.add("aac");
			//command.add("-ar");
			//command.add("22050");
			//command.add("-crf");
			//command.add("28");

			command.add(outputPath);
			
		} else{
			throw new FFmpegException("不支持当前上传的文件格式");
		}
		return command;
		}
	}

//"D:\Program Files\FFmpeg\bin\ffmpeg.exe" -y -i %1 -vcodec libvpx -quality good -cpu-used 5 -b:v 700k -maxrate 700k -bufsize 1000k -qmin 10 -qmax 42 -vf scale=trunc(oh*a/2)*2:480 -threads 4 -acodec libvorbis -f webm %1.webm
		 
	class PrintStream extends Thread{
		java.io.InputStream __is =null;
		 
		public PrintStream(java.io.InputStream is){
			__is = is;
		}
		 
		public void run() {
			try {
				while (this != null) {
					int _ch = __is.read();
					if (_ch == -1) {
						break;
					} else {
						System.out.print((char) _ch);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

}

