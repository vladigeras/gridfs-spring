package ru.vladigeras.gridfs;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vladi_geras on 01.09.2019
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/files")
public class FileController {
	private final GridFsTemplate gridFsTemplate;

	@PostMapping("")
	public String save(@RequestParam("file") MultipartFile file) {
		try {
			String filename = file.getOriginalFilename();

			int i = 1;
			String fileNameTemp = filename;
			while (getFileIfExist(filename) != null) {
				filename = i + "_" + fileNameTemp;
				i++;
			}

			gridFsTemplate.store(file.getInputStream(), filename, file.getContentType());

			return filename;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@GetMapping("")
	public List<String> get() {
		List<String> fileNames = new ArrayList<>();
		gridFsTemplate
				.find(new Query())
				.map(GridFSFile::getFilename)
				.into(fileNames);
		return fileNames;
	}

	@GetMapping("/{filename}")
	public ResponseEntity<byte[]> get(@PathVariable("filename") String filename) {
		GridFSFile file = getFileIfExist(filename);
		if (file == null) return ResponseEntity.notFound().build();

		GridFsResource resource = gridFsTemplate.getResource(file);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			resource.getInputStream().transferTo(outputStream);
		} catch (IOException e) {
			throw new RuntimeException("Error when read file", e);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(resource.getContentType()));
		return ResponseEntity
				.ok()
				.headers(headers)
				.body(outputStream.toByteArray());
	}

	private GridFSFile getFileIfExist(String filename) {
		if (filename == null) return null;

		return gridFsTemplate
				.find(new Query()
						.addCriteria(Criteria.where("filename").is(filename))
				)
				.first();
	}
}
