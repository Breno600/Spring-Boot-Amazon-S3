package com.javatechie.s3.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.ByteArrayDecoder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.javatechie.s3.service.StorageService;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

@RestController
@RequestMapping("/file")
public class StorageController {
	
	@Autowired
	private StorageService service;

	@Value("${application.bucket.name}")
	private String bucketName;

	@Autowired
	private AmazonS3 s3Client;
	
	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(@RequestParam(value = "file") MultipartFile file) {
		return new ResponseEntity<>(service.uploadFile(file), HttpStatus.OK);
	}
	
	@GetMapping("/download/{filename}")
	public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("filename") String fileName ) throws IOException {
		S3Object s3Object = s3Client.getObject(bucketName, fileName);
		S3ObjectInputStream inputStream = s3Object.getObjectContent();
		byte[] content = IOUtils.toByteArray(inputStream);
		ByteArrayResource resource = new ByteArrayResource(content);
		ByteArrayInputStream bais = new ByteArrayInputStream(content);
		ImageInputStream image = ImageIO.createImageInputStream(bais);
		Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(image);
		if (imageReaders.hasNext())
			return ResponseEntity
					.ok()
					.contentLength(content.length)
					.header("Content-type", "image/png")
					.header("Content-disposition", "attachment; filename=\"" +fileName+ ".png\"")
					.body(resource);
		return ResponseEntity
				.ok()
				.contentLength(content.length)
				.header("Content-type", "application/pdf")
				.header("Content-disposition", "attachment; filename=\"" +fileName+ ".pdf\"")
				.body(resource);
	}
	
	@DeleteMapping("/delete/{fileName}")
	public ResponseEntity<String> deleteFile(@PathVariable(value = "fileName") String fileName){
		return new ResponseEntity<>(service.deleteFile(fileName), HttpStatus.OK);
	}

}
