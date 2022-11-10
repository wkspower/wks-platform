package com.wks.caseengine.rest.app;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.wks.caseengine.data.iimport.DataImportService;

//TODO move this out of the Rest API module - better to be independent
@Component
public class DataImportCommandRunner implements CommandLineRunner {

	@Autowired
	private DataImportService dataImportService;

	@Value("${data.import.folder}")
	private String dataImportFolder;

	@Override
	public void run(String... args) throws Exception {
		if (dataImportFolder != null && !dataImportFolder.isEmpty()) {

			// load and import json files
			Gson gson = new Gson();
			listFiles(dataImportFolder).forEach(fileName -> {
				JsonReader reader;
				try (FileReader fileReader = new FileReader(fileName)) {
					reader = new JsonReader(fileReader);
					dataImportService.importData(gson.fromJson(reader, JsonObject.class));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});

			// delete files
			Arrays.stream(new File(dataImportFolder).listFiles()).forEach(File::delete);
		}
	}

	public Set<String> listFiles(String dir) throws IOException {
		try (Stream<Path> stream = Files.list(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::toAbsolutePath).map(Path::toString)
					.collect(Collectors.toSet());
		}
	}

}
