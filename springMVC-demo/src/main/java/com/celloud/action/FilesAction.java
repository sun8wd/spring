package com.celloud.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/files")
public class FilesAction {
	private static String upload_path = "/Users/sun8wd/Documents/testUpload";
	public ModelAndView index(HttpSession session){
	    session.setAttribute("userId", 18);
	    return new ModelAndView("upload/upload");
	}
	@RequestMapping("/upload1")
	public ModelAndView upload1(@RequestParam("file") CommonsMultipartFile[] files, HttpServletRequest request) throws InterruptedException {
	    HttpSession session = request.getSession();
	    Object userId = session.getAttribute("userId");
		long time = System.currentTimeMillis();
		for (int i = 0; i < files.length; i++) {
			System.out.println("fileName---------->" + files[i].getOriginalFilename());
			if (files[i].isEmpty()) {
				continue;
			}
			try {
				File file = new File(upload_path + File.separatorChar + files[i].getOriginalFilename());
				System.out.println(file.getAbsolutePath());
				OutputStream outputStream = new FileOutputStream(file);
				InputStream inputStream = files[i].getInputStream();
				byte[] temp = new byte[1024 * 1024];
				int length = 0;
				while ((length = inputStream.read(temp)) > 0) {
				    Thread.sleep(1000*10*6L);
				    System.out.println("继续上传。。。");
					outputStream.write(temp, 0, length);
				}
				outputStream.flush();
				outputStream.close();
				inputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(System.currentTimeMillis() - time);
		System.out.println("开始上传时，获取到的userId："+userId);
		userId = session.getAttribute("userId");
		System.out.println("上传结束时，获取到的userId："+userId);
		// 相同5个文件，执行多次，每次执行时间在80-90毫秒之间
		return listFiles();
	}

	@RequestMapping("/upload2")
	public ModelAndView upload2(HttpServletRequest request, HttpServletResponse response) {
		// 创建一个通用的多部分解析器
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 判断 request 是否有文件上传,即多部分请求
		if (multipartResolver.isMultipart(request)) {
			// 记录上传过程起始时的时间，用来计算上传时间
			long time = System.currentTimeMillis();
			// 转换成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 取得request中的所有文件名
			Iterator<String> iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				// 取得上传文件
				MultipartFile file = multiRequest.getFile(iter.next());
				if (file != null) {
					// 取得当前上传文件的文件名称
					String myFileName = file.getOriginalFilename();
					// 如果名称不为“”,说明该文件存在，否则说明该文件不存在
					if (!"".equals(myFileName.trim())) {
						System.out.println(myFileName);
						// 重命名上传后的文件名
						String fileName = upload_path + File.separatorChar + file.getOriginalFilename();
						// 定义上传路径
						File localFile = new File(fileName);
						try {
							file.transferTo(localFile);
						} catch (IllegalStateException | IOException e) {
							e.printStackTrace();
						}
					}
				}
				// 记录上传该文件后的时间

			}
			System.out.println(System.currentTimeMillis() - time);
			// 相同5个文件，执行多次，每次执行时间在1-6毫秒之间
		}
		return listFiles();
	}

	@RequestMapping("listfiles")
	public ModelAndView listFiles() {
		ModelAndView mv = new ModelAndView("upload/listfiles");
		File file = new File(upload_path);
		String[] files = file.list();
		mv.addObject("files", files);
		return mv;
	}

	@RequestMapping("/download")
	public ResponseEntity<byte[]> download(String filename) throws Exception {
		System.out.println(filename);
		File file = new File(upload_path + File.separatorChar + filename);
		HttpHeaders headers = new HttpHeaders();
		String fileName = new String(filename.getBytes("UTF-8"), "iso-8859-1");// 为了解决中文名称乱码问题
		headers.setContentDispositionFormData("attachment", fileName);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
	}
}
