package com.wks.caseengine.loader.runner;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.wks.caseengine.data.iimport.DataImportService;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty("data.import.enabled")
@Slf4j
public class DataImportCommandRunner implements CommandLineRunner {

	@Autowired
	private DataImportService dataImportService;

	@Value("${data.import.folder}")
	private String dataImportFolder;

	@Override
	public void run(String... args) throws Exception {
		log.info("Start of data importing");

		if (dataImportFolder != null && !dataImportFolder.isEmpty()) {
			Gson gson = new Gson();
			listFiles(dataImportFolder).forEach(fileName -> {
				JsonReader reader;
				try (FileReader fileReader = new FileReader(fileName)) {
					reader = new JsonReader(fileReader);
					dataImportService.importData(gson.fromJson(reader, JsonObject.class));
				} catch (Exception e1) {
					log.error(e1.getMessage());
				}
			});
		}

		log.info("End of data importing");
	}

	public Set<String> listFiles(String dir) throws IOException {
		try (Stream<Path> stream = Files.list(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::toAbsolutePath).map(Path::toString)
					.collect(Collectors.toSet());
		}
	}

}
